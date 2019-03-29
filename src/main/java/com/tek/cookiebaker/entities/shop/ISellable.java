package com.tek.cookiebaker.entities.shop;

import com.tek.cookiebaker.storage.UserProfile;

public interface ISellable {
	
	public String getDisplayName(UserProfile profile);
	public String getPrice(UserProfile profile);
	public boolean has(UserProfile profile);
	public boolean canBuy(UserProfile profile);
	public void buy(UserProfile profile);
	
}
