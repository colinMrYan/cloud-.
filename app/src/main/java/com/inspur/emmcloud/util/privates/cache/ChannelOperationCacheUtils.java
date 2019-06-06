package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.chat.ChannelOperationInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 频道列表缓存处理类
 *
 * @author Administrator
 */
public class ChannelOperationCacheUtils {

    /**
     * 设置是否被置顶
     *
     * @param context
     * @param cid
     * @param isChanelSetTop
     * @param setTopTime     被置顶的时间
     */
    public static void setChannelTop(Context context, String cid,
                                     boolean isChanelSetTop) {
        try {

            ChannelOperationInfo opInfo = DbCacheUtils.getDb(context).findById(
                    ChannelOperationInfo.class, cid);
            if (opInfo == null) {
                opInfo = new ChannelOperationInfo();
                opInfo.setCid(cid);
                opInfo.setIsSetTop(isChanelSetTop);
                if (isChanelSetTop) {
                    opInfo.setTopTime(System.currentTimeMillis());
                }
                DbCacheUtils.getDb(context).save(opInfo);
            } else {
                opInfo.setIsSetTop(isChanelSetTop);
                if (isChanelSetTop) {
                    opInfo.setTopTime(System.currentTimeMillis());
                }
                DbCacheUtils.getDb(context).update(opInfo,
                        "isSetTop",
                        "setTopTime");
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 判断此频道是否被置顶
     *
     * @param context
     * @param cid
     * @return
     */
    public static boolean isChannelSetTop(Context context, String cid) {
        boolean isChannelSetTop = false;
        try {

            ChannelOperationInfo opInfo = DbCacheUtils.getDb(context).findById(
                    ChannelOperationInfo.class, cid);
            if (opInfo != null) {
                isChannelSetTop = opInfo.getIsSetTop();
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return isChannelSetTop;

    }

    /**
     * 设置频道是否被隐藏
     *
     * @param context
     * @param cid
     * @param isChanelHide
     */
    public static void setChannelHide(Context context, String cid,
                                      boolean isChanelHide) {
        try {
            ChannelOperationInfo opInfo = DbCacheUtils.getDb(context).findById(
                    ChannelOperationInfo.class, cid);
            if (opInfo == null) {
                opInfo = new ChannelOperationInfo();
                opInfo.setCid(cid);
                opInfo.setIsHide(isChanelHide);
                DbCacheUtils.getDb(context).save(opInfo);
            } else {
                opInfo.setIsHide(isChanelHide);
                DbCacheUtils.getDb(context).update(opInfo,
                        "isHide");
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 获取置顶的频道操作信息列表
     *
     * @param context
     * @return
     */
    public static List<ChannelOperationInfo> getSetTopChannelOpList(
            Context context) {
        List<ChannelOperationInfo> setTopChannelOpList = null;
        try {
            setTopChannelOpList = DbCacheUtils.getDb(context).selector(ChannelOperationInfo.class)
                    .where("isSetTop", "=", true)
                    .and("isHide", "=", false)
                    .orderBy("setTopTime", true).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (setTopChannelOpList == null) {
            setTopChannelOpList = new ArrayList<>();
        }
        return setTopChannelOpList;
    }


    /**
     * 获取隐藏的频道操作信息列表
     *
     * @param context
     * @return
     */
    public static List<ChannelOperationInfo> getHideChannelOpList(
            Context context) {
        List<ChannelOperationInfo> hideChannelOpList = null;
        try {
            hideChannelOpList = DbCacheUtils.getDb(context).selector(ChannelOperationInfo.class).where("isHide",
                    "=", true).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (hideChannelOpList == null) {
            hideChannelOpList = new ArrayList<>();
        }
        return hideChannelOpList;
    }

}
