package com.tek.cookiebaker.entities.games;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.tek.cookiebaker.api.trivia.TriviaQuestion;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.entities.games.framework.Game;
import com.tek.cookiebaker.entities.games.framework.GameData;
import com.tek.cookiebaker.jda.MessageWaiter;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.Storage;
import com.tek.cookiebaker.storage.UserProfile;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;

public class TriviaGame extends Game<TriviaData> {
	
	private final int DELAY = 3;
	private final char WHITE_CHECK_MARK = (char)9989;
	
	public TriviaGame() {
		super("Trivia Quiz", "trivia", "Test your general knowledge with this fun trivia!");
	}

	@Override
	public TriviaData createUserGameData(UserProfile profile, String channelId) {
		return new TriviaData(profile.getUserId(), channelId, profile.getMultiplier());
	}

	@Override
	public void start(GameData gameData, User user, TextChannel channel) {
		TriviaData data = (TriviaData) gameData;
		
		EmbedBuilder builder = Reference.createBlankEmbed(new Color(0, 255, 255), user.getJDA());
		builder.addField("Trivia Quiz", "Hey! Sounds like you want to test your general knowledge!\n\n"
				+ "You will be asked **3** questions. Your reward will depend of your correct answers.\n"
				+ "_1/3 -> **" + (int)(25 * data.getMultiplier()) + "**_ " + CookieType.ORIGINAL_COOKIE.getEmoteName() + "\n"
				+ "_2/3 -> **" + (int)(75 * data.getMultiplier()) + "**_ " + CookieType.ORIGINAL_COOKIE.getEmoteName() + "\n"
				+ "_3/3 -> **" + (int)(150 * data.getMultiplier()) + "**_ " + CookieType.ORIGINAL_COOKIE.getEmoteName() + " _and a single_ " + CookieType.GOLDEN_COOKIE.getEmoteName() + "\n\n"
				+ "_You will only have 45 seconds per question._\n\n"
				+ "**When you're ready, react with** " + WHITE_CHECK_MARK, false);
		
		channel.sendMessage(builder.build()).queue(msg -> {
			msg.addReaction(WHITE_CHECK_MARK + "").queue();
			data.setMessageId(msg.getId());
			
			EmbedBuilder timeout = Reference.createBlankEmbed(Color.red, user.getJDA());
			timeout.addField("Oh no!", "It seems you have gone inactive.", false);
			data.setTimeoutFuture(msg.editMessage(timeout.build()).queueAfter(60, TimeUnit.SECONDS, msge -> {
				try{
					msge.clearReactions().queue();
				} catch(InsufficientPermissionException e) { }
				
				destroyUserGameData(data.getUserId());
			}));
		});
	}
	
