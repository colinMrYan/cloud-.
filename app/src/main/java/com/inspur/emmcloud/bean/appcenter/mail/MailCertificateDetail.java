package com.inspur.emmcloud.bean.appcenter.mail;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by libaochao on 2018/12/29.
 */

public class MailCertificateDetail implements Parcelable {

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



    protected MailCertificateDetail(Parcel in) {
         certificateName=in.readString();
         certificateIssuerDN =in.readString();
         certificateSubjectDN=in.readString();
        certificatePassword  = in.readString();
         certificateStartDate=in.readString();
         certificateFinalDate=in.readString();
         certificatePublicKey=in.readString();
         certificatePrivateKey=in.readString();
         isEncryptedMail=in.readInt()==1;
         isSignedMail   =in.readInt()==1;
    }

    public MailCertificateDetail(String cName,String cIssuerDN,String cSubjectDN,String cPassword,String cStartTime,String cFinalTime,String pubKey,String priKey){
        certificateName=cName;
        certificateIssuerDN=cIssuerDN;
        certificateSubjectDN=cSubjectDN;
        certificatePassword  =cPassword ;
        certificateStartDate=cStartTime;
        certificateFinalDate=cFinalTime;
        certificatePublicKey=pubKey;
        certificatePrivateKey=priKey;
    }

    public MailCertificateDetail(){
        isEncryptedMail=true;
        isSignedMail=true;
    }

    public static final Creator<MailCertificateDetail> CREATOR = new Creator<MailCertificateDetail>() {
        @Override
        public MailCertificateDetail createFromParcel(Parcel in) {
            return new MailCertificateDetail( in );
        }

        @Override
        public MailCertificateDetail[] newArray(int size) {
            return new MailCertificateDetail[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString( certificateName );
        dest.writeString( certificateIssuerDN );
        dest.writeString( certificateSubjectDN );
        dest.writeString( certificatePassword );
        dest.writeString( certificateStartDate );
        dest.writeString( certificateFinalDate );
        dest.writeString( certificatePublicKey );
        dest.writeString( certificatePrivateKey );
        dest.writeInt( isEncryptedMail?1:0 );
        dest.writeInt( isSignedMail?1:0 );
    }
}
