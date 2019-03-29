package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Optional;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.entities.enums.LevelRank;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.Guild;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class ProfileCommand extends Command {

	public ProfileCommand() {
		this.name = "profile";
		this.arguments = "[user]";
		this.aliases = new String[] {"inventory", "inv"};
		this.help = "Displays your profile";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(user);
		ServerProfile serverProfile = CookieBaker.getInstance().getStorage().getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 0) {
			EmbedBuilder builder = buildProfile(user, profile);
			
			event.reply(builder.build());
		} else if(arguments.length == 1) {
			if(event.getMessage().getMentionedMembers().size() == 1) {
				Member mentioned = event.getMessage().getMentionedMembers().get(0);
				
				if(arguments[0].length() == mentioned.getAsMention().length() || arguments[0].length() == mentioned.getUser().getAsMention().length()) {
					if(!mentioned.getUser().isBot()) {
						UserProfile mentionedProfile = CookieBaker.getInstance().getStorage().getUserProfile(mentioned.getUser());
						
						EmbedBuilder builder = buildProfile(mentioned.getUser(), mentionedProfile);
						event.reply(builder.build());
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Hey there!", "You can't view the inventory of a bot", false);
						event.reply(builder.build());
					}
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "You need to specify a user.", false);
					event.reply(builder.build());
				}
			} else {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
				builder.addField("Oh no!", "You need to specify a user.", false);
				event.reply(builder.build());
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}
	
	public EmbedBuilder buildProfile(User user, UserProfile profile) {
		Optional<Guild> guildOpt = CookieBaker.getInstance().getStorage().getGuildByMemberId(profile.getUserId());
		
		EmbedBuilder builder = Reference.createBlankEmbed(Color.magenta, user.getJDA());
		builder.setThumbnail(user.getAvatarUrl());
		builder.setTitle(user.getName() + "'s profile");
		
		StringBuilder level = new StringBuilder();
		short segmentCount = 15;
		double levelPercent = Reference.round((float)profile.getXp() / (float)profile.getLevelMax() * 100);
		double partPercent = levelPercent / 100;
		
		level.append("_Level " + Reference.FORMATTER.format(profile.getLevel()) + "_, ");
		level.append("**" + LevelRank.getLevelRank(profile.getLevel()).getDisplayName() + "** ");
		level.append("(_" + Reference.FORMATTER.format(profile.getXp()) + "/" + Reference.FORMATTER.format(profile.getLevelMax()) + "_)\n");
		
		level.append(levelPercent + "% ");
		level.append("``[");
		
		for(short i = 1; i <= segmentCount; i++) {
			double part = Reference.round((double)i / (double)segmentCount);
			
			if(part <= partPercent) {
				level.append("#");
			} else {
				level.append("-");
			}
		}
		
		level.append("]``");
		
		builder.addField("Level & Experience", level.toString(), false);
		builder.addBlankField(false);
		
		StringBuilder cookieBuilder = new StringBuilder();
		for(CookieType type : CookieType.values()) {
			cookieBuilder.append(type.getDisplayName() + ": **" + profile.getCookies(type) + "** " + type.getEmoteName() + "\n");
		}
		builder.addField("Cookies", cookieBuilder.toString(), false);
		builder.addBlankField(false);
		
		builder.addField("Balance", Reference.FORMATTER.format(profile.getMoney()) + "  " + Reference.EMOTE_DOLLAR, true);
		
		builder.addField("Oven", profile.getCurrentOven().getDisplayName(), true);
		builder.addField("Guild", guildOpt.isPresent() ? guildOpt.get().getName() : "None", true);
		return builder;
	}

}
