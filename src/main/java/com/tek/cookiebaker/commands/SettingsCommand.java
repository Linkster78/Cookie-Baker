package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.LevelRank;
import com.tek.cookiebaker.jda.MessageWaiter;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

public class SettingsCommand extends Command {

	public SettingsCommand() {
		this.name = "settings";
		this.arguments = "<prefix/roles> <value/action>";
		this.help = "Edits guild specific settings";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		Member member = event.getMember();
		Storage storage = CookieBaker.getInstance().getStorage();
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		UserProfile profile = storage.getUserProfile(member.getUser());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(member.hasPermission(Permission.ADMINISTRATOR)) {
			if(arguments.length == 2) {
				if(arguments[0].equalsIgnoreCase("prefix")) {
					if(arguments[1].length() <= 3) {
						serverProfile.setPrefix(arguments[1]);
						storage.updateServerProfile(serverProfile, "prefix");
						
						EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
						builder.addField("Prefix Changed", "You changed the prefix to \"" + arguments[1] + "\"", false);
						event.reply(builder.build());
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Hey there!", "Prefixes must be 3 characters or less", false);
						event.reply(builder.build());
					}
				} else if(arguments[0].equalsIgnoreCase("roles")) {
					if(arguments[1].equalsIgnoreCase("setup")) {
						Runnable cancel = () -> {
							serverProfile.getRoleMap().clear();
							
							EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
							builder.addField("Oh no!", "Role setup was cancelled", false);
							event.reply(builder.build());
						};
						
						MessageWaiter messageWaiter = CookieBaker.getInstance().getDiscord().getMessageWaiter();
						Consumer<String> previousConsumer = null;
						List<LevelRank> ranks = Arrays.asList(LevelRank.values());
						Collections.reverse(ranks);
						Guild guild = event.getGuild();
						int c = 0;
						for(LevelRank rank : ranks) {
							previousConsumer = getRoleConsumer(storage, serverProfile, profile, rank, c, cancel, previousConsumer, guild, event, messageWaiter);
							
							c++;
						}
						
						event.reply("**Role Setup Started!** Use \"cancel\" at any time to cancel setup.\nWhat's the name of the role associated to **" + LevelRank.getLevelRankById(0).get().getDisplayName() + "**");
						
						messageWaiter.waitForMessage(45, event.getChannel().getId(), event.getAuthor().getId(), event.getMessage().getId(), previousConsumer, cancel);
					} else if(arguments[1].equalsIgnoreCase("clear")) {
						serverProfile.getRoleMap().clear();
						storage.updateServerProfile(serverProfile, "roleMap");
						
						EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
						builder.addField("Roles Cleared", "Cleared the mapped roles", false);
						event.reply(builder.build());
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Hey there!", "**That action doesn't exist.**\nValid Actions: _setup, clear_", false);
						event.reply(builder.build());
					}
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Hey there!", "That setting doesn't exist", false);
					event.reply(builder.build());
				}
			} else {
				event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
			}
		} else {
			EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
			builder.addField("Hey there!", "You don't have the permissions to do this", false);
			event.reply(builder.build());
		}
	}
	
	public Consumer<String> getRoleConsumer(Storage storage, ServerProfile profile, UserProfile userProfile, LevelRank rank, int c, Runnable cancel, Consumer<String> previous, Guild guild, CommandEvent event, MessageWaiter waiter) {
		if(c == 0) {
			Consumer<String> consumer = message -> {
				if(message.equalsIgnoreCase("cancel")) {
					cancel.run();
					return;
				}
				
				List<Role> roles = guild.getRolesByName(message, true);
				
				if(roles.isEmpty()) {
					event.reply("**No role exists with that name**");
					cancel.run();
					return;
				}
				
				Role role = roles.get(0);
				
				profile.getRoleMap().put(rank.getId() + "", role.getId());
				
				event.reply("Role **" + role.getName() + "** associated to rank **" + rank.getDisplayName() + "**\n\n**Setup Complete!** Make sure I have the _MANAGE ROLES_ permission and that my role is above the rank roles.");
				storage.updateServerProfile(profile, "roleMap");
			};
			
			return consumer;
		} else {
			Consumer<String> consumer = message -> {
				if(message.equalsIgnoreCase("cancel")) {
					cancel.run();
					return;
				}
				
				List<Role> roles = guild.getRolesByName(message, true);
				
				if(roles.isEmpty()) {
					event.reply("**No role exists with that name**");
					cancel.run();
					return;
				}
				
				Role role = roles.get(0);
				
				profile.getRoleMap().put(rank.getId() + "", role.getId());
				
				event.reply("Role **" + role.getName() + "** associated to rank **" + rank.getDisplayName() + "**\n"
						+ "What's the name of the role associated to **" + LevelRank.getLevelRankById(rank.getId() + 1).get().getDisplayName() + "**");
				
				waiter.waitForMessage(45, event.getChannel().getId(), event.getAuthor().getId(), event.getMessage().getId(), previous, cancel);
			};
			
			return consumer;
		}
	}

}
