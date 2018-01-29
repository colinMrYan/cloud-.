package com.inspur.emmcloud.api;


import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.util.common.StringUtils;



/**
 * 本类中包含4个常量，分别是
 * 域名的Url：URL_BASE_ECM
 * 其次，每个模块下有一个获取基础Url的方法
 * 整体有一个getEcmTanent的方法，获取到tanent这一级
 */
public class APIUri {
    private static final String URL_BASE_ECM = "https://ecm.inspur.com/";
    private static final String URL_BASE_EMM = "https://emm.inspur.com/";
    private static final String URL_BASE_ID = "https://id.inspuronline.com/";
    private static final String URL_BASE_YUNJIA = "https://yunjia.inspur.com/";
    private static final String URL_BASE_VOLUME = URL_BASE_YUNJIA+"cloud-drive/api/v1/volume";
    private static final String URL_BASE_GROUP = URL_BASE_YUNJIA+"cloud-drive/api/v1/group";


    /**
     * 获取到租户级的URL
     *
     * @return
     */
    private static String getEcmTanentUrl() {
        return URL_BASE_ECM + MyApplication.getInstance().getTanent();
    }

    public static String getECMBaseUrl(){
        return URL_BASE_ECM;
    }

    public static String getEMMBaseUrl(){
        return URL_BASE_EMM;
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
        return getEcmTanentUrl() + "/api/v0/preference/main-tab/latest";
    }


    public static String getAppConfigUrl() {
        return URL_BASE_EMM + "v3.0/api/app/config/array?key=WebAutoRotate&key=CommonFunctions&key=IsShowFeedback&key=IsShowCustomerService&key=PosReportTimeInterval&key=WorkPortlet";
    }


    /**
     * app闪屏页面
     *
     * @return
     */
    public static String getSplashPageUrl() {
        return getEcmTanentUrl() + "/api/v0/preference/launch-screen/latest";
    }

    /**
     * @return
     */
    public static String getUploadSplashPageWriteBackLogUrl() {
        return getEcmTanentUrl() + "/api/v0/preference/launch-screen/update";
    }

    /**
     * 存储app配置url
     *
     * @param key
     * @return
     */
    public static String saveAppConfigUrl(String key) {
        return URL_BASE_EMM + "api/sys/v6.0/config/" + key;
    }

    /**
     * 获取上传位置信息url
     *
     * @return
     */
    public static String getUploadPositionUrl() {
        return URL_BASE_EMM + "api/mam/v6.0/app/pos";
    }

    /**
     * 获取上传推送信息的url
     * @return
     */
    public static String getUploadPushInfoUrl(){
        return  URL_BASE_YUNJIA+"message/api/v1/client";
    }

    /************************************************************************登录*****************************************************************/

    /**
     * 请求短信验证码
     * @param mobile
     * @return
     */
    public static String getReqLoginSMSUrl(String mobile) {
        return URL_BASE_ID + "api/v1/passcode?phone=" + mobile;
    }

    /**
     * 验证短信验证码
     * @return
     */
    public static String  getSMSRegisterCheckUrl(){
        return  URL_BASE_EMM+"/api?module=register&method=verify_smscode";
    }

    /**
     * 获取用户信息
     * @return
     */
    public static String getMyInfoUrl(){
        return URL_BASE_ID+"oauth2.0/profile";
    }

    /**
     * 修改密码
     **/
    public static String getChangePsdUrl() {
        return URL_BASE_ID+"console/api/v1/account/password";
    }


    /**
     * 获取oauth认证的基础
     *
     * @return
     */
    public static String getOauthSigninUrl() {
        return URL_BASE_ID + "oauth2.0/token";
    }

    /**
     * 返回我的信息
     *
     * @return
     */
    public static String getOauthMyInfoUrl() {
        return URL_BASE_ID + "oauth2.0/token/profile";
    }

    /**
     * 刷新token
     *
     * @return
     */
    public static String getRefreshToken() {
        return URL_BASE_ID + "oauth2.0/token";
    }


