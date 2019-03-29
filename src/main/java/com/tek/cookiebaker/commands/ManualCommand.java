package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Optional;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;

import net.dv8tion.jda.core.EmbedBuilder;

public class ManualCommand extends Command {

	public ManualCommand() {
		this.name = "manual";
		this.aliases = new String[] {"man", "documentation", "doc", "docs"};
		this.arguments = "<command>";
		this.help = "Provides detailed documentation about a command";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		ServerProfile serverProfile = CookieBaker.getInstance().getStorage().getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 1) {
			Optional<Command> command = event.getClient().getCommands().stream().filter(cmd -> {
				if(cmd.getName().equalsIgnoreCase(arguments[0])) return true;
				if(cmd.getAliases() == null) return false;
				for(int i = 0; i < cmd.getAliases().length; i++) {
					if(cmd.getAliases()[i].equalsIgnoreCase(arguments[0])) return true;
				}
				return false;
			}).findFirst();
			
			Optional<String> manual = command.isPresent() ? CookieBaker.getInstance().getManual().getManualPage(command.get()) : Optional.empty();
			
			if(manual.isPresent()) {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.orange, event.getJDA());
				builder.addField(Reference.capitalize(command.get().getName()) + " Command Documentation", manual.get(), false);
				event.reply(builder.build());
			} else {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
				builder.addField("Oops", "That command could not be found", false);
				event.reply(builder.build());
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}

}
