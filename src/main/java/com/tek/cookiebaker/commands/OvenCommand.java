package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mongodb.client.model.Updates;
import com.tek.cookiebaker.entities.enums.Oven;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class OvenCommand extends Command {

	public OvenCommand() {
		this.name = "oven";
		this.aliases = new String[] {"ovens"};
		this.arguments = "[oven]";
		this.help = "Switches to the specified oven";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(user);
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 0) {
			 EmbedBuilder builder = Reference.createBlankEmbed(Color.orange, event.getJDA());
			 builder.setThumbnail(user.getAvatarUrl());
			 
			 StringBuilder ovenBuilder = new StringBuilder();
			 for(Oven oven : Oven.values()) {
				 ovenBuilder.append("`[");
				 ovenBuilder.append(profile.getCurrentOven().equals(oven) ? "#" : " ");
				 ovenBuilder.append("]` | ");
				 ovenBuilder.append(oven.getDisplayName() + " ");
				 if(profile.getUnlockedOvens().contains(oven)) ovenBuilder.append("**OWNED**");
				 ovenBuilder.append("\n");
			 }
			 
			 builder.addField("Available Ovens", ovenBuilder.toString(), false);
			 builder.setFooter("To select an oven, use " + serverProfile.getPrefix() + "oven <name>", null);
			 
			 event.reply(builder.build());
		} else if(arguments.length == 1) {
			Optional<Oven> oven = Oven.getOvenByAlias(arguments[0]);
			
			if(oven.isPresent()) {
				if(profile.getUnlockedOvens().contains(oven.get())) {
					profile.setCurrentOven(oven.get());
					storage.updateUserProfileDirect(profile, Updates.set("currentOven", oven.get().name()));
					
					EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
					builder.addField("Oven Changed", "Changed your current oven to the **" + oven.get().getDisplayName() + "**", false);
					event.reply(builder.build());
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "You don't own the **" + oven.get().getDisplayName() + "**", false);
					event.reply(builder.build());
				}
			} else {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
				builder.addField("Oh no!", "**That oven doesn't exist**\nValid ovens: _" + 
						Arrays.stream(Oven.values()).map(Oven::getAlias).collect(Collectors.joining(", ")) + "_", false);
				event.reply(builder.build());
			}
		} else if(arguments.length > 1) {
			EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
			builder.addField("Oh no!", "**That oven doesn't exist**\nValid ovens: _" + 
					Arrays.stream(Oven.values()).map(Oven::getAlias).collect(Collectors.joining(", ")) + "_", false);
			event.reply(builder.build());
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}

}