    /**
     * 网页登录
     * @return
     */
    public static String getWebLoginUrl(){
        return  URL_BASE_ID+"oauth2.0/authorize";
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
                headImgUrl = URL_BASE_EMM + "api/sys/v3.0/img/userhead/" + inspurID;
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
     * 个人信息头像显示图片
     *
     * @param url
     * @return
     */
    public static String getUserInfoPhotoUrl(String url) {
        return URL_BASE_EMM + url;
    }

    /**
     * 获取机器人头像路径
     *
     * @return
     */
    public static String getRobotIconUrl(String iconUrl) {
        return getEcmTanentUrl() + "/avatar/stream/"
                + iconUrl;
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
        return URL_BASE_ECM + MyApplication.getInstance().getTanent() + "/res/" + url;
    }

    /**
     * 添加群组成员
     *
     * @param cid
     * @return
     */
    public static String getAddGroupMembersUrl(String cid) {
        return getEcmTanentUrl() + "/channel/group/" + cid + "/users?";
    }

    /**
     * 消息免打扰
     *
     * @return
     */
    public static String getNointerRuptionUrl() {
        return getEcmTanentUrl() + "/session/dnd";
    }

    /**
     * 获取所有机器人信息
     *
     * @return
     */
    public static String getAllBotInfo() {
        return getEcmTanentUrl() + "/api/v0/registry/bot";
    }

    /**
     * 通过Id获取机器人信息
     *
     * @return
     */
    public static String getBotInfoById() {
        return getEcmTanentUrl() + "/api/v0/registry/bot/";
    }

    /**
     * 获取websocket链接url
     * @return
     */
    public static String getWebsocketConnectUrl(){
        return URL_BASE_ECM;
    }

    /**************************************************应用和应用中心********************************************************************/


    /**
     * 获取所有App以及查询app
     *
     * @return
     */
    public static String getAllApps() {
        return URL_BASE_EMM + "api/mam/v3.0/imp_app/getAllApps";
    }

    /**
     * 获取所有App以及查询app
     *
     * @return
     */
    public static String getNewAllApps() {
        return URL_BASE_EMM + "api/mam/v3.0/imp_app/appCenterList";
    }

    /**
     * 验证身份的Uri
     *
     * @return
     */
    public static String getAppAuthCodeUri() {
        return URL_BASE_ID+"oauth2.0/quick_authz_code";
    }

    /**
     * 获取我的应用小部件的url
     *
     * @return
     */
    public static String getMyAppWidgetsUrl() {
        return URL_BASE_EMM + "v3.0/api/app/recommend/apps";
    }

    /**
     * 获取用户apps
     *
     * @return
     */
    public static String getUserApps() {
        return URL_BASE_EMM + "api/imp_app/userApps";
    }

    /**
     * 获取引用详情
     *
     * @return
     */
    public static String getAppInfo() {
        return URL_BASE_EMM + "api/mam/v3.0/imp_app/getAppInfo";
    }

    /**
     * 添加app
     *
     * @returnsunqx
     */
    public static String addApp() {
        return URL_BASE_EMM + "api/mam/v3.0/imp_app/installApp";
    }

    /**
     * 移除app
     *
     * @return
     */
    public static String removeApp() {
        return URL_BASE_EMM + "api/mam/v3.0/imp_app/uninstallApp";
    }

    /**
     * 检查更新
     *
     * @return
     */
    public static String checkUpgrade() {
        return URL_BASE_EMM + "api/sys/v3.0/upgrade/checkVersion";
    }

    /**
     * 获取获取所有通讯录
     *
     * @return
     */
    public static String getAllContact() {
        return URL_BASE_EMM + "api/sys/v3.0/contacts/get_all";
    }


    /**
     * 行政审批验证密码
     */
    public static String getVeriryApprovalPasswordUrl() {
        return URL_BASE_EMM + "proxy/shenpi/langchao.ecgap.inportal/login/CheckLoginDB.aspx?";
    }


    /**
     * 获取gs-msg  scheme url
     * @param host
     * @return
     */
    public static String getGSMsgSchemeUrl(String host){
        return "https://emm.inspur.com/api/mam/v3.0/gs_sso/msg_uri?id=" + host;
    }

    /*****************************************ReactNative**************************************/
    /**
     * 更新的Native地址
     *
     * @return
     */
    public static String getReactNativeUpdate() {
        return getEcmTanentUrl() + "/api/v0/view/DISCOVER/bundle/?";
    }

    /**
     * 获取clientid的
     *
     * @return
     */
    public static String getClientId() {
        return URL_BASE_ECM + MyApplication.getInstance().getTanent() + "/api/v0/client/registry";
    }

    /**
     * 写回客户端日志
     *
     * @return
     */
    public static String getClientLog() {
        return getEcmTanentUrl() + "/api/v0/view/update/DISCOVER/?";
    }

    /**
     * zip文件下载地址
     *
     * @return
     */
    public static String getZipUrl() {
        return getEcmTanentUrl() + "/res/stream/";
    }

    /**
     * ReactNative应用安装地址查询接口
     *
     * @return
     */
    public static String getReactNativeInstallUrl() {
        return URL_BASE_EMM+"api/mam/v3.0/imp_app/queryByUri";
    }

    /**
     * ReactNative应用更新写回
     *
     * @return
     */
    public static String getReactNativeWriteBackUrl(String appModule) {
        return getEcmTanentUrl() + "/api/v0/app/" + appModule + "/update";
    }

    /******************新闻接口**************************/
    /**
     * 获取新闻
     *
     * @param url
     * @return
     */
    public static String getGroupNewsUrl(String url) {
        return URL_BASE_ECM + url;
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
     * @param newsId
     * @return
     */
    public static String getNewsInstruction(String newsId) {
        return getEcmTanentUrl() + "/api/v0/content/news/" + newsId + "/editor-comment";
    }



    /***********************VOLUME云盘****************/
    /**
     * 获取云盘列表
     *
     * @return
     */
    public static String getVolumeListUrl() {
        return URL_BASE_VOLUME;
    }

    /**
     * 更新网盘信息
     * @param volumeId
     * @return
     */
    public static String getUpdateVolumeInfoUrl(String volumeId){
        return URL_BASE_VOLUME+"/"+volumeId;
    }

    /**
     * 获取云盘成员url
     * @param volumeId
     * @return
     */
    public static String getVolumeMemUrl(String volumeId){
        return  URL_BASE_VOLUME+"/"+volumeId+"/member";
    }

    /**
     * 获取组url
     * @param groupId
     * @return
     */
    public static String getGroupBaseUrl(String groupId){
        return  URL_BASE_GROUP+"/"+groupId;
    }

    /**
     * 获取组成员URL
     * @param groupId
     * @return
     */
    public static String getGroupMemBaseUrl(String groupId){
        return  getGroupBaseUrl(groupId)+"/member";
    }

    /**
     * 获取云盘文件列表
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileOperationUrl(String volumeId) {
        return URL_BASE_VOLUME + "/" + volumeId + "/file";
    }

    /**
     * 获取云盘上传STS token
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileUploadSTSTokenUrl(String volumeId) {
        return URL_BASE_VOLUME + "/" + volumeId + "/file/request";
    }

    /**
     * 获取云盘创建文件夹url
     *
     * @param volumeId
     * @return
     */
    public static String getCreateForderUrl(String volumeId) {
        return URL_BASE_VOLUME + "/" + volumeId + "/directory";
    }

    /**
     * 获取文件重命名url
     *
     * @param volumeId
     * @return
     */
    public static String getVolumeFileRenameUrl(String volumeId) {
        return URL_BASE_VOLUME + "/" + volumeId + "/file/name";
    }

    /**
     * 获取云盘文件移动url
     *
     * @param volumeId
     * @return
     */
    public static String getMoveVolumeFileUrl(String volumeId) {
        return URL_BASE_VOLUME + "/" + volumeId + "/file/path";
    }

    /**
     * 获取复制文件的url
     * @param volumeId
     * @return
     */
    public static String getCopyVolumeFileUrl(String volumeId){
        return URL_BASE_VOLUME + "/" + volumeId +"/file/duplication";
    }
/************************************************************************工作****************************************************************************/
    /***************会议接口*****************************/
    /**
     * 工作页面会议
     *
     * @return
     */
    private static String getMeetingBaseUrl() {
        return URL_BASE_ECM + MyApplication.getInstance().getTanent() + "/meeting/";
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
        return getEcmTanentUrl() + "/meeting";
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
        return getEcmTanentUrl() + "/api/v0";
    }

    /*******************任务*****************************/
    /**
     * 任务基础URL
     *
     * @return
     */
    private static String getToDoBaseUrl() {
        return URL_BASE_ECM + MyApplication.getInstance().getTanent() + "/api/v0/todo/";
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
        return getEcmTanentUrl() + "/api/v0/todo";
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
     * 通过车站名获取到达城市
     *
     * @return
     */
    public static String getTripArriveCityUrl() {
        return getEcmTanentUrl() + "/trip/simple/city";
    }

    /**
     * 知识
     *
     * @return
     */
    public static String getKnowledgeTipsUrl() {
        return getEcmTanentUrl() + "/tips";
    }

    /**
     * 卡包
     *
     * @param uri
     * @return
     */
    public static String getHttpApiUrl(String uri) {
        return getEcmTanentUrl() + "/" + uri;
    }

    /**
     * 获取语言的接口
     * @return
     */
    public static String getLangUrl(){
        return getEcmTanentUrl() + "/settings/lang";
    }


    /**
     * 获取应用未处理消息条数的URL
     *
     * @return
     */
    public static String getAppBadgeNumUrl() {
        return URL_BASE_EMM + "api/mam/v6.0/app/badge";
    }


    /***************************************设置*********************************************************************/

    /**
     * 获得推荐云+页面url
     * @return
     */
    public static String getRecommandAppUrl() {
        return URL_BASE_EMM +"admin/share_qr";
    }

    /**
     * 获取我的信息展示配置
     *
     * @return
     */
    public static String getUserProfileUrl() {
        return URL_BASE_EMM + "api/sys/v3.0/userprofile/displayconfig";
    }

    /**
     * 修改用户头像
     *
     * @param
     */
    public static String getUpdateUserHeadUrl() {
        return  URL_BASE_EMM+"api/sys/v3.0/user/update_head";
    }

    /**
     * 修改用户信息
     * @return
     */
    public static String getModifyUserInfoUrl(){
        return  URL_BASE_EMM+"api?module=user&method=update_baseinfo";
    }


    /**
     * 设置人脸头像
     * @return
     */
    public static String getFaceSettingUrl(){
        return  URL_BASE_EMM+"api/sys/v6.0/face/save";
    }

    /**
     * 脸部图像验证
     * @return
     */
    public static String getFaceVerifyUrl(){
        return  URL_BASE_EMM+"api/sys/v6.0/face/verify";
    }

    /***********设备管理******************
    /**
     * 获取解绑设备url
     *
     * @return
     */
    public static String getUnBindDeviceUrl() {
        return URL_BASE_EMM + "mdm/v3.0/mdm/unbind";
    }

    /**
     * 获取绑定设备
     *
     * @return
     */
    public static String getBindingDevicesUrl() {
        return URL_BASE_EMM + "api/v1/device/getUserDevices";
    }

    /**
     * 获取设备注册URl
     * @param context
     * @return
     */
    public static String getDeviceRegisterUrl(Context context){
        return "https://emm.inspur.com/mdm/v3.0/loadForRegister?udid="+ AppUtils.getMyUUID(context);
    }

    /**
     * 获取绑定设备
     *
     * @return
     */
    public static String getDeviceLogUrl() {
        return URL_BASE_EMM + "api/mdm/v3.0/device/getDeviceLogs";
    }

    /**
     * 获取MDM启用状态
     *
     * @return
     */
    public static String getMDMStateUrl() {
        return URL_BASE_EMM + "api/sys/v3.0/userprofile/mdm_state";
    }

    /**
     * 上传设备管理所需token和设备ID
     *
     * @return
     */
    public static String getUploadMDMInfoUrl() {
        return URL_BASE_EMM + "api/mdm/v3.0/device/getUserDevices";
    }

    /**
     * 设备检查
     * @return
     */
    public static String getDeviceCheckUrl(){
       return  URL_BASE_EMM+"api/mdm/v3.0/mdm/check_state";
    }
}
