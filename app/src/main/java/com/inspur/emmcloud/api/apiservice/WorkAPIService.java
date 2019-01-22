/**
 * WorkAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.WorkAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:35:11
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.CloudHttpMethod;
import com.inspur.emmcloud.api.HttpUtils;
import com.inspur.emmcloud.bean.appcenter.GetIDResult;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.work.Attachment;
import com.inspur.emmcloud.bean.work.GetCalendarEventsResult;
import com.inspur.emmcloud.bean.work.GetCreateOfficeResult;
import com.inspur.emmcloud.bean.work.GetIsAdmin;
import com.inspur.emmcloud.bean.work.GetLoctionResult;
import com.inspur.emmcloud.bean.work.GetMeetingListResult;
import com.inspur.emmcloud.bean.work.GetMeetingRoomsResult;
import com.inspur.emmcloud.bean.work.GetMeetingsResult;
import com.inspur.emmcloud.bean.work.GetMyCalendarResult;
import com.inspur.emmcloud.bean.work.GetOfficeResult;
import com.inspur.emmcloud.bean.work.GetTagResult;
import com.inspur.emmcloud.bean.work.GetTaskAddResult;
import com.inspur.emmcloud.bean.work.GetTaskListResult;
import com.inspur.emmcloud.bean.work.TaskResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RequestTracker;
import org.xutils.http.request.UriRequest;

import java.util.Calendar;
import java.util.List;

/**
 * com.inspur.emmcloud.api.apiservice.WorkAPIService create at 2016年11月8日
 * 下午2:35:11
 */
public class WorkAPIService {
    private Context context;
    private APIInterface apiInterface;

