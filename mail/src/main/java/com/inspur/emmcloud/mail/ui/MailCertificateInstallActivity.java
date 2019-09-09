package com.inspur.emmcloud.mail.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.EncryptUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.R2;
import com.inspur.emmcloud.mail.api.MailAPIInterfaceImpl;
import com.inspur.emmcloud.mail.api.MailAPIService;
import com.inspur.emmcloud.mail.bean.MailCertificateDetail;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2018/12/20.
 */
public class MailCertificateInstallActivity extends BaseActivity {
    public static final int SELECT_CREDIFICATE_FILE = 10;
    public static String CERTIFICATER_KEY = "certificate";
    @BindView(R2.id.rl_installed_certificate)
    RelativeLayout installedCerLayout;
    @BindView(R2.id.tv_cer_use_state)
    TextView cerUseStateText;
    @BindView(R2.id.tv_installed_cer_title)
    TextView installedCerTitleText;
    @BindView(R2.id.tv_installed_cer_owner_name)
    TextView cerOwnerNameText;
    @BindView(R2.id.tv_installed_cer_issuer_name)
    TextView cerIssuerNameText;
    @BindView(R2.id.tv_installed_cer_final_data)
    TextView cerFinalDataText;
    @BindView(R2.id.sv_encryption_action)
    SwitchCompat encryptionSwitchView;
    @BindView(R2.id.sv_signature_action)
    SwitchCompat signatureSwitchView;
    @BindView(R2.id.tv_new_cer_title)
    TextView newCerTitleText;
    @BindView(R2.id.tv_new_cer_ower_name)
    TextView newCerOwnerNameText;
    @BindView(R2.id.tv_new_cer_issuer_name)
    TextView newCerIssuerNameText;
    @BindView(R2.id.tv_new_cer_final_data)
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
        return R.layout.mail_certificate_install_activity;
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

        encryptionSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                myCertificate.setEncryptedMail(b);
                PreferencesByUsersUtils.putObject(MailCertificateInstallActivity.this, myCertificate, CERTIFICATER_KEY);
            }
        });

        signatureSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                myCertificate.setSignedMail(b);
                PreferencesByUsersUtils.putObject(MailCertificateInstallActivity.this, myCertificate, CERTIFICATER_KEY);
            }
        });

    }

    /**
     * 点击事件
     */
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.tv_install_certificate) {
            Bundle bundle = new Bundle();
            bundle.putInt("extra_maximum", 1);
            ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_FILEMANAGER).with(bundle).navigation(MailCertificateInstallActivity.this, SELECT_CREDIFICATE_FILE);

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
                    ToastUtils.show(getBaseContext(), "选取文件失败");
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
        final CustomDialog.EditDialogBuilder builder = new CustomDialog.EditDialogBuilder(this);
        View editLayout = View.inflate(this, R.layout.cus_dialog_edit, null);
        final EditText editText = editLayout.findViewById(R.id.cus_dialog_edit_text);
        editText.setHint("请在此输入证书密码：");
        editText.setTextSize(16);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setTitle("证书密码：")
                .setView(editLayout)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CharSequence text = editText.getText();
                        String key = text.toString().trim();
                        if (getCertificate(path, key)) {
                            certificatePassWord = key;
                            ContactUser contactUser = null;
                            Router router = Router.getInstance();
                            if (router.getService(ContactService.class) != null) {
                                ContactService service = router.getService(ContactService.class);
                                contactUser = service.getContactUserByUid(BaseApplication.getInstance().getUid());
                            }
                            String mail = contactUser.getEmail();
                            uploadCertificateFile(mail, path, certificatePassWord);
                            dialog.dismiss();
                        } else {
                            ToastUtils.show(getBaseContext(), "密码无效或证书有误，请重试");
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
                MailAPIService apiService = new MailAPIService(this);
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
        if (encryptionSwitchView.isChecked() != mailCertificateDetail.isEncryptedMail()) {
            encryptionSwitchView.setChecked(mailCertificateDetail.isEncryptedMail());
        }
        if (signatureSwitchView.isChecked() != mailCertificateDetail.isSignedMail()) {
            signatureSwitchView.setChecked(mailCertificateDetail.isSignedMail());
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
    private class WebService extends MailAPIInterfaceImpl {
        @Override
        public void returnMailCertificateUploadSuccess(byte[] arg0) {
            ToastUtils.show(getBaseContext(), "证书安装成功");
            PreferencesByUsersUtils.putObject(MailCertificateInstallActivity.this, myCertificate, CERTIFICATER_KEY);
            updataCertificateUI(myCertificate);
            super.returnMailCertificateUploadSuccess(arg0);
        }

        @Override
        public void returnMailCertificateUploadFail(String error, int errorCode) {
            ToastUtils.show(getBaseContext(), "证书安装失败");
            super.returnMailCertificateUploadFail(error, errorCode);
        }
    }
}
