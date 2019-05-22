package com.inspur.emmcloud.bean.contact;

import android.content.Context;

import com.facebook.react.bridge.ReadableMap;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Table(name = "SearchModel")
public class SearchModel implements Serializable {
    public static final String TYPE_USER = "USER";
    public static final String TYPE_STRUCT = "STRUCT";
    public static final String TYPE_GROUP = "GROUP";
    @Column(name = "id", isId = true)
    private String id = "";
    @Column(name = "name")
    private String name = "";
    @Column(name = "type")
    private String type = ""; // 单人：user 组织：struct 群组：channelGroup
    @Column(name = "icon")
    private String icon = "";
    @Column(name = "heat")
    private int heat = 0;
    private String email = "";

    public SearchModel() {

    }

    public SearchModel(ChannelGroup channelGroup) {
        if (channelGroup == null) {
            return;
        }
        this.id = channelGroup.getCid();
        this.name = channelGroup.getChannelName();
        this.type = TYPE_GROUP;
        this.icon = channelGroup.getIcon();
    }

    public SearchModel(Conversation conversation) {
        if (conversation == null) {
            return;
        }
        this.id = conversation.getId();
        this.name = conversation.getName();
        this.type = TYPE_GROUP;
        this.icon = conversation.getAvatar();
    }

    public SearchModel(Contact contact) {
        if (contact == null) {
            return;
        }
        type = contact.getType();
        name = contact.getName();
        id = contact.getId();
        this.email = contact.getEmail();
    }

    public SearchModel(ContactUser contactUser) {
        if (contactUser == null) {
            return;
        }
        this.type = TYPE_USER;
        this.id = contactUser.getId();
        this.name = contactUser.getName();
        this.email = contactUser.getEmail();

    }

    public SearchModel(ContactOrg contactOrg) {
        if (contactOrg == null) {
            return;
        }
        this.type = TYPE_STRUCT;
        this.id = contactOrg.getId();
        this.name = contactOrg.getName();
    }

    public SearchModel(ReadableMap nativeInfo) {
        try {
            if (nativeInfo.hasKey("inspur_id")) {
                id = nativeInfo.getString("inspur_id");
            }
            if (nativeInfo.hasKey("real_name")) {
                name = nativeInfo.getString("real_name");
            }
            if (nativeInfo.hasKey("type")) {
                type = nativeInfo.getString("type").toUpperCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public SearchModel(Channel channel) {
        if (channel == null) {
            return;
        }
        id = channel.getCid();
        name = channel.getTitle();
        type = channel.getType();
    }

    public SearchModel(String uid){
        try{
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
            if (contactUser == null) {
                return;
            }
            this.type = TYPE_USER;
            this.id = contactUser.getId();
            this.name = contactUser.getName();
            this.email = contactUser.getEmail();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static List<SearchModel> channelGroupList2SearchModelList(List<ChannelGroup> channelGroupList) {
        List<SearchModel> searchModelList = new ArrayList<>();
        if (channelGroupList != null) {
            for (ChannelGroup channelGroup : channelGroupList) {
                searchModelList.add(new SearchModel(channelGroup));
            }
        }
        return searchModelList;
    }

    public static List<SearchModel> conversationList2SearchModelList(List<Conversation> conversationList) {
        List<SearchModel> searchModelList = new ArrayList<>();
        if (conversationList != null) {
            for (Conversation conversation : conversationList) {
                searchModelList.add(new SearchModel(conversation));
            }
        }
        return searchModelList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 中文名+英文名
     *
     * @return
     */
    public String getCompleteName() {
        String completeName = name;
        String globalName = null;
        if (type.equals(TYPE_USER)) {
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(id);
            if (contactUser != null) {
                globalName = contactUser.getNameGlobal();
            }
        } else if (type.equals(TYPE_STRUCT)) {
            ContactOrg contactOrg = ContactOrgCacheUtils.getContactOrg(id);
            if (contactOrg != null) {
                globalName = contactOrg.getNameGlobal();
            }
        }
        if (!StringUtils.isBlank(globalName)) {
            completeName = completeName + "（" + globalName + "）";
        }
        return completeName;

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getHeat() {
        return heat;
    }

    public void setHeat(int heat) {
        this.heat = heat;
    }

    public String getIcon(Context context) {
        return icon;
    }

    /*
     * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
     */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof SearchModel))
            return false;

        final SearchModel otherSearchModel = (SearchModel) other;
        if (!getId().equals(otherSearchModel.getId()))
            return false;
        if (!getName().equals(otherSearchModel.getName()))
            return false;
        return getType().equals(otherSearchModel.getType());
    }

    @Override
    public String toString() {
        return "SearchModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", icon='" + icon + '\'' +
                ", heat=" + heat +
                '}';
    }
}
