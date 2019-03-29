package com.tek.cookiebaker.api.donations;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.tek.cookiebaker.log.Logger;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;

public class DonoHook {
	
	private static final String DONOHOOK_URL = "http://www.donohook.com/panelj.php?token=%s";
	
	public void updateMissingDonations(Consumer<Donation> donationCallback) throws UnirestException {
		List<Donation> recentDonations = getDonations(CookieBaker.getInstance().getConfig().getDonoHookToken());
		
		for(Donation recentDonation : recentDonations) {
			Optional<Donation> stored = CookieBaker.getInstance().getStorage().getDonation(recentDonation.getTxnId());
			
			if(!stored.isPresent() && recentDonation.isCompleted()) {
				CookieBaker.getInstance().getStorage().insertDonation(recentDonation);
				donationCallback.accept(recentDonation);
			}
		}
	}
	
	public List<Donation> getDonations(String token) throws UnirestException {
		String url = String.format(DONOHOOK_URL, token);
		
		HttpResponse<String> response = Unirest.get(url).asString();
		
		if(response.getBody().equalsIgnoreCase("Connection failed: Connection refused")) return Arrays.asList();
		
		Type donationListType = new TypeToken<List<Donation>>(){}.getType();
		try{
			List<Donation> donations = Reference.GSON.fromJson(response.getBody(), donationListType);
			return donations;
		} catch(IllegalStateException | JsonSyntaxException e) {
			Logger.error(e);
			Logger.debug("DonoHook Response was \"" + response.getBody() + "");
			return Arrays.asList();
		}
	}
	
}
