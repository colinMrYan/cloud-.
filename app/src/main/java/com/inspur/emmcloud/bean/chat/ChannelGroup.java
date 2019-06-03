package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.ArrayList;

@Table(name = "ChannelGroup")
public class ChannelGroup {
    @Column(name = "cid", isId = true)
    private String cid = "";
    @Column(name = "channelName")
    private String channelName = "";
    @Column(name = "icon")
    private String icon = "";
    @Column(name = "members")
    private String members = "";
    @Column(name = "isPrivate")
    private boolean isPrivate = false;
    @Column(name = "pyFull")
    private String pyFull = "";
    @Column(name = "pyShort")
    private String pyShort = "";
    @Column(name = "type")
    private String type = "";
    @Column(name = "owner")
    private String owner = "";
    @Column(name = "inputs")
    private String inputs = "";
    @Column(name = "action")
    private String action = "";
    @Column(name = "avatar")
    private String avatar = "";

    public ChannelGroup() {

    }

    public ChannelGroup(String cid, String extra) {
        this.cid = cid;
    }

    public ChannelGroup(Channel channel) {
        this.cid = channel.getCid();
        this.type = channel.getType();
    }

    public ChannelGroup(String json) {
        try {
            JSONObject obj = new JSONObject(json);

            if (obj.has("cid")) {
                cid = obj.getString("cid");
            }

            if (obj.has("name")) {
                channelName = obj.getString("name");
            }
            if (obj.has("icon")) {
                icon = obj.getString("icon");
            }
            if (obj.has("members")) {
                members = obj.getString("members");
            }
            if (obj.has("private")) {
                isPrivate = obj.getBoolean("private");
            }
            if (obj.has("pyShort")) {
                pyShort = obj.getString("pyShort");
            }
            if (obj.has("pyFull")) {
                pyFull = obj.getString("pyFull");
            }
            if (obj.has("type")) {
                type = obj.getString("type");
            }

            if (obj.has("owner")) {
                owner = obj.getString("owner");
            }
            if (obj.has("inputs")) {
                this.inputs = obj.getString("inputs");
            }

            if (obj.has("action")) {
                this.action = obj.getString("action");
            }

            if (obj.has("avatar")) {
                this.avatar = obj.getString("avatar");
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public ChannelGroup(JSONObject obj) {
        try {
            if (obj.has("cid")) {
                cid = obj.getString("cid");
            }
            if (obj.has("name")) {
                channelName = obj.getString("name");
            }
            if (obj.has("icon")) {
                icon = obj.getString("icon");
            }
            if (obj.has("members")) {
                members = obj.getString("members");
            }
            if (obj.has("private")) {
                isPrivate = obj.getBoolean("private");
            }
            if (obj.has("pyShort")) {
                pyShort = obj.getString("pyShort");
            }
            if (obj.has("pyFull")) {
                pyFull = obj.getString("pyFull");
            }
            if (obj.has("type")) {
                type = obj.getString("type");
            }
            if (obj.has("owner")) {
                owner = obj.getString("owner");
            }
            if (obj.has("inputs")) {
                this.inputs = obj.getString("inputs");
            }

            if (obj.has("action")) {
                this.action = obj.getString("action");
            }

            if (obj.has("avatar")) {
                this.avatar = obj.getString("avatar");
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getIcon() {
        if (!icon.startsWith("http")) {
            if (type.equals("DIRECT")) {
                return APIUri.getUserInfoPhotoUrl(icon);
            } else {
                return APIUri.getPreviewUrl(icon);
            }

        }
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getPyFull() {
        return pyFull;
    }

    public void setPyFull(String pyFull) {
        this.pyFull = pyFull;
    }

    public String getPyShort() {
        return pyShort;
    }

    public void setPyShort(String pyShort) {
        this.pyShort = pyShort;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }


    public String getInputs() {
        return inputs;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public JSONArray getMembersArray() {
        JSONArray memberArray = null;
        try {
            memberArray = new JSONArray(members);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (memberArray == null) {
            memberArray = new JSONArray();
        }
        return memberArray;
    }

    public ArrayList<String> getMemberList() {
        ArrayList<String> memberList = JSONUtils.JSONArray2List(members, new ArrayList<String>());
        return memberList;
    }

    /*
     * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
     */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof ChannelGroup))
            return false;

        final ChannelGroup otherChannelGroup = (ChannelGroup) other;
        return getCid().equals(otherChannelGroup.getCid());
    }
}
