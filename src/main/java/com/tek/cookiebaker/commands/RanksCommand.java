package com.tek.cookiebaker.commands;

import java.awt.Color;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.LevelRank;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class RanksCommand extends Command {

	public RanksCommand() {
		this.name = "ranks";
		this.aliases = new String[] {"rank", "role", "roles"};
		this.help = "Provides the role ladder";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(user);
		
		EmbedBuilder builder = Reference.createBlankEmbed(Color.orange, event.getJDA());
		builder.setThumbnail(user.getAvatarUrl());
		
		LevelRank userRank = LevelRank.getLevelRank(profile.getLevel());
		LevelRank[] ranks = LevelRank.values();
		boolean passed = false;
		int lastRange = 1;
		
		StringBuilder rankBuilder = new StringBuilder();
		for(int i = 0; i < ranks.length; i++) {
			LevelRank rank = ranks[i];
			
			rankBuilder.append("`[");
			
			if(!passed) {
				rankBuilder.append(rank.equals(userRank) ? "#" : "|");
				if(rank.equals(userRank)) passed = true;
			} else {
				rankBuilder.append(" ");
			}
			
			rankBuilder.append("]`");
			rankBuilder.append(" | ");
			rankBuilder.append("**" + rank.getDisplayName() + "** ");
			rankBuilder.append(lastRange + " - " + (rank.getRange() == Integer.MAX_VALUE ? "..." : rank.getRange() - 1));
			rankBuilder.append("\n");
			
			lastRange = rank.getRange();
		}
		
		builder.addField("Rank Ladder", rankBuilder.toString(), false);
		
		event.reply(builder.build());
	}

}
