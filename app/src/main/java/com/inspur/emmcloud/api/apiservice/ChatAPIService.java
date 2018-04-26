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
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.GetAddMembersSuccessResult;
import com.inspur.emmcloud.bean.chat.GetChannelInfoResult;
import com.inspur.emmcloud.bean.chat.GetChannelListResult;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.GetFileUploadResult;
import com.inspur.emmcloud.bean.chat.GetMsgCommentCountResult;
import com.inspur.emmcloud.bean.chat.GetMsgCommentResult;
import com.inspur.emmcloud.bean.chat.GetMsgResult;
import com.inspur.emmcloud.bean.chat.GetNewMessagesResult;
import com.inspur.emmcloud.bean.chat.GetNewMsgsResult;
import com.inspur.emmcloud.bean.chat.GetNewsImgResult;
import com.inspur.emmcloud.bean.chat.GetNewsInstructionResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.GetUploadPushInfoResult;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;

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
		final String completeUrl = APIUri.getHttpApiUrl("channel/session");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context, completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						getChannelList();
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
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnChannelListSuccess(new GetChannelListResult(
						arg0));
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
		final String completeUrl = APIUri.getHttpApiUrl("session/message");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("limit", count);
		if (!StringUtils.isBlank(msgId)) {
			params.addParameter("mid", msgId);
		}
		if (!StringUtils.isBlank(cid)) {
			params.addParameter("cid", cid);
		}

		x.http().get(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}


			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnNewMsgsSuccess(new GetNewMsgsResult(arg0));
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
	 * 获取新消息
	 *
	 * @param cid
	 * @param msgId
	 * @param count
	 */
	public void getNewMessages(final String cid, final String msgId, final int count) {
		final String completeUrl = APIUri.getHttpApiUrl("session/message");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("limit", count);
		if (!StringUtils.isBlank(msgId)) {
			params.addParameter("mid", msgId);
		}
		if (!StringUtils.isBlank(cid)) {
			params.addParameter("cid", cid);
		}

		x.http().get(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}


			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnNewMessagesSuccess(new GetNewMessagesResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnNewMessagesFail(error, responseCode);
			}
		});

	}

	/**
	 * 获取评论
	 *
	 * @param mid
	 */
	public void getComment(final String mid) {
		final String completeUrl = APIUri.getHttpApiUrl("message/" + mid
				+ "/comment");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}


			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnMsgCommentSuccess(new GetMsgCommentResult(
						arg0), mid);
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
		final String completeUrl = APIUri.getHttpApiUrl("channel/" + cid);
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}


			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnChannelInfoSuccess(new GetChannelInfoResult(
						arg0));
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
		final String completeUrl = APIUri.getHttpApiUrl("message");
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
			actionObj.put("type","open-url");
			actionObj.put("url","ecc-channel://"+channelId);
			JSONObject extrasObj = new JSONObject();
			extrasObj.put("action",actionObj);
			paramObj.put("extras",extrasObj);
			params.setBodyContent(paramObj.toString());
			params.setAsJsonContent(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		x.http().post(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
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
		final String completeUrl = APIUri.getHttpApiUrl("message/" + mid);
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}


			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnMsgSuccess(new GetMsgResult(arg0));
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
	 * @param isImg
	 *            区分是否是图片类型
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
		x.http().post(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}


			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				if (isImg) {
					try {
						JSONObject jsonObject = new JSONObject(arg0);
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
							new GetFileUploadResult(arg0), fakeMessageId);
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

		final String completeUrl = APIUri
				.getHttpApiUrl("channel/group?limit=-1");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
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
				apiInterface.returnSearchChannelGroupFail(error, responseCode);
			}
		});

	}

	/**
	 *  获取频道列表信息
	 * @param cidArray
	 */
	public void getChannelGroupList(final String[] cidArray) {
		final String completeUrl = APIUri.getHttpApiUrl("channel?")
				+ "limit=1000";
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		// 解决服务端的一个bug
		if (cidArray.length == 1) {
			String[] cidArray2 = {cidArray[0], cidArray[0]};
			params.addParameter("cids", cidArray2);
		} else {
			params.addParameter("cids", cidArray);
		}

		x.http().get(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
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
		final String completeUrl = APIUri.getHttpApiUrl("channel");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("mate", uid);
		params.addParameter("type", "DIRECT");
		x.http().post(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
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
				apiInterface.returnCreatSingleChannelFail(error, responseCode);

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
		final String completeUrl = APIUri.getHttpApiUrl("channel?cid=") + cid;
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
		x.http().request(HttpMethod.PUT, params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
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
		x.http().request(HttpMethod.PUT, params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
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
		x.http().request(HttpMethod.DELETE, params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
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
				apiInterface.returnDelMembersFail(error, responseCode);
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

		final String completeUrl = APIUri.getNointerRuptionUrl() + "?cid=" + cid
				+ "&dnd=" + nointerruption;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().request(HttpMethod.PUT, params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}


			@Override
			public void callbackSuccess(String arg0) {
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
	 * 创建群组
	 *
	 * @param name
	 * @param members
	 */
	public void createGroupChannel(final String name, final JSONArray members) {
		final String completeUrl = APIUri.getHttpApiUrl("channel");
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

		x.http().post(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
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
				apiInterface.returnCreateChannelGroupFail(error, responseCode);
			}
		});

	}

	public void getMsgCommentCount(final String mid) {
		final String completeUrl = APIUri.getHttpApiUrl("message/" + mid
				+ "/comment/count");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context, completeUrl) {

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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}


			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnMsgCommentCountSuccess(new GetMsgCommentCountResult(arg0), mid);
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnMsgCommentCountFail(error, responseCode);
			}
		});
	}

	/**
	 * 新闻批示接口，传入内容为批示内容
	 * @param instruction
	 */
	public void sendNewsInstruction(final String newsId, final String instruction) {
		final String completeUrl = APIUri.getNewsInstruction(newsId);
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.setHeader("Content-Type", "url-encoded-form");
		params.addQueryStringParameter("comment", instruction);
		x.http().post(params, new APICallback(context, completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnNewsInstructionSuccess(new GetNewsInstructionResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnNewsInstructionFail(error, responseCode);
			}

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						sendNewsInstruction(newsId, instruction);
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
     * 上传推送相关信息
     * @param deviceId
     * @param deviceName
     * @param pushProvider
     * @param pushTracer
     */
	public void uploadPushInfo(final String deviceId,final String deviceName,final String pushProvider,final String pushTracer){
		final String url = APIUri.getUploadPushInfoUrl();
		RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
		params.addParameter("deviceId",deviceId);
		params.addParameter("deviceName",deviceName);
		params.addParameter("notificationProvider",pushProvider);
		params.addParameter("notificationTracer",pushTracer);
		x.http().post(params, new APICallback(context,url) {
            @Override
            public void callbackSuccess(String arg0) {
            	apiInterface.returnUploadPushInfoResultSuccess(new GetUploadPushInfoResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
				apiInterface.returnUploadPushInfoResultFail(error,responseCode);
            }

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						uploadPushInfo(deviceId,deviceName,pushProvider,pushTracer);
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
	 * 活动卡片点击按钮
	 * @param url
	 */
	public void openActionBackgroudUrl(final String url){
		RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
		x.http().get(params, new APICallback(context,url) {
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnOpenActionBackgroudUrlSuccess();
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnOpenActionBackgroudUrlFail(error,responseCode);
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
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}
		});
	}

}
