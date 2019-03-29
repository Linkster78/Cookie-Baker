package com.tek.cookiebaker.commands.admin;

import java.awt.Color;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.entities.enums.Oven;
import com.tek.cookiebaker.entities.enums.UpgradeType;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

public class AdminProfileCommand extends Command {

	public AdminProfileCommand() {
		this.name = "adminprofile";
		this.aliases = new String[] {"aprofile"};
		this.arguments = "<ID/name>";
		this.help = "Displays a user's bot profile";
		this.ownerCommand = true;
		this.hidden = true;
	}
	
	@Override
	protected void execute(CommandEvent event) {
		ServerProfile serverProfile = CookieBaker.getInstance().getStorage().getServerProfile(event.getGuild());
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 0) {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		} else {
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < arguments.length; i++) builder.append(arguments[i] + " ");
			if(builder.length() > 0) builder.setLength(builder.length() - 1);
			
			if(Reference.isWholeNumber(builder.toString())) {
				User idUser = CookieBaker.getInstance().getDiscord().getShardManager().getUserById(builder.toString());
				
				if(idUser != null) {
					event.reply(createProfile(Reference.createBlankEmbed(Color.yellow, event.getJDA()), idUser));
				} else {
					EmbedBuilder ebuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
					ebuilder.addField("Woops", "User `" + builder.toString() + "` not found", false);
					event.reply(ebuilder.build());
				}
			} else {
				List<User> nameUsers = CookieBaker.getInstance().getDiscord().getShardManager().getUsers().stream()
						.filter(user -> Reference.getUsername(user).equalsIgnoreCase(builder.toString())).collect(Collectors.toList());
			
				if(nameUsers.size() > 0) {
					event.reply(createProfile(Reference.createBlankEmbed(Color.yellow, event.getJDA()), nameUsers.get(0)));
				} else {
					EmbedBuilder ebuilder = Reference.createBlankEmbed(Color.red, event.getJDA());
					ebuilder.addField("Woops", "User `" + builder.toString() + "` not found", false);
					event.reply(ebuilder.build());
				}
			}
		}
	}
	
	public MessageEmbed createProfile(EmbedBuilder builder, User user) {
		UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(user);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		builder.setThumbnail(user.getAvatarUrl());
		builder.setTitle(Reference.getUsername(user) + "'s Admin Profile");
		builder.addField("ID", user.getId(), true);
		builder.addField("Level", profile.getLevel() + "", true);
		builder.addField("XP", profile.getXp() + "/" + profile.getLevelMax(), true);
		builder.addField("Donated", NumberFormat.getCurrencyInstance().format(profile.getDonated()), true);
		builder.addField("Last Baked", profile.getLastBaked() == 0 ? "NA" : formatter.format(new Timestamp(profile.getLastBaked()).toLocalDateTime()), true);
		builder.addField("Money", "$" + Reference.FORMATTER.format(profile.getMoney()), true);
		builder.addField("Bake Count", Reference.FORMATTER.format(profile.getBakeCount()), true);
		builder.addField("Vote Count", Reference.FORMATTER.format(profile.getVoteCount()), true);
		builder.addField("Current Oven", profile.getCurrentOven().getDisplayName(), true);
		builder.addField("Unlocked Ovens", profile.getUnlockedOvens().stream().map(Oven::getDisplayName).collect(Collectors.joining(", ")), true);
		builder.addField("Cookies", Arrays.stream(CookieType.values()).map(type -> type.getDisplayName() + ": " + profile.getCookies(type)).collect(Collectors.joining("\n")), true);
		builder.addField("Oven Stats", Arrays.stream(Oven.values()).filter(oven -> profile.getUnlockedOvens().contains(oven)).map(oven -> {
			return "**" + oven.getDisplayName() + "**: " + CookieBaker.getInstance().getUpgradeManager().getRegisteredUpgradesByType(UpgradeType.OVEN).stream().map(upgrade -> {
				return upgrade.getDisplayName() + ": " + profile.getUpgrade(oven, upgrade.getClass()) + "/" + upgrade.getMaxLevel();
			}).collect(Collectors.joining(" **|** "));
		}).collect(Collectors.joining("\n")), true);
		builder.addField("Packages", CookieBaker.getInstance().getPackageManager().getPackages().stream().map(pack -> pack.getDisplayName() + ": " + profile.getPackages(pack.getAlias())).collect(Collectors.joining("\n")), true);
		builder.addField("Last Mining Session", "Lasted " + Reference.getFormattedTime(profile.getSession().elapsed()), true);
		
		return builder.build();
	}

}
