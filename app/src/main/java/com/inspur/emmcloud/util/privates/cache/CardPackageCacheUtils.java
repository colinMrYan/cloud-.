package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.mine.CardPackageBean;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/8/1.
 */

public class CardPackageCacheUtils {

    /**
     * 存储或更新List
     */
    public static void saveCardPackageList(Context context, List<CardPackageBean> cardPackageBeanList) {
        try {
            if (cardPackageBeanList == null || cardPackageBeanList.size() == 0) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(cardPackageBeanList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 存储单个CardPackage
     *
     * @param context
     * @param cardPackageBean
     */
    public static void saveCardPackage(Context context, CardPackageBean cardPackageBean) {
        try {
            if (cardPackageBean == null) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(cardPackageBean);
        } catch (Exception e) {
            LogUtils.YfcDebug("存储数据报错：" + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * 查询CardPackageList
     *
     * @param context
     * @return
     */
    public static List<CardPackageBean> getCardPackageList(Context context) {
        List<CardPackageBean> cardPackageBeanList = new ArrayList<CardPackageBean>();
        try {
            cardPackageBeanList = DbCacheUtils.getDb(context).selector(CardPackageBean.class).orderBy("id").findAll();
        } catch (Exception e) {
            LogUtils.YfcDebug("读取数据报错：" + e.getMessage());
            e.printStackTrace();
        }
        if (cardPackageBeanList == null) {
            cardPackageBeanList = new ArrayList<CardPackageBean>();
        }
        return cardPackageBeanList;
    }

    /**
     * 删除去除的CardPackage list
     *
     * @param context
     */
    public static void deleteCardPackageList(Context context) {
        try {
            DbCacheUtils.getDb(context).delete(CardPackageBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 同步选中状态
     *
     * @param cardPackageBeanCacheList
     * @param cardPackageBeanList
     */
    public static List<CardPackageBean> syncCardPackageStateList(List<CardPackageBean> cardPackageBeanCacheList, List<CardPackageBean> cardPackageBeanList) {
        if (cardPackageBeanCacheList != null && cardPackageBeanList != null) {
            for (int i = 0; i < cardPackageBeanCacheList.size(); i++) {
                int index = cardPackageBeanList.indexOf(cardPackageBeanCacheList.get(i));
                if (index != -1) {
                    cardPackageBeanList.get(index).setState(cardPackageBeanCacheList.get(i).getState());
                }
            }
        }
        return cardPackageBeanList;
    }

    /**
     * 查询选中的Card
     *
     * @return
     */
    public static List<CardPackageBean> getSelectedCardPackageList(Context context) {
        List<CardPackageBean> cardPackageBeanList = new ArrayList<>();
        try {
            cardPackageBeanList = DbCacheUtils.getDb(context).selector(CardPackageBean.class).where(WhereBuilder.b("state", "=", 1)).orderBy("id").findAll();
        } catch (Exception e) {
            LogUtils.YfcDebug("查询状态报错：" + e.getMessage());
            e.printStackTrace();
        }
        if (cardPackageBeanList == null) {
            cardPackageBeanList = new ArrayList<>();
        }
        return cardPackageBeanList;
    }
}
