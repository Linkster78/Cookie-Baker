package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class TransferCommand extends Command {

	public TransferCommand() {
		this.name = "transfer";
		this.aliases = new String[] {"pay"};
		this.arguments = "<user> <money/type> <amount>";
		this.help = "Transfers cookies/money to another user";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(user);
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 3) {
			if(event.getMessage().getMentionedMembers().size() == 1) {
				Member mentioned = event.getMessage().getMentionedMembers().get(0);
				UserProfile payTo = storage.getUserProfile(mentioned.getUser());
				
				if(arguments[0].length() == mentioned.getAsMention().length() || arguments[0].length() == mentioned.getUser().getAsMention().length()) {
					if(!payTo.getUserId().equals(profile.getUserId())) {
						if(!mentioned.getUser().isBot()) {
							if(arguments[1].equalsIgnoreCase("money")) {
								if(Reference.isDouble(arguments[2])) {
									double amount = Double.parseDouble(arguments[2]);
									
									if(profile.getMoney() >= amount) {
										if(amount >= 10) {
											profile.setMoney(profile.getMoney() - amount);
											payTo.setMoney(payTo.getMoney() + amount);
											storage.updateUserProfile(profile, "money");
											storage.updateUserProfile(payTo, "money");
											
											CookieBaker.getInstance().getLogger().logMoneyTransfer(profile, payTo, amount, event.getChannel().getId(), event.getGuild().getId());
											
											EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
											builder.addField("Money Transferred", "You gave " + Reference.FORMATTER.format(amount) + " " + Reference.EMOTE_DOLLAR + " to `" + payTo.getName() + "`", false);
											event.reply(builder.build());
										} else {
											EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
											
											builder.addField("Oh no!", "You can only transfer upwards of 10.00 " + Reference.EMOTE_DOLLAR, false);
										
											event.reply(builder.build());
										}
									} else {
										EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
										
										builder.addField("Oh no!", "You don't have enough " + Reference.EMOTE_DOLLAR, false);
									
										event.reply(builder.build());
									}
								} else {
									event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
								}
							} else {
								Optional<CookieType> typeOpt = CookieType.getCookieTypeByAlias(arguments[1]);
								
								if(typeOpt.isPresent()) {
									CookieType type = typeOpt.get();
									
									if(Reference.isWholeNumber(arguments[2])) {
										long amount = Long.parseLong(arguments[2]);
										
										if(profile.getCookies(type) >= amount) {
											profile.setCookies(type, profile.getCookies(type) - amount);
											payTo.setCookies(type, payTo.getCookies(type) + amount);
											storage.updateUserProfile(profile, "cookies");
											storage.updateUserProfile(payTo, "cookies");
											
											CookieBaker.getInstance().getLogger().logCookieTransfer(profile, payTo, type, amount, event.getChannel().getId(), event.getGuild().getId());
											
											EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
											builder.addField("Cookies Transferred", "You gave " + Reference.FORMATTER.format(amount) + " " + type.getEmoteName() + " to `" + payTo.getName() + "`", false);
											event.reply(builder.build());
										} else {
											EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
											
											builder.addField("Oh no!", "You don't have enough " + type.getEmoteName(), false);
										
											event.reply(builder.build());
										}
									} else {
										event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
									}
								} else {
									EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
									builder.addField("Oh no!", "**That source doesn't exist.**"
											+ "\nValid Types: _money, " + Arrays.stream(CookieType.values()).map(CookieType::getAlias).collect(Collectors.joining(", ")) + "_", false);
									event.reply(builder.build());
								}
							}
						} else {
							EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
							builder.addField("Hey there!", "You can't transfer to a bot", false);
							event.reply(builder.build());
						}
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						builder.addField("Hey there!", "You can't transfer to  yourself", false);
						event.reply(builder.build());
					}
				} else {
					event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
				}
			} else {
				event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}

}
