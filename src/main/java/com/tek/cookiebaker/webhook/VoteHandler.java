package com.tek.cookiebaker.webhook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tek.cookiebaker.entities.voting.Vote;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;

@SuppressWarnings("restriction")
public class VoteHandler implements HttpHandler {

	private Consumer<Vote> voteCallback;
	
	public VoteHandler(Consumer<Vote> voteCallback) {
		this.voteCallback = voteCallback;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		Headers hs = exchange.getRequestHeaders();
		String auth = hs.getFirst("Authorization");
		
		if(!auth.equals(CookieBaker.getInstance().getConfig().getDiscordbotsAuth())) {
			exchange.sendResponseHeaders(500, 0);
			exchange.getResponseBody().close();
			return;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
		
		StringBuilder body = new StringBuilder();
		String line;
		while((line = br.readLine()) != null) {
			body.append(line + "\n");
		}
		if(body.length() > 0) body.setLength(body.length() - 1);
		
		try {
			Vote vote = Reference.GSON.fromJson(body.toString(), Vote.class);
			voteCallback.accept(vote);
		} catch(JsonSyntaxException e) { }
		
		exchange.sendResponseHeaders(200, 0);
		exchange.getResponseBody().close();
		exchange.close();
	}

}
