package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.CloudHttpMethod;
import com.inspur.emmcloud.api.HttpUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailFolderResult;
import com.inspur.emmcloud.bean.appcenter.mail.GetMailListResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.privates.OauthUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;

import org.xutils.http.RequestParams;

import java.io.ByteArrayInputStream;

import static com.inspur.emmcloud.MyApplication.getInstance;

/**
 * Created by chenmch on 2018/12/24.
 */

public class MailApiService {
    private Context context;
    private APIInterface apiInterface;

    public MailApiService(Context context) {
        this.context = context;

    }

    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    public void getMailFolder() {
        String completeUrl = APIUri.getMailFolderUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
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
        String completeUrl = APIUri.getMailListUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("folderId", folderId);
        params.addQueryStringParameter("pageSize", pageSize + "");
        params.addQueryStringParameter("offset", offset + "");
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
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
        String completeUrl = APIUri.getMailDetailUrl(isEncrypted);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("mailId", mailId);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
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
        String completeUrl = APIUri.getLoginMailUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("Email", username);
        params.addParameter("Password", password);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
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
        final String Url = APIUri.getCertificateUrl();
        RequestParams params = getInstance().getHttpRequestParams(Url);
        params.addParameter("email", mail);
        params.addParameter("data0", certifivate);
        params.addParameter("data1", key);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, Url) {
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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 发送加密邮件
     */
    public void sendEncryptMail(final byte[] mailContent) {
        final String url = APIUri.getUploadMailUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        params.setMultipart(true);
        params.addQueryStringParameter("mail", PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT));
        params.addBodyParameter("file", new ByteArrayInputStream(mailContent), "application/octet-stream", "111");
//        String fileName = System.currentTimeMillis()+".aa";
//        String path = MyAppConfig.LOCAL_DOWNLOAD_PATH+fileName;
//        FileUtils.writeFile(new File(path),new ByteArrayInputStream(mailContent));
        // params.addBodyParameter("file",new File(path));
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, url) {
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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 删除邮件
     */
    public void removeMail(final String mailInfo) {
        final String url = APIUri.getRemoveMailUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        params.setBodyContent(mailInfo);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, url) {
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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }
}
