package com.inspur.emmcloud.componentservice.communication;

import android.text.TextUtils;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PinyinUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/9/20.
 */

@Table(name = "Conversation")
public class Conversation implements Serializable {
    public static final String TYPE_DIRECT = "DIRECT";
    public static final String TYPE_GROUP = "GROUP";
    public static final String TYPE_CAST = "CAST";
    public static final String TYPE_LINK = "LINK";
    public static final String TYPE_TRANSFER = "FILE_TRANSFER";
    public static final String TYPE_SERVICE = "SERVICE";
    @Column(name = "id", isId = true)
    private String id;
    @Column(name = "enterprise")
    private String enterprise;
    @Column(name = "name")
    private String name;
    @Column(name = "owner")
    private String owner;
    @Column(name = "type")
    private String type;
    @Column(name = "state")
    private String state;
    @Column(name = "creationDate")
    private long creationDate;
    @Column(name = "lastUpdate")
    private long lastUpdate;
    @Column(name = "members")
    private String members;
    @Column(name = "administrators")
    private String administrators;
    @Column(name = "input")
    private String input;
    @Column(name = "dnd")
    private boolean dnd;
    @Column(name = "silent")
    private Integer silent = 0; //0表示不禁言 1表示禁言
    @Column(name = "stick")
    private boolean stick;
    @Column(name = "hide")
    private boolean hide;
    @Column(name = "action")
    private String action = "";
    @Column(name = "avatar")
    private String avatar = "";
    @Column(name = "pyFull")
    private String pyFull = "";
    @Column(name = "showName")
    private String showName = "";
    @Column(name = "serviceId")
    private String serviceId = "";
    private String draft = "";

    public Conversation() {

    }

    public Conversation(String id) {
        this.id = id;
    }

    public Conversation(JSONObject obj) {
        this.id = JSONUtils.getString(obj, "id", "");
        this.enterprise = JSONUtils.getString(obj, "enterprise", "");
        this.name = JSONUtils.getString(obj, "name", "");
        this.owner = JSONUtils.getString(obj, "owner", "");
        this.avatar = JSONUtils.getString(obj, "avatar", "");
        this.type = JSONUtils.getString(obj, "type", "");
        this.state = JSONUtils.getString(obj, "state", "");
        String UTCCreationDate = JSONUtils.getString(obj, "creationDate", "");
        this.creationDate = TimeUtils.UTCString2Long(UTCCreationDate);
        String UTCLastUpdate = JSONUtils.getString(obj, "lastUpdate", "");
        this.lastUpdate = TimeUtils.UTCString2Long(UTCLastUpdate);
        this.members = JSONUtils.getString(obj, "members", "");
        this.administrators = JSONUtils.getString(obj, "administrators", "");
        this.input = JSONUtils.getString(obj, "input", "");
        this.dnd = JSONUtils.getBoolean(obj, "dnd", false);
        this.silent = JSONUtils.getBoolean(obj, "silent", false) ? 1 : 0;
        this.stick = JSONUtils.getBoolean(obj, "stick", false);
        this.hide = JSONUtils.getBoolean(obj, "hide", false);
        this.action = JSONUtils.getString(obj, "action", "");
        this.serviceId = JSONUtils.getString(obj, "serviceId", "");
        Router router = Router.getInstance();
        if (router != null) {
            CommunicationService contactService = router.getService(CommunicationService.class);
            if (contactService != null) {
                this.showName = contactService.getShowName(this);
            }
        }
        this.pyFull = PinyinUtils.getPingYin(showName);
    }

    public static List<SearchModel> conversationList2SearchModelList(List<Conversation> conversationList) {
        List<SearchModel> searchModelList = new ArrayList<>();
        if (conversationList != null) {
            for (Conversation conversation : conversationList) {
                searchModelList.add(conversation.conversation2SearchModel());
            }
        }
        return searchModelList;
    }

    public String getId() {
        return id;
    }

    public String getServiceConversationId(){
        String conversationId = "";
        if (this.id.startsWith("FIBER")){
            String[] strings = id.split(":");
            conversationId = strings[1];
        }
        return conversationId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(String enterprise) {
        this.enterprise = enterprise;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }


    public String getAdministrators() {
        return administrators;
    }

    public void setAdministrators(String administrators) {
        this.administrators = administrators;
    }

    public ArrayList<String> getMemberList() {
        ArrayList<String> memberList = JSONUtils.JSONArray2List(members, new ArrayList<String>());
        return memberList;
    }

    public ArrayList<String> getAdministratorList() {
        ArrayList<String> administratorList = JSONUtils.JSONArray2List(administrators, new ArrayList<String>());
        return administratorList;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public boolean isDnd() {
        return dnd;
    }

    public void setDnd(boolean dnd) {
        this.dnd = dnd;
    }

    public boolean isSilent() {
        return silent > 0;
    }

    public void setSilent(boolean silent) {
        this.silent = silent ? 1 : 0;
    }

    public boolean isStick() {
        return stick;
    }

    public void setStick(boolean stick) {
        this.stick = stick;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public String getDraft() {
        return draft;
    }

    public void setDraft(String draft) {
        this.draft = draft;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPyFull() {
        return pyFull;
    }

    public void setPyFull(String pyFull) {
        this.pyFull = pyFull;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public boolean isServiceConversationType(){
        return !TextUtils.isEmpty(serviceId);
    }

    public SearchModel conversation2SearchModel() {
        SearchModel searchModel = new SearchModel();
        searchModel.setId(getId());
        searchModel.setName(getShowName());
        switch (type) {
            case TYPE_GROUP:
                searchModel.setType(SearchModel.TYPE_GROUP);
                searchModel.setIcon(getAvatar());
                break;
            case TYPE_DIRECT:
                searchModel.setIcon(getAvatar());
                searchModel.setType(SearchModel.TYPE_DIRECT);
                break;
            case TYPE_CAST:
                searchModel.setIcon(getAvatar());
                searchModel.setType(SearchModel.TYPE_STRUCT);
                break;
            case TYPE_LINK:
                searchModel.setIcon(getAvatar());
                searchModel.setType(SearchModel.TYPE_STRUCT);
                break;
            case TYPE_TRANSFER:
                searchModel.setIcon(getAvatar());
                searchModel.setType(SearchModel.TYPE_TRANSFER);
                break;
        }
        return searchModel;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Conversation))
            return false;

        final Conversation otherConversation = (Conversation) other;
        return getId().equals(otherConversation.getId());
    }
}
