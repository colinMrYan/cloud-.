package com.inspur.emmcloud.bean.appcenter.mail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/1/10.
 */

public class RemoveItemModel {

    public static int DELECTE_FROM_PHY=0;
    public static int DELECTE_TO_SPAM_FOLDER=1;
    public static int DELECTE_TO_DELECTEITEMS=2;
    private String Email="";
    private List<String> ItemIds=new ArrayList<>();
    private   int   DeleteMode=DELECTE_TO_DELECTEITEMS;

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public List<String> getItemIds() {
        return ItemIds;
    }

    public void setItemIds(List<String> itemIds) {
        ItemIds = itemIds;
    }

    public int getDeleteMode() {
        return DeleteMode;
    }

    public void setDeleteMode(int deleteMode) {
        DeleteMode = deleteMode;
    }
}
