package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.Guild;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class BakeCommand extends Command {

	public BakeCommand() {
		this.name = "bake";
		this.aliases = new String[] {"cook", "b"};
		this.help = "Bakes some delicious cookies";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(user);
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		Optional<Guild> guild = storage.getGuildByMemberId(user.getId());
		
		if(profile.isVerifying()) {
			profile.setVerificationBakeAttempts(profile.getVerificationBakeAttempts() + 1);
			storage.updateUserProfile(profile, "verificationBakeAttempts");
			
			event.async(() -> {
				RepairCommand.newVerification(false, user, profile, serverProfile, event.getChannel());
			});
			
			return;
		}
		
		long now = System.currentTimeMillis();
		long delta = now - profile.getLastBaked();
		
		if(delta >= profile.getCooldown()) {
			Map<CookieType, Integer> baked = new HashMap<CookieType, Integer>();
			
			long experience = 0;
			
			for(CookieType type : CookieType.values()) {
				if(Math.random() > type.getChance()) continue;
				
				int min = type.getMin();
				int max = type.getMax();
				
				if(type.equals(CookieType.ORIGINAL_COOKIE)) {
					min *= profile.getMultiplier();
					max *= profile.getMultiplier();
				}
				
				int bakedType = min == max ? min : Reference.RANDOM.nextInt(max - min) + min;
				
				baked.put(type, bakedType);
				experience += guild.isPresent() ? guild.get().getGuildXPMultiplier() * bakedType * type.getExperience() : bakedType * type.getExperience();
				profile.setCookies(type, profile.getCookies(type) + bakedType);
			}
			
			boolean leveledUp = profile.incrementExperience((int) experience);
			profile.setLastBaked(now);
			boolean verification = profile.bake(System.currentTimeMillis());
			storage.updateUserProfile(profile, "cookies", "lastBaked", "xp", "bakesVerif", "bakeCount", "session");
			if(verification) {
				storage.updateUserProfile(profile, "verifAttempts", "verificationStart", "verificationBakeAttempts");
				CookieBaker.getInstance().getLogger().logStartedVerification(profile, event.getChannel().getId(), event.getGuild().getId());
			}
			
			EmbedBuilder builder = Reference.createBlankEmbed(new Color(175, 108, 62), event.getJDA());
			
			StringBuilder cookieBuilder = new StringBuilder();
			cookieBuilder.append("_" + profile.getName() + "_, you baked: \n\n");
			for(CookieType bakedType : baked.keySet()) {
				cookieBuilder.append(" - **" + baked.get(bakedType) + "** " + bakedType.getEmoteName() + "\n");
			}
			
			builder.addField("Baked!", cookieBuilder.toString(), false);
			
			if(leveledUp) {
				profile.setCookies(CookieType.RAINBOW_COOKIE, profile.getCookies(CookieType.RAINBOW_COOKIE) + 2);
				storage.updateUserProfile(profile, "level", "cookies");
				
				builder.addField("Congratulations!", "You have advanced to **Level " + profile.getLevel() + "**! Keep baking for special rewards ;)", true);
			}
			
			event.reply(builder.build());
		} else {
			EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
			long left = profile.getCooldown() - (System.currentTimeMillis() - profile.getLastBaked());
			
			builder.addField("Slow down!", "You must wait " + Reference.getFormattedTime(left) + " , geez.", false);
			
			event.reply(builder.build());
		}
	}

}