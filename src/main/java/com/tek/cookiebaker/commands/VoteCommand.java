package com.tek.cookiebaker.commands;

import java.awt.Color;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;

import net.dv8tion.jda.core.EmbedBuilder;

public class VoteCommand extends Command {

	public VoteCommand() {
		this.name = "vote";
		this.help = "Provides a link to vote for the bot!";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		if(CookieBaker.getInstance().getConfig().getDiscordBotsId() != null) {
			String url = "https://discordbots.org/bot/" + CookieBaker.getInstance().getConfig().getDiscordBotsId() + "/vote";
			
			EmbedBuilder builder = Reference.createBlankEmbed(new Color(0, 255, 255), event.getJDA());
			builder.addField("Vote", "To vote for the server, click [here](" + url + ").\nVoting will give you a vote package as a thank you from the cookie baker team.", false);
			event.reply(builder.build());
		} else {
			event.reply("**Sorry but the vote command is not configured**");
		}
	}

}
