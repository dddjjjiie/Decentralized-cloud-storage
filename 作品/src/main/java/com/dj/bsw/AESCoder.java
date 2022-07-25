package com.dj.bsw;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESCoder {
	/**
	 * generate key of AES algorithm
	 * @param seed, seed of pseudo random number generator
	 * @return key
	 */
	private static byte[] getKey(byte[] seed){
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(seed);
			kgen.init(128, sr);
			SecretKey key = kgen.generateKey();
			byte[] rawKey = key.getEncoded();
			return rawKey;
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}
	
	/**
	 * use seed to generate key and use AES algorithm to encrypt message
	 * @param seed, element in Gt
	 * @param message
	 * @return ciphertext
	 */
	public static byte[] encrypt(byte[] seed, byte[] message) {
		try {
			byte[] key = getKey(seed);
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] cipherText = cipher.doFinal(message);
			return cipherText;
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}
	
	/**
	 * use seed to generate key and use AES algorithm to decrypt message
	 * @param seed, element in Gt
	 * @param cipherText
	 * @return message
	 */
	public static byte[] decrypt(byte[] seed, byte[] cipherText) {
		try {
			byte[] key = getKey(seed);
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] message = cipher.doFinal(cipherText);
			return message;
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}
}
