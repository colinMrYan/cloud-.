package com.inspur.emmcloud.baselib.util;

import android.util.Log;

/**
 * Log util
 */
public class LogUtils {
    public static boolean isDebug = true;

    public static void debug(String tag, String message) {
        if (isDebug) {
            Log.d(tag, getTraceInfo() + message);
        }
    }

    public static void jasonDebug(String message) {
        if (isDebug) {
            Log.d("jason", getTraceInfo() + message);
        }
    }

    public static void YfcDebug(String message) {
        if (isDebug) {
            Log.d("yfcLog", getTraceInfo() + message);
        }
    }

    public static void LbcDebug(String message) {
        if (isDebug) {
            Log.d("lbcLog", getTraceInfo() + message);
        }
    }

    public static void exceptionDebug(String tag, String message) {
        if (isDebug) {
            Log.e(tag, getTraceInfo() + message);
        }
    }

    public static String getTraceInfo() {
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        int stacksLen = stacks.length;
        if (stacksLen > 3) {
            sb.append(stacks[2].getFileName().split("\\.")[0])
                    .append("---line: ").append(stacks[2].getLineNumber())
                    .append(": ");
        }
        return sb.toString();
    }
}
