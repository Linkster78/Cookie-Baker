package com.tek.cookiebaker.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.main.CookieBaker;

public class ServerCommand extends Command {

	public ServerCommand() {
		this.name = "server";
		this.help = "Gives an invite to the main server";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		StringBuilder builder = new StringBuilder();
		builder.append("[" + CookieType.ORIGINAL_COOKIE.getEmoteName() + "] __**Official Cookie Baker Server**__ [" + CookieType.ORIGINAL_COOKIE.getEmoteName() + "]\n\n");
		builder.append("*This is our current Non-Expiring Server Invite*\n\n");
		builder.append("**You may use this invite to access our support channels, community bot channels and more**\n\n");
		builder.append("Server Invite: " + CookieBaker.getInstance().getConfig().getServerInvite());
		event.reply(builder.toString());
	}

}
