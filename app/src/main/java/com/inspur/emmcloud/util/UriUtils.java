package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.PVCollectModel;
import com.inspur.emmcloud.ui.app.ReactNativeAppActivity;
import com.inspur.emmcloud.ui.app.groupnews.GroupNewsActivity;
import com.inspur.imp.api.ImpActivity;


public class UriUtils {
    // public static String res;
    public static String tanent;

    public static void openApp(Activity activity, App app) {
        String uri = app.getUri();
        int appType = app.getAppType();
        String appName = app.getAppName();
        Intent intent = new Intent();
        switch (appType) {
            case 0:
            case 1:
                if (uri.equals("emm://news")) {
                    intent.setClass(activity, GroupNewsActivity.class);
                    activity.startActivity(intent);
                } else {
                    ToastUtils.show(activity,
                            activity.getString(R.string.not_support_app_type));
                }

                break;
            case 3:
            case 4:
                intent.setClass(activity, ImpActivity.class);
                intent.putExtra("uri", uri);
                String token = ((MyApplication) activity.getApplicationContext())
                        .getToken();
                intent.putExtra("Authorization", token);
                intent.putExtra("userAgentExtra",
                        "/emmcloud/" + AppUtils.getVersion(activity));
                String webLanguageCookie = getLanguageCookie(activity);
                intent.putExtra("cookie", webLanguageCookie);
                if (appType == 3) {
                    intent.putExtra("appName", appName);
                }
                activity.startActivity(intent);
                break;
            case 5:
//                Intent intentRN = new Intent();
                intent.setClass(activity, ReactNativeAppActivity.class);
                //将这里换成对应的应用模块
                intent.putExtra("react_module","WhoseCar");
                activity.startActivity(intent);
                break;

            default:
                ToastUtils.show(activity,
                        activity.getString(R.string.not_support_app_type));
                break;
        }

        //web应用PV收集
        PVCollectModel collectModel = new PVCollectModel(activity, app.getAppID(), "webApp", app.getAppName());
        PVCollectModelCacheUtils.saveCollectModel(activity, collectModel);
    }

