package com.tek.cookiebaker.entities.upgrades;

import java.util.Optional;

import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.entities.enums.UpgradeType;
import com.tek.cookiebaker.entities.shop.ISellable;
import com.tek.cookiebaker.log.Logger;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.storage.Guild;
import com.tek.cookiebaker.storage.UserProfile;

public class GuildCapacityUpgrade extends Upgrade implements ISellable {

	public GuildCapacityUpgrade() {
		super(UpgradeType.GUILD, "Guild Capacity", "capacity", 5);
	}

	@Override
	public String getDisplayName(UserProfile profile) {
		Optional<Guild> guild = CookieBaker.getInstance().getStorage().getGuildByMemberId(profile.getUserId());
		
		if(guild.isPresent()) {
			int level = guild.get().getUpgrade(GuildCapacityUpgrade.class);
			return "Guild Capacity Upgrade " + (level >= getMaxLevel() ? "MAXED" : level + 1);
		} else {
			return "Guild Capacity Upgrade";
		}
	}

	@Override
	public String getPrice(UserProfile profile) {
		Optional<Guild> guild = CookieBaker.getInstance().getStorage().getGuildByMemberId(profile.getUserId());
		
		if(guild.isPresent()) {
			int level = guild.get().getUpgrade(GuildCapacityUpgrade.class);
			return level >= getMaxLevel() ? "NA" : getPrice(level + 1) + " " + CookieType.REDVELVET_COOKIE.getEmoteName();
		} else {
			return "NA";
		}
	}

	@Override
	public boolean has(UserProfile profile) {
		Optional<Guild> guild = CookieBaker.getInstance().getStorage().getGuildByMemberId(profile.getUserId());
		return guild.isPresent() ? guild.get().getUpgrade(GuildCapacityUpgrade.class) >= getMaxLevel() : true;
	}

	@Override
	public boolean canBuy(UserProfile profile) {
		Optional<Guild> guild = CookieBaker.getInstance().getStorage().getGuildByMemberId(profile.getUserId());
		if(guild.isPresent()) {
			int level = guild.get().getUpgrade(GuildCapacityUpgrade.class);
			return profile.getCookies(CookieType.REDVELVET_COOKIE) >= getPrice(level + 1);
		} else {
			return false;
		}
	}

	@Override
	public void buy(UserProfile profile) {
		Optional<Guild> guild = CookieBaker.getInstance().getStorage().getGuildByMemberId(profile.getUserId());
		if(!guild.isPresent()) {
			Logger.error("Guild was not present on GuildCapacityUpgrade purchase");
			return;
		}
		int level = guild.get().getUpgrade(GuildCapacityUpgrade.class);
		profile.setCookies(CookieType.REDVELVET_COOKIE, profile.getCookies(CookieType.REDVELVET_COOKIE) - getPrice(level + 1));
		guild.get().upgrade(GuildCapacityUpgrade.class);
		CookieBaker.getInstance().getStorage().updateUserProfile(profile, "cookies");
		CookieBaker.getInstance().getStorage().updateGuild(guild.get(), "upgrades");
	}
	
	private int getPrice(int level) {
		return level * 50;
	}

}
