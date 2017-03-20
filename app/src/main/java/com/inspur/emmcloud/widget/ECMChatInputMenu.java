/**
 * 
 * ECMChatInputMenu.java
 * classes : com.inspur.emmcloud.widget.ECMChatInputMenu
 * V 1.0.0
 * Create at 2016年11月24日 上午10:25:52
 */
package com.inspur.emmcloud.widget;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MsgAddItemAdapter;
import com.inspur.emmcloud.bean.MentionBean;
import com.inspur.emmcloud.ui.chat.ImagePagerActivity;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.util.ChannelMentions;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.spans.ForeColorSpan;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * com.inspur.emmcloud.widget.ECMChatInputMenu create at 2016年11月24日 上午10:25:52
 */
public class ECMChatInputMenu extends LinearLayout {

	private static final int GELLARY_RESULT = 2;
	private static final int CAMERA_RESULT = 3;
	private static final int CHOOSE_FILE = 4;
	private static final int MENTIONS_RESULT = 5;
	private Context context;
	private LayoutInflater layoutInflater;
	private ChatInputEdit inputEdit;
	private ImageView addImg;
	private Button sendMsgBtn;
	private RelativeLayout addMenuLayout;
	private LinearLayout rootLayout;
	private boolean canMention = false;
	private int editWordsLenth = 0;
	private ArrayList<String> mentionsUserNameList = new ArrayList<String>();
	private ArrayList<String> mentionsUidList = new ArrayList<String>();
	private int beginMentions = 0;
	private int endMentions = 0;
	private String channelId = "";
	private InputMethodManager mInputManager;
	private ChatInputMenuListener chatInputMenuListener;
	private MsgAddItemAdapter msgAddItemAdapter;
	private List<Integer> imgList = new ArrayList<Integer>();
	private List<Integer> textList = new ArrayList<Integer>();
	private int[] imgArray = {R.drawable.icon_select_album,R.drawable.icon_select_take_photo,R.drawable.icon_select_file};
	private int[] textArray = {R.string.album,R.string.take_photo,R.string.file};
	private Fragment fragment;

	// private View view ;

