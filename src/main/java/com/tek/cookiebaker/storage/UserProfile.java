package com.tek.cookiebaker.storage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import com.tek.cookiebaker.entities.botting.MiningSession;
import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.entities.enums.Oven;
import com.tek.cookiebaker.entities.enums.UpgradeType;
import com.tek.cookiebaker.entities.samples.Sample;
import com.tek.cookiebaker.entities.upgrades.TemperatureUpgrade;
import com.tek.cookiebaker.entities.upgrades.Upgrade;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;

public class UserProfile {
	
	private ObjectId id;
	private String userId;
	private String name;
	private boolean disabled;
	
	private MiningSession session;
	
	private double donated;
	
	private String verificationCode;
	private short bakesVerif;
	private short verifAttempts;
	private long verificationBakeAttempts;
	private long verificationStart;
	
	private long lastBaked;
	private double money;
	private Map<String, Long> cookies;
	private Map<String, Long> sampleTimes;
	private Map<String, Map<String, Integer>> upgrades;
	private Map<String, Integer> packages;
	private int level;
	private long xp;
	
	private List<Oven> unlockedOvens;
	private Oven currentOven;
	
	private long bakeCount;
	private long voteCount;
	
	public UserProfile() {
		CookieBaker instance = CookieBaker.getInstance();
		
		this.session = new MiningSession();
		
		this.disabled = false;
		this.donated = 0;
		this.verificationCode = "";
		this.verificationStart = 0;
		this.verificationBakeAttempts = 0;
		this.bakesVerif = 0;
		this.verifAttempts = -1;
		this.lastBaked = 0;
		this.money = 0;
		
		this.cookies = new HashMap<String, Long>();
		Arrays.stream(CookieType.values()).forEach(type -> this.cookies.put(type.getAlias(), (long) 0));
		
		this.sampleTimes = new HashMap<String, Long>();
		instance.getSampleManager().getSamples().stream().forEach(sample -> sampleTimes.put(sample.getAlias(), 0l));
		
		this.upgrades = new HashMap<String, Map<String, Integer>>();
		Arrays.stream(Oven.values()).forEach(oven -> {
			Map<String, Integer> ovenUpgrades = new HashMap<String, Integer>();
			
			instance.getUpgradeManager().getRegisteredUpgradesByType(UpgradeType.OVEN).stream().forEach(upgrade -> {
				ovenUpgrades.put(upgrade.getAlias(), 0);
			});
			
			upgrades.put(oven.name(), ovenUpgrades);
		});
		
		this.packages = new HashMap<String, Integer>();
		instance.getPackageManager().getPackages().stream().forEach(pack -> {
			packages.put(pack.getAlias(), 0);
		});
		
		this.level = 1;
		this.xp = 0;
		this.unlockedOvens = Arrays.asList(Oven.STARTER_OVEN);
		this.currentOven = Oven.STARTER_OVEN;
		this.bakeCount = 0;
		this.voteCount = 0;
	}
	
	public UserProfile(String userId, String name) {
		this();
		this.userId = userId;
		this.name = name;
	}
	
