package com.inspur.emmcloud.bean.appcenter;


import com.inspur.emmcloud.baselib.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/2/21.
 */

public class ReactNativeUpdateBean {

    /**
     * bundle : {"androidUri":"666.zip","iosUri":"666.zip","webUri":"666.zip","androidHash":"SHA256:81ab0ed3b728571d0fe502e66ec6a91d5a8fbdcdaff49969d79a93b174e26ec9","iosHash":"SHA256:81ab0ed3b728571d0fe502e66ec6a91d5a8fbdcdaff49969d79a93b174e26ec9","webHash":"SHA256:81ab0ed3b728571d0fe502e66ec6a91d5a8fbdcdaff49969d79a93b174e26ec9","compressedFormat":"ZIP","id":{"domain":"DISCOVER","version":"diccoverv666"},"creationDate":1487830148376}
     * command : FORWARD
     */


    private BundleBean bundle;
    private String command;

    public ReactNativeUpdateBean(String reactNativeUpdate) {
        try {
            JSONObject jsonReactNative = new JSONObject(reactNativeUpdate);
            if (jsonReactNative.has("bundle")) {
                String bundleString = jsonReactNative.getString("bundle");
                if (StringUtils.isBlank(bundleString) || bundleString.equals("null")) {
                    this.bundle = new BundleBean();
                } else {
                    this.bundle = new BundleBean(bundleString);
                }
            }
            if (jsonReactNative.has("command")) {
                this.command = jsonReactNative.getString("command");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public BundleBean getBundle() {
        return bundle;
    }

    public void setBundle(BundleBean bundle) {
        this.bundle = bundle;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public static class BundleBean {
        /**
         * androidUri : 666.zip
         * iosUri : 666.zip
         * webUri : 666.zip
         * androidHash : SHA256:81ab0ed3b728571d0fe502e66ec6a91d5a8fbdcdaff49969d79a93b174e26ec9
         * iosHash : SHA256:81ab0ed3b728571d0fe502e66ec6a91d5a8fbdcdaff49969d79a93b174e26ec9
         * webHash : SHA256:81ab0ed3b728571d0fe502e66ec6a91d5a8fbdcdaff49969d79a93b174e26ec9
         * compressedFormat : ZIP
         * id : {"domain":"DISCOVER","version":"diccoverv666"}
         * creationDate : 1487830148376
         */

        private String androidUri;
        private String iosUri;
        private String webUri;
        private String androidHash;
        private String iosHash;
        private String webHash;
        private String compressedFormat;
        private IdBean id;
        private long creationDate;

        public BundleBean() {
        }

        public BundleBean(String bundle) {
            try {
                JSONObject jsonBundle = new JSONObject(bundle);
                if (jsonBundle.has("androidUri")) {
                    this.androidUri = jsonBundle.getString("androidUri");
                }
                if (jsonBundle.has("iosUri")) {
                    this.iosUri = jsonBundle.getString("iosUri");
                }
                if (jsonBundle.has("webUri")) {
                    this.webUri = jsonBundle.getString("webUri");
                }
                if (jsonBundle.has("androidHash")) {
                    this.androidHash = jsonBundle.getString("androidHash").split(":")[1];
                }
                if (jsonBundle.has("iosHash")) {
                    this.iosHash = jsonBundle.getString("iosHash");
                }
                if (jsonBundle.has("webHash")) {
                    this.webHash = jsonBundle.getString("webHash");
                }
                if (jsonBundle.has("compressedFormat")) {
                    this.compressedFormat = jsonBundle.getString("compressedFormat");
                }
                if (jsonBundle.has("id")) {
//                    this.id = jsonBundle.getString("id");
                    this.id = new IdBean(jsonBundle.getString("id"));
                }
                if (jsonBundle.has("creationDate")) {
                    this.creationDate = jsonBundle.getLong("creationDate");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public String getAndroidUri() {
            return androidUri;
        }

        public void setAndroidUri(String androidUri) {
            this.androidUri = androidUri;
        }

        public String getIosUri() {
            return iosUri;
        }

        public void setIosUri(String iosUri) {
            this.iosUri = iosUri;
        }

        public String getWebUri() {
            return webUri;
        }

        public void setWebUri(String webUri) {
            this.webUri = webUri;
        }

        public String getAndroidHash() {
            return androidHash;
        }

        public void setAndroidHash(String androidHash) {
            this.androidHash = androidHash;
        }

        public String getIosHash() {
            return iosHash;
        }

        public void setIosHash(String iosHash) {
            this.iosHash = iosHash;
        }

        public String getWebHash() {
            return webHash;
        }

        public void setWebHash(String webHash) {
            this.webHash = webHash;
        }

        public String getCompressedFormat() {
            return compressedFormat;
        }

        public void setCompressedFormat(String compressedFormat) {
            this.compressedFormat = compressedFormat;
        }

        public IdBean getId() {
            return id;
        }

        public void setId(IdBean id) {
            this.id = id;
        }

        public long getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(long creationDate) {
            this.creationDate = creationDate;
        }

        public static class IdBean {
            /**
             * domain : DISCOVER
             * version : diccoverv666
             */

            private String domain;
            private String version;

            public IdBean(String idBean) {
                try {
                    JSONObject jsonIdBean = new JSONObject(idBean);
                    if (jsonIdBean.has("domain")) {
                        this.domain = jsonIdBean.getString("domain");
                    }
                    if (jsonIdBean.has("version")) {
                        this.version = jsonIdBean.getString("version");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
}