	public ECMChatInputMenu(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context,null);
	}

	public ECMChatInputMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context,attrs);
	}

	public ECMChatInputMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context,attrs);
	}

	private void init(final Context context,AttributeSet attrs) {
		// TODO Auto-generated method stub
		this.context = context;
		layoutInflater = LayoutInflater.from(context);
		layoutInflater.inflate(R.layout.ecm_widget_chat_input_menu, this);
		inputEdit = (ChatInputEdit) findViewById(R.id.input_edit);
		inputEdit.setIsOpen(true);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ECMChatInputMenu);
		String layoutType = a.getString(R.styleable.ECMChatInputMenu_layoutType);
		if (!StringUtils.isEmpty(layoutType) && layoutType.equals("img_comment_input")){
			layoutInflater.inflate(R.layout.ecm_widget_chat_input_menu_img_comment, this);
		}else {
			layoutInflater.inflate(R.layout.ecm_widget_chat_input_menu, this);
		}
		a.recycle();
		rootLayout = (LinearLayout)findViewById(R.id.root_layout);
		inputEdit = (ChatInputEdit) findViewById(R.id.input_edit);
		addImg = (ImageView) findViewById(R.id.add_img);
		addMenuLayout = (RelativeLayout) findViewById(R.id.add_menu_layout);
		sendMsgBtn = (Button) findViewById(R.id.send_msg_btn);
		sendMsgBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String content = inputEdit.getText().toString();
				if (StringUtils.isEmpty(content)) {
					ToastUtils.show(context,
							context.getString(R.string.msgcontent_cannot_null));
				} else if (NetUtils.isNetworkConnected(context)) {
					chatInputMenuListener.onSendMsg(content, mentionsUidList,
							mentionsUserNameList);
					inputEdit.setText("");
				}
			}
		});
		addImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (addMenuLayout.isShown()) {
					lockContentHeight();
					hideAddItemLayout(true);
					unlockContentHeight();
				} else if (isSoftInputShown()) {
					lockContentHeight();
					showAddItemLayout();
					unlockContentHeight();
				} else {
					showAddItemLayout();
				}
			}
		});

		msgAddItemAdapter = new MsgAddItemAdapter(context);
		initMenuGrid();
		mInputManager = (InputMethodManager) context
				.getSystemService(context.INPUT_METHOD_SERVICE);

		inputEdit.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					handMentions();
				}
				if (event.getAction() == MotionEvent.ACTION_UP
						&& addMenuLayout.isShown()) {
					lockContentHeight();
					hideAddItemLayout(true);
					unlockContentHeight();
				}
				return false;
			}
		});

	}

	/**
	 * 处理mentions点击人，不让光标落在人名中
	 */
	private void handMentions() {
		ArrayList<MentionBean> mentionBeenList = new ArrayList<MentionBean>();
		String inputContent = inputEdit.getText().toString();
		for (int i = 0; i< mentionsUserNameList.size(); i++){
			String mentionName = mentionsUserNameList.get(i);
			int mentionNameStart = inputContent.indexOf(mentionName);
			int mentionNameEnd = mentionNameStart + mentionName.length();
			MentionBean mentionBean = new MentionBean();
			mentionBean.setMentionStart(mentionNameStart);
			mentionBean.setMentioinEnd(mentionNameEnd);
			mentionBean.setMentionName(mentionName);
			mentionBeenList.add(mentionBean);
		}
		inputEdit.setIsOpen(true);
		inputEdit.setMentionBeenList(mentionBeenList);
	}

	private void lockContentHeight() {
		chatInputMenuListener.onSetContentViewHeight(true);
	}

	private void unlockContentHeight() {
		chatInputMenuListener.onSetContentViewHeight(false);
	}

	public void showAddBtn(boolean isShowHideBtn){
		addImg.setVisibility(isShowHideBtn?View.VISIBLE:View.GONE);
	}

	public void setFragmentContext(Fragment fragment){
		this.fragment = fragment;
	}

	private void hideAddItemLayout(boolean showSoftInput) {
		if (addMenuLayout.isShown()) {
			addMenuLayout.setVisibility(View.GONE);
			if (showSoftInput) {
				showSoftInput();
			}
		}
	}

	private void showAddItemLayout() {
		int softInputHeight = getSupportSoftInputHeight();
		if (softInputHeight == 0) {
			softInputHeight = PreferencesUtils.getInt(context, "inputHight",
					DensityUtil.dip2px(context, 274));
		}
		hideSoftInput();
		addMenuLayout.getLayoutParams().height = softInputHeight;
		addMenuLayout.setVisibility(View.VISIBLE);
	}

	private void showSoftInput() {
		inputEdit.requestFocus();
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				mInputManager.showSoftInput(inputEdit, 0);
			}
		});
	}

	public void hideSoftInput() {
		mInputManager.hideSoftInputFromWindow(inputEdit.getWindowToken(), 0);
	}

	private boolean isSoftInputShown() {
		return getSupportSoftInputHeight() != 0;
	}

	private int getSupportSoftInputHeight() {
		Rect r = new Rect();
		((Activity) context).getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(r);
		int screenHeight = ((Activity) context).getWindow().getDecorView()
				.getRootView().getHeight();
		int softInputHeight = screenHeight - r.bottom;

		if (softInputHeight < 0) {
			Log.w("EmotionInputDetector",
					"Warning: value of softInputHeight is below zero!");
		}
		if (softInputHeight > 0) {
			PreferencesUtils.putInt(context, "inputHight", softInputHeight);
		}
		return softInputHeight;
	}

	/**
	 * 初始化消息发送的UI
	 */
	private void initMenuGrid() {
		GridView addItemGrid = (GridView) findViewById(R.id.add_menu_grid);
		addItemGrid.setAdapter(msgAddItemAdapter);
		addItemGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int clickItem = imgList.get(position);
				switch (clickItem) {
				case R.drawable.icon_select_album:
					openGallery();
					break;
				case R.drawable.icon_select_take_photo:
					openCamera();
//					launchCamera();
					break;
				case R.drawable.icon_select_file:
					openFileSystem();
					break;
				default:
					break;
				}

			}
		});
	}

	/**
	 * 调用文件系统
	 */
	protected void openFileSystem() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		((Activity) context).startActivityForResult(
				Intent.createChooser(intent,
						context.getString(R.string.file_upload_tips)),
				CHOOSE_FILE);
	}

	/**
	 * 调用图库
	 */
	protected void openGallery() {
		initImagePicker();
		Intent intent = new Intent(context,
				ImageGridActivity.class);
		((Activity) context).startActivityForResult(intent, GELLARY_RESULT);
//		Intent i = new Intent(Intent.ACTION_PICK,
//				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		((Activity) context).startActivityForResult(i, GELLARY_RESULT);
	}

	/**
	 * 初始化图片选择控件
	 */
	private void initImagePicker() {
		ImagePicker imagePicker = ImagePicker.getInstance();
		imagePicker.setImageLoader(new ImageDisplayUtils()); // 设置图片加载器
		imagePicker.setShowCamera(false); // 显示拍照按钮
		imagePicker.setCrop(false); // 允许裁剪（单选才有效）
		imagePicker.setSelectLimit(5);
//		imagePicker.setSaveRectangle(true); // 是否按矩形区域保存
		imagePicker.setMultiMode(true);
//		imagePicker.setStyle(CropImageView.Style.RECTANGLE); // 裁剪框的形状
//		imagePicker.setFocusWidth(1000); // 裁剪框的宽度。单位像素（圆形自动取宽高最小值）
//		imagePicker.setFocusHeight(1000); // 裁剪框的高度。单位像素（圆形自动取宽高最小值）
//		imagePicker.setOutPutX(1000); // 保存文件的宽度。单位像素
//		imagePicker.setOutPutY(1000); // 保存文件的高度。单位像素
	}


	//启动相机
	private void launchCamera()
	{
		try{
			//获取相机包名
			Intent infoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			ResolveInfo res = context.getPackageManager().
					resolveActivity(infoIntent, 0);
			if (res != null)
			{
				String packageName=res.activityInfo.packageName;
				if(packageName.equals("android"))
				{
					infoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
					res = context.getPackageManager().
							resolveActivity(infoIntent, 0);
					if (res != null)
						packageName=res.activityInfo.packageName;
				}
				//启动相机
				startApplicationByPackageName(packageName);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}

	//通过包名启动应用
	private void startApplicationByPackageName(String packName)
	{
		PackageInfo packageInfo=null;
		try{
			packageInfo=context.getPackageManager().getPackageInfo(packName, 0);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(null==packageInfo){
			return;
		}
		Intent resolveIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageInfo.packageName);
		List<ResolveInfo> resolveInfoList =context.getPackageManager().queryIntentActivities(resolveIntent, 0);
		if(null==resolveInfoList){
			return;
		}
		Iterator<ResolveInfo> iter=resolveInfoList.iterator();
		while(iter.hasNext()){
			ResolveInfo resolveInfo=(ResolveInfo) iter.next();
			if(null==resolveInfo){
				return;
			}
			String packageName=resolveInfo.activityInfo.packageName;
			String className=resolveInfo.activityInfo.name;
			Intent intent=new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ComponentName cn=new ComponentName(packageName, className);
			intent.setComponent(cn);
			((Activity) context).startActivityForResult(intent,
					CAMERA_RESULT);
		}//while
	}//method

	
	/**
	 * 调用摄像头拍照
	 */
	protected void openCamera() {
		Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// 判断存储卡是否可以用，可用进行存储
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File appDir = new File(Environment.getExternalStorageDirectory(),
					"DCIM");
			if (!appDir.exists()) {
				appDir.mkdir();
			}
			// 指定文件名字
			String fileName = new Date().getTime() + ".jpg";
			intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(appDir, fileName)));
			PreferencesUtils.putString(context, "capturekey", fileName);
			LogUtils.YfcDebug("打开相机之前");
			((Activity) context).startActivityForResult(intentFromCapture,
					CAMERA_RESULT);
		} else {
			ToastUtils.show(context, R.string.filetransfer_sd_not_exist);
		}
	}

	public void setChatInputMenuListener(
			ChatInputMenuListener chatInputMenuListener) {
		this.chatInputMenuListener = chatInputMenuListener;
	}

	public interface ChatInputMenuListener {
		void onSetContentViewHeight(boolean isLock);

		void onSendMsg(String content, List<String> mentionsUidList,
					   List<String> mentionsUserNameList);

	};

	public void setMentionData(Intent data) {
		String result = data.getStringExtra("searchResult");
		LogUtils.jasonDebug("result===="+result);
		PreferencesUtils.putString(context, channelId, "");
		ChannelMentions.addMentions(result, mentionsUserNameList,
				mentionsUidList, inputEdit, beginMentions, endMentions);
	}

	public void setCanMention(boolean isCanMention, String channelId) {
		this.channelId = channelId;
		inputEdit.addTextChangedListener(new TextChangedListener());
		//当删除到只剩一个@时判断是否调起mentions
		inputEdit.setOnKeyListener(new OnMentionsListener());
	}

	public boolean hideAddMenuLayout() {
		if (addMenuLayout.getVisibility() != View.GONE) {
			addMenuLayout.setVisibility(View.GONE);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 根据二进制字符串更新菜单视图
	 * 
	 * @param binaryString
	 */
	public void updateMenuGrid(String binaryString) {
		imgList.clear();
		textList.clear();
		int menuGridSize = binaryString.length() - 1;
		if(binaryString.length() > imgArray.length){
			menuGridSize = imgArray.length - 1;
		}
		if(binaryString.equals("-1")){
			menuGridSize = imgArray.length - 1;
			binaryString = "111";
		}
        for (int i = menuGridSize; i>=0; i--){  
        	if((binaryString.charAt(i)+"").equals("1")){
        		imgList.add(imgArray[menuGridSize - i]);
        		textList.add(textArray[menuGridSize - i]);
        	}
         }  
		msgAddItemAdapter.updateGridView(imgList, textList);
	}

	class TextChangedListener implements TextWatcher {
		String changeContent = "";

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			beginMentions = start;
			endMentions = start + count;
			changeContent = s.toString().substring(beginMentions, endMentions);
			if (s.toString().length() > editWordsLenth) {
				canMention = true;
			}
			ForeColorSpan[] spans = ((Spanned) s).getSpans(0, s.length(),
					ForeColorSpan.class);
			int which = -1;
			for (int i = 0; i < mentionsUserNameList.size(); i++) {
				if (!s.toString().contains(mentionsUserNameList.get(i))) {
					which = i;
					mentionsUserNameList.remove(i);
					mentionsUidList.remove(i);
					i--;
				}
			}

			int spanslen = spans.length;
			for (int i = 0; i < spanslen; i++) {
				if (which == i) {
					int started = ((Spannable) s).getSpanStart(spans[i]);
					int end = ((Spannable) s).getSpanEnd(spans[i]);
					inputEdit.getText().delete(started, end);
				}

			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			Intent intent = new Intent();
			intent.setClass(context, MembersActivity.class);
			intent.putExtra("title", context.getString(R.string.friend_list));
			intent.putExtra("cid", channelId);
			String tstr = s.toString();
			int strlen = tstr.length();
			if (!TextUtils.isEmpty(s.toString())
					&& canMention
					&& (tstr.substring(strlen - 1, strlen).equals("@") || changeContent
							.equals("@"))) {
				((Activity) context).overridePendingTransition(
						R.anim.activity_open, 0);
				if (context instanceof ImagePagerActivity){
					fragment.startActivityForResult(intent,
							MENTIONS_RESULT);
				}else {
					((Activity) context).startActivityForResult(intent,
							MENTIONS_RESULT);
				}

			}
			canMention = true;
		}

	}

	class OnMentionsListener implements OnKeyListener {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			String str = inputEdit.getText().toString();
			editWordsLenth = str.length();
			if (StringUtils.isBlank(inputEdit.getText().toString())) {
				canMention = true;
				return false;
			}
			if (keyCode == 67) {
				canMention = false;
			} else {
				canMention = true;
			}
			return false;
		}

	}
}
