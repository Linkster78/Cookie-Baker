package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.entities.samples.Sample;
import com.tek.cookiebaker.entities.samples.SampleManager;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class SampleCommand extends Command {

	public SampleCommand() {
		this.name = "sample";
		this.aliases = new String[] {"samples", "kit", "kits"};
		this.arguments = "[sample]";
		this.help = "Takes one of the samples (Kits)";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(user);
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		SampleManager sampleManager = CookieBaker.getInstance().getSampleManager();
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 0) {
			EmbedBuilder builder = Reference.createBlankEmbed(Color.yellow, event.getJDA());
			
			StringBuilder sampleBuilder = new StringBuilder();
			for(Sample sample : sampleManager.getSamples()) {
				if(sample.canClaim(profile) && ((sample.isDonator() && profile.hasDonated()) || !sample.isDonator())) {
					sampleBuilder.append("**" + sample.getDisplayName() + "**: `" + serverProfile.getPrefix() + "sample " + sample.getAlias() + "`\n");
				} else {
					sampleBuilder.append("~~" + sample.getDisplayName() + "~~: `" + serverProfile.getPrefix() + "sample " + sample.getAlias() + "`\n");
				}
			}
			
			builder.addField("Available Samples", sampleBuilder.toString(), false);
			
			event.reply(builder.build());
		} else if(arguments.length == 1) {
			Optional<Sample> sampleOpt = sampleManager.getSampleByAlias(arguments[0]);
			
			if(sampleOpt.isPresent()) {
				Sample sample = sampleOpt.get();
				
				if((sample.isDonator() && profile.hasDonated()) || !sample.isDonator()) {
					if(sample.canClaim(profile)) {
						sample.grant(profile);
						
						EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
						
						StringBuilder sampleContents = new StringBuilder();
						sampleContents.append("You took a bite of the **" + sample.getDisplayName() + "**\n");
						sampleContents.append("_It contained:_ \n");
						for(String key : sample.getContents().keySet()) {
							Optional<CookieType> typeOpt = CookieType.getCookieTypeByAlias(key);
							
							if(typeOpt.isPresent()) {
								CookieType type = typeOpt.get();
								
								sampleContents.append(" - **" + sample.getContents().get(key) + "** " + type.getEmoteName() + "\n");
							}
						}
						
						builder.addField("You claimed a sample", sampleContents.toString(), false);
						
						event.reply(builder.build());
					} else {
						String time = sample.getFormattedTime(profile);
						
						EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
						
						builder.addField("Oh no.", "You must wait " + time + " before using this sample again", false);
						
						event.reply(builder.build());
					}
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					
					builder.addField("Oh no.", "You need to be a donator to claim this sample.", false);
					
					event.reply(builder.build());
				}
			} else {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
				
				builder.addField("Hey there!", "**That sample doesn't exist**\nValid Samples: _" +
								sampleManager.getSamples().stream().map(Sample::getAlias).collect(Collectors.joining(", ")) + "_", false);
				
				event.reply(builder.build());
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}

}
