package com.inspur.emmcloud.bean;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.lidroid.xutils.db.annotation.Id;

/**
 * classes : com.inspur.emmcloud.widget.DragGrid.AppItem Create at 2016年12月15日
 * 上午8:59:08
 */
public class AppItem {

	@Id
	private String appID = "";
	private String appName = "";
	private String lastModifyTime = "";
	private String appIcon = "";
	private String uri = "";
	private String note = "";
	private String description = "";
	private Boolean disabled = false;
	private Boolean isMustHave = false;
	// 0:uninstalled 1:installed 2:needUpdate
	private int useStatus = -1;
	// 1：内部原生、2：外部原生、3：内嵌网页 4.外部网页
	private int appType = 4;
	private String version = "";
	private String identifiers = "";// 应用的包名，当原生应用时才有意义，对web应用来说该值没有任何意义
	private List<String> legends;
	
	private String orderId = "";
	private String groupId = "";
	private long lastUpdateTime = 0L;
	

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	

	public AppItem() {
	}
	
	public AppItem(String appItem){
		
		try {
			JSONObject obj = new JSONObject(appItem);
			if (obj.has("id")) {
				this.appID = obj.getString("id");
			}
			if (obj.has("app_name")) {
				this.appName = obj.getString("app_name");
			}
			if (obj.has("last_modify_time")) {
				this.lastModifyTime = obj.getString("last_modify_time");
			}
			if (obj.has("ico")) {
				this.appIcon = obj.getString("ico");
			}
			if (obj.has("uri")) {
				this.uri = obj.getString("uri");
			}
			if (obj.has("essential")) {
				int essential = obj.getInt("essential");
				if (essential == 1) {
					isMustHave = true;
				}
			}
			if (obj.has("use_status")) {
				this.useStatus = obj.getInt("use_status");
			}
			if (obj.has("type")) {
				this.appType = obj.getInt("type");
			}
			if (obj.has("disabled")) {
				this.disabled = obj.getBoolean("disabled");
			}
			if (obj.has("note")) {
				this.note = obj.getString("note");
			}
			if (obj.has("description")) {
				this.description = obj.getString("description");
			}
			if (obj.has("ver")) {
				this.version = obj.getString("ver");
			}
			if (obj.has("identifiers")) {
				this.identifiers = obj.getString("identifiers");
			}
			if (obj.has("legends")) {
				JSONArray jsonArray = obj.getJSONArray("legends");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String url = jsonObject.getString("url");
					legends.add(url);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	public AppItem(App app){
//		appID = app.getAppID();
//		appName = app.getAppName();
//		lastModifyTime = app.getLastModifyTime();
//		appIcon = app.getAppIcon();
//		uri = app.getUri();
//		note = app.getNote();
//		description = app.getDescription();
//		disabled = app.getDisabled();
//		isMustHave = app.getIsMustHave();
//		useStatus = app.getUseStatus();
//		// 1：内部原生、2：外部原生、3：内嵌网页 4.外部网页
//		appType = app.getAppType();
//		version = app.getVersion();
//		identifiers = app.getIdentifiers();// 应用的包名，当原生应用时才有意义，对web应用来说该值没有任何意义
//		legends = app.getLegends();
//	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public String getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(String appIcon) {
		this.appIcon = appIcon;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public Boolean getIsMustHave() {
		return isMustHave;
	}

	public void setIsMustHave(Boolean isMustHave) {
		this.isMustHave = isMustHave;
	}

	public int getUseStatus() {
		return useStatus;
	}

	public void setUseStatus(int useStatus) {
		this.useStatus = useStatus;
	}

	public int getAppType() {
		return appType;
	}

	public void setAppType(int appType) {
		this.appType = appType;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(String identifiers) {
		this.identifiers = identifiers;
	}

	public List<String> getLegends() {
		return legends;
	}

	public void setLegends(List<String> legends) {
		this.legends = legends;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
	@Override
	public boolean equals(Object other) {
		if(this == other){
			return true;
		}
		if(other == null){
			return false;
		}
		if(!(other instanceof AppItem)){
			return false;
		}
		AppItem appItem = (AppItem) other;
		if(!(getAppID() == appItem.getAppID())){
			return false;
		}
		return true;
	}

}
