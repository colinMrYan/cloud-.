package com.inspur.emmcloud.ui.appcenter.mail;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.bean.appcenter.mail.MailCertificateDetail;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.EncryptUtils;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.imp.plugin.filetransfer.filemanager.FileManagerActivity;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import android.content.Intent;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2018/12/20.
 */
public class MailCertificateInstallActivity extends BaseActivity {
    public static final int SELECT_CREDIFICATE_FILE = 10;
    public static String CERTIFICATER_KEY = "certificate";
    @BindView(R.id.rl_installed_certificate)
    RelativeLayout installedCerLayout;
    @BindView(R.id.tv_cer_use_state)
    TextView cerUseStateText;
    @BindView(R.id.tv_installed_cer_title)
    TextView installedCerTitleText;
    @BindView(R.id.tv_installed_cer_owner_name)
    TextView cerOwnerNameText;
    @BindView(R.id.tv_installed_cer_issuer_name)
    TextView cerIssuerNameText;
    @BindView(R.id.tv_installed_cer_final_data)
    TextView cerFinalDataText;
    @BindView(R.id.switchview_encryption_action)
    SwitchView encryptionSwitchView;
    @BindView(R.id.switchview_signature_action)
    SwitchView signatureSwitchView;
    @BindView(R.id.tv_new_cer_title)
    TextView newCerTitleText;
    @BindView(R.id.tv_new_cer_ower_name)
    TextView newCerOwnerNameText;
    @BindView(R.id.tv_new_cer_issuer_name)
    TextView newCerIssuerNameText;
    @BindView(R.id.tv_new_cer_final_data)
    TextView newCerFinalDataText;
    private String certificatePassWord;
    private MailCertificateDetail myCertificate;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_mail_certificate_install;
    }

    /**
     * 初始化
     */
    private void init() {
        Object certificateObject = PreferencesByUsersUtils.getObject(this, CERTIFICATER_KEY);
        if (null == certificateObject) {
            myCertificate = new MailCertificateDetail();
        } else {
            myCertificate = (MailCertificateDetail) certificateObject;
            installedCerLayout.setVisibility(View.VISIBLE);
            updataCertificateUI(myCertificate);
        }

        encryptionSwitchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                myCertificate.setEncryptedMail(true);
                PreferencesByUsersUtils.putObject(MailCertificateInstallActivity.this, myCertificate, CERTIFICATER_KEY);
                encryptionSwitchView.setOpened(true);

            }

            @Override
            public void toggleToOff(View view) {
                myCertificate.setEncryptedMail(false);
                PreferencesByUsersUtils.putObject(MailCertificateInstallActivity.this, myCertificate, CERTIFICATER_KEY);
                encryptionSwitchView.setOpened(false);
            }
        });

        signatureSwitchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                myCertificate.setSignedMail(true);
                PreferencesByUsersUtils.putObject(MailCertificateInstallActivity.this, myCertificate, CERTIFICATER_KEY);
                signatureSwitchView.setOpened(true);
            }

            @Override
            public void toggleToOff(View view) {
                myCertificate.setSignedMail(false);
                PreferencesByUsersUtils.putObject(MailCertificateInstallActivity.this, myCertificate, CERTIFICATER_KEY);
                signatureSwitchView.setOpened(false);
            }
        });
    }

    /**
     * 点击事件
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_install_certificate:
                Intent intent = new Intent(this, FileManagerActivity.class);
                intent.putExtra(FileManagerActivity.EXTRA_MAXIMUM, 1);
                startActivityForResult(intent, SELECT_CREDIFICATE_FILE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_CREDIFICATE_FILE:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> pathList = data.getStringArrayListExtra("pathList");
                    LogUtils.LbcDebug("path" + pathList.get(0));
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
     *
     * @param path
     */
    private void showInputCreKeyWordDialog(final String path) {
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("证书密码：")
                .setPlaceholder("请在此输入证书密码：")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = builder.getEditText().getText();
                        String key = text.toString().trim();
                        if (getCertificate(path, key)) {
                            certificatePassWord = key;
                            String mail = ContactUserCacheUtils.getUserMail(MyApplication.getInstance().getUid());
                            uploadCertificateFile(mail, path, certificatePassWord);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getBaseContext(), "密码无效或证书有误，请重试", Toast.LENGTH_LONG).show();
                        }
                    }
                }).show();
    }

    /**
     * UpLoad File 上传证书文件
     *
     * @param mail
     * @param path
     * @param orgKey
     */
    private void uploadCertificateFile(String mail, String path, String orgKey) {
        try {
            String certificateBase64Data = FileUtils.encodeBase64File(path);
            String key = EncryptUtils.stringToMD5(mail);
            String iv = Constant.MAIL_ENCRYPT_IV;
            String cerBase64DataResult = EncryptUtils.encode(certificateBase64Data, key, iv, Base64.NO_WRAP);
            String KeyResult = EncryptUtils.encode(orgKey, key, iv, Base64.NO_WRAP);
            if (NetUtils.isNetworkConnected(this)) {
                MailApiService apiService = new MailApiService(this);
                apiService.setAPIInterface(new WebService());
                apiService.upLoadCertificateFile(mail, KeyResult, cerBase64DataResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取解析pfx
     *
     * @param passWord 密码
     * @param path     文件路径
     */
    private boolean getCertificate(String path, String passWord) {
        String strPfx = path;
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        LogUtils.LbcDebug("fileName:" + fileName);
        LogUtils.LbcDebug("path:" + strPfx);
        if (!StringUtils.isBlank(fileName)) {
            myCertificate.setCertificateName(fileName);
        }
        String strPassword = passWord;
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = null;
            fis = new FileInputStream(strPfx);
            char[] nPassword = null;
            if ((strPassword == null) || strPassword.trim().equals("")) {
                nPassword = null;
            } else {
                nPassword = strPassword.toCharArray();
            }
            ks.load(fis, nPassword);
            fis.close();
            Enumeration enumas = null;
            enumas = ks.aliases();
            String keyAlias = null;
            if (enumas.hasMoreElements()) { // we are readin just one certificate.
                keyAlias = (String) enumas.nextElement();
                LogUtils.LbcDebug("alias=[" + keyAlias + "]");
            }
            LogUtils.LbcDebug("5" + "is key entry=" + ks.isKeyEntry(keyAlias));
            PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, nPassword);
            Certificate cert = ks.getCertificate(keyAlias);
            try {
                String data = cert.toString();
                getCertificateContent(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            PublicKey pubkey = cert.getPublicKey();
            myCertificate.setCertificatePassword(passWord);
            myCertificate.setSignedMail(true);
            myCertificate.setEncryptedMail(true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取证书内容
     *
     * @param data 证书数据
     */
    private void getCertificateContent(String data) {
        int indexStartDate = data.indexOf("Start Date:");
        int endIndexStartDate = data.indexOf('\n', indexStartDate);

        int indexFinalDate = data.indexOf("Final Date:");
        int endIndexFinalDate = data.indexOf('\n', indexFinalDate);

        int indexIssuerDN = data.indexOf("IssuerDN:");
        int endIndexIssuerDN = data.indexOf('\n', indexIssuerDN);

        int indexSubjectDN = data.indexOf("SubjectDN:");
        int endIndexSubjectDN = data.indexOf('\n', indexSubjectDN);

        String startTime = data.substring(indexStartDate, endIndexStartDate);
        String finalTime = data.substring(indexFinalDate, endIndexFinalDate);
        String IssuerDN = data.substring(indexIssuerDN, endIndexIssuerDN);
        String SubjectDN = data.substring(indexSubjectDN, endIndexSubjectDN);
        myCertificate.setCertificateStartDate(startTime.substring(12));
        myCertificate.setCertificateFinalDate(finalTime.substring(12));
        myCertificate.setCertificateIssuerDN(IssuerDN.substring(9));
        myCertificate.setCertificateSubjectDN(SubjectDN.substring(10));
    }

    /**
     * 更新UI 数据
     *
     * @param mailCertificateDetail 更新UI数据源
     **/
    private void updataCertificateUI(MailCertificateDetail mailCertificateDetail) {
        installedCerTitleText.setText(StringUtils.isBlank(mailCertificateDetail.getCertificateName()) ? "未知" : mailCertificateDetail.getCertificateName());
        String[] SubjectDN = mailCertificateDetail.getCertificateSubjectDN().split(",");
        String[] IssuerDN = mailCertificateDetail.getCertificateIssuerDN().split(",");
        String IsUer = getContentSpeStrData(IssuerDN, "CN=");
        String Subject = getContentSpeStrData(SubjectDN, "CN=");
        cerOwnerNameText.setText(StringUtils.isBlank(Subject) ? "未知" : Subject.substring(3));
        cerIssuerNameText.setText(StringUtils.isBlank(IsUer) ? "未知" : IsUer.substring(3));
        cerFinalDataText.setText(StringUtils.isBlank(mailCertificateDetail.getCertificateFinalDate()) ? "未知" : mailCertificateDetail.getCertificateFinalDate());
        if (encryptionSwitchView.isOpened() != mailCertificateDetail.isEncryptedMail()) {
            encryptionSwitchView.setOpened(mailCertificateDetail.isEncryptedMail());
        }
        if (signatureSwitchView.isOpened() != mailCertificateDetail.isSignedMail()) {
            signatureSwitchView.setOpened(mailCertificateDetail.isSignedMail());
        }
        if (View.VISIBLE != installedCerLayout.getVisibility()) {
            installedCerLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取特定字符串
     *
     * @param datas 所有数据
     * @param Spec  要匹配数据
     **/
    private String getContentSpeStrData(String[] datas, String Spec) {
        for (int i = 0; i < datas.length; i++) {
            if (datas[i].indexOf(Spec) >= 0) {
                return datas[i];
            }
        }
        return null;
    }

    /**
     * 网络通信类
     */
    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMailCertificateUploadSuccess(byte[] arg0) {
            Toast.makeText(getBaseContext(), "证书安装成功", Toast.LENGTH_SHORT).show();
            PreferencesByUsersUtils.putObject(MailCertificateInstallActivity.this, myCertificate, CERTIFICATER_KEY);
            updataCertificateUI(myCertificate);
            super.returnMailCertificateUploadSuccess(arg0);
        }

        @Override
        public void returnMailCertificateUploadFail(String error, int errorCode) {
            Toast.makeText(getBaseContext(), "证书安装失败", Toast.LENGTH_SHORT).show();
            super.returnMailCertificateUploadFail(error, errorCode);
        }
    }
}
