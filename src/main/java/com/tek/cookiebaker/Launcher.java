package com.tek.cookiebaker;

import java.io.IOException;

import com.tek.cookiebaker.log.Logger;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.Config;

public class Launcher {
	
	public static void main(String[] args) throws IOException {
		CookieBaker cookieBaker = null;
		
		try {
			Config config = Config.load(Reference.CONFIG_PATH);
			
			cookieBaker = new CookieBaker(config);
			cookieBaker.start();
		} catch (Exception e) {
			Logger.error(e);
			if(cookieBaker != null) cookieBaker.shutdown();
		}
	}
	
}
