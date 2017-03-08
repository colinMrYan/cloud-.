package com.inspur.emmcloud.ui.app.groupnews;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
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
import com.inspur.emmcloud.config.MyAppWebConfig;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.ChatCreateUtils;
import com.inspur.emmcloud.util.ChatCreateUtils.OnCreateDirectChannelListener;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
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
	private int mCurrentChooseItem;
	private int mCurrentItem = -1;
	private boolean isNight = false;

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

		webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE,null);
		int textSize = PreferencesUtils.getInt(NewsWebDetailActivity.this,"app_news_text_size",MyAppWebConfig.NORMAL);
		switch (textSize){
			case MyAppWebConfig.SMALLESET:
				mCurrentItem = 0;
				break;
			case MyAppWebConfig.SMALLER:
				mCurrentItem = 1;
				break;
			case MyAppWebConfig.NORMAL:
				mCurrentItem = 2;
				break;
			case MyAppWebConfig.LARGER:
				mCurrentItem = 3;
				break;
			case MyAppWebConfig.LARGEST:
				mCurrentItem = 4;
				break;
			default:
				mCurrentItem = 2;
				textSize = 100;
				break;
		}
		WebSettings webSettings = webView.getSettings();
		// 设置WebView属性，能够执行Javascript脚本
		webSettings.setJavaScriptEnabled(true);
		// 设置可以访问文件
		webSettings.setAllowFileAccess(true);
		// 设置支持缩放
		webSettings.setBuiltInZoomControls(true);
		// 设置字体大小
		webSettings.setSupportZoom(true);
		webSettings.setTextZoom(textSize);
		// 加载需要显示的网页
		if (!url.startsWith("http")) {
			url = UriUtils.getGroupNewsUrl(url);
		}
		webView.loadUrl(url);
//		webView.loadUrl("file:///android_asset/news1.html");
		// 设置Web视图
		webView.setWebViewClient(new webViewClient());
		// 设置背景颜色
//		webView.setBackgroundColor(Color.BLUE);

		loadingDlg = new LoadingDialog(NewsWebDetailActivity.this);
	}




	/**
	 * 显示选择对话框
	 */
	private void showChooseDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Resources resource = getResources();
		String[] items = resource.getStringArray(R.array.app_web_news_fonts);
		builder.setTitle(getString(R.string.app_web_news_font_title));
		builder.setSingleChoiceItems(items, mCurrentItem, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mCurrentChooseItem = which;
			}
		});
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			WebSettings settings = webView.getSettings();
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (mCurrentChooseItem) {
					case 0:
						changeNewsFontSize(settings,MyAppWebConfig.SMALLESET,mCurrentChooseItem);
						break;
					case 1:
						changeNewsFontSize(settings,MyAppWebConfig.SMALLER,mCurrentChooseItem);
						break;
					case 2:
						changeNewsFontSize(settings,MyAppWebConfig.NORMAL,mCurrentChooseItem);
						break;
					case 3:
						changeNewsFontSize(settings,MyAppWebConfig.LARGER,mCurrentChooseItem);
						break;
					case 4:
						changeNewsFontSize(settings,MyAppWebConfig.LARGEST,mCurrentChooseItem);
						break;
					default:
						changeNewsFontSize(settings,MyAppWebConfig.NORMAL,mCurrentItem);
						break;
				}
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), null);
		builder.show();
	}

	/**
	 * 改变WebView字体大小
	 * @param settings
	 * @param textZoom
     */
	private void changeNewsFontSize(WebSettings settings, int textZoom,int currentItem) {
		mCurrentItem = currentItem;
		settings.setTextZoom(textZoom);
		PreferencesUtils.putInt(NewsWebDetailActivity.this,"app_news_text_size",textZoom);
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
			//调整夜间模式方案
//			WebSettings webSettings = webView.getSettings();
//			webSettings.setJavaScriptEnabled(true);
//			if(!isNight){
//				isNight = true;
//				webView.loadUrl("javascript:load_night()");
//			}else{
//				isNight = false;
//				webView.loadUrl("javascript:load_day()");
//			}
			break;
			case R.id.news_font_size_img:
				showChooseDialog();
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

	@Override
	public void finish() {
		ViewGroup view = (ViewGroup) getWindow().getDecorView();
		view.removeAllViews();
		super.finish();
	}

}
