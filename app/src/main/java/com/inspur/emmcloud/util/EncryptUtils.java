package com.inspur.emmcloud.util;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils {
	public static String keyString = "inspurIMPCloud968842022285d325h9";


	/**
	 * Encodes a String in AES-256 with a given key
	 *
	 * @param context
	 * @param password
	 * @param text
	 * @return String Base64 and AES encoded String
	 */
	public static String encode( String stringToEncode) throws Exception {
	    if (keyString.length() == 0 || keyString == null) {
	        throw new NullPointerException("Please give Password");
	    }
	    
	    if (stringToEncode.length() == 0 || stringToEncode == null) {
	        throw new NullPointerException("Please give text");
	    }
	    
	    try {
	        SecretKeySpec skeySpec = getKey(keyString);
	        byte[] clearText = stringToEncode.getBytes("UTF8");
	        
	        // IMPORTANT TO GET SAME RESULTS ON iOS and ANDROID
	        final byte[] iv = new byte[16];
	        Arrays.fill(iv, (byte) 0x00);
	        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
	        
	        // Cipher is not thread safe
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
	        
	        String encrypedValue = Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);
	        Log.d("jacek", "Encrypted: " + stringToEncode + " -> " + encrypedValue);
	        return encrypedValue;
	        
	    } catch (InvalidKeyException e) {
	        e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (BadPaddingException e) {
	        e.printStackTrace();
	    } catch (NoSuchPaddingException e) {
	        e.printStackTrace();
	    } catch (IllegalBlockSizeException e) {
	        e.printStackTrace();
	    } catch (InvalidAlgorithmParameterException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	 
	/**
	 * Decodes a String using AES-256 and Base64
	 *
	 * @param context
	 * @param password
	 * @param text
	 * @return desoded String
	 */
	public String decode(String password, String text) throws NullPointerException {
	    
	    if (password.length() == 0 || password == null) {
	        throw new NullPointerException("Please give Password");
	    }
	    
	    if (text.length() == 0 || text == null) {
	        throw new NullPointerException("Please give text");
	    }
	    
	    try {
	        SecretKey key = getKey(password);
	        
	        // IMPORTANT TO GET SAME RESULTS ON iOS and ANDROID
	        final byte[] iv = new byte[16];
	        Arrays.fill(iv, (byte) 0x00);
	        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
	        
	        byte[] encrypedPwdBytes = Base64.decode(text, Base64.DEFAULT);
	        // cipher is not thread safe
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
	        cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
	        byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));
	        
	        String decrypedValue = new String(decrypedValueBytes);
	        Log.d("EncryptUtil", "Decrypted: " + text + " -> " + decrypedValue);
	        return decrypedValue;
	        
	    } catch (InvalidKeyException e) {
	        e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (BadPaddingException e) {
	        e.printStackTrace();
	    } catch (NoSuchPaddingException e) {
	        e.printStackTrace();
	    } catch (IllegalBlockSizeException e) {
	        e.printStackTrace();
	    } catch (InvalidAlgorithmParameterException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	 
	/**
	 * Generates a SecretKeySpec for given password
	 *
	 * @param password
	 * @return SecretKeySpec
	 * @throws UnsupportedEncodingException
	 */
	private static SecretKeySpec getKey(String password) throws UnsupportedEncodingException {
	    
	    // You can change it to 128 if you wish
	    int keyLength = 256;
	    byte[] keyBytes = new byte[keyLength / 8];
	    // explicitly fill with zeros
	    Arrays.fill(keyBytes, (byte) 0x0);
	    
	    // if password is shorter then key length, it will be zero-padded
	    // to key length
	    byte[] passwordBytes = password.getBytes("UTF-8");
	    int length = passwordBytes.length < keyBytes.length ? passwordBytes.length : keyBytes.length;
	    System.arraycopy(passwordBytes, 0, keyBytes, 0, length);
	    SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
	    return key;
	}


	public static String encodeApprovalPassword(String stringToEncode) throws Exception {
		String keyString = ")P:?,ki8";

		try {
			DESKeySpec dks = new DESKeySpec(keyString.getBytes("UTF8"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			//key的长度不能够小于8位字节
			Key secretKey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			byte[] array = new byte[8];
			IvParameterSpec iv = new IvParameterSpec(array);
			AlgorithmParameterSpec paramSpec = iv;
			cipher.init(Cipher.ENCRYPT_MODE, secretKey,paramSpec);
			byte[] bytes = cipher.doFinal(stringToEncode.getBytes("UTF8"));
			Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
			return  Base64.encodeToString(bytes, android.util.Base64.DEFAULT);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
