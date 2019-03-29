package com.tek.cookiebaker.api.donations;

public class Donation {
	
	private String txn_id;
	private String user;
	private String userTag;
	private String guild;
	private double amount;
	private String currency;
	private String status;
	private String timestamp;
	
	public Donation() { }
	
	public Donation(String txn_id, String userId, String userTag, String guild, double amount, String currency, String status) {
		this.txn_id = txn_id;
		this.user = userId;
		this.userTag = userTag;
		this.guild = guild;
		this.amount = amount;
		this.currency = currency;
		this.status = status;
	}
	
	public String getTxnId() {
		return txn_id;
	}
	
	public String getUserId() {
		return user;
	}
	
	public String getUserTag() {
		return userTag;
	}
	
	public String getGuild() {
		return guild;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public String getCurrency() {
		return currency;
	}
	
	public String getStatus() {
		return status;
	}
	
	public boolean isCompleted() {
		return status.equalsIgnoreCase("completed");
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
}
