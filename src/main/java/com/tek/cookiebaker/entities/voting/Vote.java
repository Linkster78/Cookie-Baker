package com.tek.cookiebaker.entities.voting;

public class Vote {
	
	private String bot;
	private String user;
	private String type;
	private boolean isWeekend;
	private String query;
	
	public String getBot() {
		return bot;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean isWeekend() {
		return isWeekend;
	}
	
	public String getQuery() {
		return query;
	}
	
}
