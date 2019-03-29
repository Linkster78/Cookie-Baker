package com.tek.cookiebaker.api.trivia;

import java.lang.reflect.Type;
import java.util.List;

import org.json.JSONObject;

import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.tek.cookiebaker.main.Reference;

public class Trivia {
	
	private static final String URL = "https://opentdb.com/api.php?amount=%d&difficulty=easy&type=multiple&encode=base64";
	
	public static List<TriviaQuestion> getTriviaQuestions(int count) throws UnirestException {
		String url = String.format(URL, count);
		
		HttpResponse<String> response = Unirest.get(url).asString();
		String responseText = response.getBody();
		
		JSONObject jsonResponse = new JSONObject(responseText);
		String resultsString = jsonResponse.getJSONArray("results").toString();
		
		Type questionListType = new TypeToken<List<TriviaQuestion>>(){}.getType();
		List<TriviaQuestion> questions = Reference.GSON.fromJson(resultsString, questionListType);
		questions.forEach(TriviaQuestion::decode);
		
		return questions;
	}
	
}
