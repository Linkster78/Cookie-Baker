package com.tek.cookiebaker.commands.admin;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class AdminClearCommand extends Command {

	public AdminClearCommand() {
		this.name = "adminclear";
		this.aliases = new String[] {"aclear"};
		this.arguments = "<ID/name>";
		this.help = "Deletes a user record from the database";
		this.ownerCommand = true;
		this.hidden = true;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		ServerProfile serverProfile = CookieBaker.getInstance().getStorage().getServerProfile(event.getGuild());
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 0) {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		} else {
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < arguments.length; i++) builder.append(arguments[i] + " ");
			if(builder.length() > 0) builder.setLength(builder.length() - 1);
			
			if(Reference.isWholeNumber(builder.toString())) {
				User idUser = CookieBaker.getInstance().getDiscord().getShardManager().getUserById(builder.toString());
				
				if(idUser != null) {
					event.reply("**Are you sure ?** (yes/no)");
					
					UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(idUser);
					
					CookieBaker.getInstance().getDiscord().getMessageWaiter().waitForMessage(15, event.getChannel().getId(), event.getAuthor().getId(), event.getMessage().getId(), msg -> {
						if(msg.equalsIgnoreCase("yes") || msg.equalsIgnoreCase("y")) {
							CookieBaker.getInstance().getStorage().deleteUserProfile(profile);
							
							EmbedBuilder ebuilder = Reference.createBlankEmbed(Color.green, event.getJDA());
							ebuilder.addField("Deleted", "Cleared **" + profile.getName() + "**'s profile", false);
							event.reply(ebuilder.build());
						} else {
							EmbedBuilder ebuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
							ebuilder.addField("Cancelled", "User deletion cancelled", false);
							event.reply(ebuilder.build());
						}
					}, () -> {
						EmbedBuilder ebuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
						ebuilder.addField("Cancelled", "User deletion cancelled", false);
						event.reply(ebuilder.build());
					});
				} else {
					EmbedBuilder ebuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
					ebuilder.addField("Woops", "User `" + builder.toString() + "` not found", false);
					event.reply(ebuilder.build());
				}
			} else {
				List<User> nameUsers = CookieBaker.getInstance().getDiscord().getShardManager().getUsers().stream()
						.filter(user -> Reference.getUsername(user).equalsIgnoreCase(builder.toString())).collect(Collectors.toList());
			
				if(nameUsers.size() > 0) {
					event.reply("**Are you sure ?** (yes/no)");
					
					UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(nameUsers.get(0));
					
					CookieBaker.getInstance().getDiscord().getMessageWaiter().waitForMessage(15, event.getChannel().getId(), event.getAuthor().getId(), event.getMessage().getId(), msg -> {
						if(msg.equalsIgnoreCase("yes") || msg.equalsIgnoreCase("y")) {
							CookieBaker.getInstance().getStorage().deleteUserProfile(profile);
							
							EmbedBuilder ebuilder = Reference.createBlankEmbed(Color.green, event.getJDA());
							ebuilder.addField("Deleted", "Cleared **" + profile.getName() + "**'s profile", false);
							event.reply(ebuilder.build());
						} else {
							EmbedBuilder ebuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
							ebuilder.addField("Cancelled", "User deletion cancelled", false);
							event.reply(ebuilder.build());
						}
					}, () -> {
						EmbedBuilder ebuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
						ebuilder.addField("Cancelled", "User deletion cancelled", false);
						event.reply(ebuilder.build());
					});
				} else {
					EmbedBuilder ebuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
					ebuilder.addField("Woops", "User `" + builder.toString() + "` not found", false);
					event.reply(ebuilder.build());
				}
			}
		}
	}

}
