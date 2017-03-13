package com.inspur.emmcloud.widget;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Comment;
import com.inspur.emmcloud.bean.GetMsgCommentResult;
import com.inspur.emmcloud.ui.chat.GroupAlbumActivity;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.HandleMsgTextUtils;
import com.inspur.emmcloud.util.InputMethodUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.URLMatcher;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

import static android.app.Activity.RESULT_CANCELED;

/**
 * 单张图片显示Fragment
 */
public class ImageDetailFragment extends Fragment {
	private static final int RESULT_MENTIONS = 5;
	private String mImageUrl, mid, cid;
	private SmoothImageView mImageView;
	private ProgressBar progressBar;
	private PhotoViewAttacher mAttacher;
	private ECMChatInputMenu ecmChatInputMenu;
	private MaxHightListView commentListView;
	private List<Comment> commentList = new ArrayList<>();
	private LinearLayout imgCommentLayout;
	private int locationW, locationH, locationX, locationY;
	private boolean isTargetPosition;//是否是第一次显示的那个图片

	private ArrayList<String> userNameList = new ArrayList<String>();
	private ArrayList<String> uidList = new ArrayList<String>();
	private String channelType;
	private ChatAPIService apiService;
	private ImageView enterChannelImgsBtn;
	private RelativeLayout headerLayout;

	public static ImageDetailFragment newInstance(String imageUrl, String mid, String cid, int w, int h, int x, int y, boolean isTargetPosition) {
		final ImageDetailFragment f = new ImageDetailFragment();

		final Bundle args = new Bundle();
		args.putString("url", imageUrl);
		args.putString("mid", mid);
		args.putString("cid", cid);
		args.putInt("w", w);
		args.putInt("h", h);
		args.putInt("x", x);
		args.putInt("y", y);
		args.putBoolean("isTargetPosition", isTargetPosition);
		f.setArguments(args);
		return f;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString("url")
				: null;
		mid = getArguments() != null ? getArguments().getString("mid")
				: null;
		cid = getArguments() != null ? getArguments().getString("cid")
				: null;
		locationH = getArguments() != null ? getArguments().getInt("h")
				: null;
		locationW = getArguments() != null ? getArguments().getInt("w")
				: null;
		locationX = getArguments() != null ? getArguments().getInt("x")
				: null;
		locationY = getArguments() != null ? getArguments().getInt("y")
				: null;
		isTargetPosition = getArguments() != null ? getArguments().getBoolean("isTargetPosition")
				: false;
		if (!StringUtils.isBlank(mid)) {
			channelType = ChannelCacheUtils.getChannelType(getContext(),
					cid);
			apiService = new ChatAPIService(getContext());
			apiService.setAPIInterface(new WebService());
			getImgComment(mid);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_pager_detail_fragment,
				container, false);
		headerLayout = (RelativeLayout) v.findViewById(R.id.header_layout);
		((RelativeLayout)v.findViewById(R.id.back_layout)).setOnClickListener(clickListener);
		((ImageView)v.findViewById(R.id.save_img_btn)).setOnClickListener(clickListener);
		enterChannelImgsBtn = (ImageView) v.findViewById(R.id.enter_channel_imgs_btn);
		enterChannelImgsBtn.setOnClickListener(clickListener);
		commentListView = (MaxHightListView) v.findViewById(R.id.comment_list);
		commentListView.setListViewHeight(DensityUtil.dip2px(getContext(), 200));
		commentListView.setAdapter(adapter);
		ecmChatInputMenu = (ECMChatInputMenu) v.findViewById(R.id.chat_input_menu);
		initEcmChatInputMenu();
		imgCommentLayout = (LinearLayout) v.findViewById(R.id.img_comment_layout);
		if (!StringUtils.isBlank(mid)) {
			imgCommentLayout.setVisibility(View.VISIBLE);
			enterChannelImgsBtn.setVisibility(View.VISIBLE);
			if (channelType != null && channelType.equals("GROUP")) {
				ecmChatInputMenu.setCanMention(true, cid);
			}
		}
		mImageView = (SmoothImageView) v.findViewById(R.id.image);
		mImageView.setOriginalInfo(locationW, locationH, locationX, locationY);
		if (isTargetPosition) {
			mImageView.transformIn();
		}
		mImageView.setOnTransformListener(new SmoothImageView.TransformListener() {
			@Override
			public void onTransformComplete(int mode) {
				if (mode == 2) {
					getActivity().finish();
					getActivity().overridePendingTransition(0, 0);
				}
			}
		});
		mAttacher = new PhotoViewAttacher(mImageView);

		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2) {
				InputMethodUtils.hide(getActivity());
				if (mid != null) {
					if (imgCommentLayout.getVisibility() == View.VISIBLE) {
						imgCommentLayout.setVisibility(View.GONE);
						headerLayout.setVisibility(View.GONE);
					} else {
						imgCommentLayout.setVisibility(View.VISIBLE);
						headerLayout.setVisibility(View.VISIBLE);
					}
				} else {
					closeImg();
				}

			}

