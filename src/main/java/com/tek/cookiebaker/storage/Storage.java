package com.tek.cookiebaker.storage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.tek.cookiebaker.api.donations.Donation;
import com.tek.cookiebaker.main.CookieBaker;
import com.tek.cookiebaker.main.Reference;

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public class Storage {
	
	private MongoClient mongoClient;
	private MongoDatabase database;
	private MongoCollection<UserProfile> userProfiles;
	private MongoCollection<ServerProfile> serverProfiles;
	private MongoCollection<Donation> donations;
	private MongoCollection<com.tek.cookiebaker.storage.Guild> guilds;
	
	public boolean connect(String uri, String db) {
		ServerAddress address = new ServerAddress(uri);
		mongoClient = MongoClients.create(MongoClientSettings.builder()
				.applyToClusterSettings(builder -> {
					builder.serverSelectionTimeout(5000, TimeUnit.MILLISECONDS);
					builder.hosts(Arrays.asList(address));
				}).build());
		
		try { mongoClient.listDatabases().first(); } catch(Exception e) { return false; }
		
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				com.mongodb.MongoClient.getDefaultCodecRegistry(), 
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
				);
		
		database = mongoClient.getDatabase(db);
		database = database.withCodecRegistry(codecRegistry);
		
		userProfiles = database.getCollection(Reference.STORAGE_USER_PROFILES, UserProfile.class);
		serverProfiles = database.getCollection(Reference.STORAGE_SERVER_PROFILES, ServerProfile.class);
		donations = database.getCollection(Reference.STORAGE_DONATIONS, Donation.class);
		guilds = database.getCollection(Reference.STORAGE_GUILDS, com.tek.cookiebaker.storage.Guild.class);
		
		return true;
	}
	
	public void shutdown() {
		FindIterable<UserProfile> profiles = getUserProfiles();
		MongoCursor<UserProfile> profileIter = profiles.iterator();
		while(profileIter.hasNext()) {
			UserProfile profile = profileIter.next();
			if(profile.isDisabled()) {
				profile.setDisabled(false);
				updateUserProfile(profile, "disabled");
			}
		}
		
		mongoClient.close();
	}
	
	public FindIterable<UserProfile> getUserProfiles() {
		return userProfiles.find();
	}
	
	public UserProfile getUserProfile(String id) {
		ShardManager shardManager = CookieBaker.getInstance().getDiscord().getShardManager();
		User user = shardManager.getUserById(id);
		if(user == null) return null;
		return getUserProfile(user);
	}
	
	public UserProfile getUserProfile(User user) {
		FindIterable<UserProfile> profiles = userProfiles.find(Filters.eq("userId", user.getId()));
		UserProfile profile = profiles.first();
		profile = profile == null ? createUserProfile(user) : profile;
		if(!profile.getName().equals(Reference.getUsername(user))) {
			profile.setName(Reference.getUsername(user));
			updateUserProfile(profile, "name");
		}
		profile.verifyIntegrity();
		return profile;
	}
	
	public UserProfile createUserProfile(User user) {
		UserProfile profile = new UserProfile(user.getId(), Reference.getUsername(user));
		userProfiles.insertOne(profile);
		return profile;
	}
	
	public void updateUserProfile(UserProfile profile, String... updated) {
		userProfiles.updateOne(Filters.eq("userId", profile.getUserId()), updateHelper(profile, updated));
	}
	
	public void updateUserProfileDirect(UserProfile profile, Bson updates) {
		userProfiles.updateOne(Filters.eq("userId", profile.getUserId()), updates);
	}
	
	public void deleteUserProfile(UserProfile profile) {
		userProfiles.deleteOne(Filters.eq("userId", profile.getUserId()));
	}
	
	public FindIterable<ServerProfile> getServerProfiles() {
		return serverProfiles.find();
	}
	
	public ServerProfile getServerProfile(Guild server) {
		FindIterable<ServerProfile> profiles = serverProfiles.find(Filters.eq("serverId", server.getId()));
		ServerProfile profile = profiles.first();
		profile = profile == null ? createServerProfile(server) : profile;
		if(!profile.getName().equals(server.getName())) {
			profile.setName(server.getName());
			updateServerProfile(profile, "name");
		}
		return profile == null ? createServerProfile(server) : profile;
	}
	
	public ServerProfile createServerProfile(Guild server) {
		ServerProfile profile = new ServerProfile(server.getId(), server.getName());
		serverProfiles.insertOne(profile);
		return profile;
	}
	
	public void updateServerProfile(ServerProfile profile, String... updated) {
		serverProfiles.updateOne(Filters.eq("serverId", profile.getServerId()), updateHelper(profile, updated));
	}
	
	public void updateServerProfileDirect(ServerProfile profile, Bson updates) {
		serverProfiles.updateOne(Filters.eq("serverId", profile.getServerId()), updates);
	}
	
	public long getDonationCount() {
		return donations.countDocuments();
	}
	
	public FindIterable<Donation> getDonations() {
		return donations.find();
	}
	
	public void insertDonation(Donation donation) {
		donations.insertOne(donation);
	}
	
	public Optional<Donation> getDonation(String id) {
		FindIterable<Donation> filtered = donations.find(Filters.eq("txnId", id));
		MongoCursor<Donation> iter = filtered.iterator();
		while(iter.hasNext()) return Optional.of(iter.next());
		return Optional.empty();
	}
	
	public FindIterable<com.tek.cookiebaker.storage.Guild> getGuilds() {
		return guilds.find();
	}
	
	public List<com.tek.cookiebaker.storage.Guild> getGuildsListed() {
		MongoCursor<com.tek.cookiebaker.storage.Guild> iter = getGuilds().iterator();
		List<com.tek.cookiebaker.storage.Guild> guilds = new ArrayList<com.tek.cookiebaker.storage.Guild>();
		while(iter.hasNext()) {
			guilds.add(iter.next());
		}
		guilds.forEach(com.tek.cookiebaker.storage.Guild::verifyIntegrity);
		return guilds;
	}
	
	public Optional<com.tek.cookiebaker.storage.Guild> getGuildByOwnerId(String userId) {
		return getGuildsListed().stream().filter(guild -> guild.getOwner().equals(userId)).findFirst();
	}
	
	public Optional<com.tek.cookiebaker.storage.Guild> getGuildByMemberId(String userId) {
		return getGuildsListed().stream().filter(guild -> guild.getMembers().contains(userId)).findFirst();
	}
	
	public Optional<com.tek.cookiebaker.storage.Guild> getGuildByName(String name) {
		return getGuildsListed().stream().filter(guild -> guild.getName().equalsIgnoreCase(name)).findFirst();
	}
	
	public void updateGuild(com.tek.cookiebaker.storage.Guild guild, String... updated) {
		guilds.updateOne(Filters.eq("name", guild.getName()), updateHelper(guild, updated));
	}
	
	public void deleteGuild(com.tek.cookiebaker.storage.Guild guild) {
		guilds.deleteOne(Filters.eq("name", guild.getName()));
	}
	
	public void createGuild(com.tek.cookiebaker.storage.Guild guild) {
		guilds.insertOne(guild);
	}
	
	private Bson updateHelper(Object obj, String... fields) {
		try {
			List<Bson> updates = new ArrayList<Bson>();
			Class<?> objectClass = obj.getClass();
			
			for(String field : fields) {
				Field objectField = objectClass.getDeclaredField(field);
				boolean access = objectField.isAccessible();
				if(!access) objectField.setAccessible(!access);
				Object value = objectField.get(obj);
				if(!access) objectField.setAccessible(access);
				updates.add(Updates.set(field, value));
			}
			
			return Updates.combine(updates);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}