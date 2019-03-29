package com.tek.cookiebaker.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.main.CookieBaker;

public class InviteCommand extends Command {

	public InviteCommand() {
		this.name = "invite";
		this.help = "Gives the invite for the bot";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		event.reply("Bot Invite: " + CookieBaker.getInstance().getConfig().getBotInvite());
	}

}
