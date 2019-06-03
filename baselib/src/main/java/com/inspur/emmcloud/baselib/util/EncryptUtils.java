package com.inspur.emmcloud.baselib.util;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
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
//	public static String keyString = "inspurIMPCloud968842022285d325h9";


    /**
     * Encodes a String in AES-256 with a given key
     *
     * @param stringToEncode
     * @return
     * @throws Exception
     */
    public static String encode(String stringToEncode) throws Exception {
        String defaltKeyString = "inspurIMPCloud968842022285d325h9";
        return encode(stringToEncode, defaltKeyString, null, Base64.DEFAULT);
    }


    /**
     * @param stringToEncode
     * @param keyString
     * @param offset         偏移量
     * @return
     * @throws Exception
     */
    public static String encode(String stringToEncode, String keyString, String offset, int base64Flag) throws Exception {
        if (keyString == null || keyString.length() == 0) {
            throw new NullPointerException("Please give Password");
        }

        if (stringToEncode.length() == 0 || stringToEncode == null) {
            throw new NullPointerException("Please give text");
        }

        try {
            SecretKeySpec skeySpec = getKey(keyString);
            byte[] clearText = stringToEncode.getBytes("UTF8");
            IvParameterSpec ivParameterSpec = null;
            if (StringUtils.isBlank(offset)) {
                byte[] iv = new byte[16];
                Arrays.fill(iv, (byte) 0x00);
                ivParameterSpec = new IvParameterSpec(iv);
            } else {
                // IMPORTANT TO GET SAME RESULTS ON iOS and ANDROID
                byte[] iv = offset.getBytes();
                ivParameterSpec = new IvParameterSpec(iv);

            }
            // Cipher is not thread safe
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
            String encrypedValue = Base64.encodeToString(cipher.doFinal(clearText), base64Flag);
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
     * @param stringToEncode
     * @param keyString
     * @param offset         偏移量
     * @return
     * @throws Exception
     */
    public static byte[] encodeNoBase64(String stringToEncode, String keyString, String offset) throws Exception {
        if (keyString == null || keyString.length() == 0) {
            throw new NullPointerException("Please give Password");
        }

        if (stringToEncode.length() == 0 || stringToEncode == null) {
            throw new NullPointerException("Please give text");
        }

        try {
            SecretKeySpec skeySpec = getKey(keyString);
            byte[] clearText = stringToEncode.getBytes("UTF8");
            IvParameterSpec ivParameterSpec = null;
            if (StringUtils.isBlank(offset)) {
                byte[] iv = new byte[16];
                Arrays.fill(iv, (byte) 0x00);
                ivParameterSpec = new IvParameterSpec(iv);
            } else {
                // IMPORTANT TO GET SAME RESULTS ON iOS and ANDROID
                byte[] iv = offset.getBytes();
                ivParameterSpec = new IvParameterSpec(iv);

            }
            // Cipher is not thread safe
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
            return cipher.doFinal(clearText);

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
        return null;
    }


    public static String decode(String text) throws NullPointerException {
        String defaltKeyString = "inspurIMPCloud968842022285d325h9";
        return decode(text, defaltKeyString, null, Base64.DEFAULT);
    }


    /**
     * @param text
     * @param keyString
     * @param offset
     * @param base64Flag
     * @return
     * @throws NullPointerException
     */
    public static String decode(String text, String keyString, String offset, int base64Flag) throws NullPointerException {


        if (text == null || text.length() == 0) {
            throw new NullPointerException("Please give text");
        }

        try {
            SecretKey key = getKey(keyString);

            // IMPORTANT TO GET SAME RESULTS ON iOS and ANDROID
            IvParameterSpec ivParameterSpec = null;
            if (StringUtils.isBlank(offset)) {
                byte[] iv = new byte[16];
                Arrays.fill(iv, (byte) 0x00);
                ivParameterSpec = new IvParameterSpec(iv);
            } else {
                // IMPORTANT TO GET SAME RESULTS ON iOS and ANDROID
                byte[] iv = offset.getBytes();
                ivParameterSpec = new IvParameterSpec(iv);

            }

            byte[] encrypedPwdBytes = Base64.decode(text, base64Flag);
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


    public static byte[] decode(byte[] encrypedPwdBytes, String keyString, String offset) throws NullPointerException {
        if (encrypedPwdBytes == null || encrypedPwdBytes.length == 0) {
            throw new NullPointerException("Please give text");
        }

        try {
            SecretKey key = getKey(keyString);

            // IMPORTANT TO GET SAME RESULTS ON iOS and ANDROID
            IvParameterSpec ivParameterSpec = null;
            if (StringUtils.isBlank(offset)) {
                byte[] iv = new byte[16];
                Arrays.fill(iv, (byte) 0x00);
                ivParameterSpec = new IvParameterSpec(iv);
            } else {
                // IMPORTANT TO GET SAME RESULTS ON iOS and ANDROID
                byte[] iv = offset.getBytes();
                ivParameterSpec = new IvParameterSpec(iv);

            }
            // cipher is not thread safe
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));
            return decrypedValueBytes;

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
        return null;
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
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
            byte[] bytes = cipher.doFinal(stringToEncode.getBytes("UTF8"));
            Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
            return Base64.encodeToString(bytes, Base64.NO_WRAP);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }
}
