package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.chat.Conversation;

import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 频道列表缓存处理类
 *
 * @author Administrator
 */
public class ConversationCacheUtils {
    private static final int SEARCH_ALL = 0;
    private static final int SEARCH_CONTACT = 2;
    private static final int SEARCH_CHANNELGROUP = 1;
    private static final int SEARCH_NOTHIING = 4;


    /**
     * 存储会话列表
     *
     * @param context
     * @param conversationList
     */
    public static void saveConversationList(final Context context,
                                       final List<Conversation> conversationList) {

        // TODO Auto-generated method stub
        try {
            if (conversationList == null || conversationList.size() == 0) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(conversationList);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 删除所有会话
     *
     * @param context
     */
    public static void deleteAllConversation(Context context) {
        try {
            DbCacheUtils.getDb(context).delete(Conversation.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 存储频道列表
     *
     * @param context
     * @param conversation
     */
    public static void saveConversation(Context context, Conversation conversation) {
        try {
            if (conversation == null) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(conversation);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 设置是否置顶
     * @param context
     * @param id
     * @param isStick
     */
    public static void setConversationStick(Context context,String id,boolean isStick){
        try {
            DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id),new KeyValue("stick",isStick));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 隐藏会话
     * @param context
     * @param id
     */
    public static void setConversationHide(Context context,String id){
        try {
            DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id),new KeyValue("hide",true));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 设置会话名称
     * @param context
     * @param id
     * @param name
     */
    public static void updateConversationName(Context context, String id, String name){
        try {
            DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id),new KeyValue("name",name));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 设置是否免打扰
     * @param context
     * @param id
     * @param name
     */
    public static void updateConversationDnd(Context context, String id, boolean isDnd){
        try {
            DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id),new KeyValue("dnd",isDnd));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /**
     * 删除会话
     * @param context
     * @param id
     */
    public static void deleteConversation(Context context,String id){
        try {
            DbCacheUtils.getDb(context).delete(Conversation.class,WhereBuilder.b("id","=",id));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 删除会话列表
     * @param context
     * @param conversationList
     */
    public static void deleteConversationList(Context context,List<Conversation> conversationList){
        try {
            if (conversationList == null || conversationList.size()==0){
                return;
            }
            DbCacheUtils.getDb(context).delete(conversationList);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /**
     * 获取频道的类型
     *
     * @param context
     * @param id
     * @return
     */
    public static String getConversationType(Context context, String id) {
        String type = "";
        try {
            Conversation conversation = DbCacheUtils.getDb(context).findById(Conversation.class,id);
            if (conversation != null){
                type = conversation.getType();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return type;
    }


    /**
     * 获取所有会话列表
     * @param context
     * @return
     */
    public static List<Conversation> getConversationList(Context context) {
        List<Conversation> conversationList = null;
        try {
            conversationList = DbCacheUtils.getDb(context).selector(Conversation.class).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (conversationList == null) {
            conversationList = new ArrayList<Conversation>();
        }
        return conversationList;
    }


    /**
     * 获取缓存中的频道
     *
     * @param context
     * @param id
     * @return
     */
    public static Conversation getConversation(Context context, String id) {
        Conversation conversation = null;
        try {
            conversation = DbCacheUtils.getDb(context).findById(Conversation.class, id);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return conversation;
    }


    /**
     * 谋取某个类型的会话
     *
     * @param context
     * @param type
     * @return
     */
    public static List<Conversation> getConversationList(Context context, String type) {
        List<Conversation> conversationList = null;
        try {
            conversationList = DbCacheUtils.getDb(context).selector(Conversation.class).where("type", "=", type).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (conversationList == null) {
            conversationList = new ArrayList<>();
        }
        return conversationList;
    }

//    public static List<String> getConversationExistMemberUidList(Context context,String id,int limit){
//        List<String> uidList = new ArrayList<>();
//        try {
//            Conversation conversation = getConversation(context,id);
//            if (conversation != null){
//                List<String> memberUidList = conversation.getMemberList();
//
//            }
//
//
//
//            if(channelGroup == null){
//                return new ArrayList<>();
//            }
//            List<String> allMemberList = channelGroup.getMemberList();
//            //遍历如果头像存在则加入
//            for (int i = 0; i < allMemberList.size(); i++) {
//                String url = APIUri.getChannelImgUrl(context, allMemberList.get(i));
//                if(!StringUtils.isBlank(url)){
//                    uidList.add(allMemberList.get(i));
//                    if(uidList.size()>=limit){
//                        break;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return uidList;
//    }


//    /**
//     * 获取云+客服channel
//     *
//     * @param context
//     * @return
//     */
//    public static Channel getCustomerChannel(Context context) {
//        String uid = PreferencesUtils.getString(context, "userID", "");
//        Channel channel = null;
//        try {
//            channel = DbCacheUtils.getDb(context).selector
//                    (Channel.class)
//                    .where("title", "=", "BOT6005" + "-" + uid)
//                    .and("type", "=", "SERVICE").findFirst();
//        } catch (Exception e) {
//            // TODO: handle exception
//            e.printStackTrace();
//        }
//        return channel;
//    }
}
