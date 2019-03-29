package com.tek.cookiebaker.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.main.CookieBaker;

public class AdminShutdownCommand extends Command {
	
	public AdminShutdownCommand() {
		this.name = "adminshutdown";
		this.aliases = new String[] {"ashutdown"};
		this.ownerCommand = true;
		this.help = "Shuts the bot down";
		this.hidden = true;
	}

	@Override
	protected void execute(CommandEvent event) {
		event.reply("Shutting down...");
		
		CookieBaker.getInstance().shutdown();
	}
	
}
