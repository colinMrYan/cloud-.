package com.inspur.imp.plugin.rsa;


import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

/**
 * RSA加密解密字符串
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class RsaService extends ImpPlugin {

	private String str;
	private String encryptStr = null;
	private String decryptStr = null;
	private String content = null;
	String[] obj = RsaService.Skey_RSA(512);

	@Override
	public void execute(String action, JSONObject paramsObject) {
		showCallIMPMethodErrorDlg();
	}
	
	public String executeAndReturn(String action, JSONObject paramsObject){
		if (!paramsObject.isNull("str")) {
			try {
				str = paramsObject.getString("str");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if ("encrypt".equals(action)) {
			try {
				content = encrypt(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if("decrypt".equals(action)){
			try {
				content = decrypt(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			showCallIMPMethodErrorDlg();
		}
		return content;
	}

	/**
	 * 设置密钥对的生成
	 * 
	 * @param keylen
	 */
	public static String[] Skey_RSA(int keylen) {
		// 用来存储密钥的e n d p q
		String[] output = new String[5];
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			// 指定密钥的长度，初始化密钥对生成器
			kpg.initialize(keylen);
			// 生成密钥对
			KeyPair kp = kpg.generateKeyPair();
			RSAPublicKey puk = (RSAPublicKey) kp.getPublic();
			RSAPrivateCrtKey prk = (RSAPrivateCrtKey) kp.getPrivate();
			BigInteger e = puk.getPublicExponent();
			BigInteger n = puk.getModulus();
			BigInteger d = prk.getPrivateExponent();
			BigInteger p = prk.getPrimeP();
			BigInteger q = prk.getPrimeQ();
			output[0] = e.toString();
			output[1] = n.toString();
			output[2] = d.toString();
			output[3] = p.toString();
			output[4] = q.toString();
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return output;
	}

	/**
	 * 设置加密算法RSA
	 * 
	 * @param message
	 */
	public String encrypt(String message) throws Exception {
		try {
			BigInteger e = new BigInteger(obj[0]);
			BigInteger n = new BigInteger(obj[1]);
			byte[] ptext = message.getBytes("UTF-8"); // 获取明文的大整数
			BigInteger m = new BigInteger(ptext);
			BigInteger c = m.modPow(e, n);
			encryptStr = c.toString().trim();
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
		return encryptStr;
	}

	/**
	 * 设置解密算法RSA
	 * 
	 * @param message
	 */
	public String decrypt(String message) throws Exception {
		StringBuffer decryptstr = new StringBuffer();
		BigInteger d = new BigInteger(obj[2]); // 获取私钥的参数d,n
		BigInteger n = new BigInteger(obj[1]);
		BigInteger c = new BigInteger(message);
		BigInteger m = c.modPow(d, n); // 解密明文
		byte[] mt = m.toByteArray(); // 计算明文对应的字符串并输出
		for (int i = 0; i < mt.length; i++) {
			decryptstr.append((char) mt[i]);
		}
		decryptStr = decryptstr.toString().trim();
		return decryptStr;
	}

	@Override
	public void onDestroy() {

	}

}
