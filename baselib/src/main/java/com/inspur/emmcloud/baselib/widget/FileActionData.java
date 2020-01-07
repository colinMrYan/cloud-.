package com.inspur.emmcloud.baselib.widget;

/**
 * Created by libaochao on 2019/9/23.
 */

public class FileActionData {
    private String actionName;
    private int actionIc;
    private boolean isShow = false;

    public FileActionData() {
    }

    public FileActionData(String actionName, int actionIc, boolean isShow) {
        this.actionName = actionName;
        this.actionIc = actionIc;
        this.isShow = isShow;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public int getActionIc() {
        return actionIc;
    }

    public void setActionIc(int actionIc) {
        this.actionIc = actionIc;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
