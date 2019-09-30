package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PinyinUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.chat.Conversation;

import org.json.JSONArray;
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
     *
     * @param context
     * @param id
     * @param isStick
     */
    public static void setConversationStick(Context context, String id, boolean isStick) {
        try {
            DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id), new KeyValue("stick", isStick));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 隐藏会话
     *
     * @param context
     * @param id
     */
    public static void setConversationHide(Context context, String id, boolean isHide) {
        try {
            DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id), new KeyValue("hide", isHide));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public static Conversation getDirectConversationToUser(Context context, String uid) {
        Conversation conversation = null;
        String tile1 = uid + "-" + BaseApplication.getInstance().getUid();
        String tile2 = BaseApplication.getInstance().getUid() + "-" + uid;
        try {
            conversation = DbCacheUtils.getDb(context).selector(Conversation.class).where("type", "=", Conversation.TYPE_DIRECT)
                    .and(WhereBuilder.b("name", "=", tile1).or("name", "=", tile2)).findFirst();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return conversation;
    }

    /**
     * 设置会话名称
     *
     * @param context
     * @param id
     * @param name
     */
    public static void updateConversationName(Context context, String id, String name) {
        try {
            DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id), new KeyValue("name", name));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 设置会话是否隐藏
     *
     * @param context
     * @param id
     * @param isHide
     */
    public static void updateConversationHide(Context context, String id, boolean isHide) {
        try {
            DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id), new KeyValue("hide", isHide));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 设置是否免打扰
     *
     * @param context
     * @param id
     * @param isDnd
     */
    public static void updateConversationDnd(Context context, String id, boolean isDnd) {
        try {
            DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id), new KeyValue("dnd", isDnd));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /**
     * 删除会话
     *
     * @param context
     * @param id
     */
    public static void deleteConversation(Context context, String id) {
        try {
            DbCacheUtils.getDb(context).delete(Conversation.class, WhereBuilder.b("id", "=", id));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 删除会话列表
     *
     * @param context
     * @param conversationList
     */
    public static void deleteConversationList(Context context, List<Conversation> conversationList) {
        try {
            if (conversationList == null || conversationList.size() == 0) {
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
            Conversation conversation = DbCacheUtils.getDb(context).findById(Conversation.class, id);
            if (conversation != null) {
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
     *
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
     * 获取某个类型的会话
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

    /**
     * 获取整体的会话 （不区分类型）
     *
     * @param context
     * @return
     */
    public static List<Conversation> getConversationListByLastUpdate(Context context) {
        List<Conversation> conversationList = null;
        try {
            conversationList = DbCacheUtils.getDb(context).selector(Conversation.class).orderBy("lastUpdate", true).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (conversationList == null) {
            conversationList = new ArrayList<>();
        }
        return conversationList;
    }

    /**
     * 获取某个类型的会话
     *
     * @param context
     * @param type
     * @return
     */
    public static List<Conversation> getConversationListByLastUpdate(Context context, String type) {
        List<Conversation> conversationList = null;
        try {
            conversationList = DbCacheUtils.getDb(context).selector(Conversation.class).where("type", "=", type).orderBy("lastUpdate", true).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (conversationList == null) {
            conversationList = new ArrayList<>();
        }
        return conversationList;
    }

    public static void setConversationMember(Context context, String id, List<String> uidList) {
        if (uidList != null) {
            try {
                JSONArray array = JSONUtils.toJSONArray(uidList);
                DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id), new KeyValue("members", array.toString()));
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

    }

    public static List<SearchModel> getSearchConversationSearchModelList(Context context,
                                                                         String searchText) {
        List<Conversation> conversationList = null;
        if (!StringUtils.isBlank(searchText)) {

            try {
                String searchStr = "";
                for (int i = 0; i < searchText.length(); i++) {
                    if (i < searchText.length() - 1) {
                        searchStr += "%" + searchText.charAt(i);
                    } else {
                        searchStr += "%" + searchText.charAt(i) + "%";
                    }
                }
                conversationList = DbCacheUtils.getDb(context).selector(Conversation.class)
                        .where("showName", "like", searchStr)
                        .and(WhereBuilder.b("type", "=", Conversation.TYPE_GROUP)).orderBy("lastUpdate", true).findAll();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        if (conversationList == null) {
            conversationList = new ArrayList<>();
        }
        return Conversation.conversationList2SearchModelList(conversationList);
    }

    public static List<SearchModel> getSearchConversationPrivateChatSearchModelList(Context context,
                                                                                    String searchText) {
        List<Conversation> conversationList = null;
        if (!StringUtils.isBlank(searchText)) {

            try {
                String searchStr = "";
                for (int i = 0; i < searchText.length(); i++) {
                    if (i < searchText.length() - 1) {
                        searchStr += "%" + searchText.charAt(i);
                    } else {
                        searchStr += "%" + searchText.charAt(i) + "%";
                    }
                }
                conversationList = DbCacheUtils.getDb(context).selector(Conversation.class)
                        .where("showName", "like", searchStr)
                        .and(WhereBuilder.b("type", "=", Conversation.TYPE_DIRECT)).orderBy("lastUpdate", true).findAll();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        if (conversationList == null) {
            conversationList = new ArrayList<>();
        }
        return Conversation.conversationList2SearchModelList(conversationList);
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


    /**
     * 获取云+conversation
     *
     * @param context
     * @return
     */
    public static Conversation getCustomerConversation(Context context) {
        try {
            Conversation conversation = DbCacheUtils.getDb(context).selector
                    (Conversation.class)
                    .where("name", "=", "BOT6005")
                    .and("type", "=", Conversation.TYPE_CAST).findFirst();
            return conversation;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Conversation 修改拼音字段
     *
     * @param context
     * @param id
     * @param name
     */
    public static void updateConversationPyFull(Context context, String id, String name) {
        try {
            String pyFull = PinyinUtils.getPingYin(name);
            DbCacheUtils.getDb(context).update(Conversation.class, WhereBuilder.b("id", "=", id), new KeyValue("pyfull", pyFull));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public static List<Conversation> getConversationListByIdList(Context context, List<String> conversationIdList) {
        List<Conversation> conversationList = new ArrayList<>();
        try {
            conversationList = DbCacheUtils.getDb(context).selector(Conversation.class).where("id", "in", conversationIdList).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (conversationList == null) {
            conversationList = new ArrayList<>();
        }
        return conversationList;
    }
}