	//Important
	@BsonIgnore
	public void verifyIntegrity() {
		List<CookieType> streamCookies = Arrays.stream(CookieType.values()).filter(type -> !cookies.containsKey(type.getAlias())).collect(Collectors.toList());
		streamCookies.forEach(type -> cookies.put(type.getAlias(), 0l));
		if(streamCookies.size() > 0) CookieBaker.getInstance().getStorage().updateUserProfile(this, "cookies");
		
		List<Sample> streamSamples = CookieBaker.getInstance().getSampleManager().getSamples().stream().filter(sample -> !sampleTimes.containsKey(sample.getAlias())).collect(Collectors.toList());
		streamSamples.forEach(sample -> sampleTimes.put(sample.getAlias(), 0l));
		if(streamSamples.size() > 0) CookieBaker.getInstance().getStorage().updateUserProfile(this, "sampleTimes");
		
		List<Oven> streamOvensMissing = Arrays.stream(Oven.values()).filter(oven -> !upgrades.containsKey(oven.name())).collect(Collectors.toList());
		streamOvensMissing.forEach(oven -> {
			Map<String, Integer> ovenUpgrades = new HashMap<String, Integer>();
			
			CookieBaker.getInstance().getUpgradeManager().getRegisteredUpgradesByType(UpgradeType.OVEN).stream().forEach(upgrade -> {
				ovenUpgrades.put(upgrade.getAlias(), 0);
			});
			
			upgrades.put(oven.name(), ovenUpgrades);
		});
		if(streamOvensMissing.size() > 0) CookieBaker.getInstance().getStorage().updateUserProfile(this, "upgrades");
		
		Arrays.stream(Oven.values()).forEach(oven -> {
			List<Upgrade> missingUpgrades = CookieBaker.getInstance().getUpgradeManager().getRegisteredUpgradesByType(UpgradeType.OVEN).stream().filter(upgrade -> !upgrades.get(oven.name()).containsKey(upgrade.getAlias())).collect(Collectors.toList());
			missingUpgrades.forEach(upgrade -> upgrades.get(oven.name()).put(upgrade.getAlias(), 0));
			if(missingUpgrades.size() > 0) CookieBaker.getInstance().getStorage().updateUserProfile(this, "upgrades");
		});
		
		List<com.tek.cookiebaker.entities.packages.Package> streamPackagesMissing = CookieBaker.getInstance().getPackageManager().getPackages().stream().filter(pack -> !packages.containsKey(pack.getAlias())).collect(Collectors.toList());
		streamPackagesMissing.forEach(pack -> packages.put(pack.getAlias(), 0));
		if(streamPackagesMissing.size() > 0) CookieBaker.getInstance().getStorage().updateUserProfile(this, "packages");
	}
	
	public ObjectId getId() {
		return id;
	}

