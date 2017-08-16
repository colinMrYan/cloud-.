package com.inspur.emmcloud.api;


import com.inspur.emmcloud.util.UriUtils;

/**
 * 本类中包含4个常量，分别是
 * 域名的Url：ecmBaseUrl
 * 其次，每个模块下有一个获取基础Url的方法
 * 整体有一个getEcmTanent的方法，获取到tanent这一级
 */
public class APIUri {
	public static String tanent;
	private static String ecmBaseUrl = "https://ecm.inspur.com/";
	private static String emmBaseUrl = "https://emm.inspur.com/api/";
	private static String oauthBaseUrl = "https://id.inspur.com/oauth2.0/token";
	private static String smsLoginBaseUrl = "https://id.inspur.com/api/v1/passcode?phone=";
	/**
	 * 获取到租户级的URL
	 * @return
	 */
	private static String getEcmTanentUrl(){
		return ecmBaseUrl+tanent;
	}
	
    /*****************************************聊天，异常，语言，修改密码接口*********************************************/
	/**
	 * 异常上传接口
	 * @return
	 */
	public static String uploadException() {
		return "http://u.inspur.com/analytics/api/ECMException/Post";
	}
	
	/** 
	 * 添加群组成员
	 * @param cid
	 * @return
	 */
	public static String addGroupMembers(String cid) {
		return getEcmTanentUrl() + "/channel/group/"+cid+"/users?";
	}
	
	/**
	 * 消息免打扰
	 * @return
	 */
	public static String getNointerRuption(){
		return getEcmTanentUrl()+"/session/dnd";
	}
	
	/**
	 * 获取所有机器人信息
	 * @return
	 */
	public static String getAllBotInfo(){
		return getEcmTanentUrl()+"/api/v0/registry/bot";
	}
	
	/**
	 * 通过Id获取机器人信息
	 * @return
	 */
	public static String getBotInfoById(){
		return getEcmTanentUrl()+"/api/v0/registry/bot/";
	}
	
	/** 修改密码**/
	public static String getChangePsd(){
		return "https://id.inspur.com/console/api/v1/account/password";
	}

	/*****************************************新闻接口*********************************************/
	/** 
	 * 获取新闻
	 * @param url
	 * @return
	 */
	public static String getGroupNewsUrl(String url) {
		return ecmBaseUrl + url;
	};

	/**
	 * 得到集团新闻的Path
	 * @return
	 */
	public static String getGroupNewsArticle() {
		return "/" + tanent+"/res" + "/article" + "/";
	}

	public static String getNewsInstruction(String newsId){return  getEcmTanentUrl()+"/api/v0/content/news/"+newsId+"/editor-comment";}
	
	/*************************************************会议接口*******************************************/
	/**
	 * 工作页面会议
	 * @return
	 */
	private static String getMeetingBaseUrl(){
		return ecmBaseUrl + tanent + "/meeting/";
	}
	
	/**
	 * 会议预定
	 * @return
	 */
	public static String getMeetings() {
		return getMeetingBaseUrl()+"room/bookings";
	}

	/**
	 * 会议详情
	 * @param meetingId
	 * @return
	 */
	public static String getMeetingDetails(String meetingId) {
		return getMeetingBaseUrl()+"room/bookings/:"
				+ meetingId;
	}
	
	/**
	 * 会议室空闲时段接口
	 * @param param
	 * @return
	 */
	public static String getAvailableTime(String param) {
		return getMeetingBaseUrl()+"room/idle"
				+ param;
	}

	/**
	 * 会议室列表
	 * @return
	 */
	public static String getMeetingRooms() {
		return getMeetingBaseUrl()+"room";
	}
	
	/**
	 * 获取时间过滤的rooms
	 * @return
	 */
	public static String getAvailable() {
		return getMeetingBaseUrl()+"room/available";
	}
	
	/**
	 * 删除会议
	 * @return
	 */
	public static String deleteMeeting(){
		return getMeetingBaseUrl()+"room/booking/cancel";
	}

	/**
	 *  会议室接口 
	 * @return
	 */
	public static String getBookingRoom() {
		return getMeetingBaseUrl()+"booking";
	}

	/**
	 * 会议状态返回接口
	 * @return
	 */
	public static String getMeetingReply() {
		return getMeetingBaseUrl()+"booking/receipt/state";
	}
	
	/**
	 * 获取某一个会议室的会议预定情况
	 * @return
	 */
	public static String getRoomMeetingList() {
		return getMeetingBaseUrl()+"booking/room";
	}
	
	/**
	 * 获取办公地点
	 * @return
	 */
	public static String getOffice() {
		return getMeetingBaseUrl()+"location/office";
	}
	
	/**
	 * 增加办公地点
	 * @return
	 */
	public static String addOffice() {
		return getMeetingBaseUrl()+"location/office";
	}
	
