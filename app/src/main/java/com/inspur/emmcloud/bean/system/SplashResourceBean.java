package com.inspur.emmcloud.bean.system;

import com.google.gson.annotations.SerializedName;
import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/4/2.
 */

public class SplashResourceBean {
    /**
     * default : {"res1xHash":"1","mdpi":"IZHJ301KAYD.png","xxhdpi":"JQBJ301LZB0.png","hdpi":"UP7J301KYCA.png","xhdpi":"YQ4J301LFRX.png","mdpiHash":"1","res2x":"K3ZJ301JFB1.png","xxhdpiHash":"1","xxxhdpi":"U6PJ301MD1G.png","res1x":"CJBJ301IWU2.png","res3xHash":"1","hdpiHash":"1","res3x":"G0RJ301JU13.png","res2xHash":"1","xhdpiHash":"1","xxxhdpiHash":"1"}
     */

    @SerializedName("default")
    private SplashDefaultBean defaultX;

    public SplashResourceBean(String resourceBean) {
        String defaultBean = JSONUtils.getString(resourceBean, "default", "");
        this.defaultX = new SplashDefaultBean(defaultBean);
    }

    public SplashDefaultBean getDefaultX() {
        return defaultX;
    }

    public void setDefaultX(SplashDefaultBean defaultX) {
        this.defaultX = defaultX;
    }


}
