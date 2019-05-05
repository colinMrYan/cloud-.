package com.inspur.emmcloud.api;


import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.ImageUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;

import java.io.File;


/**
 * 本类中包含4个常量，分别是
 * 域名的Url：URL_BASE_ECM
 * 其次，每个模块下有一个获取基础Url的方法
 * 整体有一个getEcmTanent的方法，获取到tanent这一级
 */
public class APIUri {

//    /**
//     * 获取到租户级的URL
//     *
//     * @return
//     */
//    private static String getEcmTanentUrl() {
//        return MyApplication.getInstance().getClusterEcm() + MyApplication.getInstance().getTanent();
//    }

    public static String getEcmUrl() {
        return MyApplication.getInstance().getClusterEcm();
    }

    /**
     * EMM服务
     *
     * @return
     */
    public static String getEMMBaseUrl() {
        return MyApplication.getInstance().getClusterEmm();
    }

    /**
     * EcmChat服务
     *
     * @return
     */
    public static String getECMChatUrl() {
        return MyApplication.getInstance().getClusterChat();
    }

    /**
     * EcmSchedule服务
     *
     * @return
     */
    public static String getECMScheduleUrl() {
        return MyApplication.getInstance().getClusterSchedule();
    }

    /**
     * ECMDistribution服务
     *
     * @return
     */
    public static String getECMDistribution() {
        return MyApplication.getInstance().getClusterDistribution();
    }

    /**
     * ECMNews服务
     *
     * @return
     */
    public static String getECMNews() {
        return MyApplication.getInstance().getClusterNews();
    }

    /**
     * ECMCloudDriver服务
     *
     * @return
     */
    public static String getCloudDriver() {
        return MyApplication.getInstance().getClusterCloudDrive();
    }

    /**
     * StorageLegacy服务
     *
     * @return
     */
    public static String getStorageLegacy() {
        return MyApplication.getInstance().getClusterStorageLegacy();
    }

    public static String getUrlBaseVolume() {
        return getCloudDriver() + "/volume";
    }

    public static String getUrlBaseGroup() {
        return getCloudDriver() + "/group";
    }
    /***************************************************************系统*******************************************************************/
    /**
     * 异常上传接口
     *
     * @return
     */
    public static String getUploadExceptionUrl() {
        return "https://uvc1.inspuronline.com/cpexception";
    }

    /**
     * PV收集
     *
     * @return
     */
    public static String getUploadPVCollectUrl() {
        return "https://uvc1.inspuronline.com/clientpv";
    }

    /**
     * 新版底部Tabbar接口
     *
     * @return
     */
    public static String getAppNewTabs() {
        return getECMDistribution() + "/preference/main-tab/latest";
//        return getEMMBaseUrl() + "api/sys/v6.0/maintab";
    }

    /**
     * 新版底部Tabbar接口
     *
     * @return
     */
    public static String getAppNaviTabs() {
//        return getECMDistribution() + "/rest/api/v1/category/cloud-plus-prefer/namespace/tab-navi-schemes/latest";
//        return getEMMBaseUrl() + "api/sys/v6.0/maintab";
        return "https://ecm.inspuronline.com/distribution/rest/api/v1/category/cloud-plus-prefer/namespace/tab-navi-schemes/latest";
    }


    public static String getAppConfigUrl(boolean isGetCommonAppConfig, boolean isGetWorkPortletAppConfig, boolean isGetWebAutoRotate) {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v6.0/config/array?key=PosReportTimeInterval" + (isGetCommonAppConfig ? "&key=CommonFunctions" : "") + (isGetWorkPortletAppConfig ? "&key=WorkPortlet" : "") + (isGetWebAutoRotate ? "&key=WebAutoRotate" : "");
    }

    /**
     * app闪屏页面
     *
     * @return
     */
    public static String getSplashPageUrl() {
        return getECMDistribution() + "/preference/launch-screen/latest";
    }

    /**
     * @return
     */
    public static String getUploadSplashPageWriteBackLogUrl() {
        return getECMDistribution() + "/preference/launch-screen/update";
    }

    /**
     * 存储app配置url
     *
     * @param key
     * @return
     */
    public static String saveAppConfigUrl(String key) {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v6.0/config/" + key;
    }

