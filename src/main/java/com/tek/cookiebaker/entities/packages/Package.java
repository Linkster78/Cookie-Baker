package com.tek.cookiebaker.entities.packages;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mongodb.client.model.Updates;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.entities.enums.Oven;
import com.tek.cookiebaker.entities.upgrades.Upgrade;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class Package {
	
	private String displayName;
	private String alias;
	private List<PackageContent> contents;
	
	public MessageEmbed open(Storage storage, UserProfile profile, EmbedBuilder builder) {
		int chosenNum = Reference.RANDOM.nextInt(contents.stream().mapToInt(PackageContent::getChancePart).sum() * 10) + 1;
		
		PackageContent chosen = null;
		int previous = 0;
		int sumAccumulated = 0;
		for(PackageContent content : contents) {
			sumAccumulated += content.getChancePart();
			if(chosenNum > previous * 10 && chosenNum <= sumAccumulated * 10) {
				chosen = content;
				break;
			}
			previous = sumAccumulated;
		}
		
		StringBuilder rewardBuilder = new StringBuilder();
		rewardBuilder.append("_You opened a " + displayName + " and got:_\n");
		switch(chosen.getRewardType()) {
			case COOKIES:
				for(String key : chosen.getCookies().keySet()) {
					Optional<CookieType> typeOpt = CookieType.getCookieTypeByAlias(key);
					if(typeOpt.isPresent()) {
						rewardBuilder.append(" - **" + Reference.FORMATTER.format(chosen.getCookies().get(key)) + "** " + typeOpt.get().getEmoteName() + "\n");
						profile.setCookies(typeOpt.get(), profile.getCookies(typeOpt.get()) + chosen.getCookies().get(key));
					}
				}
				storage.updateUserProfile(profile, "cookies");
				break;
			case MONEY:
				rewardBuilder.append(" - **" + Reference.FORMATTER.format(chosen.getMoney()) + "** " + Reference.EMOTE_DOLLAR + "\n");
				profile.setMoney(profile.getMoney() + chosen.getMoney());
				storage.updateUserProfile(profile, "money");
				break;
			case OVEN:
				rewardBuilder.append(" - **" + chosen.getOven().getDisplayName() + "**\n");
				
				if(profile.getUnlockedOvens().contains(chosen.getOven())) {
					profile.setMoney(profile.getMoney() + chosen.getOven().getPrice());
					storage.updateUserProfile(profile, "money");
					
					builder.addField("Oops!", "You already own the **" + chosen.getOven().getDisplayName() + "**\n"
							+ "To compensate, here's **" + Reference.FORMATTER.format(chosen.getOven().getPrice()) + "** " + Reference.EMOTE_DOLLAR, false);
				}
				
				profile.getUnlockedOvens().add(chosen.getOven());
				CookieBaker.getInstance().getStorage().updateUserProfileDirect(profile,
								Updates.set("unlockedOvens", profile.getUnlockedOvens().stream().map(Oven::name).collect(Collectors.toList())));
				break;
			case UPGRADES:
				for(String upgrade : chosen.getUpgrades()) {
					Optional<Upgrade> upgradeOpt = CookieBaker.getInstance().getUpgradeManager().getUpgradeByAlias(upgrade);
					if(upgradeOpt.isPresent()) {
						if(profile.getUpgrade(upgradeOpt.get().getClass()) + 1 > upgradeOpt.get().getMaxLevel()) {
							profile.setCookies(CookieType.RAINBOW_COOKIE, profile.getCookies(CookieType.RAINBOW_COOKIE) + 5);
							storage.updateUserProfile(profile, "cookies");
							
							builder.addField("Oops!", "You already own the **" + upgradeOpt.get().getDisplayName() + " Upgrade " + upgradeOpt.get().getMaxLevel() + "**\n"
									+ "To compensate, here's **5** " + CookieType.RAINBOW_COOKIE.getEmoteName(), false);
						}
						
						int nextLevel = profile.getUpgrade(upgradeOpt.get().getClass()) + 1;
						nextLevel = Math.min(upgradeOpt.get().getMaxLevel(), nextLevel);
						rewardBuilder.append(" - **" + upgradeOpt.get().getDisplayName() + " Upgrade " + nextLevel + "**\n");
						profile.upgrade(upgradeOpt.get().getClass());
					}
				}
				storage.updateUserProfile(profile, "upgrades");
				break;
			default:
				rewardBuilder.append("Error");
				break;
		}
		
		builder.addField(displayName + " Opened!", rewardBuilder.toString(), false);
		
		Collections.reverse(builder.getFields());
		
		return builder.build();
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public List<PackageContent> getContents() {
		return contents;
	}
	
}
