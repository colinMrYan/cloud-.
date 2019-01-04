package com.inspur.emmcloud.util.privates.mail;

import android.content.Context;
import android.text.TextUtils;

import com.inspur.emmcloud.bean.appcenter.mail.MailCertificateDetail;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by libaochao on 2019/1/4.
 */

public class PreferencesSaveGetCerUtils {

    /**
     * 存储证书详细信息
     *
     * @param certificateDetail
     **/
    public static void saveCertifivateByUsers(Context context, MailCertificateDetail certificateDetail, String Key) {
        try {
            //先将序列化结果写到byte缓存中，其实就分配一个内存空间
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream( bos );
            //将对象序列化写入byte缓存
            os.writeObject( certificateDetail );
            //将序列化的数据转为16进制保存
            String bytesToHexString =  bytesToHexString( bos.toByteArray() );
            //保存该16进制数组
            PreferencesByUsersUtils.putString(context, Key, bytesToHexString );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *读取证书*/
    /**
     * 从 preference 数据库中读取数据
     * @param Key 关键字
     **/
    public static Object getCertificateByUsers(Context context, String Key) {
        try {
            String string = PreferencesByUsersUtils.getString( context, Key );
            if (TextUtils.isEmpty( string )) {
                return null;
            } else {
                //将16进制的数据转为数组，准备反序列化
                byte[] stringToBytes = StringToBytes( string );
                ByteArrayInputStream bis = new ByteArrayInputStream( stringToBytes );
                ObjectInputStream is = new ObjectInputStream( bis );
                //返回反序列化得到的对象
                Object readObject = is.readObject();
                return readObject;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * desc:将数组转为16进制
     * @param bArray
     * @return
     * modified:
     */
    public static String bytesToHexString(byte[] bArray) {
        if(bArray == null){
            return null;
        }
        if(bArray.length == 0){
            return "";
        }
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * desc:将16进制的数据转为数组
     * @param data
     * @return
     * modified:
     */
    public static byte[] StringToBytes(String data){
        String hexString=data.toUpperCase().trim();
        if (hexString.length()%2!=0) {
            return null;
        }
        byte[] retData=new byte[hexString.length()/2];
        for(int i=0;i<hexString.length();i++)
        {
            int int_ch;  // 两位16进制数转化后的10进制数
            char hex_char1 = hexString.charAt(i); ////两位16进制数中的第一位(高位*16)
            int int_ch3;
            if(hex_char1 >= '0' && hex_char1 <='9')
                int_ch3 = (hex_char1-48)*16;   //// 0 的Ascll - 48
            else if(hex_char1 >= 'A' && hex_char1 <='F')
                int_ch3 = (hex_char1-55)*16; //// A 的Ascll - 65
            else
                return null;
            i++;
            char hex_char2 = hexString.charAt(i); ///两位16进制数中的第二位(低位)
            int int_ch4;
            if(hex_char2 >= '0' && hex_char2 <='9')
                int_ch4 = (hex_char2-48); //// 0 的Ascll - 48
            else if(hex_char2 >= 'A' && hex_char2 <='F')
                int_ch4 = hex_char2-55; //// A 的Ascll - 65
            else
                return null;
            int_ch = int_ch3+int_ch4;
            retData[i/2]=(byte) int_ch;//将转化后的数放入Byte里
        }
        return retData;
    }


}
