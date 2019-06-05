package com.inspur.emmcloud.bean.appcenter;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class App implements Serializable {
    private static final String TAG = "App";
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
    // 1：内部原生、2：外部原生、3：内嵌网页 4.外部网页、5：ReactNative应用
    private int appType = 4;
    private String version = "";
    private String identifiers = "";// 应用的包名，当原生应用时才有意义，对web应用来说该值没有任何意义
    private String packageName = "";
    private String MainActivityName = "";
    private List<String> legendList = new ArrayList<>();

    private int orderId = -1;
    private String categoryID = "";
    private String categoryName = "";
    private long lastUpdateTime = 0L;
    private double weight = 0;
    private String installUri = "";
    private String helpUrl = "";
    private int isSSO = -1;

    //应用功能扩展字段
    private int isZoomable = 0;
    private int userHeader = 1;

    private List<App> subAppList = new ArrayList<>();

    public App() {
    }

    public App(JSONObject obj) {
        try {
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
                if (appType == 2) {
                    if (identifiers.contains(",")) {
                        String[] array = identifiers.split(",");
                        this.packageName = array[0];
                        if (array.length == 2) {
                            this.MainActivityName = array[1];
                        }
                    } else {
                        this.packageName = this.identifiers;
                    }
                }

            }
            if (obj.has("legends")) {
                JSONArray jsonArray = obj.getJSONArray("legends");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String url = jsonObject.getString("url");
                    legendList.add(url);
                }
            }
            if (obj.has("install_uri")) {
                this.installUri = obj.getString("install_uri");
            }
            isZoomable = JSONUtils.getInt(obj, "is_zoomable", 0);
            categoryID = JSONUtils.getString(obj, "category_id", "");
            categoryName = JSONUtils.getString(obj, "category_name", "");
            helpUrl = JSONUtils.getString(obj, "help_url", "");
            isSSO = JSONUtils.getInt(obj, "is_sso", -1);
            //对helpUrl特殊处理，因为服务端有时返回""，有时返回null返回null时fastJson会把此字段解析为字符串"null",需要特殊处理
            if (helpUrl.equals("null")) {
                helpUrl = "";
            }
            userHeader = JSONUtils.getInt(obj, "use_header", 1);
            JSONArray jsonArray = JSONUtils.getJSONArray(obj, "appList", new JSONArray());
            for (int i = 0; i < jsonArray.length(); i++) {
                App app = new App(JSONUtils.getJSONObject(jsonArray, i, new JSONObject()));
                subAppList.add(app);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            LogUtils.exceptionDebug(TAG, e.toString());
        }
    }

    /**
     * 修改方法
     *
     * @param appJson
     */
    public App(String appJson) {
        this(JSONUtils.getJSONObject(appJson));
    }

    public Boolean getIsMustHave() {
        return isMustHave;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public int getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(int useStatus) {
        this.useStatus = useStatus;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
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

    public int getAppType() {
        return appType;
    }

    public void setAppType(int appType) {
        this.appType = appType;
    }

    public String getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(String lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public List<String> getLegendList() {
        return legendList;
    }

    public void setLegendList(List<String> legendList) {
        this.legendList = legendList;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getMainActivityName() {
        return MainActivityName;
    }

    public void setMainActivityName(String mainActivityName) {
        this.MainActivityName = mainActivityName;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String groupId) {
        this.categoryID = groupId;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getInstallUri() {
        return installUri;
    }

    public void setInstallUri(String installUri) {
        this.installUri = installUri;
    }

    public int getIsZoomable() {
        return isZoomable;
    }

    public void setIsZoomable(int isZoomable) {
        this.isZoomable = isZoomable;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    public Boolean getMustHave() {
        return isMustHave;
    }

    public void setMustHave(Boolean mustHave) {
        isMustHave = mustHave;
    }

    public int getIsSSO() {
        return isSSO;
    }

    public void setIsSSO(int isSSO) {
        this.isSSO = isSSO;
    }

    public int getUserHeader() {
        return userHeader;
    }

    public void setUserHeader(int userHeader) {
        this.userHeader = userHeader;
    }

    public List<App> getSubAppList() {
        return subAppList;
    }

    public void setSubAppList(List<App> subAppList) {
        this.subAppList = subAppList;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof App)) {
            return false;
        }
        App app = (App) other;
        //此处从==判断是否相等  改为equals
        return getAppID().equals(app.getAppID());
    }

    @Override
    public String toString() {
        return "App{" +
                "appID='" + appID + '\'' +
                ", appName='" + appName + '\'' +
                ", lastModifyTime='" + lastModifyTime + '\'' +
                ", appIcon='" + appIcon + '\'' +
                ", uri='" + uri + '\'' +
                ", note='" + note + '\'' +
                ", description='" + description + '\'' +
                ", disabled=" + disabled +
                ", isMustHave=" + isMustHave +
                ", useStatus=" + useStatus +
                ", appType=" + appType +
                ", version='" + version + '\'' +
                ", identifiers='" + identifiers + '\'' +
                ", packageName='" + packageName + '\'' +
                ", MainActivityName='" + MainActivityName + '\'' +
                ", legendList=" + legendList +
                ", orderId=" + orderId +
                ", categoryID='" + categoryID + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", lastUpdateTime=" + lastUpdateTime +
                ", weight=" + weight +
                ", installUri='" + installUri + '\'' +
                ", helpUrl='" + helpUrl + '\'' +
                ", isSSO=" + isSSO +
                ", isZoomable=" + isZoomable +
                '}';
    }
}
