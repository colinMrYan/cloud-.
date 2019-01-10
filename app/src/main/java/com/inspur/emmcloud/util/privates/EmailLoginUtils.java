package com.inspur.emmcloud.util.privates;

/**
 * Created by libaochao on 2019/1/9.
 */

public class EmailLoginUtils {
//    private LoadingDialog loadingDlg;
//    private MailApiService apiService;
//    private Activity        activity;
//
//
//    public EmailLoginUtils(Activity activity){
//        this.activity = activity;
//        loadingDlg = new LoadingDialog(activity);
//        apiService = new MailApiService(activity);
//        apiService.setAPIInterface(new WebServie());
//    }
//
//    public void checkDefultLogin(){
//        String mail = PreferencesByUsersUtils.getString(activity,Constant.MAIL_LOG_ADDRESS,"");
//        String mailKey = PreferencesByUsersUtils.getString( activity,Constant.MAIL_LOG_KEY,"");
//        apiService.setAPIInterface(new WebServie());
//        if(!StringUtils.isBlank(mail)&&!StringUtils.isBlank(mailKey)){
//            login( mail,mailKey );
//        }else{
//            IntentUtils.startActivity(activity,MailLoginActivity.class );
//        }
//    }
//
//    private void login(String mail, String password) {
//        if (NetUtils.isNetworkConnected(activity)) {
//            String key = EncryptUtils.stringToMD5(mail);
//            try {
//                password = EncryptUtils.encode(password, key, Constant.MAIL_ENCRYPT_IV, Base64.NO_WRAP);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            loadingDlg = new LoadingDialog(activity);
//            loadingDlg.show();
//            apiService.loginMail(mail, password);
//        }
//    }
//
//
//    private class WebServie extends APIInterfaceInstance {
//        @Override
//        public void returnMailLoginSuccess() {
//            LoadingDialog.dimissDlg(loadingDlg);
//            IntentUtils.startActivity(activity,MailHomeActivity.class);
//        }
//
//        @Override
//        public void returnMailLoginFail(String error, int errorCode) {
//            LoadingDialog.dimissDlg(loadingDlg);
//            IntentUtils.startActivity(activity,MailLoginActivity.class);
//
//        }
//    }
}
