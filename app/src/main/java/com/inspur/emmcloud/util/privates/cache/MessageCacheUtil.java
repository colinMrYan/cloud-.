package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.StringUtils;

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
     * 再次进入频道时修改频道内16秒以上还在发送中状态的消息
     * @param context
     * @param messageList
     */
    public static void updateMessageSendStatus(Context context,List<Message> messageList){
        try {
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
     */
    public static void saveMessageList(final Context context,
                                       final List<Message> messageList, final Long targetMessageCreationDate) {
        saveMessageList(context,messageList,targetMessageCreationDate,true);
    }

    /**
     * 存储消息列表
     * @param context
     * @param messageList
     * @param targetMessageCreationDate
     * @param isUpdate  是否进行更新操作
     */
    public static void saveMessageList(final Context context,
                                       final List<Message> messageList, final Long targetMessageCreationDate,boolean isUpdate) {
        try {
            if (messageList == null || messageList.size() == 0) {
                return;
            }
            //去重操作，防止服务端重复消息覆盖本地消息导致已读未读状态错乱
            if (!isUpdate){
                List<String> messageIdList = new ArrayList<>();
                for (Message message:messageList){
                    messageIdList.add(message.getId());
                }
                List<Message> existMessageList = DbCacheUtils.getDb(context).selector(Message.class).where("id","in",messageIdList).findAll();
                if (existMessageList != null && existMessageList.size()>0){
                    messageList.removeAll(existMessageList);
                }
                if (messageList.size() == 0){
                    return;
                }
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
     * 将频道消息置为已读
     * @param context
     * @param cid
     */
    public static void setChannelMessageRead(Context context, String cid){
        try {
           DbCacheUtils.getDb(context).execNonQuery("update Message set read = 1 where channel = "+cid);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 将频道消息置为已读
     * @param context
     */
    public static void setAllMessageRead(Context context){
        try {
            DbCacheUtils.getDb(context).execNonQuery("update Message set read = 1 ");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取频道未读消息条数
     * @param context
     * @param cid
     * @return
     */
    public static long getChannelMessageUnreadCount(Context context,String cid){
        long unreadCount = 0;
        try {
            Long lastReadMessageCreationDate = 0L;
            Message lastReadMessage =  DbCacheUtils.getDb(context).selector(Message.class).where("read","=",1).and("channel", "=", cid).orderBy("creationDate", true).findFirst();
            if (lastReadMessage != null){
                lastReadMessageCreationDate = lastReadMessage.getCreationDate();


            }
            unreadCount = DbCacheUtils.getDb(context).selector(Message.class).where("creationDate",">",lastReadMessageCreationDate).and("channel", "=", cid).count();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return unreadCount;
    }

    /**
     * 设置消息已读
     * @param context
     * @param messageIdList
     */
    public static void setMessageStateRead(Context context,List<String> messageIdList){
        try {
            String sqlWhereIn = "(";
            for (int i = 0; i < messageIdList.size(); i++) {
                sqlWhereIn = sqlWhereIn + messageIdList.get(i) + ",";
            }
            if (sqlWhereIn.endsWith(",")) {
                sqlWhereIn = sqlWhereIn.substring(0, sqlWhereIn.length() - 1);
            }
            sqlWhereIn = sqlWhereIn + ")";
            DbCacheUtils.getDb(context).execNonQuery("update Message set read = 1 where id in "+sqlWhereIn);
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
    public static List<Message> getHistoryMessageListIncludeEditingMessage(Context context,
                                                                           String cid, Long targetMessageCreationDate, int num) {
        List<Message> messageList = null;
        try {

            if (targetMessageCreationDate == null) {
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where("channel", "=", cid).and("channel", "=", cid).orderBy("creationDate", true)
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

    public static List<Message> getHistoryMessageListByTime(Context context,String cid,Long startTime,Long endTime){
        List<Message> messageList = null;
        try {
            messageList = DbCacheUtils.getDb(context).selector(Message.class)
                    .where("channel", "=", cid)
                    .and("channel", "=", cid).and("creationDate", "<=", endTime)
                    .and("creationDate", ">=", startTime).orderBy("creationDate", true).findAll();
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
                        .where("channel", "=", cid).and("channel", "=", cid).and("sendStatus","!=",Message.MESSAGE_SEND_EDIT).orderBy("creationDate", true)
                        .limit(num).findAll();
            } else {
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where("creationDate", "<", targetMessageCreationDate).and("channel", "=", cid).and("sendStatus","!=",Message.MESSAGE_SEND_EDIT)
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
     * @param targetCreateDate 目标消息id
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
     * @param targetMessageReadCreationDate
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
     * 获取最新消息的消息id
     * @param context
     * @return
     */
    public static String getLastMessageId(Context context){
        String lastMessageId = null;
        try {
            Message message = DbCacheUtils.getDb(context).selector(Message.class).orderBy("creationDate", true).findFirst();
            if (message != null){
                lastMessageId = message.getId();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return lastMessageId;
    }

    /**
     * 获取草稿箱文字
     * @param context
     * @param cid
     * @return
     */
    public static String getDraftByCid(Context context,String cid){
        try {
            Message message = DbCacheUtils.getDb(context).selector(Message.class).where("channel","=",cid)
                    .and("sendStatus","=",Message.MESSAGE_SEND_EDIT)
                    .orderBy("creationDate", true).findFirst();
            return message != null?message.getMsgContentTextPlain().getText():"";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 删除本地假消息
     * @param context
     * @param tmpId
     */
    public static void deleteLocalFakeMessage(Context context,String tmpId){
        try {
            DbCacheUtils.getDb(context).delete(Message.class,WhereBuilder.b("id","=",tmpId));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 删除草稿箱消息
     * @param context
     * @param cid
     */
    public static void deleteDraftMessageByCid(Context context,String cid){
        try {
            DbCacheUtils.getDb(context).delete(Message.class,WhereBuilder.b("channel","=",cid).and("sendStatus","=",3));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /**
     * 处理多条，作用跟下面同名方法相同
     * @param context
     * @param messageList
     * @param targetMessageCreationDate
     */
    public static void handleRealMessage(Context context, List<Message> messageList, Long targetMessageCreationDate,String cid){
        if (messageList.size()>0){
            List<Message> localFakeMessageList = getLocalFakeMessageList(context,cid);
            for(int i = 0; i < messageList.size(); i++){
                for (int j = 0; j < localFakeMessageList.size(); j++) {
                    if(messageList.get(i).getTmpId().equals(localFakeMessageList.get(j).getTmpId())){
                        messageList.get(i).setCreationDate(localFakeMessageList.get(j).getCreationDate());
                    }
                    if(messageList.get(i).getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE)){
                        deleteLocalVoiceFile(messageList.get(i));
                    }
                }
            }
            deleteFakeMessageList(context,messageList);
            saveMessageList(context,messageList,targetMessageCreationDate);
        }
    }

    /**
     * 获取本地未发送成功的消息
     * @param context
     * @return
     */
    private static List<Message> getLocalFakeMessageList(Context context,String cid) {
        List<Message> messageList = new ArrayList<>();
        try{
            if(StringUtils.isBlank(cid)){
                messageList =  DbCacheUtils.getDb(context).selector(Message.class)
                        .where("channel", "=", cid).and("sendStatus","==",Message.MESSAGE_SEND_FAIL)
                       .findAll();
            }else{
                messageList = DbCacheUtils.getDb(context).selector(Message.class)
                        .where("sendStatus","==",Message.MESSAGE_SEND_FAIL)
                        .findAll();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(messageList == null){
            messageList = new ArrayList<>();
        }
        return messageList;
    }

    /**
     * 真实消息回来后，把消息时间修改为，本地假消息的时间以便排序
     * 然后把本地假消息删掉
     * @param context
     * @param message
     */
    public static void handleRealMessage(Context context,Message message) {
        //删除临时消息前把创建时间改为临时消息的创建时间，保证排序
        Message messageTmp = MessageCacheUtil.getMessageByMid(context,message.getTmpId());
        if(messageTmp != null){
            //如果发送的消息是音频消息，在发送成功后删除本地消息
            if(messageTmp.getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE)){
                deleteLocalVoiceFile(message);
            }
            message.setCreationDate(messageTmp.getCreationDate());
            MessageCacheUtil.saveMessage(context,message);
            MessageCacheUtil.deleteLocalFakeMessage(context,message.getTmpId());
        }
    }

    /**
     * 根据tempid修改消息
     *
     * @param context
     * @param messageList
     */
    public static void deleteFakeMessageList(final Context context, final List<Message> messageList) {
        List<String> stringList = new ArrayList<>();
        for (Message message:messageList) {
            stringList.add(message.getTmpId());
        }
        try {
            DbCacheUtils.getDb(context).delete(Message.class,WhereBuilder.b("id","in",stringList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除本地缓存中的文件
     * @param message
     */
    private static void deleteLocalVoiceFile(Message message) {
        String sendSuccessMp3FileName = FileUtils.getFileNameWithoutExtension(message.getMsgContentMediaVoice().getMedia());
        ArrayList<String> localFilePathList = FileUtils.getAllFilePathByDirPath(MyAppConfig.LOCAL_CACHE_VOICE_PATH);
        for (int i = 0; i < localFilePathList.size(); i++) {
            if(sendSuccessMp3FileName.equals(FileUtils.getFileNameWithoutExtension(localFilePathList.get(i)))){
                FileUtils.deleteFile(localFilePathList.get(i));
            }
        }
    }
}
