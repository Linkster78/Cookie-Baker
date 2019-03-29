package com.tek.cookiebaker.storage;

import java.awt.Color;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import com.tek.cookiebaker.entities.enums.UpgradeType;
import com.tek.cookiebaker.entities.upgrades.GuildCapacityUpgrade;
import com.tek.cookiebaker.entities.upgrades.Upgrade;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;

public class Guild {
	
	private ObjectId id;
	private String name;
	private String description;
	private List<String> members;
	private List<String> officers;
	private List<String> invites;
	private Map<String, Integer> upgrades;
	private String owner;
	private boolean inviteOnly;
	private long creationTime;
	
	public Guild() { 
		this.inviteOnly = true;
		this.description = "";
		this.creationTime = System.currentTimeMillis();
		this.officers = Arrays.asList();
		this.invites = Arrays.asList();
		this.upgrades = new HashMap<String, Integer>();
		CookieBaker.getInstance().getUpgradeManager().getRegisteredUpgradesByType(UpgradeType.GUILD).forEach(u -> upgrades.put(u.getAlias(), 0));
	}
	
	public Guild(String name, String owner) { 
		this();
		this.name = name;
		this.owner = owner;
		this.members = Arrays.asList(owner);
	}
	
	//Important
	@BsonIgnore
	public void verifyIntegrity() {
		List<Upgrade> missingUpgrades = CookieBaker.getInstance().getUpgradeManager().getRegisteredUpgradesByType(UpgradeType.GUILD).stream().filter(u -> !upgrades.containsKey(u.getAlias())).collect(Collectors.toList());
		missingUpgrades.forEach(u -> upgrades.put(u.getAlias(), 0));
		if(missingUpgrades.size() > 0) CookieBaker.getInstance().getStorage().updateGuild(this, "upgrades");
	}
	
	public EmbedBuilder createGuildDescription(JDA jda) {
		Storage storage = CookieBaker.getInstance().getStorage();
		EmbedBuilder builder = Reference.createBlankEmbed(Color.orange, jda);
		builder.setTitle(name + " Guild");
		UserProfile owner = storage.getUserProfile(this.owner);
		
		builder.setDescription(description.isEmpty() ? "No description set" : description);
		
		Map<String, UserProfile> profiles = new HashMap<String, UserProfile>();
		members.stream().map(id -> storage.getUserProfile(id)).filter(up -> up != null).forEach(id -> profiles.put(id.getUserId(), id));
		
		final int pageSize = 10;
		int memberPages = (int)Math.ceil((double)getMemberLimit() / pageSize);
		for(int p = 0; p < memberPages; p++) {
			StringBuilder page = new StringBuilder();
			for(int i = 0; i < pageSize; i++) {
				int n = p * pageSize + i;
				if(n >= getMemberLimit()) break;
				page.append(n >= members.size() ? " - `Empty`" : " - " + (profiles.containsKey(members.get(n)) ? profiles.get(members.get(n)).getName() : "**Error**"));
				page.append("\n");
			}
			if(page.length() > 0) page.setLength(page.length() - 1);
			builder.addField("Members " + (p + 1) + "/" + memberPages, page.toString(), true);
		}
		
		String officersStr = officers.stream().map(id -> " - " + profiles.get(id).getName()).collect(Collectors.joining("\n"));
		builder.addField("Officers", officersStr.isEmpty() ? "None" : officersStr, true);
		
		String invitesStr = invites.stream().map(id -> storage.getUserProfile(id)).filter(profile -> profile != null).map(profile -> " - " + profile.getName()).limit(3).collect(Collectors.joining("\n"));
		builder.addField("Invited Users", invitesStr.isEmpty() ? "None" : invitesStr, true);
		
		builder.addField("Owner", owner.getName(), true);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		builder.addField("Creation Time", formatter.format(new Timestamp(creationTime).toLocalDateTime()), true);
		
		builder.addField("Invite Only", inviteOnly ? "Yes" : "No", true);
		
		builder.addField("Guild XP Multiplier", Reference.round(getGuildXPMultiplier()) + "x", true);
		builder.addField("Guild Upgrades", upgrades.keySet().stream()
				.map(alias -> CookieBaker.getInstance().getUpgradeManager().getUpgradeByAlias(alias))
				.filter(Optional::isPresent)
				.map(upgrade -> upgrade.get().getDisplayName() + " | **Level " + getUpgrade(upgrade.get().getClass()) + "/" + upgrade.get().getMaxLevel() + "**")
				.collect(Collectors.joining("\n")),
				true);
		
		return builder;
	}
	
	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<String> getMembers() {
		return members;
	}
	
	public void setMembers(List<String> members) {
		this.members = members;
	}
	
	public List<String> getOfficers() {
		return officers;
	}
	
	public void setOfficers(List<String> officers) {
		this.officers = officers;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public boolean isInviteOnly() {
		return inviteOnly;
	}
	
	public void setInviteOnly(boolean inviteOnly) {
		this.inviteOnly = inviteOnly;
	}
	
	public long getCreationTime() {
		return creationTime;
	}
	
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	
	public List<String> getInvites() {
		return invites;
	}
	
	public void setInvites(List<String> invites) {
		this.invites = invites;
	}
	
	public int getUpgrade(Class<? extends Upgrade> upgradeClass) {
		Optional<? extends Upgrade> upgradeOpt = CookieBaker.getInstance().getUpgradeManager().getUpgradeByClass(upgradeClass);
		if(!upgradeOpt.isPresent()) return 0;
		return upgrades.get(upgradeOpt.get().getAlias());
	}
	
	public Optional<? extends Upgrade> getUpgradeObj(Class<? extends Upgrade> upgradeClass) {
		return CookieBaker.getInstance().getUpgradeManager().getUpgradeByClass(upgradeClass);
	}
	
	public void upgrade(Class<? extends Upgrade> upgradeClass) {
		Optional<? extends Upgrade> upgradeOpt = CookieBaker.getInstance().getUpgradeManager().getUpgradeByClass(upgradeClass);
		if(!upgradeOpt.isPresent()) return;
		upgrades.put(upgradeOpt.get().getAlias(), Math.min(upgradeOpt.get().getMaxLevel(), getUpgrade(upgradeClass) + 1));
	}
	
	public Map<String, Integer> getUpgrades() {
		return upgrades;
	}
	
	public void setUpgrades(Map<String, Integer> upgrades) {
		this.upgrades = upgrades;
	}
	
	@BsonIgnore
	public int getMemberLimit() {
		return (1 + getUpgrade(GuildCapacityUpgrade.class)) * 5;
	}
	
	@BsonIgnore
	public int getMemberCount() {
		return members.size();
	}
	
	@BsonIgnore
	public boolean canInvite(String id) {
		return owner.equals(id) || officers.contains(id);
	}
	
	@BsonIgnore
	public double getGuildXPMultiplier() {
		return 1 + (members.size() - 1) * 0.05;
	}
	
}
