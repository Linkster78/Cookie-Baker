package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Sorts;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class LeaderboardCommand extends Command {

	public LeaderboardCommand() {
		this.name = "leaderboard";
		this.aliases = new String[] {"top"};
		this.arguments = "<cookie type/money/level>";
		this.help = "Shows the best bakers in the west";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(user);
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 1) {
			FindIterable<UserProfile> profiles = storage.getUserProfiles();
			
			if(arguments[0].equalsIgnoreCase("money")) {
				FindIterable<UserProfile> sortedTop = profiles.sort(Sorts.descending("money"));
				FindIterable<UserProfile> limitedTop = sortedTop.limit(10);
				
				EmbedBuilder builder = Reference.createBlankEmbed(Color.yellow, event.getJDA());
				
				int placement = 1;
				StringBuilder topBuilder = new StringBuilder();
				MongoCursor<UserProfile> iter = limitedTop.iterator();
				while(iter.hasNext()) {
					UserProfile topProfile = iter.next();
					topBuilder.append("**#" + placement + "** `" + topProfile.getName() + "` | **" + Reference.FORMATTER.format(topProfile.getMoney()) + "$**\n");
					placement++;
				}
				
				for(; placement <= 10; placement++) {
					topBuilder.append("**#" + placement + "** `NA`\n");
				}
				
				builder.addField("Money Leaderboard", topBuilder.toString(), false);
				builder.setFooter("Your balance: " + Reference.FORMATTER.format(profile.getMoney()) + "$", user.getAvatarUrl());
				
				event.reply(builder.build());
			} else if(arguments[0].equalsIgnoreCase("level")) {
				FindIterable<UserProfile> sortedTop = profiles.sort(Sorts.descending("level"));
				FindIterable<UserProfile> limitedTop = sortedTop.limit(10);
				
				EmbedBuilder builder = Reference.createBlankEmbed(Color.yellow, event.getJDA());
				
				int placement = 1;
				StringBuilder topBuilder = new StringBuilder();
				MongoCursor<UserProfile> iter = limitedTop.iterator();
				while(iter.hasNext()) {
					UserProfile topProfile = iter.next();
					topBuilder.append("**#" + placement + "** `" + topProfile.getName() + "` | **Level " + Reference.FORMATTER.format(topProfile.getLevel()) + "**\n");
					placement++;
				}
				
				for(; placement <= 10; placement++) {
					topBuilder.append("**#" + placement + "** `NA`\n");
				}
				
				builder.addField("Level Leaderboard", topBuilder.toString(), false);
				builder.setFooter("Your level: Level " + Reference.FORMATTER.format(profile.getLevel()), user.getAvatarUrl());
				
				event.reply(builder.build());
			} else {
				Optional<CookieType> typeOpt = CookieType.getCookieTypeByAlias(arguments[0]);
				
				if(typeOpt.isPresent()) {
					CookieType type = typeOpt.get();
					
					FindIterable<UserProfile> sortedTop = profiles.sort(Sorts.descending("cookies." + type.getAlias()));
					FindIterable<UserProfile> limitedTop = sortedTop.limit(10);
					
					EmbedBuilder builder = Reference.createBlankEmbed(Color.yellow, event.getJDA());
					
					int placement = 1;
					StringBuilder topBuilder = new StringBuilder();
					MongoCursor<UserProfile> iter = limitedTop.iterator();
					while(iter.hasNext()) {
						UserProfile topProfile = iter.next();
						topBuilder.append("**#" + placement + "** `" + topProfile.getName() + "` | **" + Reference.FORMATTER.format(topProfile.getCookies(type)) + " " + type.getDisplayName() + "s**\n");
						placement++;
					}
					
					for(; placement <= 10; placement++) {
						topBuilder.append("**#" + placement + "** NA\n");
					}
					
					builder.addField(type.getDisplayName() + " Leaderboard", topBuilder.toString(), false);
					builder.setFooter("Your " + type.getDisplayName() + "s: " + Reference.FORMATTER.format(profile.getCookies(type)) + " " + type.getDisplayName() + "s", user.getAvatarUrl());
					
					event.reply(builder.build());
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					
					builder.addField("Invalid Criteria", "**Valid leaderboard criterias**: _money, level, " +
									Arrays.stream(CookieType.values()).map(CookieType::getAlias).collect(Collectors.joining(", ")) + "_", false);
					
					event.reply(builder.build());
				}
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}

}
