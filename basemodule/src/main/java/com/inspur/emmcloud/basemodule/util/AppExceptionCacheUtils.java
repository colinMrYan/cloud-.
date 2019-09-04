package com.inspur.emmcloud.basemodule.util;

import android.content.Context;

import com.inspur.emmcloud.basemodule.bean.AppException;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 频道列表缓存处理类
 *
 * @author Administrator
 */
public class AppExceptionCacheUtils {


    /**
     * 存储异常信息
     *
     * @param context
     * @param errorLevel
     */
    public static void saveAppException(final Context context, int errorLevel, String url, String error, int errorCode) {
        // TODO Auto-generated method stub
        try {
            if (!AppUtils.isApkDebugable(context)) {
                AppException appException = new AppException(System.currentTimeMillis(), AppUtils.getVersion(context), errorLevel, url, error, errorCode);
                DbCacheUtils.getDb(context).saveOrUpdate(appException);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 存储路由表
     *
     * @param context
     * @param errorLevel
     * @param url
     * @param error
     * @param errorCode
     */
    public static void saveAppClusterException(final Context context, int errorLevel, String url, String error, int errorCode) {
        if (!AppUtils.isApkDebugable(context)) {
            List<AppException> appExceptionList = getAppExceptionList(context);
            Iterator<AppException> appExceptionIterator = appExceptionList.iterator();
            while (appExceptionIterator.hasNext()) {
                AppException appException = appExceptionIterator.next();
                if (appException.getErrorInfo().equals("clusters")) {
                    appException.setHappenTime(System.currentTimeMillis());
                    appException.setAppVersion(AppUtils.getVersion(context));
                    appException.setErrorCode(errorCode);
                    appException.setErrorInfo(error);
                    appException.setErrorLevel(errorLevel);
                    appException.setErrorUrl(url);
                    try {
                        DbCacheUtils.getDb(context).saveOrUpdate(appException);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * 获取异常list
     *
     * @param context
     * @return
     */
    public static List<AppException> getAppExceptionList(final Context context) {
        List<AppException> appExceptionList = new ArrayList<>();
        try {
            appExceptionList = DbCacheUtils.getDb(context).findAll(AppException.class);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (appExceptionList == null) {
            appExceptionList = new ArrayList<>();
        }
        return appExceptionList;
    }


    /**
     * 获取异常list
     *
     * @param context
     * @param maxUpLoadItemsNum 一次上传最大数量
     * @return
     */
    public static List<AppException> getAppExceptionList(final Context context, int maxUpLoadItemsNum) {
        List<AppException> appExceptionList = new ArrayList<>();
        try {
            appExceptionList = DbCacheUtils.getDb(context).selector(AppException.class).limit(maxUpLoadItemsNum).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (appExceptionList == null) {
            appExceptionList = new ArrayList<>();
        }
        return appExceptionList;
    }

    /**
     * 获取指定级别的异常列表
     *
     * @param context
     * @param errorLevel
     * @return
     */
    public static AppException getAppExceptionListByLevel(final Context context, int errorLevel) {
        AppException appException = null;
        try {
            appException = DbCacheUtils.getDb(context).selector(AppException.class).where
                    (WhereBuilder.b("ErrorLevel", "=", errorLevel))
                    .findFirst();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (appException == null) {
            appException = new AppException();
        }
        return appException;
    }


    /**
     * 清除AppException表信息
     *
     * @param context
     */
    public static void clearAppException(Context context) {
        try {
            DbCacheUtils.getDb(context).delete(AppException.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 清除AppException表信息
     *
     * @param context
     */
    public static void deleteAppException(Context context, List<AppException> appExceptionList) {
        try {
            DbCacheUtils.getDb(context).delete(appExceptionList);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 删除AppException
     *
     * @param context
     */
    public static void deleteAppException(Context context, AppException appException) {
        try {
            DbCacheUtils.getDb(context).delete(appException);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
