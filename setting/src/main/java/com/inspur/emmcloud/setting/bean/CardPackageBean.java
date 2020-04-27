package com.inspur.emmcloud.setting.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by yufuchang on 2018/8/1.
 */
@Table(name = "CardPackageBean")
public class CardPackageBean {

    /**
     * id : 2
     * company : 浪潮集团有限公司
     * taxpayer : 913700001630477270
     * address :  济南市高新区浪潮路1036号
     * phone : 0531-85106250
     * bank : 工商银行济南山大路支行
     * bankAccount : 1602003109004609977
     * color : #579EEF
     * "barcodeUrl":"https://ecmcloud.oss-cn-beijing.aliyuncs.com/apps/tax_images/cnhfhk.png"
     */
    @Column(name = "id", isId = true)
    private String id;
    @Column(name = "company")
    private String company;
    @Column(name = "taxpayer")
    private String taxpayer;
    @Column(name = "address")
    private String address;
    @Column(name = "phone")
    private String phone;
    @Column(name = "bank")
    private String bank;
    @Column(name = "bankAccount")
    private String bankAccount;
    @Column(name = "color")
    private String color;
    @Column(name = "state")
    private int state = 0;
    @Column(name = "barcodeUrl")
    private String barcodeUrl;

    public CardPackageBean() {
    }

    public CardPackageBean(JSONObject jsonObject) {
        this.id = JSONUtils.getString(jsonObject, "id", "");
        this.company = JSONUtils.getString(jsonObject, "company", "");
        this.taxpayer = JSONUtils.getString(jsonObject, "taxpayer", "");
        this.address = JSONUtils.getString(jsonObject, "address", "");
        this.phone = JSONUtils.getString(jsonObject, "phone", "");
        this.bank = JSONUtils.getString(jsonObject, "bank", "");
        this.bankAccount = JSONUtils.getString(jsonObject, "bankAccount", "");
        this.color = JSONUtils.getString(jsonObject, "color", "");
        this.barcodeUrl = JSONUtils.getString(jsonObject,"barcodeUrl","");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTaxpayer() {
        return taxpayer;
    }

    public void setTaxpayer(String taxpayer) {
        this.taxpayer = taxpayer;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getBarcodeUrl() {
        return barcodeUrl;
    }

    public void setBarcodeUrl(String barcodeUrl) {
        this.barcodeUrl = barcodeUrl;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof CardPackageBean)) {
            return false;
        }
        CardPackageBean cardPackageBean = (CardPackageBean) other;
        //此处从==判断是否相等  改为equals
        return getId().equals(cardPackageBean.getId());
    }

}
