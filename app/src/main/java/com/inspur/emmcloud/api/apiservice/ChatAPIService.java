/**
 * ChatAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.ChatAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:32:07
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.bean.ChatFileUploadInfo;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.GetChannelListResult;
import com.inspur.emmcloud.bean.chat.GetConversationListResult;
import com.inspur.emmcloud.bean.chat.GetFileUploadResult;
import com.inspur.emmcloud.bean.chat.GetMsgCommentCountResult;
import com.inspur.emmcloud.bean.chat.GetMsgCommentResult;
import com.inspur.emmcloud.bean.chat.GetMsgResult;
import com.inspur.emmcloud.bean.chat.GetNewMsgsResult;
import com.inspur.emmcloud.bean.chat.GetNewsImgResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.ScanCodeJoinConversationBean;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.GetCreateSingleChannelResult;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.componentservice.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * com.inspur.emmcloud.api.apiservice.ChatAPIService create at 2016年11月8日
 * 下午2:32:07
 */
public class ChatAPIService {
    private Context context;
    private APIInterface apiInterface;

    public ChatAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    private void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.refreshToken(oauthCallBack, requestTime);
        }
    }

    /**
     * 获取会话列表
     */
    public void getChannelList() {
        final String completeUrl = APIUri.getChannelListUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                refreshToken(
                        new OauthCallBack() {
                            @Override
                            public void reExecute() {
                                getChannelList();
                            }

                            @Override
                            public void executeFailCallback() {
                                callbackFail("", -1);
                            }
                        }, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnChannelListSuccess(new GetChannelListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnChannelListFail(error, responseCode);
            }
        });
    }

    /**
     * 获取新消息
     *
     * @param cid
     * @param msgId
     * @param count
     */
    public void getNewMsgs(final String cid, final String msgId, final int count) {
        final String completeUrl = APIUri.getECMChatChannelUrl() + ("/session/message");
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("limit", count);
        if (!StringUtils.isBlank(msgId)) {
            params.addParameter("mid", msgId);
        }
        if (!StringUtils.isBlank(cid)) {
            params.addParameter("cid", cid);
        }

        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getNewMsgs(cid, msgId, count);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnNewMsgsSuccess(new GetNewMsgsResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnNewMsgsFail(error, responseCode);
            }
        });
    }

    /**
     * 获取10条最新消息
     */
    public void getNewMsgs() {
        getNewMsgs("", "", 15);
    }

    /**
     * 获取评论
     *
     * @param mid
     */
    public void getComment(final String mid) {

        final String completeUrl = APIUri.getECMChatChannelUrl() + ("/message/" + mid
                + "/comment");
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getComment(mid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMsgCommentSuccess(new GetMsgCommentResult(new String(arg0)), mid);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMsgCommentFail(error, responseCode);
            }
        });
    }

    /**
     * 获取频道信息
     *
     * @param cid
     */
    public void getChannelInfo(final String cid) {
        final String completeUrl = APIUri.getChannelInfoUrl(cid);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getChannelInfo(cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnChannelInfoSuccess(new ChannelGroup(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnChannelInfoFail(error, responseCode);
            }
        });
    }

    /**
     * 发送消息
     *
     * @param channelId
     * @param msgContent
     * @param type
     * @param fakeMessageId
     */
    public void sendMsg(String channelId, String msgContent, String type,
                        String fakeMessageId) {
        sendMsg(channelId, msgContent, type, "", fakeMessageId);
    }

    /**
     * 发送消息
     *
     * @param channelId
     * @param msgContent
     * @param type
     * @param mid
     * @param fakeMessageId
     */
    public void sendMsg(final String channelId, final String msgContent,
                        final String type, final String mid, final String fakeMessageId) {
        final String completeUrl = APIUri.getECMChatChannelUrl() + ("/message");
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        try {
            JSONObject paramObj = new JSONObject();
            paramObj.put("cid", channelId);
            paramObj.put("type", type);
            if (type.equals("txt_comment")) {
                JSONObject commentObj = new JSONObject(msgContent);
                commentObj.put("mid", mid);
                paramObj.put("msg", commentObj);
            } else {
                paramObj.put("msg", new JSONObject(msgContent));
            }
            JSONObject actionObj = new JSONObject();
            actionObj.put("type", "open-url");
            actionObj.put("url", "ecc-channel://" + channelId);
            JSONObject extrasObj = new JSONObject();
            extrasObj.put("action", actionObj);
            paramObj.put("extras", extrasObj);
            params.setBodyContent(paramObj.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        sendMsg(channelId, msgContent, type, mid, fakeMessageId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnSendMsgSuccess(new GetSendMsgResult(new String(arg0)),
                        fakeMessageId);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnSendMsgFail(error, fakeMessageId, responseCode);
            }
        });
    }

    /**
     * 获取消息
     *
     * @param mid
     */
    public void getMsg(final String mid) {
        final String completeUrl = APIUri.getECMChatChannelUrl() + ("/message/" + mid);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMsg(mid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMsgSuccess(new GetMsgResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMsgFail(error, responseCode);
            }
        });
    }

    /**
     * 上传文件到资源服务器 (此处需要将调用端代码改为非线程)
     *
     * @param filePath
     * @param fakeMessageId
     * @param isImg         区分是否是图片类型
     */
    public void uploadMsgResource(final String filePath,
                                  final String fakeMessageId, final boolean isImg) {
        final String completeUrl = APIUri.getResUrl("upload");
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        File file = new File(filePath);
        params.setMultipart(true);// 有上传文件时使用multipart表单, 否则上传原始文件流.
        params.addBodyParameter("file1", file);
        final Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        uploadMsgResource(filePath, fakeMessageId, isImg);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                if (isImg) {
                    try {
                        JSONObject jsonObject = new JSONObject(new String(arg0));
                        jsonObject.put("height", bitmap.getHeight());
                        jsonObject.put("width", bitmap.getWidth());
                        jsonObject.put("tmpId", AppUtils.getMyUUID(context));
                        apiInterface.returnUploadResImgSuccess(
                                new GetNewsImgResult(jsonObject.toString()), fakeMessageId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    bitmap.recycle();
                } else {
                    apiInterface.returnUpLoadResFileSuccess(
                            new GetFileUploadResult(new String(arg0)), fakeMessageId);
                }

            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                if (isImg) {
                    apiInterface.returnUploadResImgFail(error, responseCode, fakeMessageId);
                } else {
                    apiInterface.returnUpLoadResFileFail(error, responseCode, fakeMessageId);
                }
            }
        });
    }

    /**
     * 获取搜索的频道列表
     */
    public void getAllGroupChannelList() {
        final String completeUrl = APIUri.getAllGroupChannelListUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAllGroupChannelList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnSearchChannelGroupSuccess(new GetSearchChannelGroupResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnSearchChannelGroupFail(error, responseCode);
            }
        });

    }

    /**
     * 获取频道列表信息
     *
     * @param cidArray
     */
    public void getChannelGroupList(final String[] cidArray) {
        final String completeUrl = APIUri.getChannelGroupInfoList();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        // 解决服务端的一个bug
        if (cidArray.length == 1) {
            String[] cidArray2 = {cidArray[0], cidArray[0]};
            params.addParameter("cids", cidArray2);
        } else {
            params.addParameter("cids", cidArray);
        }

        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getChannelGroupList(cidArray);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnSearchChannelGroupSuccess(new GetSearchChannelGroupResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnSearchChannelGroupFail(error, responseCode);
            }
        });
    }

    /**
     * 创建点聊
     *
     * @param uid
     */
    public void createDirectChannel(final String uid) {
        final String completeUrl = APIUri.getCreateChannelUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("mate", uid);
        params.addParameter("type", "DIRECT");
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createDirectChannel(uid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnCreateSingleChannelSuccess(new GetCreateSingleChannelResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateSingleChannelFail(error, responseCode);

            }
        });
    }

    /**
     * 修改群组名称
     *
     * @param cid
     * @param name
     */
    public void updateChannelGroupName(final String cid, final String name) {
        final String completeUrl = APIUri.getUpdateChannelGroupNameUrl(cid);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        try {
            JSONObject paramObj = new JSONObject();
            paramObj.put("name", name);
            params.setBodyContent(paramObj.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateChannelGroupName(cid, name);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnUpdateChannelGroupNameSuccess(new GetBoolenResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateChannelGroupNameFail(error, responseCode);
            }
        });
    }

    /**
     * 添加群组成员
     *
     * @param uids
     * @param cid
     */
    public void addGroupMembers(final ArrayList<String> uids, final String cid) {
        String url = APIUri.getAddGroupMembersUrl(cid);
        for (int i = 0; i < uids.size(); i++) {
            url = url + "uids=" + uids.get(i) + "&";
        }

        url = url.substring(0, url.length() - 1);
        final String completeUrl = url;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addGroupMembers(uids, cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnAddMembersSuccess(new ChannelGroup(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAddMembersFail(error, responseCode);
            }
        });
    }

    /**
     * 删除群组成员
     *
     * @param uids
     * @param cid
     */
    public void deleteGroupMembers(final ArrayList<String> uids,
                                   final String cid) {
        String url = APIUri.getAddGroupMembersUrl(cid);
        for (int i = 0; i < uids.size(); i++) {
            url = url + "uids=" + uids.get(i) + "&";
        }
        url = url.substring(0, url.length() - 1);
        final String completeUrl = url;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteGroupMembers(uids, cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDelMembersSuccess(new ChannelGroup(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDelMembersFail(error, responseCode);
            }
        });
    }

    /**
     * 消息免打扰接口
     *
     * @param cid
     * @param nointerruption 是否免打扰
     */
    public void updateDnd(final String cid, final Boolean nointerruption) {

        final String completeUrl = APIUri.getNointerRuptionUrl() + "?cid=" + cid
                + "&dnd=" + nointerruption;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateDnd(cid, nointerruption);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDndSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDndFail(error, responseCode);
            }
        });
    }

    /**
     * 添加群成员
     *
     * @param id
     * @param uidList
     */
    public void addConversationGroupMember(final String id, final List<String> uidList) {
        final String url = APIUri.getModifyGroupMemberUrl(id);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        params.addParameter("members", uidList);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addConversationGroupMember(id, uidList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAddConversationGroupMemberSuccess(uidList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAddConversationGroupMemberFail(error, responseCode);
            }
        });
    }

    /**
     * 删除群成员
     *
     * @param id
     * @param uidList
     */
    public void delConversationGroupMember(final String id, final List<String> uidList) {
        final String url = APIUri.getModifyGroupMemberUrl(id);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        params.addParameter("members", JSONUtils.toJSONArray(uidList));
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, url) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        delConversationGroupMember(id, uidList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDelConversationGroupMemberSuccess(uidList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDelConversationGroupMemberFail(error, responseCode);
            }
        });
    }

    /**
     * 创建群组
     *
     * @param name
     * @param members
     */
    public void createGroupChannel(final String name, final JSONArray members) {
        final String completeUrl = APIUri.getCreateChannelUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        try {
            JSONObject paramObj = new JSONObject();
            paramObj.put("name", name);
            paramObj.put("type", "GROUP");
            paramObj.put("members", members);
            params.setBodyContent(paramObj.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createGroupChannel(name, members);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateChannelGroupSuccess(new ChannelGroup(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateChannelGroupFail(error, responseCode);
            }
        });

    }

    public void getMsgCommentCount(final String mid) {
        final String completeUrl = APIUri.getECMChatChannelUrl() + ("/message/" + mid
                + "/comment/count");
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMsgCommentCount(mid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMsgCommentCountSuccess(new GetMsgCommentCountResult(new String(arg0)), mid);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMsgCommentCountFail(error, responseCode);
            }
        });
    }



    /**
     * 活动卡片点击按钮
     *
     * @param url
     */
    public void openActionBackgroudUrl(final String url) {
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnOpenActionBackgroudUrlSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnOpenActionBackgroudUrlFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        openActionBackgroudUrl(url);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 活动卡片点击按钮
     *
     * @param triggerId
     */
    public void openDecideBotRequest(final String triggerId) {
        final String completeUrl = APIUri.getDecideCardBotRequestUrl() + triggerId;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                LogUtils.YfcDebug("点击机器人卡片返回成功：" + new String(arg0));
                apiInterface.returnOpenDecideBotRequestSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                LogUtils.YfcDebug("点击机器人卡片返回失败：" + error + "code:" + responseCode);
                apiInterface.returnOpenDecideBotRequestFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        openDecideBotRequest(completeUrl);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }


    /**
     * 获取聊天文件上传Token
     *
     * @param fileName
     * @param chatFileUploadInfo
     */
    public void getFileUploadToken(final String fileName, final ChatFileUploadInfo chatFileUploadInfo) {
        String cid = chatFileUploadInfo.getMessage().getChannel();
        boolean isMediaVoice = chatFileUploadInfo.getMessage().getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE);
        final String url = isMediaVoice ? APIUri.getUploadMediaVoiceFileTokenUrl(cid) : APIUri.getUploadFileTokenUrl(cid);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("name", fileName);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnChatFileUploadTokenSuccess(new GetVolumeFileUploadTokenResult(new String(arg0)), chatFileUploadInfo);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnChatFileUploadTokenFail(error, responseCode, chatFileUploadInfo);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getFileUploadToken(fileName, chatFileUploadInfo);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 获取建立频道的参数
     *
     * @param jsonArray
     */
    public void getAgoraParams(final JSONArray jsonArray) {
        String compelteUrl = APIUri.getAgoraUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(compelteUrl);
        params.addParameter("Users", jsonArray);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, compelteUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGetVoiceCommunicationResultSuccess(new GetVoiceCommunicationResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetVoiceCommunicationResultFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAgoraParams(jsonArray);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 告诉S已加入channel
     *
     * @param channelId
     */
    public void remindServerJoinChannelSuccess(final String channelId) {
        String compelteUrl = APIUri.getAgoraJoinChannelSuccessUrl() + channelId;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(compelteUrl);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, compelteUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnJoinVoiceCommunicationChannelSuccess(new GetBoolenResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnJoinVoiceCommunicationChannelFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        remindServerJoinChannelSuccess(channelId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 获取channel信息
     *
     * @param channelId
     */
    public void getAgoraChannelInfo(final String channelId) {
        String compelteUrl = APIUri.getAgoraChannelInfoUrl() + channelId;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(compelteUrl);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, compelteUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGetVoiceCommunicationChannelInfoSuccess(new GetVoiceCommunicationResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetVoiceCommunicationChannelInfoFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAgoraChannelInfo(channelId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 拒绝频道
     *
     * @param channelId
     */
    public void refuseAgoraChannel(final String channelId) {
        String compelteUrl = APIUri.getAgoraRefuseChannelUrl() + channelId;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(compelteUrl);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, compelteUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
//                apiInterface.returnRefuseVoiceCommunicationChannelSuccess(new GetBoolenResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
//                apiInterface.returnRefuseVoiceCommunicationChannelFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        if (BaseApplication.getInstance().isHaveLogin()) {
                            refuseAgoraChannel(channelId);
                        }
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 离开频道
     *
     * @param channelId
     */
    public void leaveAgoraChannel(final String channelId) {
        String compelteUrl = APIUri.getAgoraLeaveChannelUrl() + channelId;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(compelteUrl);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, compelteUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
//                apiInterface.returnLeaveVoiceCommunicationChannelSuccess(new GetBoolenResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
//                apiInterface.returnLeaveVoiceCommunicationChannelFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        if (BaseApplication.getInstance().isHaveLogin()) {
                            leaveAgoraChannel(channelId);
                        }
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 退出群聊
     *
     * @param cid
     */
    public void quitChannelGroup(final String cid) {
        String compelteUrl = APIUri.getQuitChannelGroupUrl(cid);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(compelteUrl);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, compelteUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnQuitChannelGroupSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnQuitChannelGroupSuccessFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        quitChannelGroup(cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 删除群聊
     *
     * @param cid
     */
    public void deleteConversation(final String cid) {
        String compelteUrl = APIUri.getDeleteChannelUrl(cid);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(compelteUrl);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, compelteUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnDeleteConversationSuccess(cid);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnDeleteConversationFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteConversation(cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 获取频道列表
     * @param conversationType  频道类型 private (企业内频道)或者public（全局频道） 默认为Private
     */
    public void getConversationList(final JSONArray conversationType) {
        final String completeUrl = APIUri.getConversationListUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("include", conversationType);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getConversationList(conversationType);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnConversationListSuccess(new GetConversationListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnConversationListFail(error, responseCode);
            }
        });
    }

    /**
     * 设置会话是否置顶
     *
     * @param id
     * @param isStick
     */
    public void setConversationStick(final String id, final boolean isStick) {
        final String completeUrl = APIUri.getConversationSetStick(id);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("stick", isStick);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        setConversationStick(id, isStick);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnSetConversationStickSuccess(id, isStick);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnSetConversationStickFail(error, responseCode);
            }
        });
    }

    /**
     * 隐藏会话
     * @param id
     * @param isHide
     */
    public void setConversationHide(final String id, final boolean isHide) {
        final String completeUrl = APIUri.getConversationSetHide(id);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("hide", isHide);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        setConversationHide(id, isHide);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnSetConversationHideSuccess(id, isHide);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnSetConversationHideFail(error, responseCode);
            }
        });
    }

    /**
     * 设置会话是否消息免打扰
     *
     * @param id
     * @param isDnd
     */
    public void updateConversationDnd(final String id, final boolean isDnd) {
        final String completeUrl = APIUri.getConversationSetDnd(id);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("dnd", isDnd);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateConversationDnd(id, isDnd);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDndSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDndFail(error, responseCode);
            }
        });
    }

    /**
     * 获取会话信息
     *
     * @param id
     */
    public void getConversationInfo(final String id) {
        final String completeUrl = APIUri.getConversationInfoUrl(id);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getConversationInfo(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                apiInterface.returnConversationInfoSuccess(new Conversation(object));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnConversationInfoFail(error, responseCode);
            }
        });
    }

    /**
     * 修改会话名称
     *
     * @param id
     * @param name
     */
    public void updateConversationName(final String id, final String name) {
        final String completeUrl = APIUri.getUpdateConversationNameUrl(id);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("name", name);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateConversationName(id, name);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateConversationNameSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateConversationNameFail(error, responseCode);
            }
        });
    }


    /**
     * 创建点聊
     *
     * @param uid
     */
    public void createDirectConversation(final String uid) {
        final String completeUrl = APIUri.getCreateDirectConversationUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("mate", uid);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createDirectConversation(uid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                apiInterface.returnCreateDirectConversationSuccess(new Conversation(object));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateDirectConversationFail(error, responseCode);

            }
        });
    }


    /**
     * 创建群组
     *
     * @param name
     * @param members
     */
    public void createGroupConversation(final String name, final JSONArray members) {
        final String completeUrl = APIUri.getCreateGroupConversationUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        try {
            params.addParameter("name", name);
            params.addParameter("members", members);
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createGroupConversation(name, members);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                apiInterface.returnCreateGroupConversationSuccess(new Conversation(object));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateGroupConversationFail(error, responseCode);
            }
        });

    }

    /**
     * 转发文件（图片）
     *
     * @param filePath 文件路径
     * @param toCid    channel ID
     */
    public void transmitFile(final String filePath, final String fromCid, final String toCid,
                             final String fileType, final Message message) {
        final String completeUrl = APIUri.getTransmitFileUrl(fromCid, fileType);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("path", filePath);
        params.addQueryStringParameter("to", toCid);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        transmitFile(filePath, fromCid, toCid, fileType, message);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                apiInterface.returnTransmitPictureSuccess(toCid, object.toString(), message);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnTransmitPictureError(error, responseCode);
            }
        });
    }

    public void shareFileToFriendsFromVolume(final String volume, final String channel, final String path, final VolumeFile volumeFile) {
        String url = APIUri.getVolumeShareFileUrl(volume, channel);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("path", path);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                String path = "";
                try {
                    JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                    path = object.getString("path");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                apiInterface.returnShareFileToFriendsFromVolumeSuccess(path, volumeFile);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnShareFileToFriendsFromVolumeFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        shareFileToFriendsFromVolume(volume, channel, path, volumeFile);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 获取扫码加群二维码内容
     *
     * @param cid
     */
    public void getInvitationContent(final String cid) {
        String url = APIUri.getInvitationUrl(cid);
        LogUtils.YfcDebug("URL:" + url);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnInvitationContentSuccess(new ScanCodeJoinConversationBean(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnInvitationContentFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getInvitationContent(cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 服务号首页服务号列表
     *
     * @param cid
     */
    public void getConversationServiceList(final String cid) {
        String url = APIUri.getConversationServiceListUrl(cid);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGetConversationServiceListSuccess(new GetConversationListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetConversationServiceListFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getConversationServiceList(cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 服务号首页所有服务号列表
     *
     * @param cid
     */
    public void getConversationServiceAllList(final String cid) {
        String url = APIUri.getConversationServiceListAllUrl(cid);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGetConversationServiceListAllSuccess(new GetConversationListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetConversationServiceListAllFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getConversationServiceAllList(cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 请求关注服务号
     *
     * @param cid
     */
    public void requestFollowConversationService(final String cid) {
        String url = APIUri.getFollowConversationServiceUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnFollowConversationServiceSuccess(new Conversation(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnFollowConversationServiceFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        requestFollowConversationService(cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 请求搜索服务号
     *
     */
    public void requestSearchConversationService(final String serviceName) {
        String url = APIUri.getSearchConversationServiceUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnSearchConversationServiceSuccess(new Conversation(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnSearchConversationServiceFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        requestSearchConversationService(serviceName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }
}
