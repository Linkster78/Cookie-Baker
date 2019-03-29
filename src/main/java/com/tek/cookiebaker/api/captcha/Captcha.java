package com.tek.cookiebaker.api.captcha;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

import com.tek.cookiebaker.main.Reference;

public class Captcha {
	
	private static String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
	
	public static String generateKey() {
		String randomString = "";
		for(int i = 0; i < 16; i++) randomString += ALPHABET.charAt(Reference.RANDOM.nextInt(ALPHABET.length()));
		return randomString;
	}
	
	public static BufferedImage fetchCaptcha(String key) throws IOException {
		String url = "http://image.captchas.net/?client=demo&random=" + key + "&width=175&height=100";
		
		int cutSize = 24;
		BufferedImage verification = ImageIO.read(new URL(url));
		verification = verification.getSubimage(0, cutSize, verification.getWidth(), verification.getHeight() - cutSize * 2);
		
		return verification;
	}
	
	public static String computeCode(String key) {
		try {
			String computedCode = "";
			String compute = "secret" + key;
			byte[] computed = MessageDigest.getInstance("MD5").digest(compute.getBytes());
			int[] resulting = new int[6];
			for(int i = 0; i < resulting.length; i++) {
				resulting[i] = (byte) (unsignedToInt(computed[i]) % 26);
				computedCode += ALPHABET.charAt(resulting[i]);
			}
			
			return computedCode;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static int unsignedToInt(byte b) {
		return b & 0xFF;
    }
	
}