    public WorkAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    /****************************************************** 会议室部分 **************************************************************/
    /**
     * 根据天数获取会议
     *
     * @param day
     */
    public void getMeetings(final int day) {
        final String completeUrl = APIUri.getMeetingsUrl() + "?day=" + day;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetings(day);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnMeetingsSuccess(new GetMeetingsResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingsFail(error, responseCode);
            }
        });

    }

    /**
     * 获取会议室
     */
    public void getMeetingRooms() {
        final String completeUrl = APIUri.getMeetingRoomsUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingRooms();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnMeetingRoomsSuccess(new GetMeetingRoomsResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingRoomsFail(error, responseCode);
            }
        });
    }

    /**
     * 获取过滤后的会议室列表
     *
     * @param start
     * @param end
     * @param commonOfficeIdList
     * @param isFilte
     */
    public void getFiltMeetingRooms(final long start, final long end,
                                    final List<String> commonOfficeIdList, final boolean isFilte) {
        String baseUrl = APIUri.getMeetingRoomsUrl() + "?";
        if (isFilte) {
            baseUrl = baseUrl + "startWebSocket=" + start + "&end=" + end;
        } else {
            baseUrl = baseUrl + "startWebSocket=" + "&end=";
        }
        baseUrl = baseUrl + "&isIdle=" + isFilte;
        for (int j = 0; j < commonOfficeIdList.size(); j++) {
            baseUrl = baseUrl + "&oids=" + commonOfficeIdList.get(j);
        }
        if (commonOfficeIdList.size() == 0) {
            baseUrl = baseUrl + "&oids=";
        }
        final String completeUrl = baseUrl;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getFiltMeetingRooms(start, end, commonOfficeIdList,
                                isFilte);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingRoomsSuccess(
                        new GetMeetingRoomsResult(new String(arg0)), isFilte);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingRoomsFail(error, responseCode);
            }
        });
    }

    /**
     * 获取历史会议记录
     *
     * @param keyword
     * @param page
     * @param limit
     * @param isLoadMore
     */
    public void getHistoryMeetingList(final String keyword, final int page,
                                      final int limit, final boolean isLoadMore) {
        String uid = PreferencesUtils.getString(context, "userID");
        final String completeUrl = APIUri.getMeetingRootUrl()
                + "/room/booking/history?uid=" + uid
                + "&page=" + page + "&limit=" + limit + "&keyword=" + keyword;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        //params.addParameter("keyword", keyword);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getHistoryMeetingList(keyword, page, limit, isLoadMore);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingsSuccess(
                        new GetMeetingsResult(new String(arg0)), page);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingsFail(error, responseCode);
            }
        });
    }

    /**
     * 获取是否管理员
     *
     * @param cid
     */
    public void getIsAdmin(final String cid) {

        final String completeUrl = APIUri.getMeetingIsAdminUrl() + "?cid=" + cid;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getIsAdmin(cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnIsAdminSuccess(new GetIsAdmin(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnIsAdminFail(error, responseCode);
            }
        });
    }


    /**
     * 更新会议接口
     *
     * @param topic
     * @param roomid
     * @param name
     * @param alert
     * @param notice
     * @param bookDate
     * @param from
     * @param to
     * @param organizer
     * @param cids
     * @param pids
     * @param attendant
     * @param id
     */
    public void updateMeeting(final String topic, final String roomid,
                              final String name, final long alert, final String notice,
                              final String bookDate, final long from, final long to,
                              final String organizer, final String[] cids,
                              final List<SearchModel> pids, final String attendant,
                              final String id) {
        final String completeUrl = APIUri.getBookingRoomUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        try {
            JSONObject bookingParam = new JSONObject();
            bookingParam.put("id", id);
            bookingParam.put("topic", topic);
            bookingParam.put("alert", alert);
            bookingParam.put("notice", notice);
            bookingParam.put("bookDate", bookDate);
            bookingParam.put("from", from);
            bookingParam.put("to", to);
            bookingParam.put("organizer", organizer);

            JSONObject roomObj = new JSONObject();
            roomObj.put("id", roomid);
            roomObj.put("name", name);
            bookingParam.put("room", roomObj);

            JSONObject participantObj = new JSONObject();
            JSONArray pidsArray = new JSONArray();
            JSONArray cidsArray = new JSONArray();
            for (int i = 0; i < pids.size(); i++) {
                pidsArray.put(pids.get(i).getId());
            }
            if (cids != null) {
                for (int i = 0; i < cids.length; i++) {
                    cidsArray.put(cids[i]);
                }
            }

            participantObj.put("user", pidsArray);
            participantObj.put("channel", cidsArray);
            bookingParam.put("participant", participantObj);
            bookingParam.put("attendant", attendant);
            params.setBodyContent(bookingParam.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpUtils.request(context,CloudHttpMethod.PUT, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateMeeting(topic, roomid, name, alert, notice,
                                bookDate, from, to, organizer, cids, pids,
                                attendant, id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnBookingRoomSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnBookingRoomFail(error, responseCode);
            }
        });


    }


    /**
     * 预定会议接口
     *
     * @param topic
     * @param roomid
     * @param name
     * @param alert
     * @param notice
     * @param from
     * @param to
     * @param organizer
     * @param cids
     * @param pids
     * @param attendant
     */
    public void getBookingRoom(final String topic, final String roomid,
                               final String name, final long alert, final String notice,
                               final long from, final long to, final String organizer,
                               final String[] cids, final List<SearchModel> pids,
                               final String attendant) {
        final String completeUrl = APIUri.getBookingRoomUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        try {
            JSONObject bookingParamObj = new JSONObject();
            bookingParamObj.put("topic", topic);
            bookingParamObj.put("alert", alert);
            bookingParamObj.put("notice", notice);
            bookingParamObj.put("from", from);
            bookingParamObj.put("to", to);
            bookingParamObj.put("organizer", organizer);

            JSONObject roomObj = new JSONObject();
            roomObj.put("id", roomid);
            roomObj.put("name", name);
            bookingParamObj.put("room", roomObj);

            JSONObject participantObj = new JSONObject();
            JSONArray pidsArray = new JSONArray();
            JSONArray cidsArray = new JSONArray();
            for (int i = 0; i < pids.size(); i++) {
                pidsArray.put(pids.get(i).getId());
            }
            if (cids != null) {
                for (int i = 0; i < cids.length; i++) {
                    cidsArray.put(cids[i]);
                }
            }

            participantObj.put("user", pidsArray);
            participantObj.put("channel", cidsArray);
            bookingParamObj.put("participant", participantObj);
            bookingParamObj.put("attendant", attendant);
            params.setBodyContent(bookingParamObj.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getBookingRoom(topic, roomid, name, alert, notice,
                                from, to, organizer, cids, pids, attendant);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnBookingRoomSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnBookingRoomFail(error, responseCode);
            }
        });
    }


    /**
     * 获取对应room的会议情况
     *
     * @param bid
     */
    public void getRoomMeetingList(final String bid) {
        final String completeUrl = APIUri.getRoomMeetingListUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("bid", bid);
        params.setRequestTracker(new RequestTracker() {

            @Override
            public void onWaiting(RequestParams arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(UriRequest arg0, Object arg1) {
                // TODO Auto-generated method stub
                String result = "";
                if(arg1 != null){
                    result = new String((byte[])arg1);
                }
                apiInterface
                        .returnMeetingListSuccess(new GetMeetingListResult(
                                result), arg0.getResponseHeader("Date"));
            }

            @Override
            public void onStart(RequestParams arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onRequestCreated(UriRequest arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFinished(UriRequest arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(UriRequest arg0, Throwable arg1, boolean arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onCancelled(UriRequest arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onCache(UriRequest arg0, Object arg1) {
                // TODO Auto-generated method stub

            }
        });
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getRoomMeetingList(bid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingListFail(error, responseCode);
            }
        });
    }

    /**
     * 获取园区
     */
    public void getLoction() {
        final String completeUrl = APIUri.getLoctionUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getLoction();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnLoctionResultSuccess(new GetLoctionResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnLoctionResultFail(error, responseCode);
            }
        });

    }

    /**
     * 获取常用办公地点
     */
    public void getOffice() {
        final String completeUrl = APIUri.getOfficeUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnOfficeResultSuccess(new GetOfficeResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnOfficeResultFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getOffice();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 创建常用办公地点
     *
     * @param name
     * @param buildingId
     */
    public void creatOffice(final String name, final String buildingId) {
        final String completeUrl = APIUri.addOfficeUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        JSONObject jsonBuild = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonBuild.put("id", buildingId);
            jsonObject.put("name", name);
            jsonObject.put("building", jsonBuild);
            params.setBodyContent(jsonObject.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        creatOffice(name, buildingId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnCreatOfficeSuccess(new GetCreateOfficeResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreatOfficeFail(error, responseCode);
            }
        });
    }

    /**
     * 删除会议
     *
     * @param rid
     */
    public void deleteMeeting(final String rid) {
        final String completeUrl;
        completeUrl = APIUri.getDeleteMeetingUrl() + "?rid=" + rid;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.DELETE, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteMeeting(rid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDelMeetingSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDelMeetingFail(error, responseCode);
            }
        });
    }

    /**
     * 删除常用办公地点
     *
     * @param buildingId
     */
    public void deleteOffice(final String buildingId, final int position) {
        final String completeUrl = APIUri.addOfficeUrl() + "?id=" + buildingId;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.DELETE, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteOffice(buildingId, position);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteOfficeSuccess(position);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteOfficeFail(error, responseCode);
            }
        });

    }


    /****************************************************** 待办任务部分 **************************************************************/

    /**
     * 获取我的任务
     *
     * @param orderBy
     * @param orderType
     */
    public void getRecentTasks(final String orderBy, final String orderType) {
        final String completeUrl = APIUri.getToDoRecentUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("order_by", orderBy);
        params.addParameter("order_type", orderType);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getRecentTasks(orderBy, orderType);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksFail(error, responseCode);
            }
        });

    }


    /**
     * 获取task
     *
     * @param id
     */
    public void getTask(final String id) {
        final String completeUrl = APIUri.getTasksList(id);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getTask(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnGetTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnGetTasksFail(error, responseCode);
            }
        });
    }

    /**
     * 获取所有任务
     *
     * @param page
     * @param limit
     * @param state
     */
    public void getAllTasks(final int page, final int limit, final String state) {
        final String completeUrl = APIUri.getCreateTaskUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addHeader("Accept", "application/json");
        params.addParameter("page", page);
        params.addParameter("limit", limit);
        params.addParameter("state", state);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAllTasks(page, limit, state);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksFail(error, responseCode);
            }
        });
    }

    /**
     * 获取我参与的任务
     */
    public void getInvolvedTasks(final String orderBy, final String orderType) {
        final String completeUrl = APIUri.getInvolvedTasksUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("order_by", orderBy);
        params.addParameter("order_type", orderType);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getInvolvedTasks(orderBy, orderType);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksFail(error, responseCode);
            }
        });
    }

    /**
     * 获取我关注的任务
     */
    public void getFocusedTasks(final String orderBy, final String orderType) {
        final String completeUrl = APIUri.getFocusedTasksUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("order_by", orderBy);
        params.addParameter("order_type", orderType);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getFocusedTasks(orderBy, orderType);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksFail(error, responseCode);
            }
        });
    }

    /**
     * 创建任务
     */
    public void createTasks(final String mession) {
        final String completeUrl = APIUri.getCreateTaskUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("title", mession);
        HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createTasks(mession);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnCreateTaskSuccess(new GetTaskAddResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateTaskFail(error, responseCode);
            }
        });

    }

    /**
     * 删除任务
     *
     * @param id
     */
    public void deleteTasks(final String id) {
        final String completeUrl = APIUri.getCreateTaskUrl() + "/" + id;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.DELETE, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteTasks(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteTaskSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteTaskFail(error, responseCode);
            }
        });
    }

    /**
     * 获取今天的任务
     */
    public void getTodayTasks() {
        final String completeUrl = APIUri.getTodayTaskUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getTodayTasks();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksFail(error, responseCode);
            }
        });

    }

    /**
     * 获取单个任务
     *
     * @param id
     */
    public void getSigleTask(final String id) {
        final String completeUrl = APIUri.getCreateTaskUrl() + "/" + id;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getSigleTask(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAttachmentSuccess(new TaskResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAttachmentFail(error, responseCode);
            }
        });
    }

    /**
     * 更新待办任务
     *
     * @param taskJson
     */
    public void updateTask(final String taskJson, final int position) {
        final String completeUrl = APIUri.getCreateTaskUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(taskJson);
        params.setAsJsonContent(true);
        HttpUtils.request(context,CloudHttpMethod.PUT,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateTask(taskJson, position);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateTaskSuccess(position);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateTaskFail(error, responseCode, position);
            }
        });
    }

    /**
     * 邀请多人参与协作
     *
     * @param taskId
     * @param uidArray
     */
    public void inviteMateForTask(final String taskId, final JSONArray uidArray) {
        final String completeUrl = APIUri.getCreateTaskUrl() + "/" + taskId
                + "/mates";
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(uidArray.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        inviteMateForTask(taskId, uidArray);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub

                apiInterface.returnInviteMateForTaskSuccess(new String(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnInviteMateForTaskFail(error, responseCode);
            }
        });
    }

    /**
     * 删除参与人
     *
     * @param taskId
     * @param uidArray
     */
    public void deleteMateForTask(final String taskId, final JSONArray uidArray) {
        final String completeUrl = APIUri.getCreateTaskUrl() + "/" + taskId
                + "/mates";
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(uidArray.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context,CloudHttpMethod.DELETE, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteMateForTask(taskId, uidArray);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDelTaskMemSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDelTaskMemFail(error, responseCode);
            }
        });
    }

    /**
     * 获取所有标签
     */
    public void getTags() {
        final String completeUrl = APIUri.getTagUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addHeader("Accept", "application/json");
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getTags();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnGetTagResultSuccess(new GetTagResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnGetTagResultFail(error, responseCode);
            }
        });

    }

    /**
     * 修改标签
     *
     * @param id
     * @param title
     * @param color
     * @param owner
     */
    public void changeTag(final String id, final String title,
                          final String color, final String owner) {
        final String completeUrl = APIUri.getTagUrl();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("title", title);
            jsonObject.put("color", color);
            jsonObject.put("owner", owner);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(jsonObject.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context,CloudHttpMethod.PUT, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        changeTag(id, title, color, owner);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateTagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateTagFail(error, responseCode);
            }
        });

    }

    /**
     * 删除标签
     *
     * @param id
     */
    public void deleteTag(final String id) {
        final String completeUrl = APIUri.getTagUrl() + "/" + id;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);

        HttpUtils.request(context,CloudHttpMethod.DELETE, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteTag(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteTagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteTagFail(error, responseCode);
            }
        });

    }

    /**
     * 创建标签
     *
     * @param title
     * @param color
     */
    public void createTag(final String title, final String color) {
        final String completeUrl = APIUri.getTagUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("title", title);
        params.addParameter("color", color);
        HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createTag(title, color);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateTagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateTagFail(error, responseCode);
            }
        });
    }


    /**
     * 修改任务所有人
     *
     * @param id
     * @param newOwner
     */
    public void changeMessionOwner(final String id, final String newOwner, final String managerName) {

        final String completeUrl = APIUri.getChangeMessionOwnerUrl() + id
                + "?";
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("owner", newOwner);
        HttpUtils.request(context,CloudHttpMethod.PUT, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        changeMessionOwner(id, newOwner, managerName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnChangeMessionOwnerSuccess(managerName);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnChangeMessionOwnerFail(error, responseCode);
            }
        });

    }


    /**
     * 创建附件
     *
     * @param id
     * @param attachments
     */
    public void addAttachments(final String id, final String attachments) {
        final String completeUrl = APIUri.getAddAttachmentsUrl(id);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        try {
            JSONObject jsonObject = new JSONObject(attachments);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject);
            params.setBodyContent(jsonArray.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addAttachments(id, attachments);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAddAttachMentSuccess(new Attachment(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAddAttachMentFail(error, responseCode);
            }
        });
    }

    /**
     * 删除附件
     *
     * @param id
     * @param attachments
     */
    public void deleteAttachments(final String id, final String attachments, final int position) {
        final String completeUrl = APIUri.getAddAttachmentsUrl(id);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        try {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(attachments);
            params.setBodyContent(jsonArray.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpUtils.request(context,CloudHttpMethod.DELETE, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteAttachments(id, attachments, position);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDelAttachmentSuccess(position);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDelAttachmentFail(error, responseCode, position);
            }
        });
    }


    /****************************************日历部分********************************************************************************/

    /**
     * 获取我的所有日历
     *
     * @param page
     * @param limit
     */
    public void getMyCalendar(final int page, final int limit) {
        final String completeUrl = APIUri.getCalendarUrl() + "/calendar";
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("page", page);
        params.addParameter("limit", limit);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMyCalendar(page, limit);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMyCalendarSuccess(new GetMyCalendarResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMyCalendarFail(error, responseCode);
            }
        });


    }


    /**
     * 删除日历
     *
     * @param id
     */
    public void delelteCalendarById(final String id) {
        final String completeUrl = APIUri.getCalendarUrl() + "/calendar/"
                + id;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.DELETE,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        delelteCalendarById(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDelelteCalendarByIdSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDelelteCalendarByIdFail(error, responseCode);
            }
        });

    }

    /**
     * 更新日历数据
     *
     * @param calendarJson
     */
    public void updateCalendar(final String calendarJson) {
        final String completeUrl = APIUri.getCalendarUrl() + "/calendar";
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setAsJsonContent(true);
        params.setBodyContent(calendarJson);
        HttpUtils.request(context,CloudHttpMethod.PUT, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateCalendar(calendarJson);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateCalendarSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateCalendarFail(error, responseCode);
            }
        });
    }


    /**
     * 指定日历内添加事件
     *
     * @param calendarId
     * @param eventJson
     */
    public void addCalEvent(final String calendarId, final String eventJson) {

        final String completeUrl = APIUri.getCalendarUrl() + "/calendar/"
                + calendarId + "/event";
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(eventJson);
        params.setAsJsonContent(true);
        HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addCalEvent(calendarId, eventJson);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAddCalEventSuccess(new GetIDResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAddCalEventFail(error, responseCode);
            }
        });
    }

    /**
     * 更新日历内的事件
     *
     * @param calEventJson
     */
    public void updateCalEvent(final String calEventJson) {

        final String completeUrl = APIUri.getCalendarUrl()
                + "/calendar/event";
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(calEventJson);
        params.setAsJsonContent(true);

        HttpUtils.request(context,CloudHttpMethod.PUT, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateCalEvent(calEventJson);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateCalEventSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateCalEventFail(error, responseCode);
            }
        });
    }


    /**
     * 获得某些日历中的事件
     *
     * @param calendarIdList
     * @param afterCalendar
     * @param beforCalendar
     * @param limit
     * @param page
     * @param isRefresh
     */
    public void getAllCalEvents(final List<String> calendarIdList,
                                final Calendar afterCalendar, final Calendar beforCalendar,
                                final int limit, final int page, final boolean isRefresh) {
        String url = APIUri.getCalendarUrl() + "/calendar/events?";
        final String completeUrl = url;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        for (int i = 0; i < calendarIdList.size(); i++) {
            params.addQueryStringParameter("id", calendarIdList.get(i));
        }
        if (beforCalendar != null) {
            params.addParameter("before", beforCalendar.getTimeInMillis());
        }
        if (afterCalendar != null) {
            params.addParameter("after", afterCalendar.getTimeInMillis());
        }
        if (limit != -1) {
            params.addParameter("limit", limit);
        }
        if (page != -1) {
            params.addParameter("page", page);
        }

        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAllCalEvents(calendarIdList, afterCalendar,
                                beforCalendar, limit, page, isRefresh);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnCalEventsSuccess(
                        new GetCalendarEventsResult(new String(arg0)),
                        isRefresh);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCalEventsFail(error, isRefresh, responseCode);
            }
        });

    }

    /**
     * 删除某个event
     *
     * @param calEventId
     */
    public void deleteCalEvent(final String calEventId) {
        final String completeUrl = APIUri.getCalendarUrl()
                + "/calendar/event/" + calEventId;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.DELETE,params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteCalEvent(calEventId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteCalEventSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteCalEventFail(error, responseCode);
            }
        });

    }

    /**
     * 上传工作页面布局配置信息
     *
     * @param configJSon
     */
    public void saveWorkPortletConfig(final String configJSon) {
        final String url = APIUri.saveAppConfigUrl("WorkPortlet");
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.setBodyContent(configJSon);
        HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnSaveConfigSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnSaveConfigFail();
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        saveWorkPortletConfig(configJSon);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }
}
