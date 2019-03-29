package com.tek.cookiebaker.entities.games.framework;

public class GameData {
	
	private String userId;
	private String channelId;
	private double multiplier;
	
	public GameData(String userId, String channelId, double multiplier) {
		this.userId = userId;
		this.channelId = channelId;
		this.multiplier = multiplier;
	}
	
	public void delete() { }
	
	public String getUserId() {
		return userId;
	}
	
	public String getChannelId() {
		return channelId;
	}
	
	public double getMultiplier() {
		return multiplier;
	}
	
}
