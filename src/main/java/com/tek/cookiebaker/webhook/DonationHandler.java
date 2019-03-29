package com.tek.cookiebaker.webhook;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tek.cookiebaker.api.donations.Donation;
import com.tek.cookiebaker.log.Logger;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.storage.UserProfile;

@SuppressWarnings("restriction")
public class DonationHandler implements HttpHandler {

	private Consumer<Donation> donationCallback;
	
	public DonationHandler(Consumer<Donation> donationCallback) {
		this.donationCallback = donationCallback;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String host = exchange.getRemoteAddress().getAddress().getHostName();
		
		if(!host.equals("149.56.110.177")) {
			try {
				exchange.sendResponseHeaders(500, 0);
				exchange.getResponseBody().close();
			} catch (IOException e) {
				Logger.error(e);
			}
			return;
		}
		
		Map<String, String> params = WebhookServer.queryToMap(exchange.getRequestURI().toString());
		String status = params.get("status");
		String userId = params.get("user_id");
		String userTag = params.get("user_tag");
		String guildId = params.get("guild_id");
		String amount = params.get("amount");
		String currency = params.get("currency");
		String txnId = params.get("txn_id");
		
		if(status.equalsIgnoreCase("completed")) {
			try{
				Donation donation = new Donation(txnId, userId, userTag, guildId, Double.parseDouble(amount), currency, status);
				UserProfile profile = CookieBaker.getInstance().getStorage().getUserProfile(donation.getUserId());
				if(profile == null) return;
				CookieBaker.getInstance().getStorage().insertDonation(donation);
				donationCallback.accept(donation);
			} catch(Exception e) {
				Logger.error(e);
			}
		}
		
		try {
			exchange.sendResponseHeaders(200, 0);
			exchange.getResponseBody().close();
			exchange.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
