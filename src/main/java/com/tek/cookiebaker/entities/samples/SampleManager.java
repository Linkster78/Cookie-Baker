package com.tek.cookiebaker.entities.samples;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;
import com.tek.cookiebaker.main.Reference;

public class SampleManager {
	
	private List<Sample> samples;
	
	public void loadSamples(String jsonArray) {
		Type sampleListType = new TypeToken<List<Sample>>(){}.getType();
		samples = Reference.GSON.fromJson(jsonArray, sampleListType);
	}
	
	public List<Sample> getSamples() {
		return samples;
	}
	
	public Optional<Sample> getSampleByAlias(String alias) {
		return samples.stream().filter(sample -> sample.getAlias().equalsIgnoreCase(alias)).findFirst();
	}
	
}
