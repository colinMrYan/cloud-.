package com.inspur.emmcloud.ui.appcenter.mail;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.appcenter.mail.MailCertificateDetail;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.util.common.EncryptUtils;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.imp.plugin.filetransfer.filemanager.FileManagerActivity;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by libaochao on 2018/12/20.
 */
@ContentView(R.layout.activity_mail_certificate_install)
public class MailCertificateInstallActivity extends BaseActivity {
    @ViewInject( R.id.tv_install_certificate)
    private TextView installCertificateTV;
    @ViewInject( R.id.rl_back_layout)
    private RelativeLayout BackLayout;
    @ViewInject( R.id.rl_installed_certificate )
    private RelativeLayout installedCertificate;
    @ViewInject( R.id.tv_certificate_use)
    private TextView certificateUseTV;
    @ViewInject( R.id.tv_installed_certificate_title)
    private TextView installedCerTitleTV;
    @ViewInject( R.id.tv_certificate_used_name)
    private TextView certificateUseNameTV;
    @ViewInject( R.id.tv_certificate_giver_id)
    private TextView certificateGiverIdTV;
    @ViewInject( R.id.tv_certificate_expirty_data)
    private TextView certificateExpirtyDataTV;
    @ViewInject( R.id.sv_secrity_action1)
    private SwitchView secrityActionSwitch1;
    @ViewInject( R.id.sv_secrity_action2)
    private SwitchView secrityActionSwitch2;


    @ViewInject( R.id.tv_new_certificate_title)
    private TextView newCertificateTitleTV;
    @ViewInject( R.id.tv_new_certificate_user)
    private TextView newCertificateUserTV;
    @ViewInject( R.id.tv_new_certificate_publisher)
    private TextView newCertificatePublisherTV;
    @ViewInject( R.id.tv_new_certificate_expirty_data)
    private TextView newCertificateExpirtyDataTV;

    public static final int SELECT_CREDIFICATE_FILE = 10;
    private String mCertificateKeyWord;
    private GetMyInfoResult myInfoResult;
    private MailCertificateDetail myCertificate;