			@Override
			public void onOutsidePhotoTap() {

			}
		});
		mAttacher.setOnSingleFlingListener(new PhotoViewAttacher.OnSingleFlingListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (Math.abs(velocityY) > Math.abs(velocityX) && e2.getY() - e1.getY() > DensityUtil.dip2px(getContext(), 30)) {
					closeImg();
				}
				return false;
			}
		});
		progressBar = (ProgressBar) v.findViewById(R.id.loading);
		return v;
	}


	/**
	 * 初始化评论输入框
	 */
	private void initEcmChatInputMenu(){
		ecmChatInputMenu.showAddBtn(false);
		ecmChatInputMenu.setFragmentContext(ImageDetailFragment.this);
		ecmChatInputMenu.setChatInputMenuListener(new ECMChatInputMenu.ChatInputMenuListener() {
			@Override
			public void onSetContentViewHeight(boolean isLock) {

			}

			@Override
			public void onSendMsg(String content, List<String> mentionsUidList, List<String> mentionsUserNameList) {
				sendComment(content,mentionsUidList,mentionsUserNameList);
				InputMethodUtils.hide(getActivity());
			}
		});
	}


	private void sendComment(String content, List<String> mentionsUidList, List<String> mentionsUserNameList){
		if (NetUtils.isNetworkConnected(getActivity())) {
			String commentConbineSendText = getConbineCommentSendText(content);
			apiService.sendMsg(cid, commentConbineSendText, "txt_comment",
					mid, "");
			addLocalComment(commentConbineSendText);
		}
	}

	private void addLocalComment(String commentConbineSendText){
		Comment newComment = combineComment(commentConbineSendText);
		commentList.add(0,newComment);
		adapter.notifyDataSetChanged();
		// 滚动到页面最后
	}

	/**
	 * 拼接评论发送的内容
	 * @param content
	 * @return
	 */
	public String getConbineCommentSendText(String content) {
		String source = "";
		ArrayList<String> urlList = URLMatcher.getUrls(content);
		JSONObject richTextObj = new JSONObject();
		source = HandleMsgTextUtils.handleMentionAndURL(content, userNameList,
				uidList);
		JSONArray mentionArray = JSONUtils.toJSONArray(uidList);
		JSONArray urlArray = JSONUtils.toJSONArray(urlList);
		try {
			richTextObj.put("source", source);
			richTextObj.put("mentions", mentionArray);
			richTextObj.put("urlList", urlArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return richTextObj.toString();
	}

	/**
	 * 拼装评论消息
	 * @param content
	 * @return
	 */
	private Comment combineComment(String content) {
		String uid = ((MyApplication) getActivity().getApplicationContext()).getUid();
		String title = PreferencesUtils.getString(
				getActivity(), "userRealName");
		String timeStamp = TimeUtils.getCurrentUTCTimeString();
		JSONObject jsonComment = new JSONObject();
		JSONObject jsonFrom = new JSONObject();
		try {
			jsonFrom.put("title", title);
			jsonFrom.put("uid", uid);
			jsonComment.put("timestamp", timeStamp);
			jsonComment.put("body", content);
			jsonComment.put("from", jsonFrom);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Comment(jsonComment);
	}

	/**
	 * 关闭图片显示
	 */
	private void closeImg() {
		InputMethodUtils.hide(getActivity());
		if (isTargetPosition && locationW != 0) {
			mImageView.transformOut();
		} else {
			getActivity().finish();
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}
	}

	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()){
				case R.id.back_layout:
					closeImg();
					break;
				case R.id.save_img_btn:
					mImageView.buildDrawingCache(true);
					mImageView.buildDrawingCache();
					Bitmap bitmap = mImageView.getDrawingCache();
					saveBitmapFile(bitmap);
					mImageView.setDrawingCacheEnabled(false);
					break;
				case R.id.enter_channel_imgs_btn:
					Bundle bundle = new Bundle();
					bundle.putString("cid", cid);
					IntentUtils.startActivity(getActivity(),
							GroupAlbumActivity.class, bundle);
					break;
				default:
					break;
			}
		}
	};

	/**
	 * 保存图片
	 *
	 * @param bitmap
	 */
	public void saveBitmapFile(Bitmap bitmap) {
		File temp = new File("/sdcard/IMP-Cloud/cache/chat/");// 要保存文件先创建文件夹
		if (!temp.exists()) {
			temp.mkdir();
		}
		// 重复保存时，覆盖原同名图片
		// 将要保存图片的路径和图片名称
		File file = new File("/sdcard/IMP-Cloud/cache/chat/"
				+ FileUtils.getFileName(mImageUrl));
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			ToastUtils.show(getActivity(), getString(R.string.save_success));
		} catch (IOException e) {
			ToastUtils.show(getActivity(), getString(R.string.save_fail));
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.plugin_camera_no_pictures)
				.showImageOnFail(R.drawable.plugin_camera_no_pictures)
				.showImageOnLoading(R.drawable.plugin_camera_no_pictures)
				// 设置图片的解码类型
				.bitmapConfig(Bitmap.Config.RGB_565)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.build();
		ImageLoader.getInstance().displayImage(mImageUrl, mImageView, options,
				new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						progressBar.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
												FailReason failReason) {
						String message = null;
						switch (failReason.getType()) {
							case IO_ERROR:
								message = getString(R.string.download_fail);
								break;
							case DECODING_ERROR:
								message = getString(R.string.picture_cannot_show);
								break;
							case NETWORK_DENIED:
								message = getString(R.string.cannot_download_for_network_exception);
								break;
							case OUT_OF_MEMORY:
								message = getString(R.string.cannot_show_for_too_big);
								break;
							case UNKNOWN:
								message = getString(R.string.unknown_error);
								break;
							default:
								message = getString(R.string.download_fail);
								break;
						}
						Toast.makeText(getActivity(), message,
								Toast.LENGTH_SHORT).show();
						progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
												  Bitmap loadedImage) {
						progressBar.setVisibility(View.GONE);
						mAttacher.update();
					}
				});
	}

	private BaseAdapter adapter = new BaseAdapter() {
		@Override
		public int getCount() {
			return commentList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.image_pager_img_comment_item_view, null);
			TextView commentContentText = (TextView) convertView.findViewById(R.id.comment_content_text);
			TextView commenterText = (TextView) convertView.findViewById(R.id.commenter_text);
			TextView commentTimeText = (TextView) convertView.findViewById(R.id.comment_time_text);
			commentContentText.setMovementMethod(LinkMovementMethod.getInstance());
			Comment comment = commentList.get(position);
			String mentionsString = comment.getMentions();
			String urlsString = comment.getUrls();
			String[] mentions = mentionsString.replace("[", "").replace("]", "").split(",");
			String[] urls = urlsString.replace("[", "").replace("]", "").split(",");
			List<String> mentionList = Arrays.asList(mentions);
			List<String> urlList = Arrays.asList(urls);
			SpannableString spannableString = MentionsAndUrlShowUtils.handleMentioin(comment.getSource(), mentionList, urlList);
			commentContentText.setText(spannableString);
			TransHtmlToTextUtils.stripUnderlines(commentContentText,
					Color.parseColor("#0f7bca"));
			commenterText.setText(comment.getTitle());
			String time = TimeUtils.getDisplayTime(getContext(),
					comment.getTimestamp());
			commentTimeText.setText(time);
			return convertView;
		}
	};


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_CANCELED && requestCode == RESULT_MENTIONS) {
			ecmChatInputMenu.setMentionData(data);
		}
	}


	/**
	 * 获取消息的评论
	 *
	 * @param mid
	 */
	private void getImgComment(String mid) {
		if (NetUtils.isNetworkConnected(getContext())) {
			apiService.getComment(mid);
		}
	}

	;

	private class WebService extends APIInterfaceInstance {
		@Override
		public void returnMsgCommentSuccess(GetMsgCommentResult getMsgCommentResult) {
			commentList = getMsgCommentResult.getCommentList();
			Collections.reverse(commentList);
			adapter.notifyDataSetChanged();


		}

		@Override
		public void returnMsgCommentFail(String error) {
			super.returnMsgCommentFail(error);
		}
	}
}
