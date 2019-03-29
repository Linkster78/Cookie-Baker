package com.tek.cookiebaker.main;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.command.Command;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;

public class Reference {
	
	public static final String OWNER_ID = "148118565386584065";
	public static final String DEFAULT_PREFIX = "~";
	
	public static final Random RANDOM = new Random();
	public static final Gson GSON = new Gson();
	public static final NumberFormat FORMATTER = NumberFormat.getNumberInstance();
	
	public static final String EMOTE_DOLLAR = ":dollar:";
	
	public static final int GUILD_COST = 75000;
	public static final int LEVEL_BASE_COST = 1500;
	public static final int COOLDOWN = 5000;
	public static final short VERIFICATION_ATTEMPTS = 3;
	public static final short VERIFICATION_INTERVAL = 240;
	public static final long VERIFICATION_TIMEOUT = TimeUnit.MINUTES.toMillis(30);
	
	public static final String CONFIG_PATH = "./config.json";
	public static final String MANUAL_PATH = "./manual.txt";
	public static final String LOG_PATH = "./log.txt";
	public static final String STORAGE_USER_PROFILES = "user_profiles";
	public static final String STORAGE_SERVER_PROFILES = "server_profiles";
	public static final String STORAGE_DONATIONS = "donations";
	public static final String STORAGE_GUILDS = "guilds";
	
	public static EmbedBuilder createBlankEmbed(Color color, JDA jda) {
		User self = jda.getSelfUser();
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(color);
		builder.setAuthor(self.getName(), null, self.getAvatarUrl());
		
		return builder;
	}
	
	public static String getInvalidSyntax(Command command, String prefix) {
		return "**Invalid Syntax** `" + prefix + command.getName() + (command.getArguments() == null || command.getArguments().isEmpty() ? "" : " " + command.getArguments()) + "`";
	}
	
	public static boolean isWholeNumberInt(String str) {
		try {
			int i = Integer.parseInt(str);
			if(i < 0) return false;
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	public static boolean isWholeNumber(String str) {
		try {
			long l = Long.parseLong(str);
			if(l < 0) return false;
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	public static boolean isDouble(String str) {
		try {
			double d = Double.parseDouble(str);
			if(d < 0) return false;
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	public static double round(double d) {
		return Math.ceil(d * 100) / 100;
	}
	
	public static String getUsername(User user) {
		return user.getName() + "#" + user.getDiscriminator();
	}
	
	public static String getFormattedTime(long left) {
		if(left < TimeUnit.MINUTES.toMillis(1)) {
			return Reference.round((double)left / (double)1000) + " seconds";
		} else if(left < TimeUnit.HOURS.toMillis(1)) {
			return TimeUnit.MILLISECONDS.toMinutes(left) + " minutes";
		} else if(left < TimeUnit.DAYS.toMillis(1)) {
			return TimeUnit.MILLISECONDS.toHours(left) + " hours";
		} else if(left >= TimeUnit.DAYS.toMillis(1)) {
			return TimeUnit.MILLISECONDS.toDays(left) + " days";
		}
		
		return "look just contact the developer ok ?";
	}
	
	public static String fromBase64(String base64) {
		return new String(Base64.getDecoder().decode(base64));
	}
	
	public static String capitalize(String text) {
		return text.toUpperCase().substring(0, 1) + text.toLowerCase().substring(1);
	}
	
}