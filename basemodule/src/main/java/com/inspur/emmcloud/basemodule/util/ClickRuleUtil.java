package com.inspur.emmcloud.basemodule.util;

public class ClickRuleUtil {
    private static long MIN_CLICK_INTERVAL_TIME = 500;
    private static long lastClickTime;

    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < MIN_CLICK_INTERVAL_TIME) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
