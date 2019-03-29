package com.tek.cookiebaker.entities.games.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.dv8tion.jda.core.entities.User;

public class GameManager {
	
	private List<Game<? extends GameData>> games;
	
	public GameManager() {
		this.games = new ArrayList<Game<? extends GameData>>();
	}
	
	public void registerGame(Game<? extends GameData> game) {
		this.games.add(game);
	}
	
	public Optional<Game<? extends GameData>> getGameByAlias(String alias) {
		return games.stream().filter(game -> game.getAlias().equalsIgnoreCase(alias)).findFirst();
	}
	
	public boolean isPlaying(User user) {
		return games.stream().map(data -> data.getUserGameData(user.getId())).anyMatch(Optional::isPresent);
	}
	
	public void eraseGameUserData(String userId) {
		games.stream().forEach(game -> game.destroyUserGameData(userId));
	}
	
	public List<Game<? extends GameData>> getGames() {
		return games;
	}
	
}
