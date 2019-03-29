package com.tek.cookiebaker.commands.admin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;

public class AdminTopCommand extends Command {

	public AdminTopCommand() {
		this.name = "admintop";
		this.aliases = new String[] {"atop"};
		this.arguments = "<sessiontime/bakecount>";
		this.help = "Shows the best bakers at odd stats";
		this.ownerCommand = true;
		this.hidden = true;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		Storage storage = CookieBaker.getInstance().getStorage();
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 0) {
			event.reply("**Valid Criterias:** _sessiontime, bakecount_");
		} else if(arguments.length == 1) {
			FindIterable<UserProfile> users = CookieBaker.getInstance().getStorage().getUserProfiles();
			MongoCursor<UserProfile> userIter = users.iterator();
			List<UserProfile> profiles = new ArrayList<UserProfile>();
			while(userIter.hasNext()) profiles.add(userIter.next());
			
			if(arguments[0].equalsIgnoreCase("sessiontime")) {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
				builder.addField("Top Session Times", profiles.stream()
						.sorted(Comparator.comparing(profile -> ((UserProfile) profile).getSession().elapsed()).reversed())
						.limit(15)
						.map(profile -> ("**" + profile.getName() + "**: " + Reference.getFormattedTime(profile.getSession().elapsed())))
						.collect(Collectors.joining("\n")), false);
				event.reply(builder.build());
			} else if(arguments[0].equalsIgnoreCase("bakecount")) {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
				builder.addField("Top Bake Counts", profiles.stream()
						.sorted(Comparator.comparing(profile -> ((UserProfile) profile).getBakeCount()).reversed())
						.limit(15)
						.map(profile -> ("**" + profile.getName() + "**: " + Reference.FORMATTER.format(profile.getBakeCount())))
						.collect(Collectors.joining("\n")), false);
				event.reply(builder.build());
			} else {
				event.reply("**Valid Criterias:** _sessiontime, bakecount_");
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}

}
