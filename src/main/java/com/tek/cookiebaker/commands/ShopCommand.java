package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mongodb.client.model.Updates;
import com.tek.cookiebaker.entities.enums.Oven;
import com.tek.cookiebaker.entities.shop.ISellable;
import com.tek.cookiebaker.entities.shop.Shop;
import com.tek.cookiebaker.entities.upgrades.GuildCapacityUpgrade;
import com.tek.cookiebaker.entities.upgrades.TemperatureUpgrade;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class ShopCommand extends Command {

	private List<Shop> shops;
	
	public ShopCommand() {
		this.name = "shop";
		this.aliases = new String[] {"shops"};
		this.arguments = "[name] [id]";
		this.help = "Displays the shop and allows purchases";
		
		shops = new ArrayList<Shop>();
		
		Shop ovenShop = new Shop("Ovens Shop", "ovens", "oven");
		for(Oven oven : Oven.values()) {
			if(oven.isBuyable()) {
				ovenShop.addItem(new ISellable() {
					public String getDisplayName(UserProfile profile) { return oven.getDisplayName() + " (" + oven.getMultiplier() + "x)"; }
					public String getPrice(UserProfile profile) { return Reference.FORMATTER.format(oven.getPrice()) + "$"; }
					public boolean has(UserProfile profile) { return profile.getUnlockedOvens().contains(oven); }
					public boolean canBuy(UserProfile profile) {
						return profile.getMoney() >= oven.getPrice();
					}
					public void buy(UserProfile profile) {
						profile.getUnlockedOvens().add(oven);
						profile.setMoney(profile.getMoney() - oven.getPrice());
						profile.setCurrentOven(oven);
						CookieBaker.getInstance().getStorage().updateUserProfileDirect(profile,
								Updates.combine(
										Updates.set("unlockedOvens", profile.getUnlockedOvens().stream().map(Oven::name).collect(Collectors.toList())),
										Updates.set("money", profile.getMoney()),
										Updates.set("currentOven", profile.getCurrentOven().name())));
					}
				});
			}
		}
		
		Shop upgradeShop = new Shop("Upgrades Shop", "upgrades", "upgrade");
		upgradeShop.addItem(new TemperatureUpgrade());
		
		Shop guildShop = new Shop("Guild Shop", "guilds", "guild");
		guildShop.addItem(new GuildCapacityUpgrade());
		
		shops.add(ovenShop);
		shops.add(upgradeShop);
		shops.add(guildShop);
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(user);
		ServerProfile serverProfile = CookieBaker.getInstance().getStorage().getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 0) {
			EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
			
			StringBuilder shopBuilder = new StringBuilder();
			for(Shop shop : shops) {
				shopBuilder.append("**" + shop.getDisplayName() + "** | ");
				shopBuilder.append("`" + serverProfile.getPrefix() + "shop " + shop.getMainAlias() + "`");
				shopBuilder.append("\n");
			}
			
			builder.addField("Available Shops", shopBuilder.toString(), false);
			
			event.reply(builder.build());
		} else if(arguments.length == 1) {
			Optional<Shop> shopOpt = shops.stream().filter(shop -> shop.matchAlias(arguments[0])).findFirst();
			
			if(shopOpt.isPresent()) {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.orange, event.getJDA());
				
				int num = 1;
				StringBuilder shopBuilder = new StringBuilder();
				for(ISellable item : shopOpt.get().getSoldItems()) {
					shopBuilder.append("**" + num + "**. `");
					shopBuilder.append(item.getDisplayName(profile) + "` | **" + item.getPrice(profile) + "**\n");
					num++;
				}
				
				builder.addField(shopOpt.get().getDisplayName(), shopBuilder.toString(), false);
				builder.setFooter("To buy, use " + serverProfile.getPrefix() + "shop " + shopOpt.get().getMainAlias() + " <id>", null);
				
				event.reply(builder.build());
			} else {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
				
				builder.addField("Oh no!", "**This shop doesn't exist**\nValid Shops: _" + 
						shops.stream().map(Shop::getMainAlias).collect(Collectors.joining(", ")) + "_", false);
				
				event.reply(builder.build());
			}
		} else if(arguments.length == 2) {
			Optional<Shop> shopOpt = shops.stream().filter(shop -> shop.matchAlias(arguments[0])).findFirst();
			
			if(shopOpt.isPresent()) {
				if(Reference.isWholeNumberInt(arguments[1])) {
					int id = Integer.parseInt(arguments[1]) - 1;
					
					if(id >= 0 && id < shopOpt.get().getSoldItems().size()) {
						ISellable toBuy = shopOpt.get().getSoldItems().get(id);
						
						if(!toBuy.has(profile)) {
							if(toBuy.canBuy(profile)) {
								EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
								
								builder.addField("Bought!", "You bought a " + toBuy.getDisplayName(profile) + " for " + toBuy.getPrice(profile), false);
								
								toBuy.buy(profile);
								
								event.reply(builder.build());
							} else {
								EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
								
								builder.addField("Oh no!", "You don't have enough to buy this", false);
								
								event.reply(builder.build());
							}
						} else {
							EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
							
							builder.addField("Oh no!", "You may not buy this item", false);
							
							event.reply(builder.build());
						}
					} else {
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						
						builder.addField("Oh no!", "The ID you gave doesn't match an item", false);
						
						event.reply(builder.build());
					}
				} else {
					event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
				}
			} else {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
				
				builder.addField("Oh no!", "**This shop doesn't exist**\nValid Shops: _" + 
						shops.stream().map(Shop::getMainAlias).collect(Collectors.joining(", ")) + "_", false);
				
				event.reply(builder.build());
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}

}
