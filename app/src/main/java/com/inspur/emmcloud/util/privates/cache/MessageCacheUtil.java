package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.ListUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.ConversationWithMessageNum;
import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.bean.chat.Message;

import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息本地数据库存储处理类
 */
public class MessageCacheUtil {

    public static List<Message> getMessageListByType(final Context context, final List<String> messageTypeList) {
        List<Message> messageList = null;
        try {
            messageList = DbCacheUtils.getDb(context).selector(Message.class).where("type", "in", messageTypeList).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        return messageList;
    }

    public static List<Message> getMessageListBySendStatus(int sendStatus) {
        List<Message> messageList = null;
        try {
            messageList = DbCacheUtils.getDb(BaseApplication.getInstance()).selector(Message.class).where("sendStatus", "=", sendStatus).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        return messageList;
    }

    public static List<Message> getMessageListBySendStatus(int sendStatus, boolean isWaitingSendRetry) {
        List<Message> messageList = null;
        try {
            messageList = DbCacheUtils.getDb(BaseApplication.getInstance()).selector(Message.class).where("sendStatus", "=", sendStatus).and("isWaitingSendRetry", "=", isWaitingSendRetry).orderBy("creationDate", false).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        return messageList;
    }


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
     * 再次进入频道时修改频道内16秒以上还在发送中状态的消息
     *
     * @param context
     * @param messageList
     */
    public static void saveMessageList(Context context, List<Message> messageList) {
        try {
            if (messageList == null || messageList.size() == 0) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(messageList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 存储消息列表
     *
     * @param context
     * @param messageList
     * @param targetMessageCreationDate
     * @param isUpdate                  是否进行更新操作
     */
    public static void saveMessageList(final Context context,
                                       final List<Message> messageList, final Long targetMessageCreationDate, boolean isUpdate) {
        try {
            if (messageList == null || messageList.size() == 0) {
                return;
            }
            List<Message> messageOperationList = ListUtils.deepCopyList(messageList);
            //去重操作，防止服务端重复消息覆盖本地消息导致已读未读状态错乱，并防止已经改过时间的消息被服务端覆盖时间
            if (!isUpdate) {
                List<String> messageIdList = new ArrayList<>();
                for (Message message : messageOperationList) {
                    messageIdList.add(message.getId());
                }
                List<Message> existMessageList = DbCacheUtils.getDb(context).selector(Message.class).where("id", "in", messageIdList).findAll();
                if (existMessageList != null && existMessageList.size() > 0) {
                    messageOperationList.removeAll(existMessageList);
                }
                if (messageOperationList.size() == 0) {
                    return;
                }
            }
            DbCacheUtils.getDb(context).saveOrUpdate(messageOperationList);
            MatheSet matheSet = new MatheSet();
            matheSet.setStart(messageList.get(0).getCreationDate());
            matheSet.setEnd((targetMessageCreationDate == null) ? messageList.get(messageList.size() - 1)
                    .getCreationDate() : targetMessageCreationDate);
            MessageMatheSetCacheUtils.add(context, messageList.get(0).getChannel(),
                    matheSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将频道消息置为已读
     *
     * @param context
     * @param cid
     */
    public static void setChannelMessageRead(Context context, String cid) {
        try {
            DbCacheUtils.getDb(context).update(Message.class, WhereBuilder.b("channel", "=", cid), new KeyValue("read", 1));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 将频道消息置为已读
     *
     * @param context
     */
    public static void setAllMessageRead(Context context) {
        try {
            DbCacheUtils.getDb(context).update(Message.class, null, new KeyValue("read", 1));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取频道未读消息条数，
     *
     * @param context
     * @param cid
     * @return
     */
    public static long getChannelMessageUnreadCount(Context context, String cid) {
        long unreadCount = 0;
        try {
            Long lastReadMessageCreationDate = 0L;
            Message lastReadMessage = DbCacheUtils.getDb(context).selector(Message.class).where("read", "=", 1)
                    .and("channel", "=", cid).orderBy("creationDate", true).findFirst();
            if (lastReadMessage != null) {
                lastReadMessageCreationDate = lastReadMessage.getCreationDate();
            }
            unreadCount = DbCacheUtils.getDb(context).selector(Message.class).
                    where("creationDate", ">", lastReadMessageCreationDate)
                    .and("channel", "=", cid).count();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return unreadCount;
    }


    /**
     * 设置消息已读
     *
     * @param context
     * @param messageIdList
     */
    public static void setMessageStateRead(Context context, List<String> messageIdList) {
        try {
            if (messageIdList == null || messageIdList.size() == 0) {
                return;
            }
            DbCacheUtils.getDb(context).update(Message.class, WhereBuilder.b("id", "in", messageIdList), new KeyValue("read", 1));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取历史消息列表
     * 给UiConversation用，沟通列表需要显示所有四种状态的消息
     *
     * @param context
     * @param cid
     * @param message
     * @param num
     * @return
     */
    public static List<Message> getHistoryMessageListIncludeEditingMessage(Context context,
                                                                           String cid, Message message, int num) {
        List<Message> messageList = null;
        try {
            if (message == null) {
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where("channel", "=", cid).orderBy("creationDate", true).orderBy("id", true)
                        .limit(num).findAll();
            } else {
                long creationDate = message.getCreationDate();
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where("channel", "=", cid)
                        .and(WhereBuilder.b("creationDate", "<", creationDate).or(WhereBuilder.b("creationDate", "=", creationDate).and("id", "<", message.getId())))
                        .orderBy("creationDate", true).orderBy("id", true).limit(num).findAll();
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
     * 获取历史消息列表，给ConversationActivity用，包含发送成功，发送失败，发送中三种状态，不包含编辑状态
     *
     * @param context
     * @param cid
     * @param message
     * @param num
     * @return
     */
    public static List<Message> getHistoryMessageList(Context context,
                                                      String cid, Message message, int num) {
        List<Message> messageList = null;
        try {
            if (message == null) {
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where("channel", "=", cid).and("sendStatus", "!=", Message.MESSAGE_SEND_EDIT).orderBy("creationDate", true).orderBy("id", true)
                        .limit(num).findAll();
            } else {
                long creationDate = message.getCreationDate();
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where("channel", "=", cid).and("sendStatus", "!=", Message.MESSAGE_SEND_EDIT)
                        .and(WhereBuilder.b("creationDate", "<", creationDate).or(WhereBuilder.b("creationDate", "=", creationDate).and("id", "<", message.getId())))
                        .orderBy("creationDate", true).orderBy("id", true).limit(num).findAll();
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

    public static List<Message> getFutureMessageList(Context context, String cid, Long targetMessageCreationDate) {
        List<Message> messageList = null;
        try {
            messageList = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("creationDate", ">=", targetMessageCreationDate).and("channel", "=", cid)
                    .orderBy("creationDate", false).orderBy("id", false).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        return messageList;
    }


//    /**
//     * 获取历史消息列表，给ConversationActivity用，包含发送成功，发送失败，发送中三种状态，不包含编辑状态
//     *
//     * @param context
//     * @param cid
//     * @param targetMessageCreationDate
//     * @return
//     */
//    public static List<Message> getHistoryMessageList(Context context,
//                                                      String cid, Long targetMessageCreationDate) {
//        List<Message> messageList = null;
//        try {
//
//            if (targetMessageCreationDate == null) {
//                messageList = DbCacheUtils.getDb(context).selector(Message.class)
//                        .where("channel", "=", cid).and("sendStatus", "!=", Message.MESSAGE_SEND_EDIT).orderBy("creationDate", true)
//                        .findAll();
//            } else {
//                messageList = DbCacheUtils.getDb(context).selector(Message.class)
//                        .where("creationDate", "<", targetMessageCreationDate).and("channel", "=", cid).and("sendStatus", "!=", Message.MESSAGE_SEND_EDIT)
//                        .orderBy("creationDate", true)
//                        .findAll();
//            }
//            if (messageList != null && messageList.size() > 1) {
//                Collections.reverse(messageList);
//            }
//
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        if (messageList == null) {
//            messageList = new ArrayList<>();
//        }
//        return messageList;
//    }


    /**
     * 获取频道最新的消息
     *
     * @param context
     * @param cid
     * @return
     */
    public static Message getNewMessage(Context context, String cid) {
        Message message = null;
        try {
            message = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("channel", "=", cid)
                    .and("sendStatus", "=", Message.MESSAGE_SEND_SUCCESS)
                    .orderBy("creationDate", true).orderBy("id", true).findFirst();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return message;
    }


    /**
     * @param context
     * @param cid              所属频道
     * @param targetCreateDate 目标消息id
     * @param num              加载历史记录的条数
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
                    .and("sendStatus", "!=", Message.MESSAGE_SEND_EDIT)
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
     * 获取特定频道图片类型的消息，在获取时应当只获取发送成功的消息
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
                    .and(WhereBuilder.b("type", "=", "image")
                            .or("type", "=", "res_image")
                            .or("type", "=", "media/image"))
                    .and("sendStatus", "=", Message.MESSAGE_SEND_SUCCESS)
                    .orderBy("creationDate", desc).findAll();
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
     * 获取特定频道图片类型的消息，在获取时应当只获取发送成功的消息
     *
     * @param context
     * @param cid
     * @return
     */
    public static List<Message> getFileTypeMsgList(Context context, String cid) {
        List<Message> fileTypeMessageList = null;
        try {

            fileTypeMessageList = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("channel", "=", cid).and(WhereBuilder.b("type", "=", "res_file")
                            .or("type", "=", "file/regular-file"))
                    .and("sendStatus", "=", Message.MESSAGE_SEND_SUCCESS)
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
     * 获取最新消息的消息id
     *
     * @param context
     * @return
     */
    public static String getLastSuccessMessageIdByCid(Context context, String cid) {
        String lastMessageId = null;
        try {
            Message message = DbCacheUtils.getDb(context).selector(Message.class).where("channel", "=", cid)
                    .orderBy("creationDate", true)
                    .and("sendStatus", "=", Message.MESSAGE_SEND_SUCCESS).findFirst();
            if (message != null) {
                lastMessageId = message.getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastMessageId;
    }

    /**
     * 获取最新消息的消息id
     *
     * @param context
     * @return
     */
    public static String getLastSuccessMessageId(Context context) {
        String lastMessageId = null;
        try {
            Message message = DbCacheUtils.getDb(context).selector(Message.class).where("sendStatus", "=", Message.MESSAGE_SEND_SUCCESS)
                    .orderBy("creationDate", true).findFirst();
            if (message != null) {
                lastMessageId = message.getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastMessageId;
    }

    /**
     * 获取草稿箱文字
     *
     * @param context
     * @param cid
     * @return
     */
    public static String getDraftByCid(Context context, String cid) {
        try {
            Message message = DbCacheUtils.getDb(context).selector(Message.class).where("channel", "=", cid)
                    .and("sendStatus", "=", Message.MESSAGE_SEND_EDIT)
                    .orderBy("creationDate", true).findFirst();
            return message != null ? message.getMsgContentTextPlain().getText() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 删除草稿箱消息
     *
     * @param context
     * @param cid
     */
    public static void deleteDraftMessageByCid(Context context, String cid) {
        try {
            DbCacheUtils.getDb(context).delete(Message.class, WhereBuilder.b("channel", "=", cid).and("sendStatus", "=", 3));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /**
     * 处理多条，作用跟下面同名方法相同
     *
     * @param context
     * @param messageList
     * @param targetMessageCreationDate
     */
    public static void handleRealMessage(Context context, List<Message> messageList, Long targetMessageCreationDate, String cid, boolean isUpdate) {
        if (messageList.size() > 0) {
            List<String> messageTmpIdList = new ArrayList<>();
            for (Message message : messageList) {
                messageTmpIdList.add(message.getTmpId());
            }
            List<Message> localFakeMessageList = getLocalFakeMessageList(context, cid, messageTmpIdList);
            for (int i = 0; i < messageList.size(); i++) {
                for (int j = 0; j < localFakeMessageList.size(); j++) {
                    //去掉修改时间逻辑
//                    if (messageList.get(i).getTmpId().equals(localFakeMessageList.get(j).getTmpId())) {
//                        messageList.get(i).setCreationDate(localFakeMessageList.get(j).getCreationDate());
//                    }
                    if (messageList.get(i).getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE)) {
                        deleteLocalVoiceFile(messageList.get(i));
                    }
                }
            }
            deleteFakeMessageList(context, localFakeMessageList);
            saveMessageList(context, messageList, targetMessageCreationDate, isUpdate);
        }
    }

    /**
     * 获取本地未发送成功的消息
     *
     * @param context
     * @return
     */
    private static List<Message> getLocalFakeMessageList(Context context, String cid, List<String> messgeTmpIdList) {
        List<Message> messageList = new ArrayList<>();
        try {
            if (StringUtils.isBlank(cid)) {
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where("channel", "=", cid)
                        .and(WhereBuilder.b("sendStatus", "=", Message.MESSAGE_SEND_FAIL).or("sendStatus", "=", Message.MESSAGE_SEND_ING))
                        .and("id", "in", messgeTmpIdList)
                        .findAll();
            } else {
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where(WhereBuilder.b("sendStatus", "=", Message.MESSAGE_SEND_FAIL).or("sendStatus", "=", Message.MESSAGE_SEND_ING))
                        .and("id", "in", messgeTmpIdList)
                        .findAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        return messageList;
    }

    public static List<Message> getMessageListWithNoRecall(Context context, List<String> midList) {
        List<Message> messageList = new ArrayList<>();
        try {
            if (midList != null && midList.size() > 0) {
                messageList = DbCacheUtils.getDb(context).selector(Message.class).where("id", "in", midList).and("recallFrom", "=", "").findAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return messageList;
    }

    /**
     * 真实消息回来后，如果是重发消息需要更新本地消息的id，如果是直接过来的消息需要存储消息
     *
     * @param context
     * @param message
     */
    public static void handleRealMessage(Context context, Message message) {
        Message messageTmp = MessageCacheUtil.getMessageByMid(context, message.getTmpId());
        if (messageTmp != null) {
            //如果发送的消息是音频消息，在发送成功后删除本地消息
            if (messageTmp.getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE)) {
                deleteLocalVoiceFile(message);
            }
            //更新本地假消息，把id改成真消息的id，并把发送状态改为发送成功
            try {
                DbCacheUtils.getDb(context).update(Message.class, WhereBuilder.b("id", "=", message.getTmpId())
                        , new KeyValue("id", message.getId()), new KeyValue("sendStatus", Message.MESSAGE_SEND_SUCCESS), new KeyValue("creationDate", message.getCreationDate()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MessageCacheUtil.saveMessage(context, message);
        }
    }

    /**
     * 根据tempid修改消息
     *
     * @param context
     * @param messageList
     */
    public static void deleteFakeMessageList(final Context context, final List<Message> messageList) {
        if (messageList == null || messageList.size() == 0) {
            return;
        }
        List<String> idList = new ArrayList<>();
        for (Message message : messageList) {
            idList.add(message.getTmpId());
        }
        try {
            DbCacheUtils.getDb(context).delete(Message.class, WhereBuilder.b("id", "in", idList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除本地缓存中的文件
     *
     * @param message
     */
    private static void deleteLocalVoiceFile(Message message) {
        String sendSuccessMp3FileName = FileUtils.getFileNameWithoutExtension(message.getMsgContentMediaVoice().getMedia());
        ArrayList<String> localFilePathList = FileUtils.getAllFilePathByDirPath(MyAppConfig.LOCAL_CACHE_VOICE_PATH);
        for (int i = 0; i < localFilePathList.size(); i++) {
            if (sendSuccessMp3FileName.equals(FileUtils.getFileNameWithoutExtension(localFilePathList.get(i)))) {
                FileUtils.deleteFile(localFilePathList.get(i));
            }
        }
    }



    /**
     * 根据聊天内容查找所有消息
     *
     * @param context
     * @param content
     */
    public static List<Message> getMessagesListByKeyword(Context context, String content) {
        List<Message> messageList = new ArrayList<>();
        try {
            List<Message> messageList1 = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("type", "=", Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN)
                    .and(WhereBuilder.b("showContent", "like", "%" + content + "%"))
                    .and(WhereBuilder.b("sendStatus", "=", 1))
                    .and(WhereBuilder.b("recallFrom", "!=", null))
                    .findAll();
            List<Message> messageList2 = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("type", "=", Message.MESSAGE_TYPE_TEXT_MARKDOWN)
                    .and(WhereBuilder.b("showContent", "like", "%" + content + "%"))
                    .and(WhereBuilder.b("sendStatus", "=", 1))
                    .and(WhereBuilder.b("recallFrom", "!=", null))
                    .findAll();
            List<Message> messageList3 = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("type", "=", Message.MESSAGE_TYPE_TEXT_PLAIN)
                    .and(WhereBuilder.b("showContent", "like", "%" + content + "%"))
                    .and(WhereBuilder.b("sendStatus", "=", 1))
                    .and(WhereBuilder.b("recallFrom", "!=", null))
                    .findAll();
            messageList.addAll(messageList1);
            messageList.addAll(messageList2);
            messageList.addAll(messageList3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        return messageList;
    }

    /**
     * 根据内容查找文本聊天记录
     */
    public static List<Message> getMessageListByKeywordAndId(Context context, String content, String id) {
        List<Message> messageList;
        try {
            messageList = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("channel", "=", id)
                    .and(WhereBuilder.b("type", "=", Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN)
                            .or("type", "=", Message.MESSAGE_TYPE_TEXT_MARKDOWN)
                            .or("type", "=", Message.MESSAGE_TYPE_TEXT_PLAIN))
                    .and(WhereBuilder.b("sendStatus", "=", 1))
                    .and(WhereBuilder.b("showContent", "like", "%" + content + "%"))
                    .findAll();
            if (messageList != null) {
                return messageList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * 根据内容查找记录
     */
    public static List<ConversationWithMessageNum> getConversationListByContent(Context context, String content) {
        Map<String, Integer> cidNumMap = new HashMap<>();
        List<ConversationWithMessageNum> conversationFromChatContentList = new ArrayList<>();
        try {
            List<Message> messageList;
            List<String> conversationIdList = new ArrayList<>();
            messageList = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("type", "=", Message.MESSAGE_TYPE_TEXT_PLAIN)
                    .and(WhereBuilder.b("showContent", "like", "%" + content + "%"))
                    .findAll();
            if (messageList != null) {
                for (int i = 0; i < messageList.size(); i++) {
                    String currentMessageConversation = messageList.get(i).getChannel();
                    if (cidNumMap != null && cidNumMap.containsKey(currentMessageConversation)) {
                        int num = cidNumMap.get(currentMessageConversation);
                        num = num + 1;
                        cidNumMap.put(currentMessageConversation, num);
                    } else {
                        cidNumMap.put(currentMessageConversation, 1);
                        conversationIdList.add(currentMessageConversation);
                    }
                }
            }
            List<Conversation> conversationList = ConversationCacheUtils.getConversationListByIdList(context, conversationIdList);
            for (int i = 0; i < conversationList.size(); i++) {
                Conversation tempConversation = conversationList.get(i);
                if (cidNumMap.containsKey(tempConversation.getId())) {
                    ConversationWithMessageNum conversationFromChatContent =
                            new ConversationWithMessageNum(tempConversation, cidNumMap.get(tempConversation.getId()));
                    if (tempConversation.getType().equals(Conversation.TYPE_DIRECT)) {
                        conversationFromChatContent.initSingleChatContact();
                    }
                    conversationFromChatContentList.add(conversationFromChatContent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (conversationFromChatContentList == null) {
            conversationFromChatContentList = new ArrayList<>();
        }
        return conversationFromChatContentList;
    }


    /**
     * 查找所有文本类型的消息
     *
     * @param context
     * @param cid
     * @return
     */
    public static List<Message> getGroupMessageWithTypeAndKeywords(Context context, String cid) {
        List<Message> messageList = new ArrayList<>();
        try {
            messageList = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("channel", "=", cid)
                    .and(WhereBuilder.b("type", "=", Message.MESSAGE_TYPE_TEXT_PLAIN))
                    .orderBy("creationDate", true)
                    .findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageList == null) {
            messageList = new ArrayList<>();
        }
        return messageList;
    }

    public static void deleteMessageById(String mid) {
        try {
            DbCacheUtils.getDb().deleteById(Message.class, mid);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
