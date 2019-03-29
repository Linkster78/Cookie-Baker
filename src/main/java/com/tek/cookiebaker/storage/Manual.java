package com.tek.cookiebaker.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.jagrosh.jdautilities.command.Command;

public class Manual {
	
	private final static Pattern MANUAL_SEPARATOR = Pattern.compile("\\[[\\w]+\\]");
	private Map<String, String> manualMap;
	
	public Manual(String contents) {
		this.manualMap = loadMap(contents);
	}
	
	public Map<String, String> loadMap(String contents) {
		Map<String, String> map = new HashMap<String, String>();
		
		Scanner scanner = new Scanner(contents);
		
		String key = null;
		StringBuilder valueBuilder = null;
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			
			if(MANUAL_SEPARATOR.matcher(line).matches()) {
				if(key != null) {
					if(valueBuilder.length() > 0) valueBuilder.setLength(valueBuilder.length() - 1);
					map.put(key, valueBuilder.toString());
				}
				
				valueBuilder = new StringBuilder();
				key = line.substring(1, line.length() - 1);
			} else if(key != null) {
				valueBuilder.append(line + "\n");
			}
		}
		
		if(key != null) {
			if(valueBuilder.length() > 0) valueBuilder.setLength(valueBuilder.length() - 1);
			map.put(key, valueBuilder.toString());
		}
		
		scanner.close();
		
		return map;
	}
	
	public static Manual load(String configPath) throws IOException, NullPointerException {
		StringBuilder configContents = new StringBuilder();
		
		try{
			FileInputStream fis = new FileInputStream(new File(configPath));
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			
			while((line = br.readLine()) != null) {
				configContents.append(line).append("\n");
			}
			
			if(configContents.length() > 0) configContents.setLength(configContents.length() - 1);
			
			br.close();
		}catch(IOException e) {
			throw new IOException("Manual file \"" + configPath + "\" not found");
		}
		
		String stringContents = configContents.toString();
		
		return new Manual(stringContents);
	}
	
	public Map<String, String> getManualMap() {
		return manualMap;
	}
	
	public Optional<String> getManualPage(Command command) {
		return getManualPage(command.getName());
	}
	
	public Optional<String> getManualPage(String command) {
		return Optional.ofNullable(manualMap.get(command.toUpperCase()));
	}
	
}
