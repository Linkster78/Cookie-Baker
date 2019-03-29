package com.tek.cookiebaker.webhook;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tek.cookiebaker.main.CookieBaker;

@SuppressWarnings("restriction")
public class LogHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		try {
			String text = CookieBaker.getInstance().getLogger().getRecentMost(-1).stream().collect(Collectors.joining("\n"));
			exchange.sendResponseHeaders(200, text.getBytes(Charset.forName("Unicode")).length);
			OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody(), "Unicode");
			writer.write(text);
			writer.close();
			exchange.getResponseBody().close();
			exchange.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
