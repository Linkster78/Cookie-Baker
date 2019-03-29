package com.tek.cookiebaker.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.tek.cookiebaker.listeners.DonationListener;
import com.tek.cookiebaker.main.CookieBaker;

public class AdminUpdateDonationsCommand extends Command {

	public AdminUpdateDonationsCommand() {
		this.name = "adminupdatedonations";
		this.aliases = new String[] {"aupdatedonations"};
		this.help = "Updates donation records";
		this.ownerCommand = true;
		this.hidden = true;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		long before = CookieBaker.getInstance().getStorage().getDonationCount();
		
		try {
			CookieBaker.getInstance().getDonoHook().updateMissingDonations(new DonationListener());
			long after = CookieBaker.getInstance().getStorage().getDonationCount();
			
			event.reply("**Updated donation records** [" + before + " -> " + after + "]");
		} catch (UnirestException e) {
			event.reply("ERROR:" + e.getMessage());
		}
	}

}
