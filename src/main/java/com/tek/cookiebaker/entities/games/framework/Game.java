package com.tek.cookiebaker.entities.games.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class Game<T extends GameData> extends ListenerAdapter {
	
	private List<T> gameData;
	private String displayName;
	private String alias;
	private String description;
	
	public Game(String displayName, String alias, String description) {
		this.gameData = new ArrayList<T>();
		this.displayName = displayName;
		this.alias = alias;
		this.description = description;
	}
	
	public List<T> getGameData() {
		return gameData;
	}
	
	public abstract T createUserGameData(UserProfile profile, String channelId);
	
	public abstract void start(GameData gameData, User user, TextChannel channel);
	
	@SuppressWarnings("unchecked")
	public void registerUserGameData(GameData gameData) {
		this.gameData.add((T)gameData);
	}
	
	public void destroyUserGameData(String userId) {
		getUserGameData(userId).ifPresent(data -> gameData.remove(data));
	}
	
	public Optional<T> getUserGameData(String userId) {
		return gameData.stream().filter(data -> data.getUserId().equals(userId)).findFirst();
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public String getDescription() {
		return description;
	}
	
}
