package com.inspur.emmcloud.ui.app.groupnews;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.GetSendMsgResult;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.ChatCreateUtils;
import com.inspur.emmcloud.util.ChatCreateUtils.OnCreateDirectChannelListener;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ProgressWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NewsWebDetailActivity extends BaseActivity {

	private ProgressWebView webView;

	private static final int SHARE_SEARCH_RUEST_CODE = 1;
	private String url;
	private String poster;
	private String title;
	private String digest;
	private LoadingDialog loadingDlg;
	private String shareCid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(R.layout.activity_newsweb_detail);
		Intent intent = getIntent();
		url = intent.getStringExtra("url");

		if (intent.hasExtra("poster")) {
			poster = intent.getStringExtra("poster");
		}

		if (intent.hasExtra("title")) {
			title = intent.getStringExtra("title");
		}

		if (intent.hasExtra("digest")) {
			digest = intent.getStringExtra("digest");
		}

		webView = (ProgressWebView) findViewById(R.id.news_webdetail_webview);

		// 加载需要显示的网页
		if (!url.startsWith("http")) {
			url = UriUtils.getGroupNewsUrl(url);
		}
		webView.loadUrl(url);
		// 设置Web视图
		webView.setWebViewClient(new webViewClient());
		loadingDlg = new LoadingDialog(NewsWebDetailActivity.this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if(webView != null){
			webView.onResume();
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;
		case R.id.news_share_img:
			Intent intent = new Intent();
			intent.putExtra("select_content", 0);
			intent.putExtra("isMulti_select", false);
			intent.putExtra("isContainMe", true);
			intent.putExtra("title", getString(R.string.news_share));
			intent.setClass(getApplicationContext(),
					ContactSearchActivity.class);
			startActivityForResult(intent, SHARE_SEARCH_RUEST_CODE);

			break;
		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SHARE_SEARCH_RUEST_CODE && resultCode == RESULT_OK
				&& NetUtils.isNetworkConnected(getApplicationContext())) {
			String result = data.getStringExtra("searchResult");
			try {
				JSONObject jsonObject = new JSONObject(result);
				if (jsonObject.has("people")) {
					JSONArray peopleArray = jsonObject.getJSONArray("people");
					if (peopleArray.length() > 0) {
						JSONObject peopleObj = peopleArray.getJSONObject(0);
						String uid = peopleObj.getString("pid");
						createDirectChannel(uid);
					}

				}

				if (jsonObject.has("channelGroup")) {
					JSONArray channelGroupArray = jsonObject
							.getJSONArray("channelGroup");
					if (channelGroupArray.length() > 0) {
						JSONObject cidObj = channelGroupArray.getJSONObject(0);
							shareCid = cidObj.getString("cid");
							shareNewsLink(shareCid);
					}
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				showShareFailToast();
			}

		}
	}

	/**
	 * 创建单聊
	 * 
	 * @param uid
	 */
	private void createDirectChannel(String uid) {
		// TODO Auto-generated method stub
		new ChatCreateUtils().createDirectChannel(NewsWebDetailActivity.this, uid,
				new OnCreateDirectChannelListener() {

					@Override
					public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
						// TODO Auto-generated method stub
						shareNewsLink(getCreateSingleChannelResult.getCid());

					}

					@Override
					public void createDirectChannelFail() {
						// TODO Auto-generated method stub
						showShareFailToast();
					}
				});
	}

	/**
	 * 分享新闻
	 * 
	 * @param cid
	 */
	private void shareNewsLink(String cid) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("url", url);
			jsonObject.put("poster", poster);
			jsonObject.put("digest", digest);
			jsonObject.put("title", title);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		sendMsg(cid, jsonObject.toString(), System.currentTimeMillis() + "");
	}

	/**
	 * 发送新闻分享
	 * 
	 * @param cid
	 * @param jsonNews
	 * @param time
	 */
	private void sendMsg(String cid, String jsonNews, String time) {
		ChatAPIService apiService = new ChatAPIService(
				NewsWebDetailActivity.this);
		apiService.setAPIInterface(new WebService());
		apiService.sendMsg(cid, jsonNews, "res_link", time);
	}

	/**
	 * 自定义WebViewClient在应用中打开页面
	 *
	 */
	private class webViewClient extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	/**
	 * 弹出分享失败toast
	 */
	private void showShareFailToast() {
		Toast.makeText(NewsWebDetailActivity.this,
				getString(R.string.news_share_fail), Toast.LENGTH_SHORT).show();
	}

	class WebService extends APIInterfaceInstance {

		@Override
		public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
				String fakeMessageId) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			Toast.makeText(NewsWebDetailActivity.this,
					getString(R.string.news_share_success), Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void returnSendMsgFail(String error,String fakeMessageId) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			showShareFailToast();
		}

	}

	protected void onPause() {
		super.onPause();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			webView.onPause(); // 暂停网页中正在播放的视频
		}
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (webView != null) {
			webView.removeAllViews();
			webView.destroy();
		}
	}

}
