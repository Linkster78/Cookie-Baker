package com.tek.cookiebaker.entities.botting;

import java.util.concurrent.TimeUnit;

public class MiningSession {
	
	private static final long MINE_TRACK_TIMEOUT = TimeUnit.MINUTES.toMillis(15);
	
	private long startTime;
	private long lastMineTime;
	
	public MiningSession() {
		this(System.currentTimeMillis(), System.currentTimeMillis());
	}
	
	public MiningSession(long startTime, long lastMineTime) {
		this.startTime = startTime;
		this.lastMineTime = lastMineTime;
	}
	
	public long elapsed() {
		return lastMineTime - startTime;
	}
	
	public void mined(long messageTime) {
		setStartTime(messageTime - MINE_TRACK_TIMEOUT <= getLastMineTime() ? getStartTime() : messageTime);
		setLastMineTime(messageTime);
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public void setLastMineTime(long lastMineTime) {
		this.lastMineTime = lastMineTime;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getLastMineTime() {
		return lastMineTime;
	}
	
}
