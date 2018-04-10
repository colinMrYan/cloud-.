package com.inspur.emmcloud.bean.chat;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.PinyinUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.common.richtext.markdown.MarkDown;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Table(name = "Channel")
public class Channel implements Serializable {
    @Column(name = "cid", isId = true)
    private String cid = "";
    @Column(name = "weight")
    private Integer weight = -1;
    @Column(name = "title")
    private String title = "";
    @Column(name = "icon")
    private String icon = "";
    @Column(name = "lastUpdate")
    private String lastUpdate = "";
    @Column(name = "msgLastUpdate")
    private long msgLastUpdate = 0; // 此频道中消息的最后更新时间，频道以此进行排序
    @Column(name = "type")
    private String type = "";
    @Column(name = "source")
    private String source = "";
    @Column(name = "isShowNotify")
    private boolean isShowNotify = false;
    @Column(name = "isSetTop")
    private boolean isSetTop = false;
    @Column(name = "isHide")
    private boolean isHide = false;
    @Column(name = "setTopTime")
    private long setTopTime = 0;
    @Column(name = "pyFull")
    private String pyFull = "";
    @Column(name = "dnd")
    private boolean dnd = false;
    @Column(name = "inputs")
    private String inputs = "";
    private List<Msg> newMsgList = new ArrayList<Msg>();
    private int unReadCount = 0;
    private String displayTitle = "";//session显示的名字
    private String newMsgContent = "";

    public Channel() {

    }

    public Channel(String cid) {
        this.cid = cid;
    }

