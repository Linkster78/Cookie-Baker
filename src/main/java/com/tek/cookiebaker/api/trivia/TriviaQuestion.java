package com.tek.cookiebaker.api.trivia;

import java.util.List;
import java.util.stream.Collectors;

import com.tek.cookiebaker.main.Reference;

public class TriviaQuestion {
	
	private String category;
	private String type;
	private String question;
	private String correct_answer;
	private List<String> incorrect_answers;
	
	public void decode() {
		this.category = Reference.fromBase64(this.category);
		this.type = Reference.fromBase64(this.type);
		this.question = Reference.fromBase64(this.question);
		this.correct_answer = Reference.fromBase64(this.correct_answer);
		this.incorrect_answers = this.incorrect_answers.stream().map(Reference::fromBase64).collect(Collectors.toList());
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getType() {
		return type;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public String getCorrectAnswer() {
		return correct_answer;
	}
	
	public List<String> getIncorrectAnswers() {
		return incorrect_answers;
	}
	
}
