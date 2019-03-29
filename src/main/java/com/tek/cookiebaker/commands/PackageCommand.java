package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

public class PackageCommand extends Command {

	public PackageCommand() {
		this.name = "package";
		this.aliases = new String[] {"packages", "crate", "crates"};
		this.arguments = "[name]";
		this.help = "Opens a package";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(user);
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 0) {
			EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
			
			StringBuilder packBuilder = new StringBuilder();
			for(com.tek.cookiebaker.entities.packages.Package pack : CookieBaker.getInstance().getPackageManager().getPackages()) {
				packBuilder.append(pack.getDisplayName() + ": **" + profile.getPackages(pack.getAlias()) + "**\n");
			}
			
			builder.addField("Owned Packages", packBuilder.toString(), false);
			
			event.reply(builder.build());
		} else if(arguments.length == 1){
			Optional<com.tek.cookiebaker.entities.packages.Package> packageOpt = CookieBaker.getInstance().getPackageManager().getPackageByAlias(arguments[0]);
			
			if(packageOpt.isPresent()) {
				if(profile.getPackages(packageOpt.get().getAlias()) > 0) {
					profile.setPackages(packageOpt.get().getAlias(), profile.getPackages(packageOpt.get().getAlias()) - 1);
					storage.updateUserProfile(profile, "packages");
					
					MessageEmbed embed = packageOpt.get().open(storage, profile, Reference.createBlankEmbed(Color.green, event.getJDA()));
					event.reply(embed);
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					
					builder.addField("Oh no!", "You have no " + packageOpt.get().getDisplayName() + "s!", false);
					
					event.reply(builder.build());
				}
			} else {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
				builder.addField("Oh no!", "**That package doesn't exist**\nValid packages: _" + 
						CookieBaker.getInstance().getPackageManager().getPackages().stream().map(com.tek.cookiebaker.entities.packages.Package::getAlias).collect(Collectors.joining(", ")) + "_", false);
				event.reply(builder.build());
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}

}
