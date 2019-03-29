package com.tek.cookiebaker.entities.games;

import java.util.concurrent.ScheduledFuture;

import com.tek.cookiebaker.entities.games.framework.GameData;

public class ClickerData extends GameData {

	private String messageId;
	private int cookies;
	private ScheduledFuture<?> endFuture;
	private boolean processedClick;
	
	public ClickerData(String userId, String channelId, double multiplier) {
		super(userId, channelId, multiplier);
		this.cookies = 0;
		this.processedClick = true;
	}
	
	@Override
	public void delete() {
		if(endFuture != null) endFuture.cancel(true);
	}
	
	public void incrementCookies(int count) {
		this.cookies += count;
	}
	
	public void setCookies(int cookies) {
		this.cookies = cookies;
	}
	
	public int getCookies() {
		return cookies;
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public ScheduledFuture<?> getEndFuture() {
		return endFuture;
	}

	public void setEndFuture(ScheduledFuture<?> endFuture) {
		this.endFuture = endFuture;
	}
	
	public void setProcessedClick(boolean processedClick) {
		this.processedClick = processedClick;
	}
	
	public boolean isProcessedClick() {
		return processedClick;
	}

}
