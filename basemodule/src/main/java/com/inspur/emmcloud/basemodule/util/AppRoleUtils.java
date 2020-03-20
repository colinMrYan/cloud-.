package com.inspur.emmcloud.basemodule.util;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;

/**
 * Created by: yufuchang
 * Date: 2020/3/19
 */
public class AppRoleUtils {

    private static final String CONTACT_KEY = "enable_contacts";
    private static final String SEND_FILE_KEY = "enable_file_send";
    private static final String SEND_IMAGE_KEY = "enable_image_send";

    /**
     * 判断是否有通讯录权限
     * @return
     */
    public static boolean isShowContact(){
        String appRole = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(),Constant.APP_ROLE,"");
        return JSONUtils.getInt(appRole,CONTACT_KEY,1) != 0;
    }

    /**
     * 是否可以发送文件
     * @return
     */
    public static boolean isCanSendFile(){
        String appRole = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(),Constant.APP_ROLE,"");
        return JSONUtils.getInt(appRole,SEND_FILE_KEY,1) != 0;
    }

    /**
     * 是否可以发送图片
     * @return
     */
    public static boolean isCanSendImage(){
        String appRole = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(),Constant.APP_ROLE,"");
        return JSONUtils.getInt(appRole,SEND_IMAGE_KEY,1) != 0;
    }
}
