package com.tek.cookiebaker.entities.enums;

import java.util.Arrays;
import java.util.Optional;

public enum Oven {
	
	EASYBAKE_OVEN("easybake", "Easy-Bake Oven", 0.5, false, 1),
	STARTER_OVEN("starter", "Starter Oven", 1, false, 0),
	KITCHEN_OVEN("kitchen", "Kitchen Oven", 1.5, true, 22500),
	PRACTICE_OVEN("practice", "Practice Oven", 2.5, true, 50000),
	STUDENT_OVEN("student", "Student Oven", 5, true, 160000),
	ADVANCED_OVEN("advanced", "Advanced Oven", 10, true, 675000),
	BAKERS_OVEN("baker", "Bakers Oven", 25, true, 2700000),
	ULTIMATE_OVEN("ultimate", "Ultimate Oven", 50, true, 13500000),
	UNLIMITED_OVEN("unlimited", "Unlimited Oven", 75, true, 54000000),
	MONSTER_OVEN("monster", "Monster Oven", 100, true, 100000000);
	
	private String alias;
	private String displayName;
	private double multiplier;
	private boolean buyable;
	private double price;
	
	private Oven(String alias, String displayName, double multiplier, boolean buyable, double price) {
		this.alias = alias;
		this.displayName = displayName;
		this.multiplier = multiplier;
		this.buyable = buyable;
		this.price = price;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public double getMultiplier() {
		return multiplier;
	}
	
	public boolean isBuyable() {
		return buyable;
	}
	
	public double getPrice() {
		return price;
	}
	
	public static Optional<Oven> getOvenByAlias(String alias) {
		return Arrays.stream(Oven.values()).filter(oven -> oven.getAlias().equalsIgnoreCase(alias)).findFirst();
	}
	
}