	public void showQuestion(boolean first, TriviaData data, Message message, int question) {
		if(question >= data.getQuestionCount()) {
			Storage storage = CookieBaker.getInstance().getStorage();
			UserProfile profile = storage.getUserProfile(data.getUserId());
			
			EmbedBuilder builder = Reference.createBlankEmbed(Color.green, message.getJDA());
			
			StringBuilder endBuilder = new StringBuilder();
			endBuilder.append("You scored **" + data.getCorrect() + "/" + data.getQuestionCount() + "**! ");
			
			switch(data.getCorrect()) {
				case 0:
					endBuilder.append("Better luck next time...");
					break;
				case 1:
					endBuilder.append("Not bad /o/\n\n_You won:_ **" + (int)(25 * data.getMultiplier()) + "** " + CookieType.ORIGINAL_COOKIE.getEmoteName());
					profile.setCookies(CookieType.ORIGINAL_COOKIE, profile.getCookies(CookieType.ORIGINAL_COOKIE) + (int)(25 * data.getMultiplier()));
					storage.updateUserProfile(profile, "cookies");
					break;
				case 2:
					endBuilder.append("Good Job!\n\n_You won:_ **" + (int)(75 * data.getMultiplier()) + "** " + CookieType.ORIGINAL_COOKIE.getEmoteName());
					profile.setCookies(CookieType.ORIGINAL_COOKIE, profile.getCookies(CookieType.ORIGINAL_COOKIE) + (int)(75 * data.getMultiplier()));
					storage.updateUserProfile(profile, "cookies");
					break;
				case 3:
					endBuilder.append("Congratulations!\n\n_You won:_ **" + (int)(150 * data.getMultiplier()) + "** " + CookieType.ORIGINAL_COOKIE.getEmoteName() + " and **1** " + CookieType.GOLDEN_COOKIE.getEmoteName());
					profile.setCookies(CookieType.ORIGINAL_COOKIE, profile.getCookies(CookieType.ORIGINAL_COOKIE) + (int)(150 * data.getMultiplier()));
					profile.setCookies(CookieType.GOLDEN_COOKIE, profile.getCookies(CookieType.GOLDEN_COOKIE) + 1);
					storage.updateUserProfile(profile, "cookies");
					break;
				default:
					endBuilder.append("_What ?_");
					break;
			}
			
			builder.addField("Quiz Ended", endBuilder.toString(), false);
			
			message.editMessage(builder.build()).queueAfter(DELAY, TimeUnit.SECONDS);
			
			destroyUserGameData(data.getUserId());
		} else {
			int qVisible = question + 1;
			TriviaQuestion trivia = data.getQuestions()[question];
			
			List<String> answers = new ArrayList<String>(trivia.getIncorrectAnswers()); answers.add(trivia.getCorrectAnswer());
			Collections.shuffle(answers);
			data.setCorrectAns(answers.indexOf(trivia.getCorrectAnswer()));
			
			StringBuilder questionBuilder = new StringBuilder();
			questionBuilder.append("**" + trivia.getQuestion() + "**\n\n");
			for(int i = 0; i < answers.size(); i++) {
				questionBuilder.append("**" + (char)((int)'A' + i) + ")** " + answers.get(i) + "\n");
			}
			questionBuilder.append("\nCategory: _" + trivia.getCategory() + "_");
			
			EmbedBuilder builder = Reference.createBlankEmbed(new Color(0, 0, 255), message.getJDA());
			builder.addField(titleNum(qVisible) + " Question", questionBuilder.toString(), false);
			builder.setFooter("To answer, simply send the corresponding answer letter.", null);
			
			Consumer<Message> editConsumer = msg -> {
				MessageWaiter waiter = CookieBaker.getInstance().getDiscord().getMessageWaiter();
				
				waiter.waitForMessage(45, data.getChannelId(), data.getUserId(), data.getMessageId(), msgt -> {
					return msgt.getContentRaw().length() == 1;
				}, response -> {
					char c = Character.toUpperCase(response.charAt(0));
					int answer = (int)c - (int)'A';
						
					if(answer == data.getCorrectAns()) {
						EmbedBuilder incorrectBuilder = Reference.createBlankEmbed(new Color(102, 255, 102), message.getJDA());
						incorrectBuilder.addField("Good Job!", "_The correct answer was_\n **" + (char)((int)'A' + data.getCorrectAns()) + ") " + trivia.getCorrectAnswer() + "**", false);
						message.editMessage(incorrectBuilder.build()).queue();
						data.correctAnswer();
							
						showQuestion(false, data, message, question + 1);
					} else {
						EmbedBuilder incorrectBuilder = Reference.createBlankEmbed(new Color(220, 20, 60), message.getJDA());
						incorrectBuilder.addField("Wrong Answer!", "_The correct answer was_\n **" + (char)((int)'A' + data.getCorrectAns()) + ") " + trivia.getCorrectAnswer() + "**", false);
						message.editMessage(incorrectBuilder.build()).queue();
							
						showQuestion(false, data, message, question + 1);
					}
				}, () -> {
					EmbedBuilder incorrectBuilder = Reference.createBlankEmbed(new Color(220, 20, 60), message.getJDA());
					incorrectBuilder.addField("Oops!", "You ran out of time!\n_The correct answer was_\n **" + (char)((int)'A' + data.getCorrectAns()) + ") " + trivia.getCorrectAnswer() + "**", false);
					message.editMessage(incorrectBuilder.build()).queue();
					
					showQuestion(false, data, message, question + 1);
				});
			};
			
			if(first) {
				message.editMessage(builder.build()).queue(editConsumer);
			} else {
				message.editMessage(builder.build()).queueAfter(DELAY, TimeUnit.SECONDS, editConsumer);
			}
		}
	}
	
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		Optional<TriviaData> dataOpt = getUserGameData(event.getUser().getId());
		
		if(dataOpt.isPresent()) {
			if(!dataOpt.get().isUnderstood()) {
				if(event.getChannel().getId().equals(dataOpt.get().getChannelId())) {
					if(event.getMessageId().equals(dataOpt.get().getMessageId())) {
						ReactionEmote emote = event.getReactionEmote();
						
						if(emote.getName().equals(Character.toString(WHITE_CHECK_MARK))) {
							try {
								dataOpt.get().setUnderstood(true);
								if(dataOpt.get().getTimeoutFuture() != null) dataOpt.get().getTimeoutFuture().cancel(true);
								
								dataOpt.get().loadQuestions();
								
								event.getChannel().getMessageById(event.getMessageId()).queue(msg -> {
									try {
										msg.clearReactions().queue();
									} catch(InsufficientPermissionException e) { }
									
									showQuestion(true, dataOpt.get(), msg, 0);
								});
							} catch (UnirestException e) {
								dataOpt.get().setUnderstood(true);
								if(dataOpt.get().getTimeoutFuture() != null) dataOpt.get().getTimeoutFuture().cancel(true);
								
								event.getChannel().getMessageById(event.getMessageId()).queue(msg -> {
									EmbedBuilder builder = Reference.createBlankEmbed(new Color(255, 0, 0), event.getJDA());
									builder.addField("Oh no.", "**Couldn't fetch trivia**", false);
									msg.editMessage(builder.build()).queue();
									
									destroyUserGameData(event.getUser().getId());
								});
							}
						}
					}
				}
			}
		}
	}
	
	public String titleNum(int num) {
		if(num == 1) return "1st";
		if(num == 2) return "2nd";
		if(num == 3) return "3rd";
		return num + "";
	}

}