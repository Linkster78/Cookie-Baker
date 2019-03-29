package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.function.Consumer;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class HelpCommand implements Consumer<CommandEvent> {

	@Override
	public void accept(CommandEvent event) {
		User user = event.getAuthor();
		ServerProfile profile = CookieBaker.getInstance().getStorage().getServerProfile(event.getGuild());
		
		user.openPrivateChannel().queue(privateChannel -> {
			EmbedBuilder builder = Reference.createBlankEmbed(Color.magenta, event.getJDA());
			
			String prefix = profile.getPrefix();
			StringBuilder helpMenu = new StringBuilder();
			helpMenu.append("[If you need any help, feel free to join our Support Server!](" + CookieBaker.getInstance().getConfig().getServerInvite() + ")\n\n");
			for(Command cmd : event.getClient().getCommands()) {
				if(cmd.isHidden()) continue;
				helpMenu.append(
						prefix + cmd.getName() + 
						(cmd.getArguments() == null || cmd.getArguments().isEmpty() ? " - " : " *" + cmd.getArguments() + "* - ")
						+ cmd.getHelp() + "\n");
			}
			helpMenu.setLength(Math.min(1000, helpMenu.length()));
			
			builder.addField("Help Menu", helpMenu.toString(), false);
			
			privateChannel.sendMessage(builder.build()).queue(message -> {
				event.reply(user.getAsMention() + ", I have sent you the help menu!");
			}, throwable -> {
				event.reply(user.getAsMention() + ", your private messages are closed :(");
			});
		});
	}

}
