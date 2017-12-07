package com.inspur.emmcloud.bean;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.api.APIUri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.ArrayList;
import java.util.List;

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

    public ChannelGroup() {

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

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getCid() {
        return cid;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public void setMembers(String members) {
        this.members = members;
    }

    public String getMembers() {
        return members;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void setPyFull(String pyFull) {
        this.pyFull = pyFull;
    }

    public String getPyFull() {
        return pyFull;
    }

    public void setPyShort(String pyShort) {
        this.pyShort = pyShort;
    }

    public String getPyShort() {
        return pyShort;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
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

    public List<String> getMemberList() {
        List<String> memberList = JSON.parseArray(members, String.class);
        if (memberList == null) {
            memberList = new ArrayList<>();
        }
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
        if (getCid().equals(otherChannelGroup.getCid())) {
            return true;
        }
        return false;
    }
}