    /**
     * 获取上传位置信息url
     *
     * @return
     */
    public static String getUploadPositionUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v6.0/app/pos";
    }

    /**
     * 获取上传推送信息的url
     *
     * @return
     */
    public static String getUploadPushInfoUrl() {
        return MyApplication.getInstance().getClusterClientRegistry() + "/client";
    }

    /**
     * 获取通用检查url
     *
     * @return
     */
    public static String getAllConfigVersionUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v6.0/config/Check";
    }


    /************************************************************************登录*****************************************************************/

    /**
     * 请求短信验证码
     *
     * @param mobile
     * @return
     */
    public static String getLoginSMSCaptchaUrl(String mobile) {
        return MyApplication.getInstance().getCloudId() + "api/v1/passcode?phone=" + mobile;
    }

    /**
     * 验证短信验证码
     *
     * @return
     */
    public static String getSMSRegisterCheckUrl() {
        return MyApplication.getInstance().getClusterEmm() + "/api?module=register&method=verify_smscode";
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public static String getMyInfoUrl() {
        return MyApplication.getInstance().getCloudId() + "oauth2.0/profile";
    }

    /**
     * 修改密码
     **/
    public static String getChangePsdUrl() {
        return MyApplication.getInstance().getCloudId() + "console/api/v1/account/password";
    }


    /**
     * 获取oauth认证的基础
     *
     * @return
     */
    public static String getOauthSigninUrl() {
        return MyApplication.getInstance().getCloudId() + "oauth2.0/token";
    }

    /**
     * 返回我的信息
     *
     * @return
     */
    public static String getOauthMyInfoUrl() {
        return MyApplication.getInstance().getCloudId() + "oauth2.0/token/profile";
    }

    /**
     * 刷新token
     *
     * @return
     */
    public static String getRefreshToken() {
        return MyApplication.getInstance().getCloudId() + "oauth2.0/token";
    }


    /**
     * 网页登录
     *
     * @return
     */
    public static String getWebLoginUrl() {
        return MyApplication.getInstance().getCloudId() + "oauth2.0/authorize";
    }


    /**************************************************************沟通***************************************************************/

    /**
     * 频道页面头像显示图片
     **/
    public static String getChannelImgUrl(Context context, String uid) {
        if (StringUtils.isBlank(uid) || uid.equals("null"))
            return null;
        String headImgUrl = null;
        boolean isCacheUserPhotoUrl = MyApplication.getInstance().isKeysContainUid(uid);
        if (isCacheUserPhotoUrl) {
            headImgUrl = MyApplication.getInstance().getUserPhotoUrl(uid);
        } else {
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
            if (contactUser != null) {
                if (contactUser.getHasHead() == 1) {
                    headImgUrl = MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/img/userhead/" + uid;
                    String lastQueryTime = contactUser.getLastQueryTime();
                    if (!StringUtils.isBlank(lastQueryTime) && (!lastQueryTime.equals("null"))) {
                        headImgUrl = headImgUrl + "?" + lastQueryTime;
                    }
                    MyApplication.getInstance().setUsesrPhotoUrl(uid, headImgUrl);
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
            if (MyApplication.getInstance().getIsContactReady() && headImgUrl == null) {
                MyApplication.getInstance().setUsesrPhotoUrl(uid, headImgUrl);
            }
        }
        return headImgUrl;
    }

    /**
     * Imp获取头像路径
     *
     * @param uid
     * @return
     */
    public static String getChannelImgUrl4Imp(String uid) {
        String headImgUrl = "";
        ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
        if (contactUser != null) {
            headImgUrl = MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/img/userhead/" + uid;
            String lastQueryTime = contactUser.getLastQueryTime();
            if (!StringUtils.isBlank(lastQueryTime) && (!lastQueryTime.equals("null"))) {
                headImgUrl = headImgUrl + "?" + lastQueryTime;
            }
        }
        return headImgUrl;
    }

    /**
     * 统一接口
     *
     * @param uri
     * @return
     */
    public static String getHttpApiUrl(String uri) {
        return getECMChatUrl() + "/" + uri;
    }


    /**
     * 返回忽略v0,v1版本的地址
     *
     * @return
     */
    public static String getECMChatChannelUrl() {
        String channelUrl = "";
        if (MyApplication.getInstance().isV0VersionChat()) {
            channelUrl = getECMChatUrl();
        } else if (MyApplication.getInstance().isV1xVersionChat()) {
            channelUrl = (getWebsocketConnectUrl() + "/" + MyApplication.getInstance().getTanent());
        }
        return channelUrl;
    }

    /**
     * 获取chat v1 channel base url
     *
     * @return
     */
    public static String getECMChatConversationBaseUrl() {
        return getECMChatUrl() + "/api/v1";
    }


    /**
     * 个人信息头像显示图片
     *
     * @param url
     * @return
     */
    public static String getUserInfoPhotoUrl(String url) {
        return MyApplication.getInstance().getClusterEmm() + url;
    }

    /**
     * 获取机器人头像路径
     *
     * @return
     */
    public static String getRobotIconUrl(String iconUrl) {
        return getStorageLegacy() + "/avatar/stream/"
                + iconUrl;
    }

    /**
     * 获取普通人和机器人人头像url
     *
     * @param context
     * @param uid
     * @return
     */
    public static String getUserIconUrl(Context context, String uid) {
        String iconUrl = null;
        if (uid.startsWith("BOT")) {
            Robot robot = RobotCacheUtils.getRobotById(context, uid);
            if (robot != null) {
                iconUrl = APIUri.getRobotIconUrl(robot.getAvatar());
            }
        } else {
            iconUrl = APIUri.getChannelImgUrl(context, uid);
        }
        return iconUrl;
    }

    /**
     * 预览图片或视频
     **/
    public static String getPreviewUrl(String fileName) {
        return getResUrl("stream/" + fileName);
    }

    /**
     * 获取资源
     *
     * @param url
     * @return
     */
    public static String getResUrl(String url) {
        return getStorageLegacy() + "/res/" + url;
    }

    /**
     * 添加群组成员
     *
     * @param cid
     * @return
     */
    public static String getAddGroupMembersUrl(String cid) {
        return getECMChatChannelUrl() + "/channel/group/" + cid + "/users?";
    }

    /**
     * 消息免打扰
     *
     * @return
     */
    public static String getNointerRuptionUrl() {
        return getECMChatChannelUrl() + "/session/dnd";
    }

    /**
     * 获取所有机器人信息
     *
     * @return
     */
    public static String getAllBotInfo() {
        return MyApplication.getInstance().getClusterBot();
    }

    /**
     * 通过Id获取机器人信息
     *
     * @return
     */
    public static String getBotInfoById() {
        return MyApplication.getInstance().getClusterBot();
    }

    /**
     * 获取websocket链接url
     *
     * @return
     */
    public static String getWebsocketConnectUrl() {
        return MyApplication.getInstance().getClusterChatSocket();
    }


    /**
     * 获取聊天上传文件token url
     *
     * @param cid
     * @return
     */
    public static String getUploadFileTokenUrl(String cid) {
        return getECMChatUrl() + "/api/v1/channel/" + cid + "/file/request";
    }

    /**
     * 获取聊天语音文件token url
     *
     * @param cid
     * @return
     */
    public static String getUploadMediaVoiceFileTokenUrl(String cid) {
        return getECMChatUrl() + "/api/v1/channel/" + cid + "/voice/request";
    }

    /**
     * 获取V1版消息中聊天文件下载地址
     *
     * @param cid
     * @param path
     * @return
     */
    public static String getChatFileResouceUrl(String cid, String path) {
        return getECMChatUrl() + "/api/v1/channel/" + cid + "/file/request?path=" + path;
    }

    /**
     * 获取V1版消息中聊天语音文件下载地址
     *
     * @param cid
     * @param path
     * @return
     */
    public static String getChatVoiceFileResouceUrl(String cid, String path) {
        return getECMChatUrl() + "/api/v1/channel/" + cid + "/voice/request?path=" + path;
    }

    /**
     * 获取session列表url
     *
     * @return
     */
    public static String getChannelListUrl() {
        return getECMChatChannelUrl() + "/channel/session";
    }

    /**
     * 获取频道信息url
     *
     * @param cid
     * @return
     */
    public static String getChannelInfoUrl(String cid) {
        return getECMChatChannelUrl() + "/channel/" + cid;
    }

    /**
     * 获取所有群组类型频道列表url
     *
     * @return
     */
    public static String getAllGroupChannelListUrl() {
        return getECMChatChannelUrl() + "/channel/group?limit=-1";
    }

    /**
     * 获取所有群组频道信息
     *
     * @return
     */
    public static String getChannelGroupInfoList() {
        return getECMChatChannelUrl() + "/channel?limit=1000";
    }

    /**
     * 获取创建聊天url
     *
     * @return
     */
    public static String getCreateChannelUrl() {
        return getECMChatChannelUrl() + "/channel";
    }

    /**
     * 获取创建聊天url
     *
     * @return
     */
    public static String getCreateDirectConversationUrl() {
        return getECMChatConversationBaseUrl() + "/channel/direct";
    }

    /**
     * 获取创建聊天url
     *
     * @return
     */
    public static String getCreateGroupConversationUrl() {
        return getECMChatConversationBaseUrl() + "/channel/group";
    }


    /**
     * 获取更新群组名称url
     *
     * @param cid
     * @return
     */
    public static String getUpdateChannelGroupNameUrl(String cid) {
        return getECMChatChannelUrl() + "/channel?cid=" + cid;
    }

    /**
     * 获取退出群聊url
     *
     * @param cid
     * @return
     */
    public static String getQuitChannelGroupUrl(String cid) {
        return getECMChatConversationBaseUrl() + "/channel/group/" + cid + "/participation";
    }

    /**
     * 获取删除频道url
     *
     * @param cid
     * @return
     */
    public static String getDeleteChannelUrl(String cid) {
        return getECMChatConversationBaseUrl() + "/channel/" + cid;
    }

    /**
     * 获取会话列表
     *
     * @return
     */
    public static String getConversationListUrl() {
        return getECMChatConversationBaseUrl() + "/channel";
    }

    /**
     * 设置会话是否置顶
     *
     * @return
     */
    public static String getConversationSetStick(String id) {
        return getECMChatConversationBaseUrl() + "/channel/" + id + "/focus";
    }

    /**
     * 设置会话是否可见
     *
     * @return
     */
    public static String getConversationSetHide(String id) {
        return getECMChatConversationBaseUrl() + "/channel/" + id + "/visuality";
    }

    /**
     * 设置会话是否消息免打扰
     *
     * @return
     */
    public static String getConversationSetDnd(String id) {
        return getECMChatConversationBaseUrl() + "/channel/" + id + "/dnd";
    }

    /**
     * 修改群组成员
     *
     * @param id
     * @return
     */
    public static String getModifyGroupMemberUrl(String id) {
        return getECMChatConversationBaseUrl() + "/channel/group/" + id + "/member";
    }

    /**
     * 获取会话信息
     *
     * @param id
     * @return
     */
    public static String getConversationInfoUrl(String id) {
        return getECMChatConversationBaseUrl() + "/channel/" + id;
    }

    /**
     * 获取修改会话名称url
     *
     * @param id
     * @return
     */
    public static String getUpdateConversationNameUrl(String id) {
        return getECMChatConversationBaseUrl() + "/channel/" + id + "/name";
    }
    /**************************************************应用和应用中心********************************************************************/


    /**
     * 获取所有App以及查询app
     *
     * @return
     */
    public static String getAllApps() {
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/getAllApps";
    }

    /**
     * 获取所有App以及查询app
     *
     * @return
     */
    public static String getNewAllApps() {
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/appCenterList";
    }

    /**
     * 验证身份的Uri
     *
     * @return
     */
    public static String getAppAuthCodeUri() {
        return MyApplication.getInstance().getCloudId() + "oauth2.0/quick_authz_code";
    }

    /**
     * 获取我的应用小部件的url
     *
     * @return
     */
    public static String getMyAppWidgetsUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v6.0/app/recommend/apps";
    }

    /**
     * 获取用户apps
     *
     * @return
     */
    public static String getUserApps() {
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v3.1/imp_app/userApps";
    }

    /**
     * 获取引用详情
     *
     * @return
     */
    public static String getAppInfo() {
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/getAppInfo";
    }

    /**
     * 添加app
     *
     * @returnsunqx
     */
    public static String addApp() {
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/installApp";
    }

    /**
     * 移除app
     *
     * @return
     */
    public static String removeApp() {
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/uninstallApp";
    }

    /**
     * 检查更新
     *
     * @return
     */
    public static String checkUpgrade() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/upgrade/checkVersion";
    }


    /**
     * 获取通讯录中的人员
     *
     * @return
     */
    public static String getContactUserUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v4.0/contacts/users";
    }

    /**
     * 获取通讯录中的人员更新
     *
     * @return
     */
    public static String getContactUserUrlUpdate() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/contacts/users";
    }


    /**
     * 获取通讯录中的组织
     *
     * @return
     */
    public static String getContactOrgUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v4.0/contacts/orgs";
    }

    /**
     * 获取通讯录中的组织更新
     *
     * @return
     */
    public static String getContactOrgUrlUpdate() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/contacts/orgs";
    }


    /**
     * 行政审批验证密码
     */
    public static String getVeriryApprovalPasswordUrl() {
        return MyApplication.getInstance().getClusterEmm() + "proxy/shenpi/langchao.ecgap.inportal/login/CheckLoginDB.aspx?";
    }


    /**
     * 获取gs-msg  scheme url
     *
     * @param host
     * @return
     */
    public static String getGSMsgSchemeUrl(String host) {
        return getEMMBaseUrl() + "api/mam/v3.0/gs_sso/msg_uri?id=" + host;
    }

    /**
     * 获取app真实地址
     *
     * @param appId
     * @return
     */
    public static String getAppRealUrl(String appId) {
        return getEMMBaseUrl() + "api/mam/v3.0/gs_sso/app_uri?id=" + appId;
    }

    /*****************************************ReactNative**************************************/
    /**
     * 更新的Native地址
     *
     * @return
     */
    public static String getReactNativeUpdate() {
        return getECMDistribution() + "/view/DISCOVER/bundle/?";
    }

    /**
     * 获取clientid的
     *
     * @return
     */
    public static String getClientId() {
        return getECMDistribution() + "/client/registry";
    }

    /**
     * 写回客户端日志
     *
     * @return
     */
    public static String getClientLog() {
        return getECMDistribution() + "/view/update/DISCOVER/?";
    }

    /**
     * zip文件下载地址
     *
     * @return
     */
    public static String getZipUrl() {
        return getStorageLegacy() + "/res/stream/";
    }

    /**
     * ReactNative应用安装地址查询接口
     *
     * @return
     */
    public static String getReactNativeInstallUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/queryByUri";
    }

    /**
     * ReactNative应用更新写回
     *
     * @return
     */
    public static String getReactNativeWriteBackUrl(String appModule) {
        return getECMDistribution() + "/app/" + appModule + "/update";
    }

    /******************新闻接口**************************/
    /**
     * 获取新闻
     *
     * @param url
     * @return
     */
    public static String getGroupNewsUrl(String url) {
        return getECMNews() + url;
    }

    /**
     * 获取网页地址
     *
     * @param url
     * @return
     */
    public static String getGroupNewsHtmlUrl(String url) {
        return getStorageLegacy() + url;
    }

    /**
     * 得到集团新闻的Path
     *
     * @return
     */
    public static String getGroupNewsArticleUrl() {
        return "/res" + "/article" + "/";
    }

    /**
     * 获取新闻批示
     *
     * @param newsId
     * @return
     */
    public static String getNewsInstruction(String newsId) {
        return getECMNews() + "/content/news/" + newsId + "/editor-comment";
    }


    /***********************VOLUME云盘****************/
    /**
     * 获取云盘列表
     *
     * @return
     */
    public static String getVolumeListUrl() {
        return getUrlBaseVolume();
    }

    /**
     * 更新网盘信息
     *
     * @param volumeId
     * @return
     */
    public static String getUpdateVolumeInfoUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId;
    }

    /**
     * 获取云盘成员url
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeMemUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/member";
    }

    /**
     * 获取云盘组url
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeGroupUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/group";
    }


    /**
     * 获取组url
     *
     * @param groupId
     * @return
     */
    public static String getGroupBaseUrl(String groupId) {
        return getUrlBaseGroup() + "/" + groupId;
    }

    /**
     * 获取组成员URL
     *
     * @param groupId
     * @return
     */
    public static String getGroupMemBaseUrl(String groupId) {
        return getGroupBaseUrl(groupId) + "/member";
    }

    /**
     * 获取云盘文件列表
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileOperationUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file";
    }

    /**
     * 获取云盘上传STS token
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileUploadSTSTokenUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file/request";
    }

    /**
     * 获取云盘创建文件夹url
     *
     * @param volumeId
     * @return
     */
    public static String getCreateForderUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/directory";
    }

    /**
     * 获取文件重命名url
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileRenameUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file/name";
    }

    /**
     * 获取云盘文件移动url
     *
     * @param volumeId
     * @return
     */
    public static String getMoveVolumeFileUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file/path";
    }

    /**
     * 获取复制文件的url
     *
     * @param volumeId
     * @return
     */
    public static String getCopyVolumeFileUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file/duplication";
    }

    /**
     * 根据volumeId
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileGroupUrl(String volumeId) {
        return getUrlBaseVolume() + "/" + volumeId + "/file/group/privilege";
    }
    /**************************Webex********************************************/

    /**
     * 获取webex会议列表
     *
     * @return
     */
    public static String getWebexMeetingListUrl() {
        return getEMMBaseUrl() + "api/mam/v6.0/webex";
    }

    /**
     * 预定会议
     *
     * @return
     */
    public static String getScheduleWebexMeetingUrl() {
        return getEMMBaseUrl() + "api/mam/v6.0/webex/v2";
    }

    /**
     * 获取webex头像地址
     *
     * @param email
     * @return
     */
    public static String getWebexPhotoUrl(String email) {
        return getEMMBaseUrl() + "img/userhead/" + email;
    }

    /**
     * 获取webex会议
     *
     * @return
     */
    public static String getWebexMeetingUrl(String meetingID) {
        return getEMMBaseUrl() + "api/mam/v6.0/webex/SessionInfo/" + meetingID;
    }

    /**
     * 删除webex会议
     *
     * @return
     */
    public static String getRemoveWebexMeetingUrl(String meetingID) {
        return getEMMBaseUrl() + "api/mam/v6.0/webex/remove/" + meetingID;
    }

    /**
     * 获取webex会议TK
     *
     * @return
     */
    public static String getWebexTK() {
        return getEMMBaseUrl() + "api/mam/v6.0/webex/gettk";
    }

    /**************************Mail********************************************/
    public static String getMailBaseUrl() {
        return getEMMBaseUrl() + "api/ews/v1.0";
    }

    public static String getMailFolderUrl() {
        return getMailBaseUrl() + "/Folder";
    }


    public static String getMailListUrl() {
        return getMailBaseUrl() + "/Mail/List";
    }

    public static String getMailDetailUrl(boolean isEncrypted) {
        return getMailBaseUrl() + (isEncrypted ? "/Mail/EncryptedDetail" : "/Mail/Detail");
    }

    public static String getLoginMailUrl() {
        return getMailBaseUrl() + "/UserProfile/MailBind";
    }

    public static String getMailAttachmentUrl() {
        return getMailBaseUrl() + "/Mail/SafeAttachment?";
    }

    /**
     * 获取上传Certificate的接口
     *
     * @return
     */
    public static String getCertificateUrl() {
        return getMailBaseUrl() + "/UserProfile/CheckData";
    }

    /**
     * 获取上传邮件Url
     *
     * @return
     */
    public static String getUploadMailUrl() {
        return getMailBaseUrl() + "/Mail/SafeSend";
    }

    /**
     * 获取删除邮件Url
     *
     * @return
     */
    public static String getRemoveMailUrl() {
        return getMailBaseUrl() + "/Mail/Remove";
    }

