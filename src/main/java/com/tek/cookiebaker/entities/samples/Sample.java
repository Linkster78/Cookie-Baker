package com.tek.cookiebaker.entities.samples;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.UserProfile;

public class Sample {
	
	private boolean donator;
	private String displayName;
	private String alias;
	private int minutes;
	private Map<String, Integer> contents;
	
	public Sample(boolean donator, String displayName, String alias, int minutes, Map<String, Integer> contents) {
		this.donator = donator;
		this.displayName = displayName;
		this.alias = alias;
		this.minutes = minutes;
		this.contents = contents;
	}
	
	public boolean canClaim(UserProfile profile) {
		return TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - profile.getSampleTime(this)) >= minutes;
	}
	
	public String getFormattedTime(UserProfile profile) {
		long elapsed = System.currentTimeMillis() - profile.getSampleTime(this);
		long left = TimeUnit.MINUTES.toMillis(minutes) - elapsed;
		
		return Reference.getFormattedTime(left);
	}
	
	public void grant(UserProfile profile) {
		for(String key : contents.keySet()) {
			Optional<CookieType> typeOpt = CookieType.getCookieTypeByAlias(key);
			
			if(typeOpt.isPresent()) {
				profile.setCookies(typeOpt.get(), profile.getCookies(typeOpt.get()) + contents.get(key));
			}
		}
		
		profile.setSampleTime(this, System.currentTimeMillis());
		
		CookieBaker.getInstance().getStorage().updateUserProfile(profile, "cookies", "sampleTimes");
	}
	
	public boolean isDonator() {
		return donator;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public int getMinutes() {
		return minutes;
	}
	
	public Map<String, Integer> getContents() {
		return contents;
	}
	
}
