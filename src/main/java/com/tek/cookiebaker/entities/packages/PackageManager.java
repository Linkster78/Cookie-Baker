package com.tek.cookiebaker.entities.packages;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;
import com.tek.cookiebaker.main.Reference;

public class PackageManager {
	
	private List<Package> packages;
	
	public void loadPackages(String jsonArray) {
		Type packageListType = new TypeToken<List<Package>>(){}.getType();
		packages = Reference.GSON.fromJson(jsonArray, packageListType);
	}
	
	public List<Package> getPackages() {
		return packages;
	}
	
	public Optional<Package> getPackageByAlias(String alias) {
		return packages.stream().filter(pack -> pack.getAlias().equalsIgnoreCase(alias)).findFirst();
	}
	
}
