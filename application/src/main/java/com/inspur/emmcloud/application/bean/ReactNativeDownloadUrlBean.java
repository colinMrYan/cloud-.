package com.inspur.emmcloud.application.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/3/29.
 */

public class ReactNativeDownloadUrlBean {

    /**
     * id : {"namespace":"com.inspur.ecc.core.apps","domain":"10002","version":"v1.0.1"}
     * command : FORWARD
     * uri : CCJJ0UMX02V.zip
     * hash : sha256:76f904428c6fcf1b9979ac35d5376965a83b2dea787ffe246c2bd197efdf5fcf
     */

    private IdBean id;
    private String command;
    private String uri;
    private String hash;

    public ReactNativeDownloadUrlBean(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("id")) {
                this.id = new IdBean(jsonObject.getString("id"));
            }
            if (jsonObject.has("command")) {
                this.command = jsonObject.getString("command");
            }
            if (jsonObject.has("uri")) {
                this.uri = jsonObject.getString("uri");
            }
            if (jsonObject.has("hash")) {
                if (jsonObject.getString("hash").startsWith("sha256") || jsonObject.getString("hash").startsWith("sha1")) {
                    this.hash = jsonObject.getString("hash").split(":")[1];
                } else {
                    this.hash = jsonObject.getString("hash");
                }
            }
        } catch (JSONException e) {
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }


    public static class IdBean {
        /**
         * namespace : com.inspur.ecc.core.apps
         * domain : 10002
         * version : v1.0.1
         */


        private String namespace;
        private String domain;
        private String version;

        public IdBean(String idResponse) {
            try {
                JSONObject jsonObject = new JSONObject(idResponse);
                if (jsonObject.has("namespace")) {
                    this.namespace = jsonObject.getString("namespace");
                }
                if (jsonObject.has("domain")) {
                    this.domain = jsonObject.getString("domain");
                }
                if (jsonObject.has("version")) {
                    this.version = jsonObject.getString("version");
                }
            } catch (JSONException e) {
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
}
