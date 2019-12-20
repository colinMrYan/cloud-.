package com.inspur.emmcloud.application.util;

import android.content.Context;

import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.bean.AppCommonlyUse;
import com.inspur.emmcloud.application.bean.AppGroupBean;
import com.inspur.emmcloud.application.bean.AppOrder;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * classes : com.inspur.emmcloud.util.privates.db.AppCacheUtils Create at 2016年12月17日
 * 下午2:59:11
 */
public class AppCacheUtils {

    /**
     * 保存顺序
     *
     * @param context
     * @param appOrderList
     * @param categoryID
     */
    public static void saveAppOrderList(Context context,
                                        List<AppOrder> appOrderList, String categoryID) {
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(appOrderList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取所又AppOrder
     *
     * @param context
     */
    public static List<AppOrder> getAllAppOrderList(Context context) {
        List<AppOrder> appOrderList = new ArrayList<AppOrder>();
        try {
            appOrderList = DbCacheUtils.getDb(context).findAll(AppOrder.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (appOrderList == null) {
            appOrderList = new ArrayList<AppOrder>();
        }
        return appOrderList;
    }


    /**
     * 存储常用应用顺序
     */
    public static void saveAppCommonlyUseList(Context context, List<AppCommonlyUse> appCommonlyUseList) {
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(appCommonlyUseList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除常用app
     *
     * @param context
     */
    public static void deleteAppCommonlyByAppID(Context context, String appID) {
        try {
            DbCacheUtils.getDb(context).deleteById(AppCommonlyUse.class, appID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定数量的常用app
     *
     * @param context
     * @param commonlyUseAppNum
     * @return
     */
    public static List<AppCommonlyUse> getCommonlyUseList(Context context, int commonlyUseAppNum) {
        List<AppCommonlyUse> commonlyUseAppList = null;
        try {
            commonlyUseAppList = DbCacheUtils.getDb(context).selector(AppCommonlyUse.class)
                    .orderBy("lastUpdateTime", true).limit(commonlyUseAppNum).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (commonlyUseAppList == null) {
            commonlyUseAppList = new ArrayList<AppCommonlyUse>();
        }
        return commonlyUseAppList;
    }

    /**
     * 获取所有AppCommonLyUse
     *
     * @param context
     * @return
     */
    public static List<AppCommonlyUse> getCommonlyUseList(Context context) {
        List<AppCommonlyUse> commonlyUseAppList = null;
        try {
            commonlyUseAppList = DbCacheUtils.getDb(context).findAll(AppCommonlyUse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (commonlyUseAppList == null) {
            commonlyUseAppList = new ArrayList<AppCommonlyUse>();
        }
        return commonlyUseAppList;
    }

    public static List<AppCommonlyUse> getUploadCommonlyUseAppList(Context context) {
        try {
            List<AppCommonlyUse> orderCommonlyUseAppList = DbCacheUtils.getDb(context).selector(AppCommonlyUse.class).orderBy("weight", true).limit(10).findAll();
            if (orderCommonlyUseAppList == null) {
                orderCommonlyUseAppList = new ArrayList<>();
            }
            return orderCommonlyUseAppList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获取遍历缓存列表后的AppCommonLyUse
     *
     * @param context
     * @return
     */
    public static List<App> getCommonlyUseNeedShowList(Context context) {
        List<AppGroupBean> appGroupBeanList = MyAppCacheUtils.getMyAppList(context);
        List<AppCommonlyUse> appCommonlyUseList = null;
        List<App> appList = new ArrayList<>();
        try {
            appCommonlyUseList = DbCacheUtils.getDb(context).findAll(AppCommonlyUse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (appCommonlyUseList == null) {
            appCommonlyUseList = new ArrayList<AppCommonlyUse>();
        }
        for (int i = 0; i < appCommonlyUseList.size(); i++) {
            App app = new App();
            app.setAppID(appCommonlyUseList.get(i).getAppID());
            for (int j = 0; j < appGroupBeanList.size(); j++) {
                List<App> appItemList = appGroupBeanList.get(j).getAppItemList();
                int index = appItemList.indexOf(app);
                int allreadHas = appList.indexOf(app);
                if (index != -1 && allreadHas == -1) {
                    App appAdd = appItemList.get(index);
                    appAdd.setWeight(appCommonlyUseList.get(i).getWeight());
                    appList.add(appAdd);
                }
            }
        }
        return appList;
    }

}
