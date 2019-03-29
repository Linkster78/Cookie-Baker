package com.tek.cookiebaker.webhook;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.tek.cookiebaker.api.donations.Donation;
import com.tek.cookiebaker.entities.voting.Vote;

@SuppressWarnings("restriction")
public class WebhookServer {
	
	private HttpServer server;
	
	private static Consumer<Vote> voteCallback;
	private static Consumer<Donation> donationCallback;
	
	public WebhookServer(Consumer<Vote> voteCallback, Consumer<Donation> donationCallback) {
		WebhookServer.voteCallback = voteCallback;
		WebhookServer.donationCallback = donationCallback;
	}
	
	public void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(8443), 0);
		HttpContext donationContext = server.createContext("/donations");
		HttpContext voteContext = server.createContext("/votes");
		HttpContext logContext = server.createContext("/logs");
		donationContext.setHandler(new DonationHandler(donationCallback));
		voteContext.setHandler(new VoteHandler(voteCallback));
		logContext.setHandler(new LogHandler());
		
		server.start();
	}
	
	public static Map<String, String> queryToMap(String query){
	    Map<String, String> result = new HashMap<String, String>();
	    query = query.substring(query.indexOf('?') + 1);
	    System.out.println(query);
	    for (String param : query.split("&")) {
	        String pair[] = param.split("=");
	        if (pair.length>1) {
	            result.put(pair[0], pair[1]);
	        }else{
	            result.put(pair[0], "");
	        }
	    }
	    return result;
	}
	
}
