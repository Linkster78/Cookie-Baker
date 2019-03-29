package com.tek.cookiebaker.commands;

import java.awt.Color;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.UpgradeType;
import com.tek.cookiebaker.entities.upgrades.Upgrade;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class UpgradesCommand extends Command {
	
	public UpgradesCommand() {
		this.name = "upgrades";
		this.aliases = new String[] {"upgrade"};
		this.help = "Displays your current oven upgrades";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(user);
		
		EmbedBuilder builder = Reference.createBlankEmbed(Color.orange, event.getJDA());
		
		StringBuilder upgradeBuilder = new StringBuilder();
		for(Upgrade upgrade : CookieBaker.getInstance().getUpgradeManager().getRegisteredUpgradesByType(UpgradeType.OVEN)) {
			upgradeBuilder.append(upgrade.getDisplayName() + " | **Level " + profile.getUpgrade(upgrade.getClass()) + "/" + upgrade.getMaxLevel() + "**\n");
		}
		
		builder.addField("Upgrades for the " + profile.getCurrentOven().getDisplayName() + ":", upgradeBuilder.toString(), false);
		
		event.reply(builder.build());
	}
	
}
