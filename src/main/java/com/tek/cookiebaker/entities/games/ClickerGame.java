package com.tek.cookiebaker.entities.games;

import java.awt.Color;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.entities.games.framework.Game;
import com.tek.cookiebaker.entities.games.framework.GameData;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;

public class ClickerGame extends Game<ClickerData> {
	
	private final long SUSPICIOUS_CAP = 100000;
	
	public ClickerGame() {
		super("Cookie Clicker", "clicker", "Click away and gain cookies!");
	}

	@Override
	public ClickerData createUserGameData(UserProfile profile, String channelId) {
		return new ClickerData(profile.getUserId(), channelId, profile.getMultiplier());
	}

	@Override
	public void start(GameData gameData, User user, TextChannel channel) {
		ClickerData data = (ClickerData) gameData;
		EmbedBuilder builder = Reference.createBlankEmbed(new Color(0, 255, 255), user.getJDA());
		
		builder.addField("Cookie Clicker Started!", "Click the " + CookieType.ORIGINAL_COOKIE.getEmoteName()
				+ " reaction to collect cookies!\n\n_It is suggested to click about two times a second as more clicks could be lost due to rate-limiting_"
				+ "\n\nEvery click is worth **" + (int)(2 * data.getMultiplier()) + "** " + CookieType.ORIGINAL_COOKIE.getEmoteName() + "\n\n**After 10 seconds of inactivity, the game will end.**", false);
		
		channel.sendMessage(builder.build()).queue(msg -> {
			Emote emoji = user.getJDA().getEmoteById(CookieType.ORIGINAL_COOKIE.getEmoteID());
			msg.addReaction(emoji).queue();
			
			data.setMessageId(msg.getId());
			setTimeout(user.getJDA(), data);
		});
	}
	
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		Optional<ClickerData> dataOpt = getUserGameData(event.getUser().getId());
		
		if(dataOpt.isPresent()) {
			if(event.getReactionEmote().getId() != null && event.getReactionEmote().getId().equals(CookieType.ORIGINAL_COOKIE.getEmoteID())) {
				if(event.getChannel().getId().equals(dataOpt.get().getChannelId())) {
					if(dataOpt.get().getMessageId().equals(event.getMessageId())) {
						click(dataOpt.get(), event.getUser());
					}
				}
			}
		}
	}
	
	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
		Optional<ClickerData> dataOpt = getUserGameData(event.getUser().getId());
		
		if(dataOpt.isPresent()) {
			if(event.getReactionEmote().getId() != null && event.getReactionEmote().getId().equals(CookieType.ORIGINAL_COOKIE.getEmoteID())) {
				if(event.getChannel().getId().equals(dataOpt.get().getChannelId())) {
					if(dataOpt.get().getMessageId().equals(event.getMessageId())) {
						click(dataOpt.get(), event.getUser());
					}
				}
			}
		}
	}
	
	public void setTimeout(JDA jda, ClickerData data) {
		data.setProcessedClick(false);
		
		EmbedBuilder builder = Reference.createBlankEmbed(new Color(0, 255, 50), jda);
				
		builder.addField("Cookie Clicker Ended!", "You gained **" + data.getCookies() + "** cookies!", false);
			
		TextChannel channel = jda.getTextChannelById(data.getChannelId());
		channel.getMessageById(data.getMessageId()).queue(msg -> {
			if(data.getEndFuture() != null) {
				data.getEndFuture().cancel(true);
				data.setEndFuture(null);
			}
			
			data.setEndFuture(msg.editMessage(builder.build()).queueAfter(10, TimeUnit.SECONDS, msge -> {
				try {
					msge.clearReactions().queue();
				} catch(InsufficientPermissionException e) { }
				
				Storage storage = CookieBaker.getInstance().getStorage();
				UserProfile profile = storage.getUserProfile(data.getUserId());
				if(profile != null) {
					if(data.getCookies() >= SUSPICIOUS_CAP) {
						CookieBaker.getInstance().getLogger().logClickerGame(profile, data.getCookies(), data.getChannelId(), msge.getGuild().getId());
					}
					
					profile.setCookies(CookieType.ORIGINAL_COOKIE, profile.getCookies(CookieType.ORIGINAL_COOKIE) + data.getCookies());
					storage.updateUserProfile(profile, "cookies");
				}
					
				destroyUserGameData(data.getUserId());
			}, t -> {
				destroyUserGameData(data.getUserId());
			}));
			
			data.setProcessedClick(true);
		});
	}
	
	public void click(ClickerData data, User user) {
		if(data.isProcessedClick()) {
			data.incrementCookies((int) (2 * data.getMultiplier()));
			setTimeout(user.getJDA(), data);
		}
	}

}
