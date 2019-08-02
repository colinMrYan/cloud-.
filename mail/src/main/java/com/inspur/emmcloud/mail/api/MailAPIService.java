package com.inspur.emmcloud.mail.api;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.mail.bean.GetMailFolderResult;
import com.inspur.emmcloud.mail.bean.GetMailListResult;

import org.xutils.http.RequestParams;

import java.io.ByteArrayInputStream;

/**
 * Created by libaochao on 2019/7/22.
 */

public class MailAPIService {
    private Context context;
    private MailAPIInterface apiInterface;

    public MailAPIService(Context context) {
        this.context = context;

    }

    public void setAPIInterface(MailAPIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    private void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.refreshToken(oauthCallBack, requestTime);
        }
    }

    public void getMailFolder() {
        String completeUrl = MailAPIUri.getMailFolderUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMailFolder();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMailFolderSuccess(new GetMailFolderResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMailFolderFail(error, responseCode);

            }
        });
    }

    public void getMailList(final String folderId, final int pageSize, final int offset) {
        String completeUrl = MailAPIUri.getMailListUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("folderId", folderId);
        params.addQueryStringParameter("pageSize", pageSize + "");
        params.addQueryStringParameter("offset", offset + "");
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMailList(folderId, pageSize, offset);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMailListSuccess(folderId, pageSize, offset, new GetMailListResult(new String(arg0), folderId));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMailListFail(folderId, pageSize, offset, error, responseCode);

            }
        });
    }

    public void getMailDetail(final String mailId, final boolean isEncrypted) {
        String completeUrl = MailAPIUri.getMailDetailUrl(isEncrypted);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("mailId", mailId);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMailDetail(mailId, isEncrypted);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMailDetailSuccess(arg0);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMailDetailFail(error, responseCode);

            }
        });
    }

    public void loginMail(final String username, final String password) {
        String completeUrl = MailAPIUri.getLoginMailUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addParameter("Email", username);
        params.addParameter("Password", password);
        params.setAsJsonContent(true);
        LogUtils.LbcDebug("completeUrl" + completeUrl + "username" + username + "password" + password);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        loginMail(username, password);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMailLoginSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMailLoginFail(error, responseCode);

            }
        });
    }

    /**
     * @param mail        邮箱
     * @param certifivate 加密后证书文件
     * @param key         加密后证书密码
     */
    public void upLoadCertificateFile(final String mail, final String key, final String certifivate) {
        final String Url = MailAPIUri.getCertificateUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(Url);
        params.addParameter("email", mail);
        params.addParameter("data0", certifivate);
        params.addParameter("data1", key);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, Url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnMailCertificateUploadSuccess(arg0);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnMailCertificateUploadFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        upLoadCertificateFile(mail, key, certifivate);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 发送加密邮件
     */
    public void sendEncryptMail(final byte[] mailContent) {
        final String url = MailAPIUri.getUploadMailUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.setMultipart(true);
        params.addQueryStringParameter("mail", PreferencesByUsersUtils.getString(BaseApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT));
        params.addBodyParameter("file", new ByteArrayInputStream(mailContent), "application/octet-stream", "111");
//        String fileName = System.currentTimeMillis()+".aa";
//        String path = MyAppConfig.LOCAL_DOWNLOAD_PATH+fileName;
//        FileUtils.writeFile(new File(path),new ByteArrayInputStream(mailContent));
        // params.addBodyParameter("file",new File(path));
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnSendMailSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnSendMailFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        sendEncryptMail(mailContent);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 删除邮件
     */
    public void removeMail(final String mailInfo) {
        final String url = MailAPIUri.getRemoveMailUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.setBodyContent(mailInfo);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnRemoveMailSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRemoveMailFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        removeMail(mailInfo);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }
}
