/**
 * ECMChatInputMenu.java
 * classes : com.inspur.emmcloud.widget.ECMChatInputMenu
 * V 1.0.0
 * Create at 2016年11月24日 上午10:25:52
 */
package com.inspur.emmcloud.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MsgInputAddItemAdapter;
import com.inspur.emmcloud.bean.MentionBean;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.util.ChannelMentions;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
    private boolean isChannelGroup = false;
    private ArrayList<String> mentionsUserNameList = new ArrayList<String>();
    private ArrayList<String> mentionsUidList = new ArrayList<String>();
    private int mentionPosition = 0;
    private int endMentions = 0;
    private String cid = "";
    private InputMethodManager mInputManager;
    private ChatInputMenuListener chatInputMenuListener;
    private MsgInputAddItemAdapter msgInputAddItemAdapter;
    private List<Integer> imgList = new ArrayList<>();
    private boolean isSetWindowListener = true;//是否监听窗口变化自动跳转输入框ui

    // private View view ;

    public ECMChatInputMenu(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context, null);
    }

    public ECMChatInputMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context, attrs);
    }

    public ECMChatInputMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        // TODO Auto-generated method stub
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ECMChatInputMenu);
        String layoutType = a.getString(R.styleable.ECMChatInputMenu_layoutType);
        if (layoutType != null && layoutType.equals("img_comment")) {
            layoutInflater.inflate(R.layout.ecm_widget_chat_input_menu_img_comment, this);
        } else {
            layoutInflater.inflate(R.layout.ecm_widget_chat_input_menu, this);
        }
        a.recycle();
        inputEdit = (ChatInputEdit) findViewById(R.id.input_edit);
        inputEdit.setIsOpen(true);
        rootLayout = (LinearLayout) findViewById(R.id.root_layout);
        inputEdit = (ChatInputEdit) findViewById(R.id.input_edit);
        addImg = (ImageView) findViewById(R.id.add_img);
        addMenuLayout = (RelativeLayout) findViewById(R.id.add_menu_layout);
        sendMsgBtn = (Button) findViewById(R.id.send_msg_btn);
        sendMsgBtn.setEnabled(false);
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

        msgInputAddItemAdapter = new MsgInputAddItemAdapter(context);
        initMenuGrid();
        mInputManager = (InputMethodManager) context
                .getSystemService(context.INPUT_METHOD_SERVICE);
        //防止长按输入框进行粘贴的事件被消化掉
        inputEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//				LogUtils.jasonDebug("isSetWindowListener="+isSetWindowListener);
