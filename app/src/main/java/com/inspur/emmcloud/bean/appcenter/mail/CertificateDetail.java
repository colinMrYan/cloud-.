package com.inspur.emmcloud.bean.appcenter.mail;

/**
 * Created by libaochao on 2018/12/24.
 * 包含证书信息，
 */

public class CertificateDetail {

    private String mCertificateName;    //证书文件名称
    private String mCertificateIssuerDN;//颁发者
    private String mCertificateSubjectDN;//颁发给
    private String mCertificatePassword; //证书密码
    private String mCertificateStartDate; //证书有效期Start
    private String mCertificateFinalDate;

    public  CertificateDetail(Object data) {
    }

    public String getmCertificateName() {
        return mCertificateName;
    }

    public void setmCertificateName(String mCertificateName) {
        this.mCertificateName = mCertificateName;
    }

    public String getmCertificateIssuerDN() {
        return mCertificateIssuerDN;
    }

    public void setmCertificateIssuerDN(String mCertificateIssuerDN) {
        this.mCertificateIssuerDN = mCertificateIssuerDN;
    }

    public String getmCertificateSubjectDN() {
        return mCertificateSubjectDN;
    }

    public void setmCertificateSubjectDN(String mCertificateSubjectDN) {
        this.mCertificateSubjectDN = mCertificateSubjectDN;
    }

    public String getmCertificatePassword() {
        return mCertificatePassword;
    }

    public void setmCertificatePassword(String mCertificatePassword) {
        this.mCertificatePassword = mCertificatePassword;
    }

    public String getmCertificateStartDate() {
        return mCertificateStartDate;
    }

    public void setmCertificateStartDate(String mCertificateStartDate) {
        this.mCertificateStartDate = mCertificateStartDate;
    }

    public String getmCertificateFinalDate() {
        return mCertificateFinalDate;
    }

    public void setmCertificateFinalDate(String mCertificateFinalDate) {
        this.mCertificateFinalDate = mCertificateFinalDate;
    }


}
