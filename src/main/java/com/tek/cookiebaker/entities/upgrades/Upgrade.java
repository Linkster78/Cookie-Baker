package com.tek.cookiebaker.entities.upgrades;

import com.tek.cookiebaker.entities.enums.UpgradeType;

public class Upgrade {
	
	private UpgradeType type;
	private String displayName;
	private String alias;
	private int maxLevel;
	
	public Upgrade(UpgradeType type, String displayName, String alias, int maxLevel) {
		this.type = type;
		this.displayName = displayName;
		this.alias = alias;
		this.maxLevel = maxLevel;
	}
	
	public UpgradeType getType() {
		return type;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public int getMaxLevel() {
		return maxLevel;
	}
	
}
