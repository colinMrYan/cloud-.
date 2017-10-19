package com.inspur.emmcloud.bean;

import com.google.gson.annotations.SerializedName;
import com.inspur.emmcloud.util.JSONUtils;

/**
 * Created by yufuchang on 2017/5/18.
 */

public class SplashPageBean {


    /**
     * id : {"namespace":"com.inspur.ecc.core.preferences","domain":"launch-screen","version":"v1.0.0"}
     * command : FORWARD
     * payload : {"version":"v1.0.0","state":"ACTIVED","effectiveDate":1495393588000,"expireDate":1495825594000,"res1xHash":"1","res2xHash":"1","res3xHash":"1","mdpiHash":"1","hdpiHash":"1","xhdpiHash":"1","xxhdpiHash":"1","xxxhdpiHash":"1","resource":{"default":{"res1xHash":"1","mdpi":"IZHJ301KAYD.png","xxhdpi":"JQBJ301LZB0.png","hdpi":"UP7J301KYCA.png","xhdpi":"YQ4J301LFRX.png","mdpiHash":"1","res2x":"K3ZJ301JFB1.png","xxhdpiHash":"1","xxxhdpi":"U6PJ301MD1G.png","res1x":"CJBJ301IWU2.png","res3xHash":"1","hdpiHash":"1","res3x":"G0RJ301JU13.png","res2xHash":"1","xhdpiHash":"1","xxxhdpiHash":"1"}}}
     */

    private IdBean id;
    private String command = "STANDBY";
    private PayloadBean payload;
    private String response = "";

    public SplashPageBean(String response){
        this.response = response;
        this.command = JSONUtils.getString(response,"command","");
        String idBean = JSONUtils.getString(response,"id","");
        String payLoadBean = JSONUtils.getString(response,"payload","");
        this.id = new IdBean(idBean);
        this.payload = new PayloadBean(payLoadBean);
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
//        if(payload == null){
//            this.payload = new PayloadBean();
//        }
        this.payload = payload;
    }

    public static class IdBean {
        /**
         * namespace : com.inspur.ecc.core.preferences
         * domain : launch-screen
         * version : v1.0.0
         */
        private String namespace = "";
        private String domain = "";
        private String version = "";

