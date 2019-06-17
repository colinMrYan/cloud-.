package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.contact.SearchModel;

import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.List;

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
     *
     * @param context
     */
    public static void clearChannelGroupList(final Context context) {
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
    public static List<SearchModel> getSearchChannelGroupSearchModelList(Context context,
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
        return SearchModel.channelGroupList2SearchModelList(searchChannelGroupList);

    }


    /**
     * 群头像获取群里人员id在通讯录里存在的人，保证群头像完整
     *
     * @param context
     * @param cid
     * @param limit   指定返回的成员个数，遍历所有的人员直到满足个数或者所有人员已全部遍历
     * @return
     */
    public static List<String> getExistMemberUidList(Context context, String cid,
                                                     int limit) {
        List<String> userList = new ArrayList<>();
        try {
            ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
            if (channelGroup == null) {
                return new ArrayList<>();
            }
            List<String> allMemberList = channelGroup.getMemberList();
            //遍历如果头像存在则加入
            for (int i = 0; i < allMemberList.size(); i++) {
                String url = APIUri.getChannelImgUrl(context, allMemberList.get(i));
                if (!StringUtils.isBlank(url)) {
                    userList.add(allMemberList.get(i));
                    if (userList.size() >= limit) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    public static List<String> getMemberUidList(Context context, String cid,
                                                int limit) {
        List<String> userList = new ArrayList<String>();
        try {
            ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
            if (channelGroup != null) {
                List<String> allMemberList = channelGroup.getMemberList();
                int size = allMemberList.size();
                if (limit <= 0 || limit > size) {
                    limit = size;
                }
                List<String> limitMemberList = allMemberList.subList(0, limit);
                userList.addAll(limitMemberList);
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return userList;
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
            DbCacheUtils.getDb(context).update(ChannelGroup.class, WhereBuilder.b("cid", "=", cid), new KeyValue("channelName", name));
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

            DbCacheUtils.getDb(context).update(ChannelGroup.class, WhereBuilder.b("cid", "=", cid), new KeyValue("pyFull", pyFull));
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
            DbCacheUtils.getDb(context).update(ChannelGroup.class, WhereBuilder.b("cid", "=", cid), new KeyValue("pyShort", pyShort));
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
            DbCacheUtils.getDb(context).update(ChannelGroup.class, WhereBuilder.b("cid", "=", cid), new KeyValue("members", members));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


}
