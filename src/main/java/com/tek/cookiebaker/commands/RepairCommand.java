package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.api.captcha.Captcha;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class RepairCommand extends Command {

	public RepairCommand() {
		this.name = "repair";
		this.aliases = new String[] {"verify"};
		this.arguments = "<code>";
		this.help = "Repairs the oven";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(user);
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 1) {
			if(profile.isVerifying()) {
				String code = arguments[0];
				
				if(code.equalsIgnoreCase(profile.getVerificationCode())) {
					CookieBaker.getInstance().getLogger().logSuccessVerification(profile, code, Reference.VERIFICATION_ATTEMPTS - profile.getVerifAttempts() + 1, profile.getVerificationBakeAttempts(), System.currentTimeMillis() - profile.getVerificationStart(), event.getChannel().getId(), event.getGuild().getId());
					
					profile.resetVerify();
					storage.updateUserProfile(profile, "bakesVerif", "verifAttempts");
					
					EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
					builder.addField("Hurray!", "You repaired your oven", false);
					event.reply(builder.build());
				} else {
					event.async(() -> newVerification(true, user, profile, serverProfile, event.getChannel()));
				}
			} else {
				EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
				builder.addField("Huh what?", "There's no verification needed for you", false);
				event.reply(builder.build());
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}
	
	public static void newVerification(boolean incorrect, User user, UserProfile profile, ServerProfile serverProfile, MessageChannel channel) {
		try {
			boolean out = false;
			if(incorrect) out = profile.failedVerification();
			
			String key = Captcha.generateKey();
			BufferedImage verif = Captcha.fetchCaptcha(key);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(verif, "png", bos);
			String code = Captcha.computeCode(key);
			
			MessageBuilder messageBuilder = new MessageBuilder();
			EmbedBuilder builder = Reference.createBlankEmbed(Color.red, channel.getJDA());
			
			if(incorrect) {
				if(out) {
					CookieBaker.getInstance().getLogger().logFailedVerification(profile, profile.getVerificationCode(), System.currentTimeMillis() - profile.getVerificationStart(), channel.getId(), serverProfile.getServerId());
					
					profile.resetVerify();
					profile.timeout(Reference.VERIFICATION_TIMEOUT);
					CookieBaker.getInstance().getStorage().updateUserProfile(profile, "bakesVerif", "verifAttempts", "lastBaked");
					
					builder.addField("Failed to repair!", "**Your oven is out of service for " + TimeUnit.MILLISECONDS.toMinutes(Reference.VERIFICATION_TIMEOUT) + " minutes**", false);
					
					messageBuilder.setEmbed(builder.build());
					channel.sendMessage(messageBuilder.build()).queue();
					return;
				} else {
					CookieBaker.getInstance().getLogger().logErrorVerification(profile, profile.getVerificationCode(), channel.getId(), serverProfile.getServerId());
					
					CookieBaker.getInstance().getStorage().updateUserProfile(profile, "verifAttempts");
					builder.addField("Wrong code!", "Attempts left: **" + profile.getVerifAttempts() + "**", false);
				}
			}
			
			profile.setVerificationCode(code);
			CookieBaker.getInstance().getStorage().updateUserProfile(profile, "verificationCode");
			
			builder.addField("Oh no...", "**Your oven broke down!**\n" + 
										"_The repair guide reads as follows_", false);
			builder.setImage("attachment://captcha.png");
			builder.setFooter("Repair it with " + serverProfile.getPrefix() + "repair <code>", null);
			
			messageBuilder.setEmbed(builder.build());
			
			channel.sendFile(new ByteArrayInputStream(bos.toByteArray()), "captcha.png", messageBuilder.build()).queue();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