        public IdBean(String response){
            this.namespace = JSONUtils.getString(response,"namespace","");
            this.domain = JSONUtils.getString(response,"domain","");
            this.version = JSONUtils.getString(response,"version","");

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
         * state : ACTIVED
         * effectiveDate : 1495393588000
         * expireDate : 1495825594000
         * res1xHash : 1
         * res2xHash : 1
         * res3xHash : 1
         * mdpiHash : 1
         * hdpiHash : 1
         * xhdpiHash : 1
         * xxhdpiHash : 1
         * xxxhdpiHash : 1
         * resource : {"default":{"res1xHash":"1","mdpi":"IZHJ301KAYD.png","xxhdpi":"JQBJ301LZB0.png","hdpi":"UP7J301KYCA.png","xhdpi":"YQ4J301LFRX.png","mdpiHash":"1","res2x":"K3ZJ301JFB1.png","xxhdpiHash":"1","xxxhdpi":"U6PJ301MD1G.png","res1x":"CJBJ301IWU2.png","res3xHash":"1","hdpiHash":"1","res3x":"G0RJ301JU13.png","res2xHash":"1","xhdpiHash":"1","xxxhdpiHash":"1"}}
         */



        private String version = "";
        private String state = "";
        private long effectiveDate = 0;
        private long expireDate = 0;
        private String res1xHash = "";
        private String res2xHash = "";
        private String res3xHash = "";
        private String mdpiHash = "";
        private String hdpiHash = "";
        private String xhdpiHash = "";
        private String xxhdpiHash = "";
        private String xxxhdpiHash = "";
        private ResourceBean resource;

        public PayloadBean(String payloadBean){
            this.version = JSONUtils.getString(payloadBean,"version","");
            this.state = JSONUtils.getString(payloadBean,"state","");
            this.effectiveDate = JSONUtils.getLong(payloadBean,"effectiveDate",0);
            this.expireDate = JSONUtils.getLong(payloadBean,"expireDate",0);
            this.res1xHash = JSONUtils.getString(payloadBean,"res1xHash","");
            this.res2xHash = JSONUtils.getString(payloadBean,"res2xHash","");
            this.res3xHash = JSONUtils.getString(payloadBean,"res3xHash","");
            this.mdpiHash = JSONUtils.getString(payloadBean,"mdpiHash","");
            this.hdpiHash = JSONUtils.getString(payloadBean,"hdpiHash","");
            this.xhdpiHash = JSONUtils.getString(payloadBean,"xhdpiHash","");
            this.xxhdpiHash = JSONUtils.getString(payloadBean,"xxhdpiHash","");
            this.xxxhdpiHash = JSONUtils.getString(payloadBean,"xxxhdpiHash","");
            String resourceBean = JSONUtils.getString(payloadBean,"resource","");
            this.resource = new ResourceBean(resourceBean);
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public long getEffectiveDate() {
            return effectiveDate;
        }

        public void setEffectiveDate(long effectiveDate) {
            this.effectiveDate = effectiveDate;
        }

        public long getExpireDate() {
            return expireDate;
        }

        public void setExpireDate(long expireDate) {
            this.expireDate = expireDate;
        }

        public String getRes1xHash() {
            return res1xHash;
        }

        public void setRes1xHash(String res1xHash) {
            this.res1xHash = res1xHash;
        }

        public String getRes2xHash() {
            return res2xHash;
        }

        public void setRes2xHash(String res2xHash) {
            this.res2xHash = res2xHash;
        }

        public String getRes3xHash() {
            return res3xHash;
        }

        public void setRes3xHash(String res3xHash) {
            this.res3xHash = res3xHash;
        }

        public String getMdpiHash() {
            return mdpiHash;
        }

        public void setMdpiHash(String mdpiHash) {
            this.mdpiHash = mdpiHash;
        }

        public String getHdpiHash() {
            return hdpiHash;
        }

        public void setHdpiHash(String hdpiHash) {
            this.hdpiHash = hdpiHash;
        }

        public String getXhdpiHash() {
            return xhdpiHash;
        }

        public void setXhdpiHash(String xhdpiHash) {
            this.xhdpiHash = xhdpiHash;
        }

        public String getXxhdpiHash() {
            return xxhdpiHash;
        }

        public void setXxhdpiHash(String xxhdpiHash) {
            this.xxhdpiHash = xxhdpiHash;
        }

        public String getXxxhdpiHash() {
            return xxxhdpiHash;
        }

        public void setXxxhdpiHash(String xxxhdpiHash) {
            this.xxxhdpiHash = xxxhdpiHash;
        }

        public ResourceBean getResource() {
            return resource;
        }

        public void setResource(ResourceBean resource) {
            this.resource = resource;
        }

        public static class ResourceBean {
            /**
             * default : {"res1xHash":"1","mdpi":"IZHJ301KAYD.png","xxhdpi":"JQBJ301LZB0.png","hdpi":"UP7J301KYCA.png","xhdpi":"YQ4J301LFRX.png","mdpiHash":"1","res2x":"K3ZJ301JFB1.png","xxhdpiHash":"1","xxxhdpi":"U6PJ301MD1G.png","res1x":"CJBJ301IWU2.png","res3xHash":"1","hdpiHash":"1","res3x":"G0RJ301JU13.png","res2xHash":"1","xhdpiHash":"1","xxxhdpiHash":"1"}
             */

            @SerializedName("default")
            private DefaultBean defaultX;
            public ResourceBean(String resourceBean){
                String defaultBean = JSONUtils.getString(resourceBean,"default","");
                this.defaultX = new DefaultBean(defaultBean);
            }

            public DefaultBean getDefaultX() {
                return defaultX;
            }

            public void setDefaultX(DefaultBean defaultX) {
                this.defaultX = defaultX;
            }

            public static class DefaultBean {
                /**
                 * res1xHash : 1
                 * mdpi : IZHJ301KAYD.png
                 * xxhdpi : JQBJ301LZB0.png
                 * hdpi : UP7J301KYCA.png
                 * xhdpi : YQ4J301LFRX.png
                 * mdpiHash : 1
                 * res2x : K3ZJ301JFB1.png
                 * xxhdpiHash : 1
                 * xxxhdpi : U6PJ301MD1G.png
                 * res1x : CJBJ301IWU2.png
                 * res3xHash : 1
                 * hdpiHash : 1
                 * res3x : G0RJ301JU13.png
                 * res2xHash : 1
                 * xhdpiHash : 1
                 * xxxhdpiHash : 1
                 */

                private String res1xHash = "";
                private String mdpi = "";
                private String xxhdpi = "";
                private String hdpi = "";
                private String xhdpi = "";
                private String mdpiHash = "";
                private String res2x = "'";
                private String xxhdpiHash = "";
                private String xxxhdpi = "";
                private String res1x = "";
                private String res3xHash = "";
                private String hdpiHash = "";
                private String res3x = "";
                private String res2xHash = "";
                private String xhdpiHash = "";
                private String xxxhdpiHash = "";

                public DefaultBean(String defaultBean){
                    this.res1xHash = JSONUtils.getString(defaultBean,"res1xHash","");
                    this.mdpi = JSONUtils.getString(defaultBean,"mdpi","");
                    this.xxhdpi = JSONUtils.getString(defaultBean,"xxhdpi","");
                    this.hdpi = JSONUtils.getString(defaultBean,"hdpi","");
                    this.xhdpi = JSONUtils.getString(defaultBean,"xhdpi","");
                    this.mdpiHash = JSONUtils.getString(defaultBean,"mdpiHash","");
                    this.res2x = JSONUtils.getString(defaultBean,"res2x","");
                    this.xxhdpiHash = JSONUtils.getString(defaultBean,"xxhdpiHash","");
                    this.xxxhdpi = JSONUtils.getString(defaultBean,"xxxhdpi","");
                    this.res1x = JSONUtils.getString(defaultBean,"res1x","");
                    this.res3xHash = JSONUtils.getString(defaultBean,"res3xHash","");
                    this.hdpiHash = JSONUtils.getString(defaultBean,"hdpiHash","");
                    this.res3x = JSONUtils.getString(defaultBean,"res3x","");
                    this.res2xHash = JSONUtils.getString(defaultBean,"res2xHash","");
                    this.xhdpiHash = JSONUtils.getString(defaultBean,"xhdpiHash","");
                    this.xxxhdpiHash = JSONUtils.getString(defaultBean,"xxxhdpiHash","");
                }

                public String getRes1xHash() {
                    return res1xHash;
                }

                public void setRes1xHash(String res1xHash) {
                    this.res1xHash = res1xHash;
                }

                public String getMdpi() {
                    return mdpi;
                }

                public void setMdpi(String mdpi) {
                    this.mdpi = mdpi;
                }

                public String getXxhdpi() {
                    return xxhdpi;
                }

                public void setXxhdpi(String xxhdpi) {
                    this.xxhdpi = xxhdpi;
                }

                public String getHdpi() {
                    return hdpi;
                }

                public void setHdpi(String hdpi) {
                    this.hdpi = hdpi;
                }

                public String getXhdpi() {
                    return xhdpi;
                }

                public void setXhdpi(String xhdpi) {
                    this.xhdpi = xhdpi;
                }

                public String getMdpiHash() {
                    return mdpiHash;
                }

                public void setMdpiHash(String mdpiHash) {
                    this.mdpiHash = mdpiHash;
                }

                public String getRes2x() {
                    return res2x;
                }

                public void setRes2x(String res2x) {
                    this.res2x = res2x;
                }

                public String getXxhdpiHash() {
                    return xxhdpiHash;
                }

                public void setXxhdpiHash(String xxhdpiHash) {
                    this.xxhdpiHash = xxhdpiHash;
                }

                public String getXxxhdpi() {
                    return xxxhdpi;
                }

                public void setXxxhdpi(String xxxhdpi) {
                    this.xxxhdpi = xxxhdpi;
                }

                public String getRes1x() {
                    return res1x;
                }

                public void setRes1x(String res1x) {
                    this.res1x = res1x;
                }

                public String getRes3xHash() {
                    return res3xHash;
                }

                public void setRes3xHash(String res3xHash) {
                    this.res3xHash = res3xHash;
                }

                public String getHdpiHash() {
                    return hdpiHash;
                }

                public void setHdpiHash(String hdpiHash) {
                    this.hdpiHash = hdpiHash;
                }

                public String getRes3x() {
                    return res3x;
                }

                public void setRes3x(String res3x) {
                    this.res3x = res3x;
                }

                public String getRes2xHash() {
                    return res2xHash;
                }

                public void setRes2xHash(String res2xHash) {
                    this.res2xHash = res2xHash;
                }

                public String getXhdpiHash() {
                    return xhdpiHash;
                }

                public void setXhdpiHash(String xhdpiHash) {
                    this.xhdpiHash = xhdpiHash;
                }

                public String getXxxhdpiHash() {
                    return xxxhdpiHash;
                }

                public void setXxxhdpiHash(String xxxhdpiHash) {
                    this.xxxhdpiHash = xxxhdpiHash;
                }
            }
        }
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
