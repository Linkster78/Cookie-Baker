package com.tek.cookiebaker.entities.upgrades;

import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.entities.enums.UpgradeType;
import com.tek.cookiebaker.entities.shop.ISellable;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.storage.UserProfile;

public class TemperatureUpgrade extends Upgrade implements ISellable {
	
	public TemperatureUpgrade() {
		super(UpgradeType.OVEN, "Temperature", "temperature", 5);
	}
	
	public int getCooldown(int level) {
		return 5000 - level * 500;
	}
	
	@Override
	public String getDisplayName(UserProfile profile) {
		int level = profile.getUpgrade(TemperatureUpgrade.class);
		return "Temperature Upgrade " + (level >= getMaxLevel() ? "MAXED" : level + 1);
	}
	
	@Override
	public String getPrice(UserProfile profile) {
		int level = profile.getUpgrade(TemperatureUpgrade.class);
		return level >= getMaxLevel() ? "NA" : getPrice(level + 1) + " " + CookieType.RAINBOW_COOKIE.getEmoteName();
	}
	
	@Override
	public boolean has(UserProfile profile) {
		int level = profile.getUpgrade(TemperatureUpgrade.class);
		return level >= getMaxLevel();
	}
	
	@Override
	public boolean canBuy(UserProfile profile) {
		int level = profile.getUpgrade(TemperatureUpgrade.class);
		return profile.getCookies(CookieType.RAINBOW_COOKIE) >= getPrice(level + 1);
	}
	
	@Override
	public void buy(UserProfile profile) {
		int level = profile.getUpgrade(TemperatureUpgrade.class);
		profile.setCookies(CookieType.RAINBOW_COOKIE, profile.getCookies(CookieType.RAINBOW_COOKIE) - getPrice(level + 1));
		profile.upgrade(TemperatureUpgrade.class);
		CookieBaker.getInstance().getStorage().updateUserProfile(profile, "cookies", "upgrades");
	}
	
	private int getPrice(int level) {
		return level * 10;
	}
	
}
