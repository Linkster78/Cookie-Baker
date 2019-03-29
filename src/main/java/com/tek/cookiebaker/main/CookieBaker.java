package com.tek.cookiebaker.main;

import java.io.IOException;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.tek.cookiebaker.api.donations.DonoHook;
import com.tek.cookiebaker.entities.games.ClickerGame;
import com.tek.cookiebaker.entities.games.TriviaGame;
import com.tek.cookiebaker.entities.games.framework.GameManager;
import com.tek.cookiebaker.entities.packages.PackageManager;
import com.tek.cookiebaker.entities.samples.SampleManager;
import com.tek.cookiebaker.entities.upgrades.GuildCapacityUpgrade;
import com.tek.cookiebaker.entities.upgrades.TemperatureUpgrade;
import com.tek.cookiebaker.entities.upgrades.UpgradeManager;
import com.tek.cookiebaker.entities.voting.DiscordBots;
import com.tek.cookiebaker.listeners.DonationListener;
import com.tek.cookiebaker.listeners.VoteListener;
import com.tek.cookiebaker.log.FileLogger;
import com.tek.cookiebaker.log.Logger;
import com.tek.cookiebaker.storage.Config;
import com.tek.cookiebaker.storage.Manual;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.webhook.WebhookServer;

import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CookieBaker {
	
	private static CookieBaker instance;
	
	private FileLogger logger;
	private Manual manual;
	private Config config;
	private Storage storage;
	private DiscordBot discord;
	private DonoHook donoHook;
	private SampleManager sampleManager;
	private PackageManager packageManager;
	private UpgradeManager upgradeManager;
	private GameManager gameManager;
	private WebhookServer server;
	
	public CookieBaker(Config config) {
		this.config = config;
	}
	
	public static int shardCount = 0;
	
	public void start() throws Exception {
		instance = this;
		
		logger = new FileLogger(Reference.LOG_PATH);
		logger.initialize();
		
		manual = Manual.load(Reference.MANUAL_PATH);
		
		sampleManager = new SampleManager();
		sampleManager.loadSamples(config.getSamples());
		
		packageManager = new PackageManager();
		packageManager.loadPackages(config.getPackages());
		
		upgradeManager = new UpgradeManager();
		upgradeManager.registerUpgrade(new TemperatureUpgrade());
		upgradeManager.registerUpgrade(new GuildCapacityUpgrade());
		
		gameManager = new GameManager();
		gameManager.registerGame(new ClickerGame());
		gameManager.registerGame(new TriviaGame());
		
		storage = new Storage();
		if(!storage.connect(config.getMongoHost(), config.getMongoDB())) return;
		
		discord = new DiscordBot();
		discord.connect(config.getToken());
		
		VoteListener vl = new VoteListener();
		DonationListener dl = new DonationListener();
		
		discord.getShardManager().addEventListener(new ListenerAdapter() {
			@Override
			public void onReady(ReadyEvent event) {
				CookieBaker.shardCount++;
				if(CookieBaker.shardCount >= CookieBaker.getInstance().getDiscord().getShardManager().getShardsTotal()) {
					if(config.getDiscordBotsToken() != null) DiscordBots.startUpdater(5);
					
					donoHook = new DonoHook();
					try {
						donoHook.updateMissingDonations(dl);
					} catch (UnirestException e1) {
						Logger.error(e1);
					}
					
					server = new WebhookServer(vl, dl);
					try {
						server.start();
					} catch (IOException e) {
						Logger.error(e);
					}
				}
			}
		});
	}
	
	public void shutdown() {
		discord.shutdown();
		storage.shutdown();
	}
	
	public FileLogger getLogger() {
		return logger;
	}
	
	public Manual getManual() {
		return manual;
	}
	
	public Config getConfig() {
		return config;
	}
	
	public Storage getStorage() {
		return storage;
	}
	
	public DiscordBot getDiscord() {
		return discord;
	}
	
	public DonoHook getDonoHook() {
		return donoHook;
	}
	
	public SampleManager getSampleManager() {
		return sampleManager;
	}
	
	public PackageManager getPackageManager() {
		return packageManager;
	}
	
	public UpgradeManager getUpgradeManager() {
		return upgradeManager;
	}
	
	public GameManager getGameManager() {
		return gameManager;
	}
	
	public static CookieBaker getInstance() {
		return instance;
	}
	
}
