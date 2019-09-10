package com.inspur.emmcloud.mail.bean;

import java.io.Serializable;

/**
 * Created by libaochao on 2018/12/29.
 */

public class MailCertificateDetail implements Serializable {

    private static final long serialVersionUID = 1232612312342333L;
    private String certificateName;     //证书文件名称
    private String certificateIssuerDN; //颁发者
    private String certificateSubjectDN;//颁发给
    private String certificatePassword; //证书密码
    private String certificateStartDate;//证书有效期Start
    private String certificateFinalDate;//证书截止日期
    private String certificatePublicKey;//证书公钥
    private String certificatePrivateKey;//证书私钥

    private boolean isEncryptedMail;
    private boolean isSignedMail;

    public MailCertificateDetail(String cName, String cIssuerDN, String cSubjectDN, String cPassword, String cStartTime, String cFinalTime, String pubKey, String priKey) {
        certificateName = cName;
        certificateIssuerDN = cIssuerDN;
        certificateSubjectDN = cSubjectDN;
        certificatePassword = cPassword;
        certificateStartDate = cStartTime;
        certificateFinalDate = cFinalTime;
        certificatePublicKey = pubKey;
        certificatePrivateKey = priKey;
        isEncryptedMail = true;
        isSignedMail = true;
    }

    public MailCertificateDetail() {
        isEncryptedMail = true;
        isSignedMail = true;
        certificateName = "";     //证书文件名称
        certificateIssuerDN = ""; //颁发者
        certificateSubjectDN = "";//颁发给
        certificatePassword = ""; //证书密码
        certificateStartDate = "";//证书有效期Start
        certificateFinalDate = "";//证书截止日期
        certificatePublicKey = "";//证书公钥
        certificatePrivateKey = "";//证书私钥
    }

    public String getCertificateName() {
        return certificateName;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    public String getCertificateIssuerDN() {
        return certificateIssuerDN;
    }

    public void setCertificateIssuerDN(String certificateIssuerDN) {
        this.certificateIssuerDN = certificateIssuerDN;
    }

    public String getCertificateSubjectDN() {
        return certificateSubjectDN;
    }

    public void setCertificateSubjectDN(String certificateSubjectDN) {
        this.certificateSubjectDN = certificateSubjectDN;
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }

    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    public String getCertificateStartDate() {
        return certificateStartDate;
    }

    public void setCertificateStartDate(String certificateStartDate) {
        this.certificateStartDate = certificateStartDate;
    }

    public String getCertificateFinalDate() {
        return certificateFinalDate;
    }

    public void setCertificateFinalDate(String certificateFinalDate) {
        this.certificateFinalDate = certificateFinalDate;
    }

    public String getCertificatePublicKey() {
        return certificatePublicKey;
    }

    public void setCertificatePublicKey(String certificatePublicKey) {
        this.certificatePublicKey = certificatePublicKey;
    }

    public String getCertificatePrivateKey() {
        return certificatePrivateKey;
    }

    public void setCertificatePrivateKey(String certificatePrivateKey) {
        this.certificatePrivateKey = certificatePrivateKey;
    }

    public boolean isEncryptedMail() {
        return isEncryptedMail;
    }

    public void setEncryptedMail(boolean encryptedMail) {
        this.isEncryptedMail = encryptedMail;
    }

    public boolean isSignedMail() {
        return isSignedMail;
    }

    public void setSignedMail(boolean signedMail) {
        this.isSignedMail = signedMail;
    }

}
