package com.tek.cookiebaker.entities.upgrades;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tek.cookiebaker.entities.enums.UpgradeType;

public class UpgradeManager {
	
	private List<Upgrade> registeredUpgrades;
	
	public UpgradeManager() {
		this.registeredUpgrades = new ArrayList<Upgrade>();
	}
	
	public void registerUpgrade(Upgrade upgrade) {
		registeredUpgrades.add(upgrade);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Upgrade> Optional<T> getUpgradeByClass(Class<T> upgradeClass) {
		return registeredUpgrades.stream().filter(upgrade -> upgrade.getClass() == upgradeClass).map(upgrade -> (T) upgrade).findFirst();
	}
	
	public Optional<Upgrade> getUpgradeByAlias(String alias) {
		return registeredUpgrades.stream().filter(upgrade -> upgrade.getAlias().equalsIgnoreCase(alias)).findFirst();
	}
	
	public List<Upgrade> getRegisteredUpgradesByType(UpgradeType type) {
		return registeredUpgrades.stream().filter(u -> u.getType().equals(type)).collect(Collectors.toList());
	}
	
}