    public Channel(JSONObject obj) {
        try {
            // newestMsg = new Msg();
            if (obj.has("cid")) {
                this.cid = obj.getString("cid");
            }
            if (obj.has("title")) {
                this.title = obj.getString("title");
                this.pyFull = PinyinUtils.getPingYin(title);
            }
            if (obj.has("icon")) {
                this.icon = obj.getString("icon");
            }
            if (obj.has("lastUpdate")) {
                this.lastUpdate = obj.getString("lastUpdate");
                // 解决最新创建的会话无法显示在列表的最上端的问题
                setMsgLastUpdate(TimeUtils.UTCString2Long(lastUpdate));
            }
            // if (obj.has("weight")) {
            // this.weight = obj.getInt("weight");
            // }
            if (obj.has("type")) {
                this.type = obj.getString("type");
            }
            if (obj.has("source")) {
                this.source = obj.getString("source");
            }

            if (obj.has("dnd")) {
                this.dnd = obj.getBoolean("dnd");
            }
            if (obj.has("inputs")) {
                this.inputs = obj.getString("inputs");
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public Channel(ChannelGroup channelGroup) {
        this.cid = channelGroup.getCid();
        this.title = channelGroup.getChannelName();
        this.type = channelGroup.getType();
        this.inputs = channelGroup.getInputs();
    }

    public void setNewMsgList(Context context, List<Msg> msgList) {
        newMsgList.clear();
        if (msgList != null) {
            newMsgList.addAll(msgList);
        }
        String lastMsgTime = lastUpdate;
        if (newMsgList.size() > 0) {
            Msg msg = newMsgList.get(newMsgList.size() - 1);
            lastMsgTime = msg.getTime();
        }
        setMsgLastUpdate(TimeUtils.UTCString2Long(lastMsgTime));
        setNewMsgContent(context);
    }

    public void setMsgLastUpdate(long time) {
        this.msgLastUpdate = time;
    }

    public long getMsgLastUpdate() {
        return msgLastUpdate;
    }

    public List<Msg> getNewMsgList() {
        return newMsgList;
    }

    public void addReceivedNewMsg(Msg receivedMsg) {
        newMsgList.add(receivedMsg);
        setMsgLastUpdate(TimeUtils.UTCString2Long(receivedMsg.getTime()));
    }

    public boolean getIsShowNotify() {
        return isShowNotify;
    }

    public void setIsShowNotify(boolean isShowNotify) {
        this.isShowNotify = isShowNotify;
    }

    public String getNewestMid() {
        if (newMsgList.size() > 0) {
            Msg msg = newMsgList.get(newMsgList.size() - 1);
            return msg.getMid();
        } else {
            return null;
        }
    }

    public String getNewMsgContent() {
        return newMsgContent;
    }


    public void setNewMsgContent(Context context) {
        if (newMsgList.size() > 0) {
            Msg msg = newMsgList.get(newMsgList.size() - 1);
            String title = msg.getTitle();
            String uid = ((MyApplication) context.getApplicationContext()).getUid();
            String msgType = msg.getType();
            if (type.equals("DIRECT") || ((!type.equals("DIRECT")) && uid.equals(msg.getUid()))) {
                title = "";
            } else {
                title = title + "：";
            }

            switch (msgType) {
                case "text":
                    newMsgContent = title + msg.getBody();
                    break;
                case "image":
                case "res_image":
                    newMsgContent = title + context.getString(R.string.send_a_picture);
                    break;
                case "txt_comment":
                case "comment":
                    newMsgContent = title + context.getString(R.string.send_a_comment);
                    break;
                case "file":
                case "res_file":
                    newMsgContent = title + context.getString(R.string.send_a_file);
                    break;
                case "news":
                    newMsgContent = msg.getNTitle();
                    break;
                case "res_link":
                    newMsgContent = title + context.getString(R.string.share_a_link);
                    break;
                case "txt_rich":
                    String msgBody = msg.getBody();
                    String source = JSONUtils.getString(msgBody, "source", "");
                    newMsgContent = source;
                    if (!StringUtils.isBlank(source)) {
                        newMsgContent = MarkDown.fromMarkdown(source);
                    }
                    if (type.equals("GROUP")) {
                        newMsgContent = title + newMsgContent;
                    }
                    break;
                default:
                    newMsgContent = title + context
                            .getString(R.string.send_a_message_of_unknown_type);
                    break;
            }
        } else {
            if (type.equals("SERVICE")) {
                newMsgContent = context
                        .getString(R.string.welcome_to_attention) + " " + DirectChannelUtils.getRobotInfo(context, title).getName();
            } else if (type.equals("GROUP")) {
                newMsgContent = context.getString(
                        R.string.group_no_message);
            } else {
                newMsgContent = context.getString(
                        R.string.direct_no_message);
            }
        }

    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public boolean getDnd() {
        return dnd;
    }

    public void setDnd(boolean dnd) {
        this.dnd = dnd;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLastUpdate() {
        if (newMsgList.size() > 0) {
            Msg msg = newMsgList.get(newMsgList.size() - 1);
            lastUpdate = msg.getTime();
        }
        return lastUpdate;

    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean getIsSetTop() {
        return isSetTop;
    }

    public void setIsSetTop(boolean isSetTop) {
        this.isSetTop = isSetTop;
    }

    public boolean getIsHide() {
        return isHide;
    }

    public void setIsHide(boolean isHide) {
        this.isHide = isHide;
    }

    public void setTopTime(long setTopTime) {
        this.setTopTime = setTopTime;
    }

    public long getTopTime() {
        return setTopTime;
    }

    public String getInputs() {
        return inputs;
    }


    public void setInputs(String inputs) {
        this.inputs = inputs;
    }

    public int getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getPyFull() {
        return pyFull;
    }

    public void setPyFull(String pyFull) {
        this.pyFull = pyFull;
    }


    /*
         * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
         */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Channel))
            return false;

        final Channel otherChanel = (Channel) other;
        if (!getCid().equals(otherChanel.getCid()))
            return false;
        return true;
    }

    public class SortComparator implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            Channel channelA = (Channel) lhs;
            Channel channelB = (Channel) rhs;
            long diff = channelA.getMsgLastUpdate()
                    - channelB.getMsgLastUpdate();
            if (diff > 0) {
                return -1;
            } else if (diff == 0) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