    public static String CERTIFICATER_KEY="certificate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        init();
    }

    /**
     * 初始化*/
    private void init(){
        Object certificateObject=readCertificate( CERTIFICATER_KEY);
        if(null==certificateObject){
            myCertificate = new MailCertificateDetail();
        }else {
            myCertificate =(MailCertificateDetail)certificateObject;
            installedCertificate.setVisibility(View.VISIBLE);
        }
        if(null==myInfoResult){
            String myInfo = PreferencesUtils.getString(this, "myInfo", "");
            myInfoResult = new GetMyInfoResult(myInfo);
        }
        secrityActionSwitch1.setOnStateChangedListener( new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                myCertificate.setEncryptedMail( true );
                secrityActionSwitch1.toggleSwitch( true );
                LogUtils.LbcDebug( "On" );
            }
            @Override
            public void toggleToOff(View view) {
               myCertificate.setSignedMail( false);
                secrityActionSwitch1.toggleSwitch( false );
                LogUtils.LbcDebug( "OFF" );
            }
        } );

        secrityActionSwitch2.setOnStateChangedListener( new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                myCertificate.setEncryptedMail( true );
                secrityActionSwitch2.toggleSwitch( true );
                LogUtils.LbcDebug( "On" );
            }

            @Override
            public void toggleToOff(View view) {
                myCertificate.setSignedMail( false );
                secrityActionSwitch2.toggleSwitch( false );
                LogUtils.LbcDebug( "Off" );
            }
        } );
    }

    /**
     *
     * */
    public void onClick(View v){
        switch(v.getId()){
            case R.id.rl_back_layout:
                finish();
                LogUtils.LbcDebug( "返回按钮" );
                break;
            case R.id.tv_install_certificate:
                Intent intent =  new Intent(this, FileManagerActivity.class);
                intent.putExtra( FileManagerActivity.EXTRA_MAXIMUM,1);
                startActivityForResult( intent,SELECT_CREDIFICATE_FILE );
                break;
            case R.id.btn_data11:
                myCertificate=(MailCertificateDetail)readCertificate( CERTIFICATER_KEY);
                if(null==myCertificate){
                   LogUtils.LbcDebug( "null" );
                }else {
                    LogUtils.LbcDebug( "data"+myCertificate.getCertificateName() );
                    LogUtils.LbcDebug( "data"+myCertificate.getCertificateFinalDate() );
                    installedCertificate.setVisibility(View.VISIBLE);
                    saveCertifivate(myCertificate);
                    updataCertificateUI( myCertificate );
                    LogUtils.LbcDebug( "Seri:"+myCertificate.isEncryptedMail()+" Sign:"+myCertificate.isEncryptedMail() );
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case  SELECT_CREDIFICATE_FILE:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> pathList = data.getStringArrayListExtra("pathList");
                    LogUtils.LbcDebug( "path"+pathList.get( 0 ) );
                    showInputCreKeyWordDialog(pathList.get(0));
                } else {
                    Toast.makeText(getBaseContext(), "选取文件失败", Toast.LENGTH_SHORT).show();
                }
                break;
                default:
                    break;
        }
    }

    /**
     * 弹出输入密码对话框
     * @param path */
    private void showInputCreKeyWordDialog( final String path){
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
                            String key = text.toString().trim();
                            if(dealCertificate(path,key)){
                                mCertificateKeyWord =key;
                                upLoadCertificateFile(myInfoResult.getMail(),path,mCertificateKeyWord);
                                dialog.dismiss();
                            }else{
                                Toast.makeText(getBaseContext(), "密码无效或证书有误，请重试", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).show();
    }

    /**
     * UpLoad File
     * @param mail
     * @param path
     * @param orgKey */
    private void upLoadCertificateFile(String mail,String path,String orgKey) {
        try {
            String certificateBase64Data = FileUtils.encodeBase64File( path );
            String key = EncryptUtils.stringToMD5( mail );
            String iv  = "inspurcloud+2019";
            String cerBase64DataResult= EncryptUtils.encode( certificateBase64Data,key,iv, Base64.NO_WRAP);
            String KeyResult = EncryptUtils.encode( orgKey,key,iv, Base64.NO_WRAP);
            if(NetUtils.isNetworkConnected( this )){
                AppAPIService apiService = new AppAPIService(this);
                apiService.setAPIInterface(new WebService());
                apiService.upLoadCertificateFile( mail,KeyResult,cerBase64DataResult );
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 解析pfx
     * @param passWord  密码
     * @param path  文件路径*/
    private boolean  dealCertificate(String path,String passWord){
        String strPfx =  path;
        String fileName = path.substring(path.lastIndexOf("/")+1);
        LogUtils.LbcDebug("fileName:"+ fileName );
        LogUtils.LbcDebug( "path:"+strPfx );
        if(!StringUtils.isBlank(fileName)){
            myCertificate.setCertificateName(fileName);
        }
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
            enumas = ks.aliases();
            String keyAlias = null;
            if (enumas.hasMoreElements()){ // we are readin just one certificate.
                keyAlias = (String)enumas.nextElement();
                LogUtils.LbcDebug( "alias=[" + keyAlias + "]" );
            }
            LogUtils.LbcDebug("5"+"is key entry=" + ks.isKeyEntry(keyAlias));
            PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, nPassword);
            Certificate cert = ks.getCertificate(keyAlias);
            try {
                 String data = cert.toString();
                 analysisCertificate(data);
            }catch (Exception e){
                e.printStackTrace();
            }
            PublicKey pubkey = cert.getPublicKey();
            myCertificate.setCertificatePassword(passWord);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return  true;
    }

    private void analysisCertificate(String data){
        int indexStartDate= data.indexOf( "Start Date:" );
        int endIndexStartDate =data.indexOf('\n',indexStartDate);

        int indexFinalDate=  data.indexOf( "Final Date:" );
        int endIndexFinalDate =data.indexOf('\n',indexFinalDate);
        LogUtils.LbcDebug("indexFinalDate"+indexFinalDate  );

        int indexIssuerDN =  data.indexOf( "IssuerDN:" );
        int endIndexIssuerDN =data.indexOf('\n',indexIssuerDN);

        int indexSubjectDN=  data.indexOf( "SubjectDN:" );
        int endIndexSubjectDN =data.indexOf('\n',indexSubjectDN);

      String startTime = data.substring( indexStartDate,endIndexStartDate );
      String finalTime = data.substring( indexFinalDate,endIndexFinalDate );
      String IssuerDN  = data.substring( indexIssuerDN,endIndexIssuerDN);
      String SubjectDN  = data.substring( indexSubjectDN,endIndexSubjectDN );
       myCertificate.setCertificateStartDate( startTime.substring( 12) );
       myCertificate.setCertificateFinalDate( finalTime.substring( 12 ) );
       myCertificate.setCertificateIssuerDN( IssuerDN.substring( 9 ) );
       myCertificate.setCertificateSubjectDN( SubjectDN.substring(10) );
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMailCertificateUploadSuccess(byte[] arg0) {
            Toast.makeText(getBaseContext(), "上传证书成功", Toast.LENGTH_SHORT).show();
            saveCertifivate(myCertificate);
            updataCertificateUI(myCertificate);
            super.returnMailCertificateUploadSuccess( arg0 );
        }

        @Override
        public void returnMailCertificateUploadFail(String error, int errorCode) {
            Toast.makeText(getBaseContext(), "上传证书失败", Toast.LENGTH_SHORT).show();
            super.returnMailCertificateUploadFail( error, errorCode );
        }
    }

    /**
     *存储证书详细信息*/
    private void saveCertifivate(MailCertificateDetail certificateDetail){
        try {
            //先将序列化结果写到byte缓存中，其实就分配一个内存空间
            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            ObjectOutputStream os=new ObjectOutputStream(bos);
            //将对象序列化写入byte缓存
            os.writeObject(certificateDetail);
            //将序列化的数据转为16进制保存
            String bytesToHexString = bytesToHexString(bos.toByteArray());
            //保存该16进制数组
            PreferencesByUserAndTanentUtils.putString(this,CERTIFICATER_KEY,bytesToHexString);
        } catch (Exception e) {
        }
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
     * desc:获取保存的Object对象
     * @param key
     * @return
     * modified:
     */
    public Object readCertificate(String key){
        try {
                String string = PreferencesByUserAndTanentUtils.getString(this,CERTIFICATER_KEY);
                if(TextUtils.isEmpty(string)){
                    return null;
                }else{
                    //将16进制的数据转为数组，准备反序列化
                    byte[] stringToBytes = StringToBytes(string);
                    ByteArrayInputStream bis=new ByteArrayInputStream(stringToBytes);
                    ObjectInputStream is=new ObjectInputStream(bis);
                    //返回反序列化得到的对象
                    Object readObject = is.readObject();
                    return readObject;
                }
        } catch (Exception e) {
        }
        //所有异常返回null
        return null;

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

    private void updataCertificateUI(MailCertificateDetail mailCertificateDetail){
         installedCerTitleTV.setText(StringUtils.isBlank( mailCertificateDetail.getCertificateName())?"未知":mailCertificateDetail.getCertificateName());
         String[] SubjectDN = mailCertificateDetail.getCertificateSubjectDN().split(",");
         String[] IssuerDN  = mailCertificateDetail.getCertificateIssuerDN().split(",");
         String IsUer    = getContentSpeStrData( IssuerDN,"CN=");
         String Subject  = getContentSpeStrData( SubjectDN,"CN=");
         certificateUseNameTV.setText(StringUtils.isBlank( Subject)?"未知":Subject.substring(3));
         certificateGiverIdTV.setText( StringUtils.isBlank( IsUer)?"未知":IsUer.substring( 3 ));
         certificateExpirtyDataTV.setText( StringUtils.isBlank(mailCertificateDetail.getCertificateFinalDate())?"未知":mailCertificateDetail.getCertificateFinalDate());
         secrityActionSwitch1.toggleSwitch(mailCertificateDetail.isEncryptedMail());
         secrityActionSwitch2.toggleSwitch(mailCertificateDetail.isSignedMail());
         if(View.VISIBLE!=installedCertificate.getVisibility()){
             installedCertificate.setVisibility(View.VISIBLE);
         }
        LogUtils.LbcDebug("Data"+mailCertificateDetail.isEncryptedMail()+"::"+mailCertificateDetail.isSignedMail());
    }

    private String getContentSpeStrData(String[] datas,String Spec){
        for (int i=0;i<datas.length;i++){
            if(datas[i].indexOf(Spec)>=0){
                return datas[i];
            }
        }
        return null;
    }
}
