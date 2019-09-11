package com.inspur.emmcloud.mail.api;

import com.inspur.emmcloud.mail.bean.GetMailDetailResult;
import com.inspur.emmcloud.mail.bean.GetMailFolderResult;
import com.inspur.emmcloud.mail.bean.GetMailListResult;

/**
 * Created by libaochao on 2019/7/22.
 */

public interface MailAPIInterface {
    void returnMailFolderSuccess(GetMailFolderResult getMailForderResult);

    void returnMailFolderFail(String error, int errorCode);

    void returnMailListSuccess(String folderId, int pageSize, int offset, GetMailListResult getMailListResult);

    void returnMailListFail(String folderId, int pageSize, int offset, String error, int errorCode);

    void returnMailDetailSuccess(GetMailDetailResult getMailDetailResult);

    void returnMailDetailSuccess(byte[] arg0);

    void returnMailDetailFail(String error, int errorCode);

    void returnMailLoginSuccess();

    void returnMailLoginFail(String error, int errorCode);

    void returnMailCertificateUploadSuccess(byte[] arg0);

    void returnMailCertificateUploadFail(String error, int errorCode);

    void returnSendMailSuccess();

    void returnSendMailFail(String error, int errorCode);

    void returnRemoveMailSuccess();

    void returnRemoveMailFail(String error, int errorCode);
}
