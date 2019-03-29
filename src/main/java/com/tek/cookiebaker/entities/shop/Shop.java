package com.tek.cookiebaker.entities.shop;

import java.util.ArrayList;
import java.util.List;

public class Shop {
	
	private String displayName;
	private List<String> aliases;
	private List<ISellable> soldItems;
	
	public Shop(String displayName, String... aliases) {
		this.displayName = displayName;
		this.aliases = new ArrayList<String>();
		for(String alias : aliases) this.aliases.add(alias);
		this.soldItems = new ArrayList<ISellable>();
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public boolean matchAlias(String alias) {
		for(String shopAlias : aliases) {
			if(shopAlias.equalsIgnoreCase(alias)) return true;
		}
		
		return false;
	}
	
	public List<String> getAliases() {
		return aliases;
	}
	
	public String getMainAlias() {
		return aliases.get(0);
	}
	
	public void addItem(ISellable sellable) {
		soldItems.add(sellable);
	}
	
	public List<ISellable> getSoldItems() {
		return soldItems;
	}
	
}
