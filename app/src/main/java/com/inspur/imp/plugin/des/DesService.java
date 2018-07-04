package com.inspur.imp.plugin.des;

import android.util.Base64;

import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * DES加密解密字符串
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class DesService extends ImpPlugin {

	static byte[] result = null;
	private String str;
	private String key;
	private String encryptStr = null;
	private String decryptStr = null;
	private String resultStr = null;

	@Override
	public void execute(String action, JSONObject paramsObject) {
		((ImpActivity)getActivity()).showImpDialog();
	}

	public String executeAndReturn(String action, JSONObject paramsObject) {

		try {
			if (!paramsObject.isNull("str"))
				str = paramsObject.getString("str");
			if (!paramsObject.isNull("key"))
				key = paramsObject.getString("key");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		if (action.equals("encrypt")) {
			try {
				resultStr = encrypt(str, key);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (action.equals("decrypt")) {
			try {
				resultStr = decrypt(str, key);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			((ImpActivity)getActivity()).showImpDialog();
		}
		return resultStr;
	}

	/**
	 * Description 根据键值进行加密 设置加密算法DES
	 * 
	 * @param
	 * @param
	 * @throws Exception
	 */
	public String encrypt(String message, String key) throws Exception {

		//初始化一个ciper对象
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5PADDING");
		
		//第二个参数创建一个密匙工厂，然后用它把DESKeySpec转换成 SecretKey参数类型的参数
		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(desKeySpec);

		//第三个参数，随机数源
		IvParameterSpec iv = new IvParameterSpec(new byte[8]);
		
		// Cipher对象实际完成加密操作,用密钥初始化Cipher对象
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		
		// ciper对象实现加密
		result = cipher.doFinal(message.getBytes("UTF-8"));
		
		// 将二进制格式的数组进行字符串格式化处理
		encryptStr = Base64.encodeToString(result, Base64.DEFAULT).trim();
		return encryptStr;
	}

	/**
	 * Description 根据键值进行解密 设置解算法DES
	 * 复杂
	 * @param
	 * @param
	 * @throws Exception
	 */
	public String decrypt(String message, String key) throws Exception {
		
		//初始化一个ciper对象
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5PADDING");
		
		//第二个参数,创建一个密匙工厂，然后用它把DESKeySpec转换成 SecretKey参数类型的参数
		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
		
		//第三个参数，可靠的随机数来源
		IvParameterSpec iv = new IvParameterSpec(new byte[8]);
		
		// 用密匙初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
		
		//完成加密
		result = cipher.doFinal(Base64.decode(message.getBytes("UTF-8"),
				Base64.DEFAULT));
		decryptStr = new String(result, "utf-8");
		decryptStr.trim();
		return decryptStr;
	}
	@Override
	public void onDestroy() {
	}

}
