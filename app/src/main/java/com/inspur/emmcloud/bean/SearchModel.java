package com.inspur.emmcloud.bean;

import android.content.Context;

import com.facebook.react.bridge.ReadableMap;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.lidroid.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "SearchModel")
public class SearchModel implements Serializable {
	private String id = "";
	private String name = "";
	private String type = ""; // 单人：user 组织：struct 群组：channelGroup
	private String icon = "";
	//private String inspurID= "";
	private int heat = 0;

	public SearchModel() {

	}

	public SearchModel(ChannelGroup channelGroup) {
		if (channelGroup == null) {
			return;
		}
		this.id = channelGroup.getCid();
		this.name = channelGroup.getChannelName();
		this.type = "GROUP";
		this.icon = channelGroup.getIcon();
	}

	public SearchModel(Contact contact) {
		if (contact == null) {
			return;
		}
		if (contact.getType().equals("user")) {
			type = "USER";
			name = contact.getRealName();
			id = contact.getInspurID();
		} else {
			type = "STRUCT";
			name = contact.getName();
			id = contact.getId();
		}
	}

	public SearchModel(ReadableMap nativeInfo){
		try {
			if(nativeInfo.hasKey("new_id")){
				id = nativeInfo.getString("new_id");
			}
			if(nativeInfo.hasKey("real_name")){
				name = nativeInfo.getString("real_name");
			}
			if(nativeInfo.hasKey("type")){
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
		icon = channel.getIcon();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * 中文名+英文名
	 *
	 * @return
	 */
	public String getCompleteName(Context context){
		String completeName = name; 
		Contact contact = null;
		if (type.equals("USER")) {
			contact = ContactCacheUtils.getUserContact(context, id);
		}else if (type.equals("STRUCT")) {
			contact = ContactCacheUtils.getStructContact(context, id);
		}
		if (contact != null) {
			String globalName = contact.getGlobalName();
			if (!StringUtils.isBlank(globalName)) {
				completeName = completeName + "（"+globalName+"）";
			}
		}
		return completeName;
		
	}

	public String getType() {
		return type;
	}

	public void setHeat(int heat) {
		this.heat = heat;
	}

	public int getHeat() {
		return heat;
	}

	public String getIcon() {
		if (!icon.startsWith("http")) {
			if (type.equals("GROUP")) {
				return UriUtils.getPreviewUri(icon);
			} else if (type.equals("DIRECT")) {
				return UriUtils.getUserInfoPhotoUri(icon);
			} else if (type.equals("USER")) {
				return UriUtils.getChannelImgUri(id);
			} else {
				return null;
			}
		}
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
//		if (getType().equals("USER")&&otherSearchModel.getType().equals("DIRECT")) {
//			
//		}
		
		if (!getId().equals(otherSearchModel.getId()))
			return false;
		if (!getName().equals(otherSearchModel.getName()))
			return false;
		if (!getType().equals(otherSearchModel.getType()))
			return false;
		return true;
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
