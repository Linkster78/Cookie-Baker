package com.tek.cookiebaker.entities.enums;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public enum CookieType {
	
	ORIGINAL_COOKIE("Cookie", "EMOTE_COOKIE", true, 5, 1, 12, 24, 5),
	RAINBOW_COOKIE("Rainbow Cookie", "EMOTE_RAINBOW", false, 250, 0.02, 1, 1, 500),
	BONE_COOKIE("Bone Cookie", "EMOTE_BONE", false, 100, 0, 0, 0, 0),
	GOLDEN_COOKIE("Golden Cookie", "EMOTE_GOLDEN", true, 50, 0.10, 1, 3, 25),
	CUPCAKE_COOKIE("Cupcake", "EMOTE_CUPCAKE", true, 20, 0.4, 2, 8, 10),
	REDVELVET_COOKIE("Red Velvet Cookie", "EMOTE_REDVELVET", false, 10, 0, 0, 0, 0);
	
	public static String EMOTE_COOKIE;
	public static String EMOTE_RAINBOW;
	public static String EMOTE_BONE;
	public static String EMOTE_GOLDEN;
	public static String EMOTE_CUPCAKE;
	public static String EMOTE_REDVELVET;
	
	private String displayName;
	private String fieldName;
	private boolean sellAll;
	private int value;
	private double chance;
	private int min;
	private int max;
	private double experience;
	
	private CookieType(String displayName, String fieldName, boolean sellAll, int value, double chance, int min, int max, double experience) {
		this.displayName = displayName;
		this.fieldName = fieldName;
		this.sellAll = sellAll;
		this.value = value;
		this.chance = chance;
		this.min = min;
		this.max = max;
		this.experience = experience;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getAlias() {
		return name().toLowerCase().split("_")[0];
	}
	
	public boolean isSellAll() {
		return sellAll;
	}
	
	public int getValue() {
		return value;
	}
	
	public double getChance() {
		return chance;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	public double getExperience() {
		return experience;
	}
	
	public String getEmoteID() {
		return getEmoteName().split(":")[2].replaceAll(">", "");
	}
	
	public String getEmoteName() {
		try {
			Class<?> cookieClass = CookieType.class;
			Field field = cookieClass.getField(fieldName);
			return (String) field.get(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setEmoteName(String emoteName) {
		try {
			Class<?> cookieClass = CookieType.class;
			Field field = cookieClass.getField(fieldName);
			field.set(null, emoteName);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static Optional<CookieType> getCookieTypeByAlias(String alias) {
		return Arrays.stream(CookieType.values()).filter(type -> type.getAlias().equalsIgnoreCase(alias)).findFirst();
	}
	
}
