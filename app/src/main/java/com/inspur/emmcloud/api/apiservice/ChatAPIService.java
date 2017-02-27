/**
 * 
 * ChatAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.ChatAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:32:07
 */
package com.inspur.emmcloud.api.apiservice;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.GetAddMembersSuccessResult;
import com.inspur.emmcloud.bean.GetBoolenResult;
import com.inspur.emmcloud.bean.GetChannelInfoResult;
import com.inspur.emmcloud.bean.GetChannelListResult;
import com.inspur.emmcloud.bean.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.GetFileUploadResult;
import com.inspur.emmcloud.bean.GetMeetingReplyResult;
import com.inspur.emmcloud.bean.GetMsgCommentResult;
import com.inspur.emmcloud.bean.GetNewMsgsResult;
import com.inspur.emmcloud.bean.GetNewsImgResult;
import com.inspur.emmcloud.bean.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.GetSendMsgResult;
import com.inspur.emmcloud.bean.GetMsgResult;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.OauthCallBack;
import com.inspur.emmcloud.util.OauthUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;

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

	/**
	 * 获取会话列表
	 */
	public void getChannelList() {
		final String completeUrl = UriUtils.getHttpApiUri("channel/session");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						getChannelList();
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnChannelListSuccess(new GetChannelListResult(
						arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnChannelListFail(error);
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
		final String completeUrl = UriUtils.getHttpApiUri("session/message");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("limit", count);
		if (!StringUtils.isBlank(msgId)) {
			params.addParameter("mid", msgId);
		}
		if (!StringUtils.isBlank(cid)) {
			params.addParameter("cid", cid);
		}

		x.http().get(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						getNewMsgs(cid, msgId, count);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnNewMsgsSuccess(new GetNewMsgsResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnNewMsgsFail(error);
			}
		});

	}

	/**
	 * 获取10条最新消息
	 */
	public void getNewMsgs() {
		getNewMsgs("", "", 10);
	}

	/**
	 * 获取评论
	 * 
	 * @param mid
	 */
	public void getComment(final String mid) {

		final String completeUrl = UriUtils.getHttpApiUri("message/" + mid
				+ "/comment");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						getComment(mid);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnMsgCommentSuccess(new GetMsgCommentResult(
						arg0, mid));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnMsgCommentFail(error);
			}
		});
	}

	/**
	 * 获取频道信息
	 * 
	 * @param cid
	 */
	public void getChannelInfo(final String cid) {
		final String completeUrl = UriUtils.getHttpApiUri("channel/" + cid);
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						getChannelInfo(cid);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug("yfcLog", "返回数据："+arg0);
				apiInterface.returnChannelInfoSuccess(new GetChannelInfoResult(
						arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnChannelInfoFail(error);
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
		final String completeUrl = UriUtils.getHttpApiUri("message");
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
			params.setBodyContent(paramObj.toString());
			params.setAsJsonContent(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		x.http().post(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						sendMsg(channelId, msgContent, type, mid, fakeMessageId);
					}
				}, context).refreshTocken(completeUrl);

			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnSendMsgSuccess(new GetSendMsgResult(arg0),
						fakeMessageId);
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnSendMsgFail(error);
			}
		});

	}

	/**
	 * 获取消息
	 * 
	 * @param mid
	 */
	public void getMsg(final String mid) {
		final String completeUrl = UriUtils.getHttpApiUri("message/" + mid);
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						getMsg(mid);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnMsgSuccess(new GetMsgResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnMsgFail(error);
			}
		});
	}

	/**
	 * 上传文件到资源服务器 (此处需要将调用端代码改为非线程)
	 *
	 * @param filePath
	 * @param fakeMessageId
	 * @param isImg
	 *            区分是否是图片类型
	 */
	public void uploadMsgResource(final String filePath,
			final String fakeMessageId, final boolean isImg) {
		final String completeUrl = UriUtils.getResUri("upload");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		File file = new File(filePath);
		params.setMultipart(true);// 有上传文件时使用multipart表单, 否则上传原始文件流.
		params.addBodyParameter("file1", file);
		final Bitmap bitmap = BitmapFactory.decodeFile(filePath);
		x.http().post(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						uploadMsgResource(filePath, fakeMessageId, isImg);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				if (isImg) {
					try {
						JSONObject jsonObject = new JSONObject(arg0);
						jsonObject.put("height", bitmap.getHeight());
						jsonObject.put("width", bitmap.getWidth());
						apiInterface.returnUploadMsgImgSuccess(
								new GetNewsImgResult(jsonObject.toString()), fakeMessageId);
					} catch (Exception e) {
						e.printStackTrace();
					}
					bitmap.recycle();
				} else {
					apiInterface.returnFileUpLoadSuccess(
							new GetFileUploadResult(arg0), fakeMessageId);
				}

			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				if (isImg) {
					apiInterface.returnUploadMsgImgFail(error);
				} else {
					apiInterface.returnFileUpLoadFail(error);
				}
			}
		});
	}

	/**
	 * 获取搜索的频道列表
	 * 
	 * @param searchText
	 */
	public void getAllGroupChannelList() {

		final String completeUrl = UriUtils
				.getHttpApiUri("channel/group?limit=-1");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						getAllGroupChannelList();
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				// FileUtils.writeFile(Environment.getExternalStorageDirectory()
				// + "/IMP-Cloud/contact.txt", new String(arg2));
				apiInterface
						.returnSearchChannelGroupSuccess(new GetSearchChannelGroupResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnSearchChannelGroupFail(error);
			}
		});

	}

	/**
	 * 获取频道列表信息
	 * 
	 * @param searchText
	 */
	public void getChannelGroupList(final String[] cidArray) {
		final String completeUrl = UriUtils.getHttpApiUri("channel?")
				+ "limit=1000";
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		// 解决服务端的一个bug
		if (cidArray.length == 1) {
			String[] cidArray2 = { cidArray[0], cidArray[0] };
			params.addParameter("cids", cidArray2);
		} else {
			params.addParameter("cids", cidArray);
		}

		x.http().get(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						getChannelGroupList(cidArray);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface
						.returnSearchChannelGroupSuccess(new GetSearchChannelGroupResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnSearchChannelGroupFail(error);
			}
		});

	}

	/**
	 * 创建点聊
	 * 
	 * @param uid
	 */
	public void createDirectChannel(final String uid) {
		final String completeUrl = UriUtils.getHttpApiUri("channel");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("mate", uid);
		params.addParameter("type", "DIRECT");
		x.http().post(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						createDirectChannel(uid);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface
						.returnCreateSingleChannelSuccess(new GetCreateSingleChannelResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnCreatSingleChannelFail(error);

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
		final String completeUrl = UriUtils.getHttpApiUri("channel?cid=") + cid;
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
		x.http().request(HttpMethod.PUT, params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						updateChannelGroupName(cid, name);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface
						.returnUpdateChannelGroupNameSuccess(new GetBoolenResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnUpdateChannelGroupNameFail(error);
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
		String url = UriUtils.addGroupMembers(cid);
		for (int i = 0; i < uids.size(); i++) {
			url = url + "uids=" + uids.get(i) + "&";
		}

		url = url.substring(0, url.length() - 1);
		final String completeUrl = url;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().request(HttpMethod.PUT, params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						addGroupMembers(uids, cid);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface
						.returnAddMembersSuccess(new GetAddMembersSuccessResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnAddMembersFail(error);
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
		String url = UriUtils.addGroupMembers(cid);
		for (int i = 0; i < uids.size(); i++) {
			url = url + "uids=" + uids.get(i) + "&";
		}
		url = url.substring(0, url.length() - 1);
		final String completeUrl = url;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().request(HttpMethod.DELETE, params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						deleteGroupMembers(uids, cid);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnDelMembersSuccess(new GetChannelInfoResult(
						arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnDelMembersFail(error);
			}
		});
	}

	/**
	 * 消息免打扰接口
	 * 
	 * @param cid
	 * @param nointerruption
	 *            是否免打扰
	 */
	public void updateDnd(final String cid, final Boolean nointerruption) {

		final String completeUrl = UriUtils.getNointerruption() + "?cid=" + cid
				+ "&dnd=" + nointerruption;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().request(HttpMethod.PUT, params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						// TODO Auto-generated method stub
						updateDnd(cid, nointerruption);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnDndSuccess();
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnDndFail(error);
			}
		});
	}

	/**
	 * 会议邀请卡片的应答
	 * 
	 * @param rid
	 * @param mpid
	 * @param state
	 */
	public void sendMeetingReply(final String rid, final String mpid,
			final String state) {
		final String completeUrl = UriUtils.getMeetingReply() + "?";
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addQueryStringParameter("rid", rid);
		params.addQueryStringParameter("mpid", mpid);
		params.addQueryStringParameter("state", state);
		x.http().request(HttpMethod.PUT, params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						sendMeetingReply(rid, mpid, state);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface
						.returnGetMeetingReplySuccess(new GetMeetingReplyResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnGetMeetingReplyFail(error);
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
		final String completeUrl = UriUtils.getHttpApiUri("channel");
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

		x.http().post(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						createGroupChannel(name, members);
					}
				}, context).refreshTocken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnCreatChannelGroupSuccess(new ChannelGroup(
						arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnCreateChannelGroupFail(error);
			}
		});

	}
	
}
