package com.tek.cookiebaker.entities.enums;

import java.util.Arrays;
import java.util.Optional;

public enum LevelRank {
	
	NEW_BAKER(0, 5, "New Baker"),
	STUDENT_BAKER(1, 25, "Student Baker"),
	BAKERY_OWNER(2, 50, "Bakery Owner"),
	ADVANCED_BAKER(3, 75, "Advanced Baker"),
	ROYAL_BAKER(4, 100, "Royal Baker"),
	GRANDMA(5, 150, "Grandma"),
	ULTIMATE_GRANDMA(6, 200, "Ultimate Grandma"),
	BAKER_EXTRAORDINAIRE(7, Integer.MAX_VALUE, "Baker Extraordinaire");
	
	private int id, range;
	private String displayName;
	
	private LevelRank(int id, int range, String displayName) {
		this.id = id;
		this.range = range;
		this.displayName = displayName;
	}
	
	public int getId() {
		return id;
	}
	
	public int getRange() {
		return range;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public static LevelRank getLevelRank(int level) {
		int previousLevel = 1;
		
		for(LevelRank rank : LevelRank.values()) {
			if(level >= previousLevel && level < rank.getRange()) {
				return rank;
			}
			
			previousLevel = rank.getRange();
		}
		
		return LevelRank.NEW_BAKER;
	}
	
	public static Optional<LevelRank> getLevelRankById(int id) {
		return Arrays.stream(LevelRank.values()).filter(rank -> rank.getId() == id).findFirst();
	}
	
}
