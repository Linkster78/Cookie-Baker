package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.jda.MessageWaiter;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class SellCommand extends Command {

	public SellCommand() {
		this.name = "sell";
		this.aliases = new String[] {"s"};
		this.arguments = "<all/type> [all/count]";
		this.help = "Sells some cookies";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(user);
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 1) {
			if(arguments[0].equalsIgnoreCase("all")) {
				MessageWaiter waiter = CookieBaker.getInstance().getDiscord().getMessageWaiter();
				
				event.reply("You're about to sell all of your _" + Arrays.stream(CookieType.values()).filter(CookieType::isSellAll).map(type -> { return type.getDisplayName() + "s"; } ).collect(Collectors.joining(", ")) + "_\n**Are you sure ?** (Yes/y No/n _10 seconds_)");
				
				waiter.waitForMessage(10, event.getChannel().getId(), event.getAuthor().getId(), event.getMessage().getId(), response -> {
					if(response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
						Map<CookieType, Integer> sold = new HashMap<CookieType, Integer>();
						double money = 0;
						
						for(CookieType type : CookieType.values()) {
							if(type.isSellAll()) {
								money += type.getValue() * profile.getCookies(type);
								sold.put(type, (int) profile.getCookies(type));
								profile.setCookies(type, 0);
							}
						}
						
						profile.setMoney(profile.getMoney() + money);
						storage.updateUserProfile(profile, "cookies", "money");
						
						EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
						
						builder.addField("Sold All!", "Sold " +
								sold.keySet().stream().map(type -> ("**" + Reference.FORMATTER.format(sold.get(type)) + "** " + type.getEmoteName())).collect(Collectors.joining(", ")) +
								" for " + Reference.FORMATTER.format(money) + " " + Reference.EMOTE_DOLLAR, false);
						
						event.reply(builder.build());
					} else {
						event.reply("**Sell all cancelled.**");
					}
				}, () -> {
					event.reply("**Timed out, sell all cancelled.**");
				});
			} else {
				event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
			}
		} else if(arguments.length == 2) {
			Optional<CookieType> typeOpt = CookieType.getCookieTypeByAlias(arguments[0]);
			
			if(typeOpt.isPresent()) {
				CookieType type = typeOpt.get();
				
				if(Reference.isWholeNumber(arguments[1])) {
					long toSell = Long.parseLong(arguments[1]);
					
					if(toSell > 0) {
						if(profile.getCookies(type) >= toSell) {
							double profit = toSell * type.getValue();
							profile.setCookies(type, profile.getCookies(type) - toSell);
							profile.setMoney(profile.getMoney() + profit);
							storage.updateUserProfile(profile, "cookies", "money");
							
							EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
							
							builder.addField("Sold!", "You sold " + Reference.FORMATTER.format(toSell) + " " + type.getEmoteName() + " for " + Reference.FORMATTER.format(profit) + " " + Reference.EMOTE_DOLLAR, false);
							
							event.reply(builder.build());
						} else {
							EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
							
							builder.addField("Oh no!", "You don't have enough " + type.getEmoteName(), false);
							
							event.reply(builder.build());
						}
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						
						builder.addField("Oh no!", "You have no " + type.getEmoteName(), false);
						
						event.reply(builder.build());
					}
				} else if(arguments[1].equalsIgnoreCase("all")) {
					long toSell = profile.getCookies(type);
					
					if(toSell > 0) {
						double profit = toSell * type.getValue();
						profile.setCookies(type, profile.getCookies(type) - toSell);
						profile.setMoney(profile.getMoney() + profit);
						storage.updateUserProfile(profile, "cookies", "money");
						
						EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
						
						builder.addField("Sold!", "You sold " + Reference.FORMATTER.format(toSell) + " " + type.getEmoteName() + " for " + Reference.FORMATTER.format(profit) + " " + Reference.EMOTE_DOLLAR, false);
						
						event.reply(builder.build());
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						
						builder.addField("Oh no!", "You have no " + type.getEmoteName(), false);
						
						event.reply(builder.build());
					}
				} else {
					event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
				}
			} else {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
				builder.addField("Oh no!", "**That cookie type doesn't exist.**"
						+ "\nValid Types: _" + Arrays.stream(CookieType.values()).map(CookieType::getAlias).collect(Collectors.joining(", ")) + "_", false);
				event.reply(builder.build());
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}

}
