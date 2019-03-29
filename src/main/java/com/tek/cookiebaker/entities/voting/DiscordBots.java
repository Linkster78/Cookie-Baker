package com.tek.cookiebaker.entities.voting;

import java.util.Timer;
import java.util.TimerTask;

import org.discordbots.api.client.DiscordBotListAPI;

import com.tek.cookiebaker.log.Logger;
import com.tek.cookiebaker.main.CookieBaker;

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;

public class DiscordBots {
	
	private static DiscordBotListAPI api;
	private static Timer timer;
	
	public static void startUpdater(int minutes) {
		try {
			api = new DiscordBotListAPI.Builder()
					.token(CookieBaker.getInstance().getConfig().getDiscordBotsToken())
					.botId(CookieBaker.getInstance().getConfig().getDiscordBotsId())
					.build();
		} catch(Exception e) { Logger.error(e); }
		
		timer = new Timer("DiscordBots Timer");
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				ShardManager shardManager = CookieBaker.getInstance().getDiscord().getShardManager();
				if(api != null) api.setStats(shardManager.getGuilds().size());
				shardManager.setGame(Game.of(GameType.LISTENING, "~help | " + shardManager.getGuilds().size() + " servers"));
			}
			
		}, 0l, minutes * 60 * 60);
	}
	
}
