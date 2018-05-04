package com.inspur.emmcloud.api;


import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;


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

    /**
     * EMM服务
     * @return
     */
    public static String getEMMBaseUrl() {
        return MyApplication.getInstance().getClusterEmm();
    }

    /**
     * EcmChat服务
     * @return
     */
    public static String getECMChatUrl(){
        return MyApplication.getInstance().getClusterChat();
    }

    /**
     * EcmSchedule服务
     * @return
     */
    public static String getECMScheduleUrl(){
        return MyApplication.getInstance().getClusterSchedule();
    }

    /**
     * ECMDistribution服务
     * @return
     */
    public static String getECMDistribution(){
        return MyApplication.getInstance().getClusterDistribution();
    }

    /**
     * ECMNews服务
     * @return
     */
    public static String getECMNews(){
        return MyApplication.getInstance().getClusterNews();
    }

    /**
     * ECMCloudDriver服务
     * @return
     */
    public static String getCloudDriver(){
        return MyApplication.getInstance().getClusterCloudDrive();
    }

    /**
     * StorageLegacy服务
     * @return
     */
    public static String getStorageLegacy(){
        return MyApplication.getInstance().getClusterStorageLegacy();
    }

    public static String getUrlBaseVolume() {
        return getCloudDriver() + "cloud-drive/api/v1/volume";
    }

    public static String getUrlBaseGroup() {
        return getCloudDriver() + "cloud-drive/api/v1/group";
    }
    /***************************************************************系统*******************************************************************/
    /**
     * 异常上传接口
     *
     * @return
     */
    public static String uploadException() {
        return "http://u.inspur.com/analytics/api/ECMException/Post";
    }

    /**
     * 新版底部Tabbar接口
     *
     * @return
     */
    public static String getAppNewTabs() {
        return getECMDistribution() + "/preference/main-tab/latest";
    }


    public static String getAppConfigUrl() {
        return MyApplication.getInstance().getClusterEmm() + "v3.0/api/app/config/array?key=WebAutoRotate&key=CommonFunctions&key=IsShowFeedback&key=IsShowCustomerService&key=PosReportTimeInterval&key=WorkPortlet";
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
        return getECMChatUrl() + "/message/api/v1/client";
    }

    /************************************************************************登录*****************************************************************/

    /**
     * 请求短信验证码
     *
     * @param mobile
     * @return
     */
    public static String getReqLoginSMSUrl(String mobile) {
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
    public static String getChannelImgUrl(Context context, String inspurID) {
        if (StringUtils.isBlank(inspurID) || inspurID.equals("null"))
            return null;
        String headImgUrl = ((MyApplication) context.getApplicationContext()).getUserPhotoUrl(inspurID);
        if (headImgUrl == null && !((MyApplication) context.getApplicationContext()).isKeysContainUid(inspurID)) {
            Contact contact = ContactCacheUtils.getUserContact(context, inspurID);
            if (contact != null) {
                headImgUrl = MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/img/userhead/" + inspurID;
                String lastUpdateTime = contact.getLastUpdateTime();
                if (!StringUtils.isBlank(lastUpdateTime) && (!lastUpdateTime.equals("null"))) {
                    headImgUrl = headImgUrl + "?" + lastUpdateTime;
                }
                ((MyApplication) context.getApplicationContext()).setUsesrPhotoUrl(inspurID, headImgUrl);
            } else if (((MyApplication) context.getApplicationContext())
                    .getIsContactReady()) {
                ((MyApplication) context.getApplicationContext()).setUsesrPhotoUrl(inspurID, null);
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
     * @param context
     * @param uid
     * @return
     */
    public static String getUserIconUrl(Context context, String uid){
        String iconUrl;
        if (uid.startsWith("BOT")) {
            iconUrl = APIUri.getRobotIconUrl(RobotCacheUtils
                    .getRobotById(context, uid)
                    .getAvatar());
        } else {
            iconUrl = APIUri.getChannelImgUrl(context, uid);
        }
        return  iconUrl;
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
        return getStorageLegacy()  + "/res/" + url;
    }

    /**
     * 添加群组成员
     *
     * @param cid
     * @return
     */
    public static String getAddGroupMembersUrl(String cid) {
        return getECMChatUrl() + "/channel/group/" + cid + "/users?";
    }

    /**
     * 消息免打扰
     *
     * @return
     */
    public static String getNointerRuptionUrl() {
        return getECMChatUrl() + "/session/dnd";
    }

    /**
     * 获取所有机器人信息
     *
     * @return
     */
    public static String getAllBotInfo() {
        return getECMChatUrl() + "/registry/bot";
    }

    /**
     * 通过Id获取机器人信息
     *
     * @return
     */
    public static String getBotInfoById() {
        return getECMChatUrl() + "/registry/bot/";
    }

    /**
     * 获取websocket链接url
     *
     * @return
     */
    public static String getWebsocketConnectUrl() {
        return MyApplication.getInstance().getClusterChatSocket();
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
        return MyApplication.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/userApps";
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
     * 获取获取所有通讯录
     *
     * @return
     */
    public static String getAllContact() {
        return MyApplication.getInstance().getClusterEmm() + "api/sys/v3.0/contacts/get_all";
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
     * 得到集团新闻的Path
     *
     * @return
     */
    public static String getGroupNewsArticleUrl() {
        return "/" + MyApplication.getInstance().getTanent() + "/res" + "/article" + "/";
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
     * @param volumeId
     * @return
     */
    public static String getVolumeFileGroupUrl(String volumeId){
        return getUrlBaseVolume() + "/" + volumeId + "/file/group/privilege";
    }
/************************************************************************工作****************************************************************************/
    /***************会议接口*****************************/
    /**
     * 工作页面会议
     *
     * @return
     */
    private static String getMeetingBaseUrl() {
        return getECMScheduleUrl()  + "/meeting/";
    }

    /**
     * 会议预定
     *
     * @return
     */
    public static String getMeetingsUrl() {
        return getMeetingBaseUrl() + "room/bookings";
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
    public static String getDeleteMeetingUrl() {
        return getMeetingBaseUrl() + "room/booking/cancel";
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
        return getMeetingBaseUrl() + "is_admin";
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
        return getECMScheduleUrl() ;
    }

    /*******************任务*****************************/
    /**
     * 任务基础URL
     *
     * @return
     */
    private static String getToDoBaseUrl() {
        return getECMScheduleUrl() + MyApplication.getInstance().getTanent() + "/todo/";
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
        return getECMScheduleUrl() + "/todo";
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
        return "https://ecm.inspur.com/trip/simple/city";
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
     * @return
     */
    public static String getUpdateTripInfoUrl(){
        return "https://ecm.inspur.com/trip/simple/upload";
    }

    public static String getTripInfoUrl(){
        return "https://ecm.inspur.com/trip/simple/detail?trip_ticket=";
    }

    /**
     * 获取语言的接口
     *
     * @return
     */
    public static String getLangUrl() {
        return getECMDistribution() + "/settings/lang";
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
}