	public void setId(final ObjectId id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getVerificationCode() {
		return verificationCode;
	}
	
	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public MiningSession getSession() {
		return session;
	}
	
	public void setSession(MiningSession session) {
		this.session = session;
	}
	
	public double getDonated() {
		return donated;
	}
	
	public void setDonated(double donated) {
		this.donated = donated;
	}
	
	public boolean hasDonated() {
		return donated > 0;
	}
	
	public short getBakesVerif() {
		return bakesVerif;
	}
	
	public void setBakesVerif(short bakesVerif) {
		this.bakesVerif = bakesVerif;
	}
	
	public short getVerifAttempts() {
		return verifAttempts;
	}
	
	public void setVerifAttempts(short verifAttempts) {
		this.verifAttempts = verifAttempts;
	}
	
	public long getLastBaked() {
		return lastBaked;
	}
	
	public void setLastBaked(long lastBaked) {
		this.lastBaked = lastBaked;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = Reference.round(money);
	}

	public Map<String, Long> getCookies() {
		return cookies;
	}
	
	public void setCookies(Map<String, Long> cookies) {
		this.cookies = cookies;
	}
	
	public long getCookies(CookieType type) {
		return cookies.get(type.getAlias());
	}
	
	public void setCookies(CookieType type, long count) {
		cookies.put(type.getAlias(), count);
	}
	
	public Map<String, Long> getSampleTimes() {
		return sampleTimes;
	}
	
	public void setSampleTimes(Map<String, Long> sampleTimes) {
		this.sampleTimes = sampleTimes;
	}
	
	public Map<String, Map<String, Integer>> getUpgrades() {
		return upgrades;
	}
	
	public int getUpgrade(Oven oven, Class<? extends Upgrade> upgradeClass) {
		Optional<? extends Upgrade> upgradeOpt = CookieBaker.getInstance().getUpgradeManager().getUpgradeByClass(upgradeClass);
		if(!upgradeOpt.isPresent()) return 0;
		return upgrades.get(oven.name()).get(upgradeOpt.get().getAlias());
	}
	
	public int getUpgrade(Class<? extends Upgrade> upgradeClass) {
		Optional<? extends Upgrade> upgradeOpt = CookieBaker.getInstance().getUpgradeManager().getUpgradeByClass(upgradeClass);
		if(!upgradeOpt.isPresent()) return 0;
		return upgrades.get(currentOven.name()).get(upgradeOpt.get().getAlias());
	}
	
	public Optional<? extends Upgrade> getUpgradeObj(Class<? extends Upgrade> upgradeClass) {
		return CookieBaker.getInstance().getUpgradeManager().getUpgradeByClass(upgradeClass);
	}
	
	public void upgrade(Class<? extends Upgrade> upgradeClass) {
		Optional<? extends Upgrade> upgradeOpt = CookieBaker.getInstance().getUpgradeManager().getUpgradeByClass(upgradeClass);
		if(!upgradeOpt.isPresent()) return;
		upgrades.get(currentOven.name()).put(upgradeOpt.get().getAlias(), Math.min(upgradeOpt.get().getMaxLevel(), getUpgrade(upgradeClass) + 1));
	}
	
	public void setUpgrades(Map<String, Map<String, Integer>> upgrades) {
		this.upgrades = upgrades;
	}
	
	public Map<String, Integer> getPackages() {
		return packages;
	}
	
	public long getSampleTime(Sample sample) {
		return this.sampleTimes.get(sample.getAlias());
	}
	
	public void setPackages(Map<String, Integer> packages) {
		this.packages = packages;
	}
	
	public void setPackages(String alias, int count) {
		this.packages.put(alias, count);
	}
	
	public int getPackages(String alias) {
		return packages.get(alias);
	}
	
	public void setSampleTime(Sample sample, long time) {
		this.sampleTimes.put(sample.getAlias(), time);
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public long getXp() {
		return xp;
	}
	
	public void setXp(long xp) {
		this.xp = xp;
	}
	
	public Oven getCurrentOven() {
		return currentOven;
	}
	
	public void setCurrentOven(Oven currentOven) {
		this.currentOven = currentOven;
	}
	
	public List<Oven> getUnlockedOvens() {
		return unlockedOvens;
	}
	
	public void setUnlockedOvens(List<Oven> unlockedOvens) {
		this.unlockedOvens = unlockedOvens;
	}
	
	public long getBakeCount() {
		return bakeCount;
	}
	
	public void setBakeCount(long bakeCount) {
		this.bakeCount = bakeCount;
	}
	
	public long getVoteCount() {
		return voteCount;
	}
	
	public void setVoteCount(long voteCount) {
		this.voteCount = voteCount;
	}
	
	public long getVerificationStart() {
		return verificationStart;
	}
	
	public void setVerificationStart(long verificationStart) {
		this.verificationStart = verificationStart;
	}
	
	public long getVerificationBakeAttempts() {
		return verificationBakeAttempts;
	}
	
	public void setVerificationBakeAttempts(long verificationBakeAttempts) {
		this.verificationBakeAttempts = verificationBakeAttempts;
	}
	
	//Utility functions
	@BsonIgnore
	public boolean incrementExperience(int xp) {
		this.xp += xp;
		
		boolean leveledUp = false;
		
		while(this.xp >= getLevelMax()) {
			int exceedent = (int) (this.xp - getLevelMax());
			this.level ++;
			this.xp = exceedent;
			leveledUp = true;
		}
		
		return leveledUp;
	}
	
	@BsonIgnore
	public double getMultiplier() {
		return currentOven.getMultiplier();
	}
	
	@BsonIgnore
	public int getCooldown() {
		TemperatureUpgrade upgrade = CookieBaker.getInstance().getUpgradeManager().getUpgradeByClass(TemperatureUpgrade.class).get();
		return upgrade.getCooldown(getUpgrade(TemperatureUpgrade.class));
	}
	
	@BsonIgnore
	public long getLevelMax() {
		return level * (Reference.LEVEL_BASE_COST + (level - 1) * 20);
	}
	
	//Verification
	@BsonIgnore
	public boolean bake(long messageTime) {
		bakeCount++;
		bakesVerif++;
		session.mined(messageTime);
		
		if(bakesVerif >= Reference.VERIFICATION_INTERVAL) {
			bakesVerif = 0;
			verificationStart = System.currentTimeMillis();
			verifAttempts = Reference.VERIFICATION_ATTEMPTS;
			verificationBakeAttempts = 0;
			return true;
		}
		
		return false;
	}
	
	@BsonIgnore
	public boolean failedVerification() {
		verifAttempts -= 1;
		return verifAttempts <= 0;
	}
	
	@BsonIgnore
	public void timeout(long milliseconds) {
		this.lastBaked = System.currentTimeMillis() + milliseconds - getCooldown();
	}
	
	@BsonIgnore
	public void resetVerify() {
		bakesVerif = 0;
		verifAttempts = -1;
	}
	
	@BsonIgnore
	public boolean isVerifying() {
		return verifAttempts != -1;
	}
	
}
