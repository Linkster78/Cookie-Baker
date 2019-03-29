package com.tek.cookiebaker.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONObject;

import com.tek.cookiebaker.entities.enums.CookieType;

public class Config {
	
	private String token;
	private String mongo_host;
	private String mongo_db;
	private String server_invite;
	private String bot_invite;
	private String donohook_token;
	private String discordbots_token;
	private String discordbots_id;
	private String discordbots_auth;
	private String samples;
	private String packages;
	
	private Config(JSONObject config) throws NullPointerException {
		if(!config.has("token")) throw new NullPointerException("Token parameter not found");
		if(!config.has("mongo_host")) throw new NullPointerException("Mongo Host parameter not found");
		if(!config.has("mongo_db")) throw new NullPointerException("Mongo DB parameter not found");
		if(!config.has("server_invite")) throw new NullPointerException("Server Invite parameter not found");
		if(!config.has("bot_invite")) throw new NullPointerException("Bot Invite parameter not found");
		if(!config.has("emote_map")) throw new NullPointerException("Emote Map parameter not found");
		if(!config.has("donohook_token")) throw new NullPointerException("DonoHook Token parameter not found");
		if(!config.has("discordbots_auth")) throw new NullPointerException("DiscordBots AUTH parameter not found");
		token = config.getString("token");
		mongo_host = config.getString("mongo_host");
		mongo_db = config.getString("mongo_db");
		server_invite = config.getString("server_invite");
		bot_invite = config.getString("bot_invite");
		donohook_token = config.getString("donohook_token");
		discordbots_auth = config.getString("discordbots_auth");
		if(config.has("discordbots_token")) discordbots_token = config.getString("discordbots_token");
		if(config.has("discordbots_id")) discordbots_id = config.getString("discordbots_id");
		
		JSONObject emoteMap = config.getJSONObject("emote_map");
		for(CookieType type : CookieType.values()) {
			if(emoteMap.has(type.getAlias())) {
				type.setEmoteName(emoteMap.getString(type.getAlias()));
			} else {
				throw new NullPointerException("In Emote Map, " + type.getAlias() + " parameter not found");
			}
		}
		
		if(config.has("samples")) {
			this.samples = config.getJSONArray("samples").toString();
		} else {
			this.samples = "[]";
		}
		
		if(config.has("packages")) {
			this.packages = config.getJSONArray("packages").toString();
		} else {
			this.packages = "[]";
		}
	}
	
	public static Config load(String configPath) throws IOException, NullPointerException {
		StringBuilder configContents = new StringBuilder();
		
		try{
			FileInputStream fis = new FileInputStream(new File(configPath));
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			
			while((line = br.readLine()) != null) {
				configContents.append(line).append("\n");
			}
			
			br.close();
		}catch(IOException e) {
			throw new IOException("Configuration file \"" + configPath + "\" not found");
		}
		
		String stringContents = configContents.toString();
		
		JSONObject config = new JSONObject(stringContents);
		
		return new Config(config);
	}
	
	public String getToken() {
		return token;
	}
	
	public String getMongoHost() {
		return mongo_host;
	}
	
	public String getMongoDB() {
		return mongo_db;
	}
	
	public String getServerInvite() {
		return server_invite;
	}
	
	public String getBotInvite() {
		return bot_invite;
	}
	
	public String getDonoHookToken() {
		return donohook_token;
	}
	
	public String getDiscordBotsToken() {
		return discordbots_token;
	}
	
	public String getDiscordBotsId() {
		return discordbots_id;
	}
	
	public String getDiscordbotsAuth() {
		return discordbots_auth;
	}
	
	public String getSamples() {
		return samples;
	}
	
	public String getPackages() {
		return packages;
	}
	
}
