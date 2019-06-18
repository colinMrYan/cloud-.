package com.inspur.emmcloud.web.ui;

import android.util.Log;

/**
 * android端日志开关
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class iLog {

    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;

    //当前的Log级别
    public static int LOGLEVEL = Log.ERROR;

    /**
     * 通过整数设置log类型
     *
     * @param logLevel log级别
     */
    public static void setLogLevel(int logLevel) {
        LOGLEVEL = logLevel;
    }

    /**
     * 通过字符串匹配设置log级别
     *
     * @param logLevel 级别字符串参数
     */
    public static void setLogLevel(String logLevel) {
        if ("VERBOSE".equals(logLevel)) LOGLEVEL = VERBOSE;
        else if ("DEBUG".equals(logLevel)) LOGLEVEL = DEBUG;
        else if ("INFO".equals(logLevel)) LOGLEVEL = INFO;
        else if ("WARN".equals(logLevel)) LOGLEVEL = WARN;
        else if ("ERROR".equals(logLevel)) LOGLEVEL = ERROR;
    }

    /**
     * 判断当前log是否会输出
     *
     * @param logLevel
     * @return
     */
    public static boolean isLoggable(int logLevel) {
        return (logLevel >= LOGLEVEL);
    }

    /**
     * Verbose log message.
     *
     * @param tag
     * @param s
     */
    public static void v(String tag, String s) {
        if (iLog.VERBOSE >= LOGLEVEL) Log.v(tag, s);
    }

    /**
     * Debug模式
     *
     * @param tag
     * @param s
     */
    public static void d(String tag, String s) {
        if (iLog.DEBUG >= LOGLEVEL) Log.d(tag, s);
    }

    /**
     * Info模式
     *
     * @param tag
     * @param s
     */
    public static void i(String tag, String s) {
        if (iLog.INFO >= LOGLEVEL) Log.i(tag, s);
    }

    /**
     * Warning模式
     *
     * @param tag
     * @param s
     */
    public static void w(String tag, String s) {
        if (iLog.WARN >= LOGLEVEL) Log.w(tag, s);
    }

    /**
     * Error模式
     *
     * @param tag
     * @param s
     */
    public static void e(String tag, String s) {
        if (iLog.ERROR >= LOGLEVEL) Log.e(tag, s);
    }

    /**
     * Verbose模式
     *
     * @param tag
     * @param s
     * @param e
     */
    public static void v(String tag, String s, Throwable e) {
        if (iLog.VERBOSE >= LOGLEVEL) Log.v(tag, s, e);
    }

    /**
     * Debug模式
     *
     * @param tag
     * @param s
     * @param e
     */
    public static void d(String tag, String s, Throwable e) {
        if (iLog.DEBUG >= LOGLEVEL) Log.d(tag, s, e);
    }

    /**
     * Info模式
     *
     * @param tag
     * @param s
     * @param e
     */
    public static void i(String tag, String s, Throwable e) {
        if (iLog.INFO >= LOGLEVEL) Log.i(tag, s, e);
    }

    /**
     * Warning模式
     *
     * @param tag
     * @param s
     * @param e
     */
    public static void w(String tag, String s, Throwable e) {
        if (iLog.WARN >= LOGLEVEL) Log.w(tag, s, e);
    }

    /**
     * Error模式
     *
     * @param tag
     * @param s
     * @param e
     */
    public static void e(String tag, String s, Throwable e) {
        if (iLog.ERROR >= LOGLEVEL) Log.e(tag, s, e);
    }

    /**
     * Verbose模式
     *
     * @param tag
     * @param s
     * @param args
     */
    public static void v(String tag, String s, Object... args) {
        if (iLog.VERBOSE >= LOGLEVEL) Log.v(tag, String.format(s, args));
    }

    /**
     * Debug 模式
     *
     * @param tag
     * @param s
     * @param args
     */
    public static void d(String tag, String s, Object... args) {
        if (iLog.DEBUG >= LOGLEVEL) Log.d(tag, String.format(s, args));
    }

    /**
     * Info 模式
     *
     * @param tag
     * @param s
     * @param args
     */
    public static void i(String tag, String s, Object... args) {
        if (iLog.INFO >= LOGLEVEL) Log.i(tag, String.format(s, args));
    }

    /**
     * Warning 模式
     *
     * @param tag
     * @param s
     * @param args
     */
    public static void w(String tag, String s, Object... args) {
        if (iLog.WARN >= LOGLEVEL) Log.w(tag, String.format(s, args));
    }

    /**
     * Error 模式
     *
     * @param tag
     * @param s
     * @param args
     */
    public static void e(String tag, String s, Object... args) {
        if (iLog.ERROR >= LOGLEVEL) Log.e(tag, String.format(s, args));
    }

}