//				if (isSetWindowListener) {
//					if (addMenuLayout.isShown()) {
//						lockContentHeight();
//						hideAddItemLayout(true);
//						unlockContentHeight();
//					}
//				}
            }
        });
        inputEdit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    handMentions();
                }

                if (isSetWindowListener) {
                    if (event.getAction() == MotionEvent.ACTION_UP
                            && addMenuLayout.isShown()) {
                        lockContentHeight();
                        hideAddItemLayout(true);
                        unlockContentHeight();
                    }
                }
                return false;
            }
        });

        inputEdit.addTextChangedListener(new TextChangedListener());
    }

    public void setWindowListener(boolean isSetWindowListener) {
        this.isSetWindowListener = isSetWindowListener;
    }

    public EditText getEdit() {
        return inputEdit;
    }

    /**
     * 处理mentions点击人，不让光标落在人名中
     */
    private void handMentions() {
        if (isChannelGroup) {
            ArrayList<MentionBean> mentionBeenList = new ArrayList<MentionBean>();
            String inputContent = inputEdit.getText().toString();
            for (int i = 0; i < mentionsUserNameList.size(); i++) {
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
    }

    private void lockContentHeight() {
        chatInputMenuListener.onSetContentViewHeight(true);
    }

    private void unlockContentHeight() {
        chatInputMenuListener.onSetContentViewHeight(false);
    }

    public void showAddBtn(boolean isShowHideBtn) {
        addImg.setVisibility(isShowHideBtn ? View.VISIBLE : View.GONE);
    }


    public void hideAddItemLayout(boolean showSoftInput) {
        if (addMenuLayout.isShown()) {
            addMenuLayout.setVisibility(View.GONE);
            if (showSoftInput) {
                showSoftInput();
            }
        }
    }

    public void showAddItemLayout() {
        int softInputHeight = getSupportSoftInputHeight();
        if (softInputHeight == 0) {
            softInputHeight = PreferencesUtils.getInt(context, "inputHight",
                    DensityUtil.dip2px(context, 274));
        }
        if (isSetWindowListener) {
            hideSoftInput();
        }
        addMenuLayout.getLayoutParams().height = softInputHeight;
        addMenuLayout.setVisibility(View.VISIBLE);
    }

    public void showSoftInput() {
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
        addItemGrid.setAdapter(msgInputAddItemAdapter);
        addItemGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                int clickItem = imgList.get(position);
                switch (clickItem) {
                    case R.drawable.ic_chat_input_add_gallery:
                        openGallery();
                        break;
                    case R.drawable.ic_chat_input_add_camera:
                        openCamera();
                        break;
                    case R.drawable.ic_chat_input_add_file:
                        openFileSystem();
                        break;
                    case R.drawable.ic_chat_input_add_mention:
                        openMention();
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
            ((Activity) context).startActivityForResult(intentFromCapture,
                    CAMERA_RESULT);
        } else {
            ToastUtils.show(context, R.string.filetransfer_sd_not_exist);
        }
    }

    private void openMention() {
        Intent intent = new Intent();
        intent.setClass(context, MembersActivity.class);
        intent.putExtra("title", context.getString(R.string.friend_list));
        intent.putExtra("cid", cid);
        ((Activity) context).overridePendingTransition(
                R.anim.activity_open, 0);

        ((Activity) context).startActivityForResult(intent,
                MENTIONS_RESULT);

    }

    public void setChatInputMenuListener(
            ChatInputMenuListener chatInputMenuListener) {
        this.chatInputMenuListener = chatInputMenuListener;
    }

    public interface ChatInputMenuListener {
        void onSetContentViewHeight(boolean isLock);

        void onSendMsg(String content, List<String> mentionsUidList,
                       List<String> mentionsUserNameList);

    }

    ;

    public void setMentionData(Intent data) {
        String result = data.getStringExtra("searchResult");
        PreferencesUtils.putString(context, cid, "");
        ChannelMentions.addMentions(result, mentionsUserNameList,
                mentionsUidList, inputEdit, mentionPosition);
    }

    public void setIsChannelGroup(boolean isChannelGroup, String cid) {
        this.cid = cid;
        this.isChannelGroup = isChannelGroup;
    }

    public boolean hideAddMenuLayout() {
        if (addMenuLayout.getVisibility() != View.GONE) {
            addMenuLayout.setVisibility(View.GONE);
            return true;
        }
        return false;

    }

    /**
     * 根据二进制字符串更新菜单视图
     *
     * @param binaryString
     */
    public void updateMenuGrid(String binaryString) {
        int[] imgArray = {R.drawable.ic_chat_input_add_gallery, R.drawable.ic_chat_input_add_camera, R.drawable.ic_chat_input_add_file, R.drawable.ic_chat_input_add_mention};
        String[] functionNameArray = {context.getString(R.string.album), context.getString(R.string.take_photo), context.getString(R.string.file), "@"};
        imgList.clear();
        List<String> textList = new ArrayList<>();
        int menuGridSize = binaryString.length() - 1;
        if (binaryString.length() > imgArray.length) {
            menuGridSize = imgArray.length - 2;
        }
        if (binaryString.equals("-1")) {
            menuGridSize = imgArray.length - 2;
            binaryString = "111";
        }
        for (int i = menuGridSize; i >= 0; i--) {
            if ((binaryString.charAt(i) + "").equals("1")) {
                imgList.add(imgArray[menuGridSize - i]);
                textList.add(functionNameArray[menuGridSize - i]);
            }
        }
        //如果是群组的话添加@功能
        if (isChannelGroup) {
            imgList.add(imgArray[3]);
            textList.add(functionNameArray[3]);
        }
        msgInputAddItemAdapter.updateGridView(imgList, textList);
    }

    class TextChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            String content = s.toString();
            boolean isContentBlank = StringUtils.isEmpty(content);
            sendMsgBtn.setEnabled(!isContentBlank);
            sendMsgBtn.setBackgroundResource(isContentBlank ? R.drawable.bg_chat_input_send_btn_disable : R.drawable.bg_chat_input_send_btn_enable);


            LogUtils.jasonDebug("s=" + s.toString());
            LogUtils.jasonDebug("start=" + start);
            LogUtils.jasonDebug("before=" + before);
            LogUtils.jasonDebug("count=" + count);

            if (isChannelGroup && count == 1) {
                mentionPosition = start;
                String inputWord = s.toString().substring(start, start + count);
                if (inputWord.equals("@")) {
                    openMention();
                }
            }




//                ForeColorSpan[] spans = ((Spanned) s).getSpans(0, s.length(),
//                        ForeColorSpan.class);
//                int which = -1;
//                for (int i = 0; i < mentionsUserNameList.size(); i++) {
//                    if (!s.toString().contains(mentionsUserNameList.get(i))) {
//                        which = i;
//                        mentionsUserNameList.remove(i);
//                        mentionsUidList.remove(i);
//                        i--;
//                    }
//                }
//                int spanslen = spans.length;
//                for (int i = 0; i < spanslen; i++) {
//                    if (which == i) {
//                        int started = ((Spannable) s).getSpanStart(spans[i]);
//                        int end = ((Spannable) s).getSpanEnd(spans[i]);
//                        inputEdit.getText().delete(started, end);
//                    }
//
//                }


//                int inputContentLength = s.toString().length();
//                if ((content.substring(inputContentLength - 1, inputContentLength).equals("@") || changeContent
//                        .equals("@"))) {
//                    openMention();
//
//                }
//                canMention = true;
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

}
