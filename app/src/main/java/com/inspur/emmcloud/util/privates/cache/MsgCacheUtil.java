package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.bean.chat.Msg;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息缓存处理类
 *
 * @author Administrator
 */
public class MsgCacheUtil {

    /**
     * @param context
     * @param msg
     */
    public static void saveMsg(final Context context, final Msg msg) {
        try {

            DbCacheUtils.getDb(context).saveOrUpdate(msg); // 存储消息
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param context
     * @param msgList
     * @param targetMsgId
     */
    public static void saveMsgList(final Context context,
                                   final List<Msg> msgList, final Long targetMsgCreationDate) {


        // TODO Auto-generated method stub
        try {
            if (msgList == null || msgList.size() == 0) {
                return;
            }

            DbCacheUtils.getDb(context).saveOrUpdate(msgList);
            MatheSet matheSet = new MatheSet();
            matheSet.setStart(msgList.get(0).getMid());
            matheSet.setEnd((targetMsgCreationDate == null) ? msgList.get(msgList.size() - 1)
                    .getTime() : targetMsgCreationDate);
            MessageMatheSetCacheUtils.add(context, msgList.get(0).getCid(),
                    matheSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    /**
     * @param context
     * @param channelID 所属的频道
     * @param num       获取消息记录的条数
     * @return
     */
    public static List<Msg> getHistoryMsgList(Context context,
                                              String channelID, Long targetMessageCreationDate, int num) {
        List<Msg> msgList = null;
        try {

            if (targetMessageCreationDate == null) {
                msgList = DbCacheUtils.getDb(context).selector(Msg.class)
                        .where("cid", "=", channelID).orderBy("time", true)
                        .limit(num).findAll();
            } else {
                msgList = DbCacheUtils.getDb(context).selector(Msg.class)
                        .where("time", "<", targetMessageCreationDate).and("cid", "=", channelID)
                        .orderBy("time", true).limit(num).findAll();
            }
            if (msgList != null && msgList.size() > 1) {
                Collections.reverse(msgList);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (msgList == null) {
            msgList = new ArrayList<Msg>();
        }
        return msgList;
    }

    /**
     * 获取频道最新的消息
     *
     * @param context
     * @param channelID
     * @return
     */
    public static Msg getNewMsg(Context context, String channelID) {
        Msg msg = null;
        try {

            msg = DbCacheUtils.getDb(context).selector(Msg.class)
                    .where("cid", "=", channelID).orderBy("time", true).findFirst();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return msg;
    }


    /**
     * @param context
     * @param channelID 所属频道
     * @param num       加载历史记录的条数
     * @return 本地是否有足够多的缓存的数据
     */
    public static boolean isDataInLocal(Context context, String channelID,
                                        long targetCreateDate, int num) {
        // TODO Auto-generated method stub
        try {

            MatheSet matheSet = MessageMatheSetCacheUtils.getInMatheSet(context,
                    channelID, targetCreateDate);
            if (matheSet == null) {
                return false;
            }
            long mathSetStart = matheSet.getStart();
            // 此处获取count后减1，因为要获取targetID以前的数据不能包含自己
            long continuousCount = DbCacheUtils.getDb(context).selector
                    (Msg.class)
                    .where("time", "between",
                            new String[]{mathSetStart + "", targetCreateDate + ""})
                    .and("time", "!=", targetCreateDate).and("cid", "=", channelID).count();


            if (continuousCount >= num) {
                return true;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 通过消息id获取消息
     *
     * @param context
     * @param mid
     * @return
     */
    public static Msg getCacheMsg(Context context, String mid) {
        Msg cacheMsg = null;
        try {

            cacheMsg = DbCacheUtils.getDb(context).findById(Msg.class, mid);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cacheMsg;
    }

    /**
     * 获取频道内比目标消息更新的消息的条数
     *
     * @param context
     * @param cid
     * @return
     */
    public static int getNewerMsgCount(Context context, String cid, long targetMessageReadCreationDate) {
        int count = 0;
        try {

            count = (int) DbCacheUtils.getDb(context).selector(Msg.class)
                    .where("time", ">", targetMessageReadCreationDate).and("cid", "=", cid).count();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return count;

    }

    /**
     * 获取特定频道图片类型的消息
     *
     * @param context
     * @param cid
     * @return
     */
    public static List<Msg> getImgTypeMsgList(Context context, String cid) {

        return getImgTypeMsgList(context, cid, true);

    }

    /**
     * 获取特定频道图片类型的消息
     *
     * @param context
     * @param cid
     * @param desc
     * @return
     */
    public static List<Msg> getImgTypeMsgList(Context context, String cid, boolean desc) {
        List<Msg> imgTypeMsgList = null;
        try {

            imgTypeMsgList = DbCacheUtils.getDb(context).selector
                    (Msg.class)
                    .where("cid", "=", cid)
                    .and(WhereBuilder.b("type", "=", "image").or("type", "=",
                            "res_image")).orderBy("time", desc).findAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (imgTypeMsgList == null) {
            imgTypeMsgList = new ArrayList<Msg>();
        }
        return imgTypeMsgList;
    }

    /**
     * 获取特定频道图片类型的消息
     *
     * @param context
     * @param cid
     * @return
     */
    public static List<Msg> getFileTypeMsgList(Context context, String cid) {
        List<Msg> fileTypeMsgList = null;
        try {

            fileTypeMsgList = DbCacheUtils.getDb(context).selector(Msg.class)
                    .where("cid", "=", cid)
                    .and("type", "=", "res_file")
                    .orderBy("time", true).findAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (fileTypeMsgList == null) {
            fileTypeMsgList = new ArrayList<Msg>();
        }
        return fileTypeMsgList;

    }

}
