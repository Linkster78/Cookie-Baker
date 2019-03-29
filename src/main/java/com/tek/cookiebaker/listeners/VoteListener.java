package com.tek.cookiebaker.listeners;

import java.awt.Color;
import java.util.function.Consumer;

import com.tek.cookiebaker.entities.voting.Vote;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class VoteListener implements Consumer<Vote> {

	@Override
	public void accept(Vote vote) {
		UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(vote.getUser());
		if(profile == null) return;
		profile.setVoteCount(profile.getVoteCount() + 1);
		profile.setPackages("vote", profile.getPackages("vote") + 1);
		CookieBaker.getInstance().getStorage().updateUserProfile(profile, "voteCount", "packages");
		
		User user = CookieBaker.getInstance().getDiscord().getShardManager().getUserById(profile.getUserId());
		if(user == null) return;
		EmbedBuilder builder = Reference.createBlankEmbed(new Color(0, 255, 255), user.getJDA());
		builder.addField("Thank You!", "**Hey, " + Reference.getUsername(user) + "!**\n"
				+ "Thanks for voting!\n\n"
				+ "_You have been given 1 Vote Package as a token of our appreciation._", false);
		
		user.openPrivateChannel().queue(pc -> {
			pc.sendMessage(builder.build()).queue(msg -> {}, e -> {});
		});
	}
	
}
