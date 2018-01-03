package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.util.common.StringUtils;

import org.json.JSONArray;
import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 频道列表缓存处理类
 *
 * @author Administrator
 */
public class ChannelGroupCacheUtils {

    /**
     * 存储群组列表
     *
     * @param context
     * @param channelGroupList
     */
    public static void saveChannelGroupList(final Context context,
                                            final List<ChannelGroup> channelGroupList) {
        try {
            if (channelGroupList == null || channelGroupList.size() == 0) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(channelGroupList);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    /**
     * 清除ChannelGroup信息
     * @param context
     */
    public static void clearChannelGroupList(final Context context){
        try {
            DbCacheUtils.getDb(context).delete(ChannelGroup.class);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 存储群组信息
     *
     * @param context
     * @param channelGroup
     */
    public static void saveChannelGroup(final Context context,
                                        final ChannelGroup channelGroup) {
        try {
            if (channelGroup == null) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(channelGroup);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    /**
     * 获取所有群组
     *
     * @param context
     * @return
     */
    public static List<ChannelGroup> getAllChannelGroupList(Context context) {
        List<ChannelGroup> ChannelGroupList = null;

        try {
            ChannelGroupList = DbCacheUtils.getDb(context).findAll(ChannelGroup.class);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (ChannelGroupList == null) {
            ChannelGroupList = new ArrayList<ChannelGroup>();
        }
        return ChannelGroupList;

    }

    public static ChannelGroup getChannelGroupById(Context context, String cid) {
        ChannelGroup channelGroup = null;
        try {
            channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return channelGroup;
    }

    /**
     * 搜索群组
     *
     * @param context
     * @param searchText
     * @return
     */
    public static List<ChannelGroup> getSearchChannelGroupList(Context context,
                                                               String searchText) {
        List<ChannelGroup> searchChannelGroupList = null;
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
                searchChannelGroupList = DbCacheUtils.getDb(context).selector
                        (ChannelGroup.class)
                        .where("pyFull", "like", searchStr)
                        .or("pyShort", "like", searchStr).or("channelName", "like", searchStr).findAll();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        if (searchChannelGroupList == null) {
            searchChannelGroupList = new ArrayList<ChannelGroup>();
        }
        return searchChannelGroupList;

    }

    public static List<String> getMemberUidList(Context context, String cid,
                                                int limit) {
        List<String> userList = new ArrayList<String>();
        try {
            ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
            if (channelGroup != null) {
                List<String>  allMemberList = channelGroup.getMemberList();
                int size = allMemberList.size();
                if (limit <= 0 || limit > size){
                    limit = size;
                }
                List<String>  limitMemberList = allMemberList.subList(0,limit);
                userList.addAll(limitMemberList);
            }

        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return  userList;
    }

    /**
     * 获取群组成员
     * @param context
     * @param channelGroup
     * @param limit
     * @return
     */
    public static List<String> getMemberUidList(Context context,  ChannelGroup channelGroup,
                                                int limit) {
        List<String> userList = new ArrayList<String>();
        try {
            if (channelGroup != null) {
                List<String>  allMemberList = channelGroup.getMemberList();
                if (limit == 0 ){
                    userList.addAll(allMemberList);
                }else {
                    int size = allMemberList.size();
                    if (size < limit) {
                        limit = size;
                    }
                    for (int i = 0; i < limit; i++) {
                        String uid = allMemberList.get(i);
                        userList.add(i,uid);
                    }
                }
            }

        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return  userList;
    }

    /**
     * 获取群组中成员列表
     *
     * @param context
     * @param cid
     * @param limit
     * @return
     */
    public static List<Contact> getMembersList(Context context, String cid,
                                               int limit) {
        List<Contact> userList = new ArrayList<Contact>();
        try {
            ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
            if (channelGroup != null) {
                JSONArray memberArray = new JSONArray();
                JSONArray allMemberArray = channelGroup.getMembersArray();
                if (limit == 0) {
                    memberArray = allMemberArray;
                } else {
                    int size = allMemberArray.length();
                    if (size < limit) {
                        limit = size;
                    }
                    for (int i = 0; i < limit; i++) {
                        String uid = allMemberArray.getString(i);
                        memberArray.put(i, uid);
                    }
                }

                userList = ContactCacheUtils.getSoreUserList(context, memberArray);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (userList == null) {
            userList = new ArrayList<Contact>();
        }
        return userList;

    }

    public static List<Contact> getMembersList(Context context, String cid) {
        return getMembersList(context, cid, 0);
    }

    public static List<Map<String, String>> getMembersMapList(Context context,
                                                              String cid) {
        List<Map<String, String>> userMapList = new ArrayList<Map<String, String>>();
        try {
            ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
            if (channelGroup != null) {
                String members = channelGroup.getMembers();
                JSONArray memberArray = new JSONArray(members);
                userMapList = ContactCacheUtils
                        .getUserMap(context, memberArray);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (userMapList == null) {
            userMapList = new ArrayList<Map<String, String>>();
        }
        return userMapList;

    }

    /**
     * 更改群组名称
     *
     * @param context
     * @param cid
     * @param name
     */
    public static void updateChannelGroupName(Context context, String cid, String name) {
        try {
            DbCacheUtils.getDb(context).update(ChannelGroup.class, WhereBuilder.b("cid", "=", cid),new KeyValue("channelName",name));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 更改群组名称全拼
     *
     * @param context
     * @param cid
     * @param pyFull
     */
    public static void updateChannelGroupNameFull(Context context, String cid, String pyFull) {
        try {

            DbCacheUtils.getDb(context).update(ChannelGroup.class, WhereBuilder.b("cid", "=", cid),new KeyValue("pyFull",pyFull));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 更改群组名称缩写
     *
     * @param context
     * @param cid
     * @param pyShort
     */
    public static void updateChannelGroupNameShort(Context context, String cid, String pyShort) {
        try {
            DbCacheUtils.getDb(context).update(ChannelGroup.class, WhereBuilder.b("cid", "=", cid),new KeyValue("pyShort",pyShort));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 更改群组成员
     *
     * @param context
     * @param cid
     * @param members
     */
    public static void updateChannelGroupMembers(Context context, String cid, String members) {
        try {
            DbCacheUtils.getDb(context).update(ChannelGroup.class, WhereBuilder.b("cid", "=", cid),new KeyValue("members",members));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


}
