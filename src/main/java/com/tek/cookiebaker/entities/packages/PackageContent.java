package com.tek.cookiebaker.entities.packages;

import java.util.List;
import java.util.Map;

import com.tek.cookiebaker.entities.enums.Oven;

public class PackageContent {
	
	private int chancePart;
	private RewardType rewardType;
	private double money;
	private Map<String, Integer> cookies;
	private Oven oven;
	private List<String> upgrades;
	
	public int getChancePart() {
		return chancePart;
	}
	
	public RewardType getRewardType() {
		return rewardType;
	}
	
	public double getMoney() {
		return money;
	}
	
	public Map<String, Integer> getCookies() {
		return cookies;
	}
	
	public Oven getOven() {
		return oven;
	}
	
	public List<String> getUpgrades() {
		return upgrades;
	}
	
	public enum RewardType {
		MONEY,
		COOKIES,
		OVEN,
		UPGRADES;
	}
	
}