/************************************************************************工作****************************************************************************/
    /***************会议接口*****************************/
    /**
     * 工作页面会议
     *
     * @return
     */
    private static String getMeetingBaseUrl() {
        return getECMScheduleUrl() + "/meeting/";
    }

    /**
     * 会议预定
     *
     * @return
     */
    public static String getMeetingsUrl() {
        String meetingUrl = "";
        if (MyApplication.getInstance().getClusterScheduleVersion().toLowerCase().startsWith("v0") || MyApplication.getInstance().getClusterScheduleVersion().toLowerCase().startsWith("v1")) {
            meetingUrl = getMeetingBaseUrl() + "room/bookings";
        }
        return meetingUrl;
    }

    /**
     * 会议室列表
     *
     * @return
     */
    public static String getMeetingRoomsUrl() {
        return getMeetingBaseUrl() + "room";
    }

    /**
     * 获取时间过滤的rooms
     *
     * @return
     */
    public static String getAvailable() {
        return getMeetingBaseUrl() + "room/available";
    }

    /**
     * 删除会议
     *
     * @return
     */
    public static String getMeetingDeleteUrl() {
        return getMeetingBaseUrl() + "room/booking/cancel?";
    }

    /**
     * 会议室接口
     *
     * @return
     */
    public static String getBookingRoomUrl() {
        return getMeetingBaseUrl() + "booking";
    }


    /**
     * 获取某一个会议室的会议预定情况
     *
     * @return
     */
    public static String getRoomMeetingListUrl() {
        return getMeetingBaseUrl() + "booking/room";
    }

    /**
     * 获取办公地点
     *
     * @return
     */
    public static String getOfficeUrl() {
        return getMeetingBaseUrl() + "location/office";
    }

    /**
     * 增加办公地点
     *
     * @return
     */
    public static String addOfficeUrl() {
        return getMeetingBaseUrl() + "location/office";
    }

    /**
     * 会议的root路径
     *
     * @return
     */
    public static String getMeetingRootUrl() {
        return getECMScheduleUrl() + "/meeting";
    }

    /**
     * 获取是否管理员接口
     *
     * @return
     */
    public static String getMeetingIsAdminUrl() {
        return getMeetingBaseUrl() + "is_admin?";
    }

    /**
     * 获取园区
     *
     * @return
     */
    public static String getLoctionUrl() {
        return getMeetingBaseUrl() + "location";
    }

    /**********************日历接口**********************/
    /**
     * 日历相关Uri
     *
     * @return
     */
    public static String getCalendarUrl() {
        String scheduleVersion = MyApplication.getInstance().getClusterScheduleVersion().toLowerCase();
        String calendarUrl = "";
        if (scheduleVersion.startsWith("v0")) {
            calendarUrl = getECMScheduleUrl() + "/api/v0";
        } else if (scheduleVersion.startsWith("v1")) {
            calendarUrl = getECMScheduleUrl();
        }
        return calendarUrl;
    }

    /*******************任务*****************************/
    /**
     * 任务基础URL
     *
     * @return
     */
    private static String getToDoBaseUrl() {
        String scheduleVersion = MyApplication.getInstance().getClusterScheduleVersion().toLowerCase();
        String todoUrl = "";
        if (scheduleVersion.startsWith("v0")) {
            todoUrl = getECMScheduleUrl() + "/api/v0/todo/";
        } else if (scheduleVersion.startsWith("v1")) {
            todoUrl = getECMScheduleUrl() + "/todo/";
        }
        return todoUrl;
    }

    /**
     * 添加附件
     *
     * @param cid
     * @return
     */
    public static String getAddAttachmentsUrl(String cid) {
        return getToDoBaseUrl() + cid + "/attachments";
    }

    /**
     * 获取我的任务
     *
     * @return
     */
    public static String getToDoRecentUrl() {
        return getToDoBaseUrl() + "recent";
    }

    /**
     * 获取我参与的任务
     *
     * @return
     */
    public static String getInvolvedTasksUrl() {
        return getToDoBaseUrl() + "involved";
    }

    /**
     * 获取我参与的任务
     *
     * @return
     */
    public static String getFocusedTasksUrl() {
        return getToDoBaseUrl() + "focused";
    }

    /**
     * 创建任务
     *
     * @return
     */
    public static String getCreateTaskUrl() {
        return getToDoBaseUrl();
    }

    /**
     * 获取今天的任务
     *
     * @return
     */
    public static String getTodayTaskUrl() {
        return getToDoBaseUrl() + "today";
    }

    /**
     * 获取所有Tag
     *
     * @return
     */
    public static String getTagUrl() {
        return getToDoBaseUrl() + "tag";
    }

    /**
     * 获取所有task
     *
     * @param id
     * @return
     */
    public static String getTasksList(String id) {
        return getToDoBaseUrl() + "list/" + id + "/tasks";
    }

    /**
     * 变更任务所有人
     *
     * @return
     */
    public static String getChangeMessionOwnerUrl() {
        return getToDoBaseUrl();
    }


    /*************************************************发现*********************************************************/
    /**
     * 通过车站名获取到达城市,改造多云写死地址
     *
     * @return
     */
    public static String getTripArriveCityUrl() {
        return "https://ecm.inspur.com/" + MyApplication.getInstance().getTanent() + "/trip/simple/city";
    }

    /**
     * 知识
     *
     * @return
     */
    public static String getKnowledgeTipsUrl() {
        return "";
    }

    /**
     * 更新行程信息接口
     *
     * @return
     */
    public static String getUpdateTripInfoUrl() {
        return "https://ecm.inspur.com/" + MyApplication.getInstance().getTanent() + "/trip/simple/upload";
    }

    public static String getTripInfoUrl() {
        return "https://ecm.inspur.com/" + MyApplication.getInstance().getTanent() + "/trip/simple/detail?trip_ticket=";
    }

    /**
     * 获取语言的接口
     *
     * @return
     */
    public static String getLangUrl() {
        return getEcmUrl() + "/" + MyApplication.getInstance().getTanent() + "/settings/lang";
    }


    /**
     * 获取应用未处理消息条数的URL
     *
     * @return
     */
    public static String getAppBadgeNumUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v6.0/app/badge";
    }


    /***************************************设置*********************************************************************/

    /**
     * 获得推荐云+页面url
     *
     * @return
     */
    public static String getRecommandAppUrl() {
        return MyApplication.getInstance().getClusterEmm() + "admin/share_qr";
    }

    /**
     * 获取我的信息展示配置
     *
     * @return
     */
    public static String getUserProfileUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/userprofile/displayconfig";
    }

    /**
     * 获取个人信息及其显示配置
     */
    public static String getUserProfileAndDisPlayUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/userprofile/detail";
    }


    /**
     * 修改用户头像
     *
     * @param
     */
    public static String getUpdateUserHeadUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/user/update_head";
    }

    /**
     * 修改用户信息
     *
     * @return
     */
    public static String getModifyUserInfoUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api?module=user&method=update_baseinfo";
    }


    /**
     * 设置人脸头像
     *
     * @return
     */
    public static String getFaceSettingUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v6.0/face/save";
    }

    /**
     * 脸部图像验证
     *
     * @return
     */
    public static String getFaceVerifyUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v6.0/face/verify";
    }

    /***********设备管理******************
     /**
     * 获取解绑设备url
     *
     * @return
     */
    public static String getUnBindDeviceUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/mdm/v3.0/device/unbind ";
    }

    /**
     * 获取绑定设备
     *
     * @return
     */
    public static String getBindingDevicesUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/mdm/v3.0/device/getUserDevices";
    }

    /**
     * 获取设备注册URl
     *
     * @param context
     * @return
     */
    public static String getDeviceRegisterUrl(Context context) {
        return getEMMBaseUrl() + "app/mdm/v3.0/loadForRegister?udid=" + AppUtils.getMyUUID(context);
    }

    /**
     * 获取绑定设备
     *
     * @return
     */
    public static String getDeviceLogUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/mdm/v3.0/device/getDeviceLogs";
    }

    /**
     * 获取MDM启用状态
     *
     * @return
     */
    public static String getMDMStateUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/userprofile/mdm_state";
    }

    /**
     * 上传设备管理所需token和设备ID
     *
     * @return
     */
    public static String getUploadMDMInfoUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/mdm/v3.0/mdm/mdm_check";
    }

    /**
     * 设备检查
     *
     * @return
     */
    public static String getDeviceCheckUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/mdm/v3.0/mdm/check_state";
    }

    /**
     * 获取卡包信息
     *
     * @return
     */
    public static String getCardPackageUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/buildinapp/v6.0/CardPackage";
    }

    /**
     * 获取是否打开体验升级
     *
     * @return
     */
    public static String getUserExperienceUpgradeFlagUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/upgrade/checkExperiencePlan";
    }

    public static String getUpdateUserExperienceUpgradeFlagUrl(int flag) {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/upgrade/joinExperiencePlan?flag=" + flag;
    }

    /**
     * 获取我的页面个人信息卡片的menu配置
     *
     * @return
     */
    public static String getUserCardMenusUrl() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v6.0/config/UserCardMenus";
    }

    /**
     * 获取声网参数Url
     *
     * @return
     */
    public static String getAgoraUrl() {
//        return MyApplication.getInstance().getClusterEmm()+"";
        return "http://172.31.2.36:88/api/sys/v6.0/voice/create";
    }

    /**
     * 加入频道成功后告诉服务端
     *
     * @return
     */
    public static String getAgoraJoinChannelSuccessUrl() {
        return "http://172.31.2.36:88/api/sys/v6.0/voice/join/";
    }

    /**
     * 获取频道信息url
     *
     * @return
     */
    public static String getAgoraChannelInfoUrl() {
        return "http://172.31.2.36:88/api/sys/v6.0/voice/";
    }

    /**
     * 拒绝频道url
     *
     * @return
     */
    public static String getAgoraRefuseChannelUrl() {
        return "http://172.31.2.36:88/api/sys/v6.0/voice/refuse/";
    }

    /**
     * 离开channel
     *
     * @return
     */
    public static String getAgoraLeaveChannelUrl() {
        return "http://172.31.2.36:88/api/sys/v6.0/voice/leave/";
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
     * 未读消息url
     *
     * @return
     */
    public static String getBadgeCountUrl() {
        String badgeCountUrl = "";
        if (MyApplication.getInstance().isV0VersionChat()) {
            badgeCountUrl = getECMChatUrl() + "/unread-count";
        } else if (MyApplication.getInstance().isV1xVersionChat()) {
            badgeCountUrl = getECMChatUrl() + "/rest/v1/unread-count";
        }
        return badgeCountUrl;
    }

    /**
     * 网络状态检测API
     * 固定地址
     *
     * @return
     */

    public static String getScheduleBaseUrl(){
        return "https://emm.inspur.com";
        //return "http://172.31.2.36:88";
    }

    public static String getCheckCloudPluseConnectUrl() {
        return  getScheduleBaseUrl()+"/api/mam/v3.0/heart/success";
    }

    public static String getCancelTokenUrl() {
        return MyApplication.getInstance().getCloudId() + "oauth2.0/profile";
    }

    public static String getScheduleListUrl() {
        return getScheduleBaseUrl()+"/api/schedule/v6.0/calendar/GetList?";
    }

    public static String getAddScheduleUrl() {
        return getScheduleBaseUrl()+"/api/schedule/v6.0/calendar/add";
    }

    public static String getUpdateScheduleUrl() {
        return getScheduleBaseUrl()+"/api/schedule/v6.0/calendar/update";
    }

    public static String getDeleteScheduleUrl(String scheduleId) {
        return getScheduleBaseUrl()+"/api/schedule/v6.0/calendar/remove/" + scheduleId;
    }

    public static String getAddMeetingUrl() {
        return getScheduleBaseUrl()+"/api/schedule/v6.0/meeting/add";
    }

    public static String getDelMeetingUrl(String meetingId) {
        return getScheduleBaseUrl() + "/api/schedule/v6.0/meeting/remove/"+meetingId;
    }

    public static String getMeetingListByStartTime(){
        return getScheduleBaseUrl()+"/api/schedule/v6.0/meeting/GetByStartTime?";
    }

    public static String getMeetingHistoryListByPage(int id){
        return getScheduleBaseUrl()+"/api/schedule/v6.0/meeting/GetHistory/"+id;
    }

    public static String getRoomMeetingListByMeetingRoom(){
        return getScheduleBaseUrl()+"/api/schedule/v6.0/meeting/GetRoomUse?";
    }

    public static String getMeetingUpdateUrl(){
        return getScheduleBaseUrl()+"/api/schedule/v6.0/meeting/update";
    }

    /**
     * 获取删除工作中的tags
     */
    public static String getDelTaskTagsUrl(String taskId) {
        return getToDoBaseUrl() + taskId + "/tags";
    }

    /**
     * 获取删除工作中的tags
     */
    public static String getAddTaskTagsUrl(String taskId) {
        return getToDoBaseUrl() + taskId + "/tags";
    }

    /**
     * 获取决策卡片机器人触发事件
     * @return
     */
    public static String getDecideCardBotRequestUrl(){
        return "https://api.inspuronline.com/bot/v1/action/trigger/";
    }

}
