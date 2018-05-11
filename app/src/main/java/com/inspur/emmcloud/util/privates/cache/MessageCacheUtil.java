package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.bean.chat.Message;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息本地数据库存储处理类
 */
public class MessageCacheUtil {

    /**
     * 存储消息
     *
     * @param context
     * @param message
     */
    public static void saveMessage(final Context context, final Message message) {
        try {

            DbCacheUtils.getDb(context).saveOrUpdate(message); // 存储消息
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 存储消息列表
     *
     * @param context
     * @param messageList
     * @param targetMessageCreationDate
     */
    public static void saveMessageList(final Context context,
                                       final List<Message> messageList, final Long targetMessageCreationDate) {


        // TODO Auto-generated method stub
        try {
            if (messageList == null || messageList.size() == 0) {
                return;
            }

            DbCacheUtils.getDb(context).saveOrUpdate(messageList);
            MatheSet matheSet = new MatheSet();
            matheSet.setStart(messageList.get(0).getCreationDate());
            matheSet.setEnd((targetMessageCreationDate == null) ? messageList.get(messageList.size() - 1)
                    .getCreationDate() : targetMessageCreationDate);
            MessageMatheSetCacheUtils.add(context, messageList.get(0).getChannel(),
                    matheSet);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    /**
     * 获取历史消息列表
     *
     * @param context
     * @param cid
     * @param targetMessageCreationDate
     * @param num
     * @return
     */
    public static List<Message> getHistoryMessageList(Context context,
                                                      String cid, Long targetMessageCreationDate, int num) {
        List<Message> messageList = null;
        try {

            if (targetMessageCreationDate == null) {
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where("channel", "=", cid).orderBy("creationDate", true)
                        .limit(num).findAll();
            } else {
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where("creationDate", "<", targetMessageCreationDate).and("channel", "=", cid)
                        .orderBy("creationDate", true).limit(num).findAll();
            }
            if (messageList != null && messageList.size() > 1) {
                Collections.reverse(messageList);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        return messageList;
    }


    /**
     * 获取频道最新的消息
     *
     * @param context
     * @param cid
     * @return
     */
    public static Message getNewMessge(Context context, String cid) {
        Message message = null;
        try {

            message = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("channel", "=", cid).orderBy("creationDate", true).findFirst();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return message;
    }


    /**
     * @param context
     * @param cid      所属频道
     * @param targetId 目标消息id
     * @param num      加载历史记录的条数
     * @return 本地是否有足够多的缓存的数据
     */
    public static boolean isDataInLocal(Context context, String cid,
                                        long targetCreateDate, int num) {
        // TODO Auto-generated method stub
        try {
            MatheSet matheSet = MessageMatheSetCacheUtils.getInMatheSet(context,
                    cid, targetCreateDate);
            if (matheSet == null) {
                return false;
            }
            long mathSetStart = matheSet.getStart();
            // 此处获取count后减1，因为要获取targetID以前的数据不能包含自己
            long continuousCount = DbCacheUtils.getDb(context).selector
                    (Message.class)
                    .where("creationDate", "between",
                            new String[]{mathSetStart + "", targetCreateDate + ""})
                    .and("creationDate", "!=", targetCreateDate).and("channel", "=", cid).count();


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
    public static Message getMessageByMid(Context context, String mid) {
        Message message = null;
        try {
            message = DbCacheUtils.getDb(context).findById(Message.class, mid);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return message;
    }

    /**
     * 获取频道内比目标消息更新的消息的条数
     *
     * @param context
     * @param cid
     * @param mid
     * @return
     */
    public static int getNewerMessageCount(Context context, String cid, long targetMessageReadCreationDate) {
        int count = 0;
        try {

            count = (int) DbCacheUtils.getDb(context).selector(Message.class)
                    .where("creationDate", ">", targetMessageReadCreationDate).and("channel", "=", cid).count();
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
    public static List<Message> getImgTypeMessageList(Context context, String cid) {

        return getImgTypeMessageList(context, cid, true);

    }

    /**
     * 获取特定频道图片类型的消息
     *
     * @param context
     * @param cid
     * @param desc
     * @return
     */
    public static List<Message> getImgTypeMessageList(Context context, String cid, boolean desc) {
        List<Message> imgTypeMessageList = null;
        try {
            imgTypeMessageList = DbCacheUtils.getDb(context).selector
                    (Message.class)
                    .where("channel", "=", cid)
                    .and(WhereBuilder.b("type", "=", "image").or("type", "=", "res_image").or("type", "=", "media/image")).orderBy("creationDate", desc).findAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (imgTypeMessageList == null) {
            imgTypeMessageList = new ArrayList<>();
        }
        return imgTypeMessageList;
    }

    /**
     * 获取特定频道图片类型的消息
     *
     * @param context
     * @param cid
     * @return
     */
    public static List<Message> getFileTypeMsgList(Context context, String cid) {
        List<Message> fileTypeMessageList = null;
        try {

            fileTypeMessageList = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("channel", "=", cid).and(WhereBuilder.b("type", "=", "res_file").or("type", "=", "file/regular-file"))
                    .orderBy("creationDate", true).findAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (fileTypeMessageList == null) {
            fileTypeMessageList = new ArrayList<>();
        }
        return fileTypeMessageList;

    }

    /**
     * 判断本地有没有缓存消息历史记录
     * @param context
     * @param enterAppTime
     * @return
     */
    public static boolean isHistoryMessageCache(Context context,long enterAppTime){
        try {
            Long count = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("creationDate", "<", enterAppTime).count();
            return count>0;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}
