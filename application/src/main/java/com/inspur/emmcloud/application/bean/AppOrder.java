package com.inspur.emmcloud.application.bean;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * classes : com.inspur.emmcloud.bean.appcenter.AppOrder
 * Create at 2016年12月17日 下午2:53:19
 */
@Table(name = "AppOrder")
public class AppOrder {

    @Column(name = "appID", isId = true)
    private String appID = "";
    @Column(name = "orderId")
    private String orderId = "";
    @Column(name = "categoryID")
    private String categoryID = "";

    public AppOrder() {
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof AppOrder)) {
            return false;
        }
        AppOrder appOrder = (AppOrder) other;
        return getAppID().equals(appOrder.getAppID());
    }
}
 