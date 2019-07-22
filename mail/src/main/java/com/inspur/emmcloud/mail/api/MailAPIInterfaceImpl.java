package com.inspur.emmcloud.mail.api;

import com.inspur.emmcloud.mail.bean.GetMailDetailResult;
import com.inspur.emmcloud.mail.bean.GetMailFolderResult;
import com.inspur.emmcloud.mail.bean.GetMailListResult;

/**
 * Created by libaochao on 2019/7/22.
 */

public class MailAPIInterfaceImpl implements MailAPIInterface {
    @Override
    public void returnMailFolderSuccess(GetMailFolderResult getMailForderResult) {

    }

    @Override
    public void returnMailFolderFail(String error, int errorCode) {

    }

    @Override
    public void returnMailListSuccess(String folderId, int pageSize, int offset, GetMailListResult getMailListResult) {

    }

    @Override
    public void returnMailListFail(String folderId, int pageSize, int offset, String error, int errorCode) {

    }

    @Override
    public void returnMailDetailSuccess(GetMailDetailResult getMailDetailResult) {

    }

    @Override
    public void returnMailDetailSuccess(byte[] arg0) {

    }

    @Override
    public void returnMailDetailFail(String error, int errorCode) {

    }

    @Override
    public void returnMailLoginSuccess() {

    }

    @Override
    public void returnMailLoginFail(String error, int errorCode) {

    }

    @Override
    public void returnMailCertificateUploadSuccess(byte[] arg0) {

    }

    @Override
    public void returnMailCertificateUploadFail(String error, int errorCode) {

    }

    @Override
    public void returnSendMailSuccess() {

    }

    @Override
    public void returnSendMailFail(String error, int errorCode) {

    }

    @Override
    public void returnRemoveMailSuccess() {

    }

    @Override
    public void returnRemoveMailFail(String error, int errorCode) {

    }
}
