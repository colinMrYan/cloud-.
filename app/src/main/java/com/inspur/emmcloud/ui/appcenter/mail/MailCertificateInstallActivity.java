package com.inspur.emmcloud.ui.appcenter.mail;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.LogUtils;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Enumeration;

/**
 * Created by libaochao on 2018/12/20.
 */
@ContentView(R.layout.activity_mail_certificate_install)
public class MailCertificateInstallActivity extends BaseActivity {
    @ViewInject( R.id.tv_install_certificate)
    private TextView mInstallCertificateTV;
    @ViewInject( R.id.back_layout)
    private RelativeLayout mBackLayout;
    @ViewInject( R.id.tv_certificate_use)
    private TextView mCertificateUseTV;
    @ViewInject( R.id.tv_installed_certificate_title)
    private TextView mInstalledCerTitleTV;
    @ViewInject( R.id.tv_certificate_used_name)
    private TextView mCertificateUseNameTV;
    @ViewInject( R.id.tv_certificate_giver_id)
    private TextView mCertificateGiverIdTV;
    @ViewInject( R.id.tv_certificate_expirty_data)
    private TextView mCertificateExpirtyDataTV;
    @ViewInject( R.id.s_secrity_action1)
    private Switch mSecrity_Action1;
    @ViewInject( R.id.s_secrity_action2)
    private Switch mSecrity_Action2;


    @ViewInject( R.id.tv_new_certificate_title)
    private TextView mNewCertificateTitleTV;
    @ViewInject( R.id.tv_new_certificate_user)
    private TextView mNewCertificateUserTV;
    @ViewInject( R.id.tv_new_certificate_publisher)
    private TextView mNewCertificatePublisherTV;
    @ViewInject( R.id.tv_new_certificate_expirty_data)
    private TextView mNewCertificateExpirtyDataTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
    }

    /**
     *
     * */
    public void onClick(View v){
        switch(v.getId()){
            case R.id.back_layout:
                finish();
                LogUtils.LbcDebug( "返回按钮" );
                break;
            case R.id.tv_install_certificate:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                LogUtils.LbcDebug("打开文件管理系统");
                startActivityForResult(intent,1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            LogUtils.LbcDebug("获取的文件路径"+uri.toString());
            String[] proj = {MediaStore.Images.Media.DATA};
             Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
         final   String img_path = actualimagecursor.getString(actual_image_column_index);
            final String[] passWord = new String[1];
            LogUtils.LbcDebug("Path::"+img_path);
            //判断文件后  判断一下文件格式
            final  QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
            builder.setTitle("证书密码：")
                    .setPlaceholder("请在此输入证书密码：")
                    .setInputType( InputType.TYPE_CLASS_TEXT)
                    .addAction("取消", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    . addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            CharSequence text = builder.getEditText().getText();
                            String ID = text.toString().trim();
                            passWord[0] = ID;
                            if(dealCertificate(img_path,passWord[0])){
                                Toast.makeText(getBaseContext(), "密码正确", Toast.LENGTH_SHORT).show();
                                //执行网络请求
                                dialog.dismiss();
                            }else{
                                Toast.makeText(getBaseContext(), "密码无效，请重试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).show();
        }
        super.onActivityResult( requestCode, resultCode, data );
    }

    /**
     * 解析pfx
     * @param passWord  密码
     * @param path  文件路径*/
    private boolean  dealCertificate(String path,String passWord){
        String strPfx =  path;
        String strPassword =passWord;
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = null;
            fis = new FileInputStream(strPfx);
            char[] nPassword = null;
            if ((strPassword == null) || strPassword.trim().equals("")){
                nPassword = null;
            } else {
                nPassword = strPassword.toCharArray();
            }
            ks.load(fis, nPassword);
            fis.close();
            Enumeration enumas = null;
            try {
                enumas = ks.aliases();
            } catch (Exception ea) {
                return false;
            }
            String keyAlias = null;
            if (enumas.hasMoreElements()){ // we are readin just one certificate.
                keyAlias = (String)enumas.nextElement();
                LogUtils.LbcDebug( "alias=[" + keyAlias + "]" );
            }
            LogUtils.LbcDebug("5"+"is key entry=" + ks.isKeyEntry(keyAlias));
            PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, nPassword);
            Certificate cert = ks.getCertificate(keyAlias);
            PublicKey pubkey = cert.getPublicKey();
            LogUtils.LbcDebug("5"+"cert class =" + cert.getClass().getName());
            LogUtils.LbcDebug("5"+"cert" + cert);
            LogUtils.LbcDebug("5"+"pubkey" + pubkey);
            LogUtils.LbcDebug("5"+"private key =" + prikey);
            LogUtils.LbcDebug( "1" );
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return  true;
    }

    public class ReadPFX {
        public ReadPFX() {
        }
        //转换成十六进制字符串
        public  String Byte2String(byte[] b) {
            String hs = "";
            String stmp = "";

            for (int n = 0; n < b.length; n++) {
                stmp = (java.lang.Integer.toHexString( b[n] & 0XFF ));
                if (stmp.length() == 1) hs = hs + "0" + stmp;
                else hs = hs + stmp;
                //if (n<b.length-1)  hs=hs+":";
            }
            return hs.toUpperCase();
        }
        public   byte[] StringToByte(int number) {
            int temp = number;
            byte[] b = new byte[4];
            for (int i = b.length - 1; i > -1; i--) {
                b[i] = new Integer( temp & 0xff ).byteValue();//将最高位保存在最低位
                temp = temp >> 8; //向右移8位
            }
            return b;
        }
        public PrivateKey GetPvkformPfx(String strPfx, String strPassword) {
            try {
                KeyStore ks = KeyStore.getInstance( "PKCS12" );
                FileInputStream fis = new FileInputStream( strPfx );
                // If the keystore password is empty(""), then we have to set
                // to null, otherwise it won't work!!!
                char[] nPassword = null;
                if ((strPassword == null) || strPassword.trim().equals( "" )) {
                    nPassword = null;
                } else {
                    nPassword = strPassword.toCharArray();
                }
                ks.load( fis, nPassword );
                fis.close();
                System.out.println( "keystore type=" + ks.getType() );
                // Now we loop all the aliases, we need the alias to get keys.
                // It seems that this value is the "Friendly name" field in the
                // detals tab <-- Certificate window <-- view <-- Certificate
                // Button <-- Content tab <-- Internet Options <-- Tools menu
                // In MS IE 6.
                Enumeration enumas = ks.aliases();
                String keyAlias = null;
                if (enumas.hasMoreElements())// we are readin just one certificate.
                {
                    keyAlias = (String) enumas.nextElement();
                    System.out.println( "alias=[" + keyAlias + "]" );
                }
                // Now once we know the alias, we could get the keys.
                System.out.println( "is key entry=" + ks.isKeyEntry( keyAlias ) );
                PrivateKey prikey = (PrivateKey) ks.getKey( keyAlias, nPassword );
                Certificate cert = ks.getCertificate( keyAlias );
                PublicKey pubkey = cert.getPublicKey();
                System.out.println( "cert class = " + cert.getClass().getName() );
                System.out.println( "cert = " + cert );
                System.out.println( "public key = " + pubkey );
                System.out.println( "private key = " + prikey );
                return prikey;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
