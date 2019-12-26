package com.inspur.emmcloud.application.util;

/**
 * Created by yufuchang on 2017/2/23.
 */

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

public class FileSafeCode {
    public static MessageDigest messagedigest = null;
    /**
     * 计算大文件 md5获取getMD5(); SHA1获取getSha1() CRC32获取 getCRC32()
     */
    protected static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 对一个文件获取md5值
     *
     * @return md5串
     * @throws NoSuchAlgorithmException
     */
    public static String getMD5(File file) throws IOException,
            NoSuchAlgorithmException {

        messagedigest = MessageDigest.getInstance("MD5");
        FileInputStream in = new FileInputStream(file);
        FileChannel ch = in.getChannel();
        MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,
                file.length());
        messagedigest.update(byteBuffer);
        return bufferToHex(messagedigest.digest());
    }

    /**
     * @param target 字符串 求一个字符串的md5值
     * @return md5 value
     */
    public static String StringMD5(String target) {
        return DigestUtils.md5Hex(target);
    }


    /**
     * 获取文件CRC32码
     *
     * @return String
     */
    public static String getCRC32(File file) {
        CRC32 crc32 = new CRC32();
        // MessageDigest.get
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                crc32.update(buffer, 0, length);
            }
            return crc32.getValue() + "";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getMD5String(String s) {
        return getMD5String(s.getBytes());
    }

    public static String getMD5String(byte[] bytes) {
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }


    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    public static boolean checkPassword(String password, String md5PwdStr) {
        String s = getMD5String(password);
        return s.equals(md5PwdStr);
    }

    /**
     * 文件校验之SHA256
     *
     * @param file
     * @return
     */
    public static String getFileSHA256(File file) {
        if (!file.isFile()) {
            System.err.println("not file");
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        //最小侵入修复bug，修复方式：高位补0 修复问题包验证 20170509 yfc
        String code = bigInt.toString(16);
        if (code.length() < 64) {
            int notEnght = 64 - bigInt.toString(16).length();
            for (int i = 0; i < notEnght; i++) {
                code = "0" + code;
            }
        }
        return code;
    }
}
