package com.tek.cookiebaker.entities.games;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.tek.cookiebaker.api.trivia.Trivia;
import com.tek.cookiebaker.api.trivia.TriviaQuestion;
import com.tek.cookiebaker.entities.games.framework.GameData;

public class TriviaData extends GameData {

	private final short QUESTION_COUNT = 3;
	
	private String messageId;
	private TriviaQuestion[] questions;
	private boolean understood;
	private ScheduledFuture<?> timeoutFuture;
	private short correct;
	private int correctAns;
	
	public TriviaData(String userId, String channelId, double multiplier) {
		super(userId, channelId, multiplier);
		this.understood = false;
		this.correct = 0;
		this.correctAns = 0;
	}
	
	@Override
	public void delete() {
		if(timeoutFuture != null) timeoutFuture.cancel(true);
	}
	
	public void loadQuestions() throws UnirestException {
		List<TriviaQuestion> questions = Trivia.getTriviaQuestions(QUESTION_COUNT);
		this.questions = new TriviaQuestion[QUESTION_COUNT];
		for(int i = 0; i < QUESTION_COUNT; i++) this.questions[i] = questions.get(i);
	}
	
	public short getQuestionCount() {
		return QUESTION_COUNT;
	}
	
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public void setQuestions(TriviaQuestion[] questions) {
		this.questions = questions;
	}
	
	public TriviaQuestion[] getQuestions() {
		return questions;
	}
	
	public void setTimeoutFuture(ScheduledFuture<?> timeoutFuture) {
		this.timeoutFuture = timeoutFuture;
	}
	
	public ScheduledFuture<?> getTimeoutFuture() {
		return timeoutFuture;
	}
	
	public boolean isUnderstood() {
		return understood;
	}
	
	public void setUnderstood(boolean understood) {
		this.understood = understood;
	}
	
	public short getCorrect() {
		return correct;
	}
	
	public void setCorrect(short correct) {
		this.correct = correct;
	}
	
	public void correctAnswer() {
		this.correct++;
	}
	
	public int getCorrectAns() {
		return correctAns;
	}
	
	public void setCorrectAns(int correctAns) {
		this.correctAns = correctAns;
	}

}
