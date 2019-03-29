package com.tek.cookiebaker.listeners;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.function.Consumer;

import com.tek.cookiebaker.api.donations.Donation;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class DonationListener implements Consumer<Donation> {

	@Override
	public void accept(Donation donation) {
		UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(donation.getUserId());
		if(profile == null) return;
		
		if(profile.getDonated() == donation.getAmount()) {
			profile.setDonated(profile.getDonated() + donation.getAmount());
			profile.setPackages("donator", profile.getPackages("donator") + 3);
			CookieBaker.getInstance().getStorage().updateUserProfile(profile, "donated", "packages");
			
			User user = CookieBaker.getInstance().getDiscord().getShardManager().getUserById(profile.getUserId());
			if(user == null) return;
			EmbedBuilder builder = Reference.createBlankEmbed(new Color(0, 255, 255), user.getJDA());
			builder.addField("Thank You!", "**Hey, " + Reference.getUsername(user) + "!**\n\n"
					+ "Thanks for donating " + NumberFormat.getCurrencyInstance().format(donation.getAmount()) + " " + donation.getCurrency() + "\n\n"
					+ "_You have been given 3 Donator Packages as a token of our appreciation <3_", false);
			
			user.openPrivateChannel().queue(pc -> {
				pc.sendMessage(builder.build()).queue(msg -> {}, e -> {});
			});
		} else {
			User user = CookieBaker.getInstance().getDiscord().getShardManager().getUserById(profile.getUserId());
			if(user == null) return;
			EmbedBuilder builder = Reference.createBlankEmbed(new Color(0, 255, 255), user.getJDA());
			builder.addField("Thank You!", "**Hey, " + Reference.getUsername(user) + "!**\n\n"
					+ "Thanks for donating $" + NumberFormat.getCurrencyInstance().format(donation.getAmount()) + " " + donation.getCurrency(), false);
			
			user.openPrivateChannel().queue(pc -> {
				pc.sendMessage(builder.build()).queue(msg -> {}, e -> {});
			});
		}
	}

}
