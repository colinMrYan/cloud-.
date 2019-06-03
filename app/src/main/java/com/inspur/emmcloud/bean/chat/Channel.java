package com.inspur.emmcloud.bean.chat;

import android.content.Context;
import android.text.SpannableString;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.richtext.markdown.MarkDown;

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
    @Column(name = "action")
    private String action = "";
    @Column(name = "avatar")
    private String avatar = "";
    private List<Msg> newMsgList = new ArrayList<>();
    private List<Message> newMessageList = new ArrayList<>();
    private long unReadCount = 0;
    private String displayTitle = "";//session显示的名字
    private String newMsgContent = "";
    private String showIcon = "";

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
//                this.pyFull = PinyinUtils.getPingYin(title);
            }
            if (obj.has("icon")) {
                this.icon = obj.getString("icon");
            }
            if (obj.has("lastUpdate")) {
                this.lastUpdate = obj.getString("lastUpdate");
                // 解决最新创建的会话无法显示在列表的最上端的问题
                setMsgLastUpdate(TimeUtils.UTCString2Long(lastUpdate));
            }
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
        long lastMsgTime;
        if (newMsgList.size() > 0) {
            lastMsgTime = newMsgList.get(newMsgList.size() - 1).getTime();
        } else {
            lastMsgTime = TimeUtils.UTCString2Long(lastUpdate);
        }

        setMsgLastUpdate(lastMsgTime);
        setNewMsgContent(context);
    }

    public void setNewMessageList(Context context, List<Message> messageList) {
        newMessageList.clear();
        if (messageList != null) {
            newMessageList.addAll(messageList);
        }
        long lastMsgTime;
        if (newMessageList.size() > 0) {
            lastMsgTime = newMessageList.get(newMessageList.size() - 1).getCreationDate();
        } else {
            lastMsgTime = TimeUtils.UTCString2Long(lastUpdate);
        }
        setMsgLastUpdate(lastMsgTime);
        setNewMessageContent(context);

    }

    public long getMsgLastUpdate() {
        return msgLastUpdate;
    }

    public void setMsgLastUpdate(long time) {
        this.msgLastUpdate = time;
    }

    public List<Msg> getNewMsgList() {
        return newMsgList;
    }

    public void addReceivedNewMsg(Msg receivedMsg) {
        newMsgList.add(receivedMsg);
        setMsgLastUpdate(receivedMsg.getTime());
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
                    newMsgContent = msg.getnTitle();
                    break;
                case "res_link":
                    newMsgContent = title + context.getString(R.string.send_a_link);
                    break;
                case "txt_rich":
                    String msgBody = msg.getBody();
                    String source = JSONUtils.getString(msgBody, "source", "");
                    if (!StringUtils.isBlank(source) && msg.getUid().toLowerCase().startsWith("bot")) {
                        newMsgContent = MarkDown.fromMarkdown(source);
                    } else {
                        newMsgContent = MentionsAndUrlShowUtils.getMsgContentSpannableString(msgBody).toString();
                    }
                    if (type.equals("GROUP")) {
                        newMsgContent = title + newMsgContent;
                    }
                    if (StringUtils.isEmpty(newMsgContent) && type.equals("SERVICE") && getTitle().contains("BOT6006")) {
                        newMsgContent = context.getString(R.string.welcome_to_attention) + " " + DirectChannelUtils.getRobotInfo(context, getTitle()).getName();
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
            } else if (type.equals("LINK")) {
                newMsgContent = context.getString(R.string.welcome_to) + " " + title;
            } else {
                newMsgContent = context.getString(
                        R.string.direct_no_message);
            }
        }

    }

    public String getShowIcon() {
        return showIcon;
    }

    public void setShowIcon(String showIcon) {
        this.showIcon = showIcon;
    }

    public void setNewMessageContent(Context context) {
        if (newMessageList.size() > 0) {
            Message message = newMessageList.get(newMessageList.size() - 1);
            String fromUserName = "";
            String messageType = message.getType();
//            if (!type.equals("DIRECT") && !message.getFromUser().equals(MyApplication.getInstance().getUid())) {
//                fromUserName = ContactUserCacheUtils.getUserName(message.getFromUser()) + "：";
//            }
            if (type.equals("GROUP") && !message.getFromUser().equals(MyApplication.getInstance().getUid())) {
                fromUserName = ContactUserCacheUtils.getUserName(message.getFromUser()) + "：";
            }
            switch (messageType) {
                case Message.MESSAGE_TYPE_TEXT_PLAIN:
                    newMsgContent = fromUserName + ChatMsgContentUtils.mentionsAndUrl2Span(context, message.getMsgContentTextPlain().getText(), message.getMsgContentTextPlain().getMentionsMap()).toString();
                    break;
                case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                    SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(context, message.getMsgContentTextMarkdown().getText(), message.getMsgContentTextMarkdown().getMentionsMap());
                    String markdownString = spannableString.toString();
                    if (!StringUtils.isBlank(markdownString)) {
                        markdownString = MarkDown.fromMarkdown(markdownString);
                    }
                    newMsgContent = fromUserName + markdownString;
                    break;
                case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                    newMsgContent = fromUserName + context.getString(R.string.send_a_comment);
                    break;
                case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                    newMsgContent = fromUserName + context.getString(R.string.send_a_file);
                    break;
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    newMsgContent = fromUserName + context.getString(R.string.send_a_picture);
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                    newMsgContent = fromUserName + context.getString(R.string.send_a_link);
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_CONTACT_CARD:
                    newMsgContent = fromUserName + context.getString(R.string.send_a_link);
                    break;
                case Message.MESSAGE_TYPE_MEDIA_VOICE:
                    newMsgContent = fromUserName + context.getString(R.string.send_a_voice);
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_SELECTED:
                    newMsgContent = MyApplication.getInstance().getString(R.string.send_action_message);
                    break;
                default:
                    newMsgContent = fromUserName + context
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
            } else if (type.equals("LINK")) {
                newMsgContent = context.getString(R.string.welcome_to) + " " + title;
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

    public void setType(String type) {
        this.type = type;
    }

    public boolean getDnd() {
        return dnd;
    }

    public void setDnd(boolean dnd) {
        this.dnd = dnd;
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

    public long getTopTime() {
        return setTopTime;
    }

    public void setTopTime(long setTopTime) {
        this.setTopTime = setTopTime;
    }

    public String getInputs() {
        return inputs;
    }


    public void setInputs(String inputs) {
        this.inputs = inputs;
    }

    public long getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(long unReadCount) {
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
        return getCid().equals(otherChanel.getCid());
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
