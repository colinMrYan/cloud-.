package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.chat.MessageReadCreationDate;

import java.util.List;


/**
 * 标记消息已读、未读的工具类
 *
 * @author Administrator
 */
public class MsgReadCreationDateCacheUtils {
    /**
     * 存储某个频道的已读到的消息创建时间
     *
     * @param context
     * @param cid
     * @param msgID
     */
    public static void saveMessageReadCreationDate(Context context, String cid, long messageCreationDate) {
        try {
            MessageReadCreationDate messageReadCreationDate = DbCacheUtils.getDb(context).findById(MessageReadCreationDate.class, cid);
            if (messageReadCreationDate != null) {
                long currentMessageReadCreationDate = messageReadCreationDate.getMessageReadCreationDate();
                if (messageCreationDate <= currentMessageReadCreationDate) {
                    return;
                }
            }
            DbCacheUtils.getDb(context).saveOrUpdate(new MessageReadCreationDate(cid, messageCreationDate));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    /**
     * 存储多个频道的已读到的消息创建时间
     *
     * @param context
     * @param cid
     * @param messageReadCreationDateList
     */
    public static void saveMessageReadCreationDateList(Context context, List<MessageReadCreationDate> messageReadCreationDateList) {
        if (messageReadCreationDateList == null || messageReadCreationDateList.size() == 0) {
            return;
        }
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(messageReadCreationDateList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取未读消息条数
     *
     * @param context
     * @param cid
     * @return
     */
    public static int getNotReadMessageCount(Context context, String cid) {
        try {

            long targetMessageReadCreationDate = 0L;
            MessageReadCreationDate messageReadCreationDate = DbCacheUtils.getDb(context).findById(MessageReadCreationDate.class, cid);
            if (messageReadCreationDate != null) {
                targetMessageReadCreationDate = messageReadCreationDate.getMessageReadCreationDate();
            }
            return MsgCacheUtil.getNewerMsgCount(context, cid, targetMessageReadCreationDate);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return 0;
        }

    }


}
