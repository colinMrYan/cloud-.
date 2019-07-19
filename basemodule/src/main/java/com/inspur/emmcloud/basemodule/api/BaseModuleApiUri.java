package com.inspur.emmcloud.basemodule.api;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;

import java.io.File;

/**
 * Created by chenmch on 2019/6/5.
 */

public class BaseModuleApiUri {
    /**
     * PV收集
     *
     * @return
     */
    public static String getUploadPVCollectUrl() {
        return "https://uvc1.inspuronline.com/clientpv";
    }

    /**
     * 获取语言的接口
     *
     * @return
     */
    public static String getLangUrl() {
        return WebServiceRouterManager.getInstance().getClusterEcm() + "/" + BaseApplication.getInstance().getTanent() + "/settings/lang";
    }

    /**
     * 获取通用检查url
     *
     * @return
     */
    public static String getAllConfigVersionUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v6.0/config/Check";
    }

    /**
     * 获取上传推送信息的url
     *
     * @return
     */
    public static String getUploadPushInfoUrl() {
        return WebServiceRouterManager.getInstance().getClusterClientRegistry() + "/client";
    }

    /**
     * 向emm注册推送token的url
     * 固定地址
     *
     * @return
     */
    public static String getRegisterPushTokenUrl() {
        return "https://emm.inspuronline.com/api/sys/v6.0/config/registerDevice";
    }

    /**
     * 解除注册token的url
     * 固定地址
     *
     * @return
     */
    public static String getUnRegisterPushTokenUrl() {
        return "https://emm.inspuronline.com/api/sys/v6.0/config/unRegisterDevice";
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public static String getMyInfoUrl() {
        return WebServiceRouterManager.getInstance().getIDMUrl() + "oauth2.0/profile";
    }


    public static String getUserPhoto(Context context, String uid) {
        if (StringUtils.isBlank(uid) || uid.equals("null"))
            return null;
        String headImgUrl = null;
        boolean isCacheUserPhotoUrl = BaseApplication.getInstance().isKeysContainUid(uid);
        if (isCacheUserPhotoUrl) {
            headImgUrl = BaseApplication.getInstance().getUserPhotoUrl(uid);
        } else {
            Router router = Router.getInstance();
            if (router.getService(ContactService.class) != null) {
                ContactService service = router.getService(ContactService.class);
                ContactUser contactUser = service.getContactUserByUid(uid);
                if (contactUser != null) {
                    if (contactUser.getHasHead() == 1) {
                        headImgUrl = WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v3.0/img/userhead/" + uid;
                        String lastQueryTime = contactUser.getLastQueryTime();
                        if (!StringUtils.isBlank(lastQueryTime) && (!lastQueryTime.equals("null"))) {
                            headImgUrl = headImgUrl + "?" + lastQueryTime;
                        }
                        BaseApplication.getInstance().setUsesrPhotoUrl(uid, headImgUrl);
                    } else {
                        String name = contactUser.getName();
                        if (!StringUtils.isBlank(name)) {
                            String photoName = name.replaceAll("(\\(|（)[^（\\(\\)）]*?(\\)|）)|\\d*$", "");
                            if (photoName.length() > 0) {
                                name = photoName.substring(photoName.length() - 1, photoName.length());
                            } else {
                                name = name.substring(name.length() - 1, name.length());
                            }
                            String localPhotoFileName = "u" + (int) (name.charAt(0));
                            File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                                    localPhotoFileName);
                            headImgUrl = "file://" + file.getAbsolutePath();
                            if (!file.exists()) {
                                ImageUtils.drawAndSavePhotoTextImg(context, name, file.getAbsolutePath());
                            }
                        }
                    }
                }
            }
            if (BaseApplication.getInstance().getIsContactReady() && headImgUrl == null) {
                BaseApplication.getInstance().setUsesrPhotoUrl(uid, headImgUrl);
            }
        }
        return headImgUrl;
    }
}
