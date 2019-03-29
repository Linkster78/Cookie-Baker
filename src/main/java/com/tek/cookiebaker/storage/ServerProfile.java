package com.tek.cookiebaker.storage;

import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;

import com.tek.cookiebaker.main.Reference;

public class ServerProfile {
	
	private ObjectId id;
	private String serverId;
	private String name;
	private String prefix;
	private Map<String, String> roleMap;
	
	public ServerProfile() {
		this.prefix = Reference.DEFAULT_PREFIX;
		this.roleMap = new HashMap<String, String>();
	}
	
	public ServerProfile(String serverId, String name) {
		this();
		this.serverId = serverId;
		this.name = name;
	}
	
	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public Map<String, String> getRoleMap() {
		return roleMap;
	}
	
	public void setRoleMap(Map<String, String> roleMap) {
		this.roleMap = roleMap;
	}
	
}