	/**
	 * 会议的root路径
	 * @return
	 */
	public static String getMeetingRootUrl() {
		return getEcmTanentUrl() + "/meeting";
	}
	
	/**
	 * 获取是否管理员接口
	 * @return
	 */
	public static String getIsAdmin(){
		return getMeetingBaseUrl()+"is_admin";
	}
	
	/**
	 * 获取园区
	 * @return
	 */
	public static String getBuilding() {
		return getMeetingBaseUrl()+"building";
	}
	
	/**
	 * 获取楼层
	 * @return
	 */
	public static String getFloor() {
		return getMeetingBaseUrl()+"floor";
	}
	
	/**
	 * 获取园区
	 * @return
	 */
	public static String getLoction() {
		return getMeetingBaseUrl()+"location";
	}
	
	/**********************************************日历接口*******************************************/
	/**
	 * 日历相关Uri
	 * @return
	 */
	public static String getCalendarUri(){
		return getEcmTanentUrl()+"/api/v0";
	}
	
	/**********************************************任务接口*******************************************/
	/**
	 * 任务基础URL
	 * @return
	 */
	private static String getToDoBaseUrl(){
		return ecmBaseUrl + tanent + "/api/v0/todo/";
	}
	
	/**
	 * 添加附件
	 * @param cid
	 * @return
	 */
	public static String addAttachments(String cid) {
		return getToDoBaseUrl()+cid+"/attachments";
	}
	
	/**
	 * 获取我的任务
	 * @return
	 */
	public static String getToDoRecent() {
		return getToDoBaseUrl()+"recent";
	}
	
	/**
	 * 获取我参与的任务
	 * @return
	 */
	public static String getInvolved() {
		return getToDoBaseUrl()+"involved";
	}
	
	/**
	 * 获取我参与的任务
	 * @return
	 */
	public static String getFocused() {
		return getToDoBaseUrl()+"focused";
	}
	
	/**
	 * 创建任务
	 * @return
	 */
	public static String createTask() {
		return getEcmTanentUrl()+"/api/v0/todo";
	}
	
	/**
	 * 获取今天的任务
	 * @return
	 */
	public static String getTodayTask() {
		return getToDoBaseUrl()+"today";
	}
	
	/**
	 * 查询所有待办任务
	 * @return
	 */
	public static String getAllListTasks(){
		return getToDoBaseUrl()+"list";
	}
	
	/**
	 * 获取所有Tag
	 * @return
	 */
	public static String getTag(){
		return getToDoBaseUrl()+"tag";
	}
	
	/**
	 * 获取所有task
	 * @param id
	 * @return
	 */
	public static String getTasks(String id){
		return getToDoBaseUrl()+"list/"+id+"/tasks";
	}
	
	/**
	 * 变更任务所有人
	 * @return
	 */
	public static String changeMessionOwner(){
		return getToDoBaseUrl();
	}
	
	/**
	 * 变更任务标签
	 * @param id
	 * @return
	 */
	public static String changeMessionTag(String id){
		return getToDoBaseUrl()+id+"/tags";
	}

	/*************************************************发现接口***********************************/
	/**
	 * 通过车站名获取到达城市
	 * @return
	 */
	public static String getTripArriveCity(){
		return getEcmTanentUrl()+"/trip/simple/city";
	}
	
	/**
	 * 知识
	 * @return
	 */
	public static String knowledgeTips(){
		return getEcmTanentUrl()+"/tips";
	}
	
	/**
	 * 卡包
	 * @param uri
	 * @return
	 */
	public static String getHttpApiUri(String uri) {
		return getEcmTanentUrl() + "/" + uri;
	}
	
	/**
	 * 获取发现搜索接口
	 * @return
	 */
	public static String getFindSearch(){
		return "http://10.24.11.232:8080/solr/collection1/select?";
	}
	
	/**
	 * 获取发现混合搜索接口
	 * @return
	 */
	public static String getFindMixSearch(){
		return "http://10.24.11.232:8080/solr-web/mix/collection1/";
	}
	
	/***************************************Oauth相关接口**************************************/
	
	/**
	 * 获取oauth认证的基础
	 * @return
	 */
	public static String getOauthBaseUrl(){
		return oauthBaseUrl;
	}
	
	/**
	 * 返回我的信息
	 * @return
	 */
	public static String getOauthMyInfoUrl(){
		return oauthBaseUrl+"/profile";
	}
	
	/**
	 * 刷新token
	 * @return
	 */
	public static String getRefreshToken(){
		return oauthBaseUrl + "/token";
	}
	


	/******************************************底座应用接口*********************************/
	/**
	 * 获取appTab的顺序和可显示性
	 * @return
	 */
	public static String getAppTabs(){
		return getEcmTanentUrl()+"/settings/client/mobile/main/tabs";
	}

	/**
	 * 新版底部Tabbar接口
	 * @return
     */
	public static String getAppNewTabs(){
		return getEcmTanentUrl() + "/api/v0/preference/main-tab/latest";
	}

