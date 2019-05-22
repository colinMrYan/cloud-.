package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.chat.MarkDownLink;
import com.inspur.emmcloud.util.common.LogUtils;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/5/22.
 */

public class MarkDownLinkCacheUtils {


    /**
     * 删除MarkDownLink 数据库表
     *
     * @param context
     */
    public static void deleteAllMarkDownLink(Context context) {
        try {
            DbCacheUtils.getDb(context).delete(MarkDownLink.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 存储单条MarkDownLink信息 （点击保存）
     *
     * @param context
     * @param markDownLink
     */
    public static void saveMarkDownLink(Context context, MarkDownLink markDownLink) {
        try {
            if (markDownLink == null) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(markDownLink);
        } catch (Exception e) {
            // TODO: handle
            LogUtils.jasonDebug("存储 makdown Error");
            e.printStackTrace();
        }
    }


    /**
     * 获取缓存中的MarkDownList
     *
     * @param context
     * @return
     */
    public static List<MarkDownLink> getMarkDownLinkList(Context context, String mid,String url) {
        List<MarkDownLink> markDownLinks = null;
        try {
          markDownLinks = DbCacheUtils.getDb(context).selector(MarkDownLink.class).where("mid","=",mid).findAll();
           // markDownLinks = DbCacheUtils.getDb(context).findAll(MarkDownLink.class);
        } catch (Exception e) {
            // TODO: handle exception
            LogUtils.jasonDebug("获取makdown 列表 Error");
            e.printStackTrace();
        }
        if (markDownLinks == null) {
            markDownLinks = new ArrayList<>();
        }
        return markDownLinks;
    }


    /**
     * 删除单条MarkDownLink 信息
     *
     * @param context
     * @param id
     */
    public static void deleteMarkDownLink(Context context, String id) {
        try {
            DbCacheUtils.getDb(context).delete(MarkDownLink.class, WhereBuilder.b("id", "=", id));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
