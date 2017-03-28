package com.inspur.emmcloud.bean;

import android.content.Context;
import android.text.SpannableString;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.DirectChannelUtils;
import com.inspur.emmcloud.util.HandMentionsMsgUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.PinyinUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Transient;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Channel implements Serializable {
	@Id
	private String cid = "";
	private Integer weight = -1;
	private String title = "";
	private String icon = "";
	private String lastUpdate = "";
	private long msgLastUpdate = 0; // 此频道中消息的最后更新时间，频道以此进行排序
	private String type = "";
	private String source = "";
	private boolean isShowNotify = false;
	private boolean isSetTop = false;
	private boolean isHide = false;
	private long setTopTime = 0;
	private String pyFull = "";
	private boolean dnd = false;
	private String inputs = "";
	// Transient使这个列被忽略，不存入数据库
	@Transient
	private List<Msg> newMsgList = new ArrayList<Msg>();

	// private Msg newestMsg;
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
			if(obj.has("inputs")){
				this.inputs = obj.getString("inputs");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void setNewMsgList(List<Msg> msgList) {
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

	public String getNewestMsgContent(Context context) {
		String newestMsgContent = "";
		if (newMsgList.size() > 0) {
			Msg msg = newMsgList.get(newMsgList.size() - 1);
			String title = msg.getTitle();
			String uid = PreferencesUtils.getString(context, "userID");
			if (type.equals("DIRECT") || ((!type.equals("DIRECT"))&& uid.equals(msg.getUid()))) {
				title = "";
			}else{
				title = title+"：";
			}
			if (msg.getType().equals("text")) {
				newestMsgContent = title + msg.getBody();
			} else if (msg.getType().equals("image")
					|| msg.getType().equals("res_image")) {
				newestMsgContent = title + context.getString(R.string.send_a_picture);
			} else if (msg.getType().equals("txt_comment")
					|| msg.getType().equals("comment")) {
				newestMsgContent = title + context.getString(R.string.send_a_comment);
			} else if (msg.getType().equals("file")
					|| msg.getType().equals("res_file")) {
				newestMsgContent = title + context.getString(R.string.send_a_file);
			} else if (msg.getType().equals("news")) {
				newestMsgContent = msg.getNTitle();
			} else if (msg.getType().equals("res_link")) {
				newestMsgContent =  title +context.getString(R.string.share_a_link);
			} else if (msg.getType().equals("act_meeting")) {
				newestMsgContent =  title +context.getString(R.string.send_a_meeting_invitation);
			} else if (msg.getType().equals("txt_rich")) {
				if (type.equals("DIRECT")) {
					newestMsgContent = HandMentionsMsgUtils.getRichText(msg);
				}else if(type.equals("SERVICE")){
					String msgBody = msg.getBody();
					String source = JSONUtils.getString(msgBody, "source", "");
					String[] mentions = JSONUtils.getString(msgBody, "mentions", "")
							.replace("[", "").replace("]", "").split(",");
					String[] urls = JSONUtils.getString(msgBody, "urls", "")
							.replace("[", "").replace("]", "").split(",");
					List<String> mentionList = Arrays.asList(mentions);
					List<String> urlList = Arrays.asList(urls);
					SpannableString spannableString = MentionsAndUrlShowUtils
							.handleMentioin(source, mentionList, urlList);
					newestMsgContent = spannableString.toString();
				} else {
					newestMsgContent = ( title + HandMentionsMsgUtils
							.getRichText(msg));
				}
			} else if (msg.getType().equals("act_meeting_cancel")) {
				newestMsgContent =  title +context.getString(R.string.cancel_a_meeting);
			} else if (msg.getType().equals("act_meeting_approve")) {
				newestMsgContent =  title +context.getString(R.string.send_a_meeting_request);
			} else {
				newestMsgContent = title +context
								.getString(R.string.send_a_message_of_unknown_type);
			}
		} else {
			if (type.equals("SERVICE")) {
				newestMsgContent = context
						.getString(R.string.welcome_to_attention) +" "+ DirectChannelUtils.getRobotInfo(context, title).getName();
			}else if (type.equals("GROUP")){
				newestMsgContent = context.getString(
						R.string.group_no_message);
			}else {
				newestMsgContent = context.getString(
						R.string.direct_no_message);
			}
		}
		return newestMsgContent;
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
				return UriUtils.getUserInfoPhotoUri(icon);
			} else {
				return UriUtils.getPreviewUri(icon);
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