	/**
	 * 获取所有App以及查询app
	 * @return
	 */
	public static String getAllApps(){
		return emmBaseUrl+"imp_app/getAllApps";
	}
	
	/**
	 * 获取所有App以及查询app
	 * @return
	 */
	public static String getNewAllApps(){
		return emmBaseUrl+"imp_app/appCenterList";
	}

	/**
	 * 验证身份的Uri
	 * @return
     */
	public static String getAppAuthCodeUri(){
		return "https://id.inspur.com/oauth2.0/quick_authz_code";
	}
	
	
	/**
	 * 获取用户apps
	 * @return
	 */
	public static String getUserApps(){
		return emmBaseUrl+"imp_app/userApps";
	}
	
	/**
	 * 获取我的app
	 * @return
	 */
	public static String getMyApp(){
		return emmBaseUrl+"imp_app/getUserApps";
	}
	
	/**
	 * 获取引用详情
	 * @return
	 */
	public static String getAppInfo(){
		return emmBaseUrl + "imp_app/getAppInfo";
	}
	
	/**
	 * 添加app
	 * @returnsunqx
	 */
	public static String addApp(){
		return emmBaseUrl + "imp_app/installApp";
	}
	
	/**
	 * 移除app
	 * @return
	 */
	public static String removeApp(){
		return emmBaseUrl + "imp_app/uninstallApp";
	}
	
	/**
	 * 检查更新
	 * @return
	 */
	public static String checkUpgrade(){
        return emmBaseUrl + "v1/upgrade/checkVersion";
	}
	
	/**
	 * 获取获取所有通讯录
	 * @return
	 */
	public static String getAllContact(){
		return emmBaseUrl + "contacts/get_all";
	}
	
	
	

	
	/**
	 * 通过短信验证码重置密码
	 */
	public static String getUpdatePwdBySMSCode(){
		return ecmBaseUrl+"";
	}
	
	/******************************************短信调用接口**************************************/
	
	/**
	 * 短信登录
	 * @return
	 */
	public static String getSMSLogin(){
		return smsLoginBaseUrl;
	}

	/*****************************************ReactNative**************************************/
	/**
	 * 更新的Native地址
	 * @return
     */
	public static String getReactNativeUpdate(){
		return getEcmTanentUrl()+"/api/v0/view/DISCOVER/bundle/?";
	}

	/**
	 * 获取clientid的
	 * @return
     */
	public static String getClientId(){
//		return getEcmTanentUrl()+"/api/v0/view/client";
		return "https://ecm.inspur.com/"+ UriUtils.tanent +"/api/v0/client/registry";
	}

	/**
	 * 写回客户端日志
	 * @return
     */
	public static String getClientLog(){
		return getEcmTanentUrl()+"/api/v0/view/update/DISCOVER/?";
	}

	/**
	 * zip文件下载地址
	 * @return
     */
	public static String getZipUrl(){
		return getEcmTanentUrl()+"/res/stream/";
	}

	/**
	 * ReactNative应用安装地址查询接口
	 * @return
     */
	public static String getReactNativeInstallUrl(){
		return "https://emm.inspur.com/api/imp_app/queryByUri";
	}

	/**
	 * ReactNative应用更新写回
	 * @return
     */
	public static String getReactNativeWriteBackUrl(String appModule){
		return getEcmTanentUrl() + "/api/v0/app/"+appModule+"/update";
	}

	/**
	 * 获取我的信息
	 * @return
     */
	public static String getUserProfileUrl(){
		return emmBaseUrl +"userprofile/displayconfig";
	}

	/**
	 * app闪屏页面
	 * @return
     */
	public static String getSplashPageUrl(){
		return getEcmTanentUrl()+"/api/v0/preference/launch-screen/latest";
	}

	/**
	 *
	 * @return
     */
	public static String getUploadSplashPageWriteBackLogUrl(){
		return getEcmTanentUrl()+"/api/v0/preference/launch-screen/update";
	}


	/**
	 * 获取解绑设备url
	 * @return
	 */
	public static String getUnBindDeviceUrl(){return  emmBaseUrl+"device/unbind";}

	public static String getBindingDevicesUrl(){return  emmBaseUrl+"device/getUserDevicesWithLogs";}

	/**
	 * 获取MDM启用状态
	 * @return
	 */
	public static String getMDMStateUrl(){return  emmBaseUrl+"userprofile/mdm_state";}

	/**
	 * 上传设备管理所需token和设备ID
	 * @return
	 */
	public static String getUploadMDMInfoUrl(){return  emmBaseUrl+"mdm/mdm_check";}

	/**
	 * 扫码登录url
	 * @return
     */
	public static String getLoginDesktopCloudPlusUrl(){
		return "";
	}

	/**
	 * 获取分享二维码的url
	 * @return
     */
	public static String getShareCloudPlusUrl(){
		return "";
	}
}
