package com.tek.cookiebaker.commands;

import java.awt.Color;
import java.util.Optional;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.tek.cookiebaker.entities.games.framework.Game;
import com.tek.cookiebaker.entities.games.framework.GameData;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.ServerProfile;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

public class GameCommand extends Command {
	
	public GameCommand() {
		this.name = "game";
		this.aliases = new String[] {"games", "minigame", "minigames"};
		this.arguments = "<game/reset>";
		this.help = "Plays a minigame!";
	}

	@Override
	protected void execute(CommandEvent event) {
		User user = event.getAuthor();
		Storage storage = CookieBaker.getInstance().getStorage();
		UserProfile profile = storage.getUserProfile(user);
		ServerProfile serverProfile = storage.getServerProfile(event.getGuild());
		
		String[] arguments = event.getArgs().isEmpty() ? new String[0] : event.getArgs().split(" ");
		
		if(arguments.length == 0) {
			EmbedBuilder builder = Reference.createBlankEmbed(Color.orange, event.getJDA());
			builder.addField("Available Minigames", CookieBaker.getInstance().getGameManager().getGames().stream()
					.map(game -> ("**" + game.getDisplayName() + "** | `" + serverProfile.getPrefix() + "game " + game.getAlias() + "`\n^: " + game.getDescription())).collect(Collectors.joining("\n")), false);
			event.reply(builder.build());
		} else if(arguments.length == 1) {
			if(!CookieBaker.getInstance().getGameManager().isPlaying(user)) {
				Optional<Game<? extends GameData>> gameOpt = CookieBaker.getInstance().getGameManager().getGameByAlias(arguments[0]);
				
				if(gameOpt.isPresent()) {
					Game<? extends GameData> game = gameOpt.get();
					GameData gameData = game.createUserGameData(profile, event.getChannel().getId());
					game.registerUserGameData(gameData);
					game.start(gameData, user, event.getTextChannel());
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "**This game doesn't exist**\nValid Games: _" +
							CookieBaker.getInstance().getGameManager().getGames().stream().map(Game::getAlias).collect(Collectors.joining(", ")) + "_", false);
					event.reply(builder.build());
				}
			} else {
				if(arguments[0].equalsIgnoreCase("reset")) {
					CookieBaker.getInstance().getGameManager().eraseGameUserData(user.getId());
					
					EmbedBuilder builder = Reference.createBlankEmbed(Color.green, event.getJDA());
					builder.addField("Session Reset", "Reset your current minigame session", false);
					event.reply(builder.build());
				} else {
					EmbedBuilder builder = Reference.createBlankEmbed(Color.red, event.getJDA());
					builder.addField("Oh no!", "You're already playing a game", false);
					event.reply(builder.build());
				}
			}
		} else {
			event.reply(Reference.getInvalidSyntax(this, serverProfile.getPrefix()));
		}
	}
	
}
