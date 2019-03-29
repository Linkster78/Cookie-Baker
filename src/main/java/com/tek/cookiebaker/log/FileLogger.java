package com.tek.cookiebaker.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.tek.cookiebaker.entities.enums.CookieType;
import com.tek.cookiebaker.main.Reference;
import com.tek.cookiebaker.storage.UserProfile;

public class FileLogger {
	
	private String fileName;
	
	public FileLogger(String fileName) {
		this.fileName = fileName;
	}
	
	public void initialize() throws IOException {
		File log = new File(fileName);
		if(!log.exists()) log.createNewFile();
	}
	
	public void logMoneyTransfer(UserProfile from, UserProfile to, double money, String channelId, String guildId) {
		attemptLog(String.format("[MONEYTRANSFER] (%s) %s paid %s to (%s) %s in C(%s) G(%s)", from.getUserId(), from.getName(), NumberFormat.getCurrencyInstance().format(money), to.getUserId(), to.getName(), channelId, guildId));
	}
	
	public void logCookieTransfer(UserProfile from, UserProfile to, CookieType type, long count, String channelId, String guildId) {
		attemptLog(String.format("[COOKIETRANSFER] (%s) %s gave %s %s to (%s) %s in C(%s) G(%s)", from.getUserId(), from.getName(), Reference.FORMATTER.format(count), type.getDisplayName() + "s", to.getUserId(), to.getName(), channelId, guildId));
	}
	
	public void logStartedVerification(UserProfile user, String channelId, String guildId) {
		attemptLog(String.format("[STARTEDVERIFY] (%s) %s started verification in C(%s) G(%s)", user.getUserId(), user.getName(), channelId, guildId));
	}
	
	public void logFailedVerification(UserProfile user, String code, long time, String channelId, String guildId) {
		attemptLog(String.format("[FAILEDVERIFY] (%s) %s couldn't verify \"%s\" in C(%s) G(%s)", user.getUserId(), user.getName(), code, channelId, guildId));
	}
	
	public void logErrorVerification(UserProfile user, String code, String channelId, String guildId) {
		attemptLog(String.format("[ERRORVERIFY] (%s) %s failed to verify once \"%s\" in C(%s) G(%s)", user.getUserId(), user.getName(), code, channelId, guildId));
	}
	
	public void logSuccessVerification(UserProfile user, String code, int attempts, long bakeAttempts, long time, String channelId, String guildId) {
		attemptLog(String.format("[SUCCESSVERIFY] (%s) %s verified \"%s\" in %d attempts, with %s bake attempts, and took %s in C(%s) G(%s)", user.getUserId(), user.getName(), code, attempts, Reference.FORMATTER.format(bakeAttempts - 1), Reference.getFormattedTime(time), channelId, guildId));
	}
	
	public void logClickerGame(UserProfile user, long cookies, String channelId, String guildId) {
		attemptLog(String.format("[CLICKEREND] (%s) %s completed a clicker game with %d cookies in C(%s) G(%s)", user.getUserId(), user.getName(), cookies, channelId, guildId));
	}
	
	public void attemptLog(String line) {
		try {
			log(line);
		} catch(Exception e) { Logger.error(e); }
	}
	
	public void log(String line) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		
		StringBuilder builder = new StringBuilder();
		String linen;
		while((linen = br.readLine()) != null) {
			builder.append(linen + "\n");
		}
		
		FileWriter writer = new FileWriter(fileName);
		writer.write(builder.toString() + "[" + Logger.dateAndTime() + "]" + line);
		writer.close();
		br.close();
	}
	
	public List<String> getRecentMost(int count) throws IOException {
		List<String> lines = new ArrayList<String>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		String line;
		while((line = br.readLine()) != null) {
			lines.add(line);
		}
		br.close();
		
		return count == -1 ? lines.stream().sorted(Collections.reverseOrder()).collect(Collectors.toList())
				: lines.stream().sorted(Collections.reverseOrder()).limit(count).collect(Collectors.toList());
	}
	
	public String getFileName() {
		return fileName;
	}
	
}
