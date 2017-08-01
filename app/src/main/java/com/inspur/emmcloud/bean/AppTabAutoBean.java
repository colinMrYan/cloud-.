package com.inspur.emmcloud.bean;

import com.google.gson.annotations.SerializedName;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2017/4/26.
 */

public class AppTabAutoBean {

    /**
     * id : {"namespace":"com.inspur.ecc.core.preferences","domain":"main-tab","version":"v1.0.0"}
     * command : FORWARD
     * payload : {"version":"v1.0.0","name":"云+tabbar","state":"PENDING","creationDate":1493186738081,"selected":"application","tabs":[{"id":1,"key":"application","component":"main-tab","icon":"hello-app","selected":true,"title":{"zh-Hans":"应用","zh-Hant":"應用","en-US":"App"}}]}
     */

    private IdBean id;
    private String command = "";
    private PayloadBean payload;

    public AppTabAutoBean(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("id")) {
                id = new IdBean(jsonObject.getString("id"));
            }
            if (jsonObject.has("payload")) {
                payload = new PayloadBean(jsonObject.getString("payload"));
            }
            if (jsonObject.has("command")) {
                this.command = jsonObject.getString("command");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IdBean getId() {
        return id;
    }

    public void setId(IdBean id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public PayloadBean getPayload() {
        return payload;
    }

    public void setPayload(PayloadBean payload) {
        this.payload = payload;
    }

    public static class IdBean {
        /**
         * namespace : com.inspur.ecc.core.preferences
         * domain : main-tab
         * version : v1.0.0
         */

        private String namespace = "";
        private String domain = "";
        private String version = "";

        public IdBean(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("namespace")) {
                    this.namespace = jsonObject.getString("namespace");
                }
                if (jsonObject.has("domain")) {
                    this.domain = jsonObject.getString("domain");
                }
                if (jsonObject.has("version")) {
                    this.version = jsonObject.getString("version");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class PayloadBean {
        /**
         * version : v1.0.0
         * name : 云+tabbar
         * state : PENDING
         * creationDate : 1493186738081
         * selected : application
         * tabs : [{"id":1,"key":"application","component":"main-tab","icon":"hello-app","selected":true,"title":{"zh-Hans":"应用","zh-Hant":"應用","en-US":"App"}}]
         */

        private String version = "";
        private String name = "";
        private String state = "";
        private long creationDate = 0;
        private String selected = "";
        private List<TabsBean> tabs = new ArrayList<>();

        public PayloadBean(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("version")) {
                    this.version = jsonObject.getString("version");
                }
                if (jsonObject.has("name")) {
                    this.name = jsonObject.getString("name");
                }
                if (jsonObject.has("state")) {
                    this.state = jsonObject.getString("state");
                }
                if (jsonObject.has("selected")) {
                    this.selected = jsonObject.getString("selected");
                }
                if (jsonObject.has("creationDate")) {
                    this.creationDate = jsonObject.getLong("creationDate");
                }
                if (jsonObject.has("tabs")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("tabs");
                    int arraySize = jsonArray.length();
                    for (int i = 0; i < arraySize; i++) {
                        this.tabs.add(new TabsBean(jsonArray.getJSONObject(i)));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getSelected() {
            return selected;
        }

        public void setSelected(String selected) {
            this.selected = selected;
        }

        public List<TabsBean> getTabs() {
            return tabs;
        }

        public void setTabs(List<TabsBean> tabs) {
            this.tabs = tabs;
        }

        public static class TabsBean {
            /**
             * id : 1
             * key : application
             * component : main-tab
             * icon : hello-app
             * selected : true
             * title : {"zh-Hans":"应用","zh-Hant":"應用","en-US":"App"}
             * "properties": {
             * "canContact": "false",
             * "canCreate": "true"
             * }
             */

            private int id;
            private String key = "";
            private String component = "";
            private String icon = "";
            private boolean selected = false;
            private TitleBean title;
            private Property property;

            public TabsBean(JSONObject jsonObject) {
                try {
                    if (jsonObject == null) {
                        return;
                    }
                    if (jsonObject.has("id")) {
                        this.id = jsonObject.getInt("id");
                    }
                    if (jsonObject.has("key")) {
                        this.key = jsonObject.getString("key");
                    }
                    if (jsonObject.has("component")) {
                        this.component = jsonObject.getString("component");
                    }
                    if (jsonObject.has("icon")) {
                        this.icon = jsonObject.getString("icon");
                    }
                    if (jsonObject.has("selected")) {
                        this.selected = jsonObject.getBoolean("selected");
                    }
                    if (jsonObject.has("title")) {
                        this.title = new TitleBean(jsonObject.getString("title"));
                    }
                    if (jsonObject.has("properties")) {
                        String response = jsonObject.getString("properties");
                        if (StringUtils.isBlank(response)) {
                            response = " ";
                        }
                        this.property = new Property(response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            public Property getProperty() {
                return property;
            }

            public void setProperty(Property property) {
                this.property = property;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getComponent() {
                return component;
            }

            public void setComponent(String component) {
                this.component = component;
            }

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }

            public boolean isSelected() {
                return selected;
            }

            public void setSelected(boolean selected) {
                this.selected = selected;
            }

            public TitleBean getTitle() {
                return title;
            }

            public void setTitle(TitleBean title) {
                this.title = title;
            }

            public static class Property {
                /**
                 * "properties": {
                 * "canContact": "false",
                 * "canCreate": "true"
                 * }
                 */
                private boolean canContact = true;
                private boolean canCreate = true;

                public Property(String response) {
                    canContact = JSONUtils.getBoolean(response, "canOpenContact", true);
                    canCreate = JSONUtils.getBoolean(response, "canCreateChannel", true);
                }

                public boolean isCanContact() {
                    return canContact;
                }

                public void setCanContact(boolean canContact) {
                    this.canContact = canContact;
                }

                public boolean isCanCreate() {
                    return canCreate;
                }

                public void setCanCreate(boolean canCreate) {
                    this.canCreate = canCreate;
                }
            }

            public static class TitleBean {
                /**
                 * zh-Hans : 应用
                 * zh-Hant : 應用
                 * en-US : App
                 */

                @SerializedName("zh-Hans")
                private String zhHans = "";
                @SerializedName("zh-Hant")
                private String zhHant = "";
                @SerializedName("en-US")
                private String enUS = "";

                public TitleBean(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("zh-Hans")) {
                            this.zhHans = jsonObject.getString("zh-Hans");
                        }
                        if (jsonObject.has("zh-Hant")) {
                            this.zhHant = jsonObject.getString("zh-Hant");
                        }
                        if (jsonObject.has("en-US")) {
                            this.enUS = jsonObject.getString("en-US");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public String getZhHans() {
                    return zhHans;
                }

                public void setZhHans(String zhHans) {
                    this.zhHans = zhHans;
                }

                public String getZhHant() {
                    return zhHant;
                }

                public void setZhHant(String zhHant) {
                    this.zhHant = zhHant;
                }

                public String getEnUS() {
                    return enUS;
                }

                public void setEnUS(String enUS) {
                    this.enUS = enUS;
                }
            }
        }
    }
}