    /**
     * 打开url
     *
     * @param context
     * @param uri
     * @param header
     */
    public static void openUrl(Activity context, String uri, String header) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        String token = ((MyApplication) context.getApplicationContext())
                .getToken();
        bundle.putString("Authorization", token);
        bundle.putString("userAgentExtra",
                "/emmcloud/" + AppUtils.getVersion(context));
        String webLanguageCookie = getLanguageCookie(context);
        bundle.putString("cookie", webLanguageCookie);
        bundle.putString("appName", header);
        IntentUtils.startActivity(context, ImpActivity.class, bundle);
    }

    /**
     * 获取带语言的cookie
     *
     * @return
     */
    public static String getLanguageCookie(Context context) {
        // TODO Auto-generated method stub
        String languageJson = PreferencesUtils.getString(context, tanent
                + "appLanguageObj");
        String cookie = "";
        if (languageJson != null) {
            cookie = "lang=" + languageJson + ";";
        }
        return cookie;
    }

    /**
     * api http请求
     **/
    public static String getHttpApiUri(String uri) {
        return "https://ecm.inspur.com/" + tanent + "/" + uri;
    }

    /**
     * 预览图片或视频
     **/
    public static String getPreviewUri(String fileName) {
        return getResUri("stream/" + fileName);
    }

    /**
     * 预览图片或视频
     **/
    public static String getChannelIcon(String fileName) {
        return getResUri("/stream/" + fileName);
    }

    public static String getResUri(String url) {
        return "https://ecm.inspur.com/" + tanent + "/res/" + url;
    }

    /**
     * 获取推送新闻
     **/
    public static String getGroupNewsUrl(String url) {
        return "https://ecm.inspur.com/" + url;
    }

    ;

    /**
     * 个人信息头像显示图片
     **/
    public static String getUserInfoPhotoUri(String url) {
        // String tanent = "10.24.14.102";
        return "https://emm.inspur.com" + url;
    }

    /**
     * 频道页面头像显示图片
     **/
    public static String getChannelImgUri(String inspurID) {
        return "https://emm.inspur.com/img/userhead/" + inspurID;
    }

    /**
     * 获取机器人头像路径
     *
     * @return
     */
    public static String getRobotIconUri(String iconUrl) {
        return "https://ecm.inspur.com/" + UriUtils.tanent + "/avatar/stream/"
                + iconUrl;
    }

    /**
     * 得到集团新闻的Path
     **/
    public static String getGroupNewsArticle() {
        return "/" + tanent + "/res" + "/article" + "/";
    }

    /**
     * 工作页面会议
     **/
    public static String getMeetings() {
        // return
        // "https://ecm.inspur.com/"+tanent+"/meeting/room/bookings/ugly";
        return "https://ecm.inspur.com/" + tanent + "/meeting/room/bookings";
    }

    /**
     * 会议详情
     **/
    public static String getMeetingDetails(String meetingId) {
        return "https://ecm.inspur.com/" + tanent + "/meeting/room/bookings/:"
                + meetingId;
    }

    /**
     * 会议室列表
     **/
    public static String getMeetingRooms() {
        return "https://ecm.inspur.com/" + tanent + "/meeting/room";
    }

    /**
     * 会议室接口
     **/
    public static String getBookingRoom() {
        // return "https://ecm.inspur.com/"+tanent+"/meeting/booking/ugly";
        return "https://ecm.inspur.com/" + tanent + "/meeting/booking";
    }

    /**
     * 会议室空闲时段接口
     **/
    public static String getAvailableTime(String param) {
        return "https://ecm.inspur.com/" + tanent + "/meeting/room/idle"
                + param;
        // return
        // "https://ecm.inspur.com/"+tanent+"/meeting/room/idle/ugly"+param;
    }

    public static String getMeetingReply() {
        return "https://ecm.inspur.com/" + tanent
                + "/meeting/booking/receipt/state";
    }

    public static String getRoomMeetingList() {
        return "https://ecm.inspur.com/" + tanent + "/meeting/booking/room";
    }

    /**
     * 异常上传接口
     **/
    public static String uploadException() {
        // return "http://172.31.1.67/api/collector/error";
        return "http://u.inspur.com:8080/voice/collector/error";
    }

    /**
     * 获取园区
     *
     * @return
     */
    public static String getLoction() {
        return "https://ecm.inspur.com/" + tanent + "/meeting/location";
    }

    /**
     * 获取园区
     *
     * @return
     */
    public static String getBuilding() {
        return "https://ecm.inspur.com/" + tanent + "/meeting/building";
    }

    /**
     * 获取楼层
     *
     * @return
     */
    public static String getFloor() {
        return "https://ecm.inspur.com/" + tanent + "/meeting/floor";
    }

    /**
     * 获取房间
     *
     * @return
     */
    public static String getRooms() {
        return "https://ecm.inspur.com/" + tanent + "/meeting/room";
    }

    /**
     * 获取办公地点
     *
     * @return
     */
    public static String getOffice() {
        return "https://ecm.inspur.com/" + tanent + "/meeting/location/office";
    }

    /**
     * 增加办公地点
     *
     * @return
     */
    public static String addOffice() {
        return "https://ecm.inspur.com/" + tanent + "/meeting/location/office";
    }

    /**
     * 会议的root路径
     *
     * @return
     */
    public static String meetingRootUrl() {
        return "https://ecm.inspur.com/" + tanent + "/meeting";
    }

    /**
     * 获取时间过滤的rooms
     *
     * @return
     */
    public static String getAvailable() {
        return "https://ecm.inspur.com/" + tanent + "/meeting/room/available";
    }

    /**
     * 添加群组成员
     *
     * @return
     */
    public static String addGroupMembers(String cid) {
        return "https://ecm.inspur.com/" + tanent + "/channel/group/" + cid
                + "/users?";
    }

    /**
     * 添加群组成员
     *
     * @return
     */
    public static String addAttachments(String cid) {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo/" + cid
                + "/attachments";
    }

    /**
     * 获取我的任务
     *
     * @return
     */
    public static String getToDoRecent() {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo/recent";
    }

    /**
     * 获取我参与的任务
     *
     * @return
     */
    public static String getInvolved() {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo/involved";
    }

    /**
     * 获取我参与的任务
     *
     * @return
     */
    public static String getFocused() {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo/focused";
    }

    /**
     * 创建任务
     *
     * @return
     */
    public static String createTask() {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo";
    }

    /**
     * 创建任务
     *
     * @return
     */
    public static String getTodayTask() {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo/today";
    }

    /**
     * 查询所有待办事项
     */
    public static String getAllListTasks() {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo/list";
    }

    /**
     * 获取所有Tag
     */
    public static String getTag() {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo/tag";
    }

    /**
     * 获取所有Tag
     */
    public static String getCalendarUri() {
        return "https://ecm.inspur.com/" + tanent + "/api/v0";
    }

    /**
     * 获取所有task
     */
    public static String getTasks(String id) {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo/list/" + id
                + "/tasks";
    }

    /**
     * nointerruption
     */
    public static String getNointerruption() {
        return "https://ecm.inspur.com/" + tanent + "/session/dnd";
    }

    /**
     * 修改密码
     */
    public static String getChangePsd() {
        return "https://id.inspur.com/console/api/v1/account/password";
    }

    /**
     * 通过车站名获取到达城市
     */
    public static String getTripArriveCity() {
        return "https://ecm.inspur.com/" + tanent + "/trip/simple/city";
    }

    /**
     * 删除会议
     */
    public static String deleteMeeting() {
        return "https://ecm.inspur.com/" + tanent
                + "/meeting/room/booking/cancel";
    }

    /**
     * 变更任务所有人
     */
    public static String changeMessionOwner() {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo/";
    }

    /**
     * 变更任务标签
     */
    public static String changeMessionTag(String id) {
        return "https://ecm.inspur.com/" + tanent + "/api/v0/todo/" + id
                + "/tags";
    }

    /**
     * 变更任务标签
     */
    public static String knowledgeTips() {
        return "https://ecm.inspur.com/" + tanent + "/tips";
    }

    /**
     * 获取是否管理员接口
     */
    public static String getIsAdmin() {
        return "https://ecm.inspur.com/" + tanent + "/meeting/is_admin";
    }

    /**
     * 获取发现搜索接口
     */
    public static String getFindSearch() {
        return "http://10.24.11.232:8080/solr/collection1/select?";
    }

    /**
     * 获取发现混合搜索接口
     */
    public static String getFindMixSearch() {
        return "http://10.24.11.232:8080/solr-web/mix/collection1/";
    }

}
