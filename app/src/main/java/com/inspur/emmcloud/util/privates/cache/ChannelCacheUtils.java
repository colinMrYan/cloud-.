package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.bean.chat.Channel;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 频道列表缓存处理类
 *
 * @author Administrator
 */
public class ChannelCacheUtils {
    private static final int SEARCH_ALL = 0;
    private static final int SEARCH_CONTACT = 2;
    private static final int SEARCH_CHANNELGROUP = 1;
    private static final int SEARCH_NOTHIING = 4;


    /**
     * 存储频道列表
     *
     * @param context
     * @param channelList
     */
    public static void saveChannelList(final Context context,
                                       final List<Channel> channelList) {

        // TODO Auto-generated method stub
        try {
            if (channelList == null || channelList.size() == 0) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(channelList);
        } catch (Exception e) {
            // TODO: handle exception
            LogUtils.debug("yfcLog", "机器人保存有异常：" + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void deleteChannelList(final Context context, final List<Channel> channelList) {
        // TODO Auto-generated method stub
        try {
            if (channelList == null || channelList.size() == 0) {
                return;
            }
            DbCacheUtils.getDb(context).delete(channelList);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 清除Channel表信息
     *
     * @param context
     */
    public static void clearChannel(Context context) {
        try {
            DbCacheUtils.getDb(context).delete(Channel.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 存储频道列表
     *
     * @param context
     * @param channel
     */
    public static void saveChannel(Context context, Channel channel) {
        try {
            if (channel == null) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(channel);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 删除频道
     *
     * @param context
     * @param cid
     */
    public static void deleteChannel(Context context, String cid) {
        try {
            DbCacheUtils.getDb(context).delete(Channel.class, WhereBuilder.b("cid", "=", cid));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    /**
     * 获取频道的类型
     *
     * @param context
     * @param cid
     * @return
     */
    public static String getChannelType(Context context, String cid) {
        String type = "";
        try {
            if (!StringUtils.isBlank(cid)) {
                Channel channel = DbCacheUtils.getDb(context).findById(Channel.class, cid);
                if (channel != null) {
                    type = channel.getType();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return type;
    }


    public static List<Channel> getCacheChannelList(Context context) {
        List<Channel> channelList = new ArrayList<Channel>();
        try {
            channelList = DbCacheUtils.getDb(context).selector(Channel.class).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (channelList == null) {
            channelList = new ArrayList<Channel>();
        }
        return channelList;
    }


    /**
     * 获取缓存中的频道
     *
     * @param context
     * @param cid
     * @return
     */
    public static Channel getChannel(Context context, String cid) {
        Channel channel = null;
        try {
            channel = DbCacheUtils.getDb(context).findById(Channel.class, cid);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return channel;
    }


    /**
     * 谋取某个类型【频道
     *
     * @param context
     * @param type
     * @return
     */
    public static List<Channel> getChannelList(Context context, String type) {
        List<Channel> channelList = new ArrayList<>();
        try {
            channelList = DbCacheUtils.getDb(context).selector(Channel.class).where("type", "=", type).findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (channelList == null) {
            channelList = new ArrayList<>();
        }
        return channelList;
    }

    /**
     * 搜索会话列表
     *
     * @param context
     * @param searchText
     * @return
     */
    public static List<Channel> getSearchChannelList(Context context,
                                                     String searchText, int selectContent) {
        List<Channel> searchChannelList = null;
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
                switch (selectContent) {
                    case SEARCH_ALL:
                    case SEARCH_NOTHIING:

                        searchChannelList = DbCacheUtils.getDb(context).selector
                                (Channel.class)
                                .where("type", " = ", "GROUP")
                                .and(WhereBuilder.b("pyFull", "like", searchStr)
                                        .or("title", "like", searchStr)).findAll();
                        break;
                    case SEARCH_CHANNELGROUP:
                        searchChannelList = DbCacheUtils.getDb(context).selector
                                (Channel.class)
                                .where(WhereBuilder.b("type", "=", "GROUP"))
                                .and(WhereBuilder.b("pyFull", "like", searchStr)
                                        .or("title", "like", searchStr)).findAll();
                        break;
                    case SEARCH_CONTACT:
                    default:
                        break;
                }

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        if (searchChannelList == null) {
            searchChannelList = new ArrayList<>();
        }
        return searchChannelList;

    }


    /**
     * 获取缓存中Channel的消息免打扰状态
     *
     * @param context
     * @param cid
     * @return
     */
    public static Boolean isChannelNotDisturb(Context context, String cid) {
        boolean isChannelNotDisturb = false;
        Channel channel = null;
        try {
            channel = DbCacheUtils.getDb(context).findById(Channel.class, cid);
            if (channel != null) {
                isChannelNotDisturb = channel.getDnd();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return isChannelNotDisturb;
    }

    /**
     * 获取云+客服channel
     *
     * @param context
     * @return
     */
    public static Channel getCustomerChannel(Context context) {
        String uid = PreferencesUtils.getString(context, "userID", "");
        Channel channel = null;
        try {
            channel = DbCacheUtils.getDb(context).selector
                    (Channel.class)
                    .where("title", "=", "BOT6005" + "-" + uid)
                    .and("type", "=", "SERVICE").findFirst();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return channel;
    }
}
