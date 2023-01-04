package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.collection.ArrayMap;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.bean.ImageOperateMoreEvent;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.basemodule.util.dialog.ShareDialogForDark;
import com.inspur.emmcloud.bean.chat.GetMessageCommentCountResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MessageForwardMultiBean;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.OnCreateDirectConversationListener;
import com.inspur.emmcloud.componentservice.communication.RecentTransmitModel;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.ui.chat.mvp.view.ConversationSendMultiActivity;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.MessageSendManager;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.widget.ECMChatInputMenuImgComment;
import com.inspur.emmcloud.widget.HackyViewPager;
import com.inspur.emmcloud.widget.SoftKeyboardStateHelper;
import com.inspur.emmcloud.widget.dialog.MessageMultiBottomDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 图片查看器
 */
@Route(path = Constant.AROUTER_CLASS_COMMUNICATION_IMAGEPAGER_NEW)
public class ImagePagerNewActivity extends BaseFragmentActivity {
    public static final String EXTRA_IMAGE_INDEX = "image_index";
    public static final String EXTRA_IMAGE_URLS = "image_urls";
    public static final String EXTRA_CURRENT_IMAGE_MSG = "channel_current_image_msg";
    public static final String EXTRA_IMAGE_MSG_LIST = "channel_image_msg_list";
    public static final String PHOTO_SELECT_X_TAG = "PHOTO_SELECT_X_TAG";
    public static final String PHOTO_SELECT_Y_TAG = "PHOTO_SELECT_Y_TAG";
    public static final String PHOTO_SELECT_W_TAG = "PHOTO_SELECT_W_TAG";
    public static final String PHOTO_SELECT_H_TAG = "PHOTO_SELECT_H_TAG";
    public static final String PHOTO_SHOW_MORE = "PHOTO_SHOW_MORE";
    public static final String EXTRA_NEED_SHOW_COMMENT = "EXTRA_NEED_SHOW_COMMENT";
    private static final int SHARE_MULTI_REQUEST_CODE = 33;
    private static final int RESULT_MENTIONS = 5;
    private static final int CHECK_IMG_COMMENT = 1;
    public static final String EXTRA_CHANNEL_ID = "channelId";
    ImageDetailFragment.DownLoadProgressRefreshListener downLoadProgressRefreshListener;
    private ECMChatInputMenuImgComment ecmChatInputMenu;
    private HackyViewPager mPager;
    private int pagerPosition;
    private int pageStartPosition = 0;
    private List<Message> imgTypeMessageList = new ArrayList<>();
    private ArrayList<String> urlList = new ArrayList<>();
    private String cid;
    private int locationX, locationY, locationW, locationH;
    private ImagePagerAdapter mAdapter;
    //    private RelativeLayout functionLayout;
    //    private LinearLayout commentCountll;
    private LinearLayout operationLl; // 底部控制按钮layout
    private TextView originalPictureDownLoadTextView;
    private Map<String, Integer> commentCountMap = new ArrayMap<>();
    private Boolean isNeedTransformIn;
    private boolean isHasTransformIn = false;
    private View mainView;
    private boolean showMore; // 显示"···"更多按钮
    //    private LinearLayout inputLayout;

    private Handler mHandler = new Handler();
    // 隐藏control view，仿微信播放器
    protected Runnable mHideViewRunnable = new Runnable() {
        @Override
        public void run() {
            hideOperateLl();
        }
    };
    private Message currentMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        // 强制隐藏状态栏，防止出现白色状态栏
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).init();
        super.onRestart();
    }

    @Override
    public void onCreate() {
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).init();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setNavigationBarColor(android.R.color.black);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //全屏显示
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
        setContentView(R.layout.activity_image_pager_new);
        init();
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void setTheme() {
        //不使用Base中的主题，使用自定义主题
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        getIntent().putExtras(intent);
        isHasTransformIn = false;
        init();
    }

    private void init() {
        mainView = findViewById(R.id.main_layout);
        mainView.setBackgroundColor(Color.parseColor("#000000"));
        initIntentData();
        originalPictureDownLoadTextView = findViewById(R.id.tv_original_picture_download_progress);
        operationLl = (LinearLayout) findViewById(R.id.ll_image_operation);
        if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG)) {
            (findViewById(R.id.enter_channel_imgs_img)).setVisibility(getIntent().getBooleanExtra(EXTRA_NEED_SHOW_COMMENT, true) ? View.VISIBLE : View.GONE);
            cid = imgTypeMessageList.get(0).getChannel();
        }

        initEcmChatInputMenu();
        mPager = (HackyViewPager) findViewById(R.id.pager);
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urlList);
        mPager.setAdapter(mAdapter);
        initIndicatorView();
        pagerPosition = pageStartPosition;
        mPager.setCurrentItem(pagerPosition);
        //解决当只有一张图片无法显示评论数的问题
//        if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG) && pagerPosition == 0) {
//            setCommentCount();
//        }

        downLoadProgressRefreshListener = new ImageDetailFragment.DownLoadProgressRefreshListener() {
            @Override
            public void refreshProgress(String url, int progress) {
                if (imgTypeMessageList.size() > 0 && urlList.get(pagerPosition).equals(url)) {
                    originalPictureDownLoadTextView.setVisibility(View.VISIBLE);
                    originalPictureDownLoadTextView.setText(progress + "%");
                }

            }

            @Override
            public void loadingComplete(String url) {
                if (imgTypeMessageList.size() > 0 && urlList.get(pagerPosition).equals(url)) {
                    originalPictureDownLoadTextView.setText(R.string.download_success);
                    originalPictureDownLoadTextView.setVisibility(View.GONE);
                }
            }
        };
        if (pageStartPosition == 0) {
            setOriginalImageButtonShow(0);
        }
        mHandler.postDelayed(mHideViewRunnable, 7000);
    }

    // 隐藏底部控制布局
    private void hideOperateLl() {
        operationLl.setVisibility(View.GONE);
    }

    // 显示底部控制布局
    private void showOperateLl() {
        operationLl.setVisibility(View.VISIBLE);
        if (mHideViewRunnable != null) {
            mHandler.removeCallbacks(mHideViewRunnable);
            mHandler.postDelayed(mHideViewRunnable, 7000);
        }
    }

    @Override
    public void onBackPressed() {
        mAdapter.getCurrentFragment().closeImg();
    }

    public void onClick(View v) {
        Bundle bundle = null;
        switch (v.getId()) {
            case R.id.close_img:
                mAdapter.getCurrentFragment().closeImg();
                break;
            case R.id.download_img:
                originalPictureDownLoadTextView.setText("0%");
                mAdapter.getCurrentFragment().downloadImg(downLoadProgressRefreshListener);
                break;
            case R.id.enter_channel_imgs_img:
                bundle = new Bundle();
                bundle.putString("cid", cid);
                IntentUtils.startActivity(ImagePagerNewActivity.this,
                        GroupAlbumActivity.class, bundle);
                break;
            case R.id.tv_original_picture_download_progress:
                LogUtils.LbcDebug("下载图片");
                if (imgTypeMessageList.size() > 0) {
                    originalPictureDownLoadTextView.setText("0%");
                    ImageDetailFragment imageDetailFragment = mAdapter.getCurrentFragment();
                    imageDetailFragment.loadingImage(downLoadProgressRefreshListener);
                }
                break;
            case R.id.iv_image_more:     //更多，弹出
                showOperateDialog();
                break;
            default:
                break;
        }
    }

    /**
     * 显示评论输入框
     */
    private void showCommentInputDlg() {
//        View view = getLayoutInflater().inflate(R.layout.communication_dialog_chat_img_input, null);
    }

    /**
     * 初始化评论输入框
     */
    private void initEcmChatInputMenu() {
        ecmChatInputMenu = findViewById(R.id.chat_input_menu);
        ecmChatInputMenu.setAddMenuLayoutShow(true);
        String conversationType = ConversationCacheUtils.getConversationType(getApplicationContext(), cid);
        if (conversationType != null && conversationType.equals(Conversation.TYPE_GROUP)) {
            ecmChatInputMenu.setCanMentions(true, cid);
        } else {
            ecmChatInputMenu.setCanMentions(false, cid);
        }
        ecmChatInputMenu.setChatInputMenuListener(new ECMChatInputMenuImgComment.ChatInputMenuListener() {

            @Override
            public void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap) {
                sendComment(content, mentionsMap);
//                inputLayout.setVisibility(View.GONE);
                ecmChatInputMenu.showSoftInput(false);
            }

            @Override
            public void hideChatInputMenu() {
//                inputLayout.setVisibility(View.GONE);
                ecmChatInputMenu.showSoftInput(false);
            }
        });
        final SoftKeyboardStateHelper softKeyboardStateHelper = new SoftKeyboardStateHelper(findViewById(R.id.main_layout));
        softKeyboardStateHelper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                LogUtils.jasonDebug("onSoftKeyboardOpened===");
                ecmChatInputMenu.setAddMenuLayoutShow(true);
            }

            @Override
            public void onSoftKeyboardClosed() {
                ecmChatInputMenu.onSoftKeyboardClosed();
                LogUtils.jasonDebug("onSoftKeyboardClosed===");
//                ecmChatInputMenu.setAddMenuLayoutShow(false);
            }
        });
    }

    /**
     * 初始化Indicator
     */
    private void initIndicatorView() {
//        final TextView indicator = (TextView) findViewById(R.id.indicator);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                CharSequence text = getString(R.string.meeting_viewpager_indicator, position + 1, mPager.getAdapter().getCount());
//                indicator.setText(text);
                pagerPosition = position;
                setOriginalImageButtonShow(position);
                showOperateLl();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

//    /**
//     * 设置评论数目
//     */
//    private void setCommentCount() {
//        String mid = imgTypeMessageList.get(pagerPosition).getId();
//        if (commentCountMap.containsKey(mid)) {
//            int count = commentCountMap.get(mid);
//            commentCountText.setText("" + count);
//        } else {
//            commentCountText.setText("0");
//            getImgCommentCount(mid);
//        }
//    }


    /**
     * 初始化Intent传递的数据
     */
    private void initIntentData() {

        if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG)) {
            LogUtils.LbcDebug("Image Message 列表 List");
            Message currentMsg = (Message) getIntent().getSerializableExtra(EXTRA_CURRENT_IMAGE_MSG);
            urlList = new ArrayList<>();
            cid = currentMsg.getChannel();
            imgTypeMessageList = (List<Message>) getIntent().getSerializableExtra(EXTRA_IMAGE_MSG_LIST);
            for (Message message : imgTypeMessageList) {
                /**改成preview*/
                String path = message.getMsgContentMediaImage().getRawMedia();
                String url = APIUri.getChatFileResouceUrl(message.getChannel(), path);
                urlList.add(url);
            }
            pageStartPosition = imgTypeMessageList.indexOf(currentMsg);
            LogUtils.LbcDebug("初始化位置选择" + pageStartPosition);
        } else {
            LogUtils.LbcDebug("直接输出Urls");
            urlList = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
            pageStartPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
        }
        locationX = getIntent().getIntExtra(PHOTO_SELECT_X_TAG, 0);
        locationY = getIntent().getIntExtra(PHOTO_SELECT_Y_TAG, 0);
        locationW = getIntent().getIntExtra(PHOTO_SELECT_W_TAG, 0);
        locationH = getIntent().getIntExtra(PHOTO_SELECT_H_TAG, 0);
        // 查看页是否显示"···"更多按钮
        showMore = getIntent().getBooleanExtra(PHOTO_SHOW_MORE, false);
        ImageView imageMoreIv = findViewById(R.id.iv_image_more);
        if (showMore) {
            imageMoreIv.setVisibility(View.VISIBLE);
        } else {
            imageMoreIv.setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED && requestCode == RESULT_MENTIONS) {
            String result = data.getStringExtra("searchResult");
            JSONArray jsonArray = JSONUtils.getJSONArray(result, new JSONArray());
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    String uid = JSONUtils.getString(jsonArray.getString(i), "uid", null);
                    String name = JSONUtils.getString(jsonArray.getString(i), "name", null);
                    boolean isInputKeyWord = data.getBooleanExtra("isInputKeyWord", false);
                    ecmChatInputMenu.addMentions(uid, name, isInputKeyWord);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == CHECK_IMG_COMMENT) {
            String mid = data.getStringExtra("mid");
            int commentCount = data.getIntExtra("commentCount", 0);
            commentCountMap.put(mid, commentCount);
//            commentCountText.setText(commentCount + "");
        } else if (resultCode == RESULT_OK && requestCode == SHARE_MULTI_REQUEST_CODE) {
            // 转发
            SearchModel searchModel = (SearchModel) data.getSerializableExtra("searchModel");
            if (searchModel != null) {
                String userOrChannelId = searchModel.getId();
                boolean isUser = searchModel.getType().equals(SearchModel.TYPE_USER);
                share2Conversation(userOrChannelId, isUser);
                return;
            }
            // 多选时消息转发多人
            List<MessageForwardMultiBean> selectList = (List<MessageForwardMultiBean>) data.getSerializableExtra("selectList");
            if (selectList != null) {
                for (int i = 0; i < selectList.size(); i++) {
                    MessageForwardMultiBean bean = selectList.get(i);
                    boolean isUser = bean.getType().equals(SearchModel.TYPE_USER);
                    if (isUser) {
                        share2Conversation(bean.getContactId(), true);
                    } else {
                        share2Conversation(bean.getConversationId(), false);
                    }
                }
            }
            // 多人消息转发后，communicationFragment列表可能更新不全，原因暂未查明。先发送event刷新list解决此问题
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_MULTI_MESSAGE_SEND, ""));

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void getImgCommentCount(String mid) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            WSAPIService.getInstance().getMessageCommentCount(mid, getIntent().hasExtra(EXTRA_CHANNEL_ID) ? getIntent().getStringExtra(EXTRA_CHANNEL_ID) : "");
        }
    }

    private void sendComment(String content, Map<String, String> mentionsMap) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            Message message = CommunicationUtils.combinLocalCommentTextPlainMessage(cid, imgTypeMessageList.get(pagerPosition).getId(), content, mentionsMap);
            MessageSendManager.getInstance().sendMessage(message);
            Integer commentCount = commentCountMap.get(imgTypeMessageList.get(pagerPosition).getId());
            if (commentCount == null) {
                commentCount = 0;
            }
            commentCountMap.put(imgTypeMessageList.get(pagerPosition).getId(), (commentCount + 1));
//            commentCountText.setText((commentCount + 1) + "");
        }
    }

    //接收到websocket发过来的获取评论消息数
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMessageCommemtCount(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT_COUNT)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                GetMessageCommentCountResult getMessageCommentCountResult = new GetMessageCommentCountResult(content);
                int number = getMessageCommentCountResult.getNumber();
                String mid = String.valueOf(eventMessage.getExtra());
                if (mid != null) {
                    commentCountMap.put(mid, number);
                    String currentMid = imgTypeMessageList.get(pagerPosition).getId();
                    if (mid.equals(currentMid)) {
//                        commentCountText.setText(number + "");
                    }
                }
            }
        }
    }

    //当包含的fragment发来OnPhotoTap信号
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotoTab(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_ON_PHOTO_TAB)) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    //当包含的fragment发来OnPhotoTap信号
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotoLong(ImageOperateMoreEvent event) {
        if (showMore && event.getEvent().equals(Constant.EVENTBUS_TAG_ON_PHOTO_LONG)) {
            showOperateDialog();
        }
    }

    // 更多操作dialog
    private void showOperateDialog() {
        // 获取最近转发
        final ArrayList<Conversation> recentList = (ArrayList<Conversation>) ConversationCacheUtils.getRecentTransmitIdList(this);
        final ArrayList<String> functionList = new ArrayList<>();
        functionList.add("transmit");
        functionList.add("download");
        functionList.add("location");
        MessageMultiBottomDialog.MessageMultiOperateBuilder builder =
                new MessageMultiBottomDialog.MessageMultiOperateBuilder(this)
                        .setFunctionList(functionList);
        if (recentList.size() > 0) {
            builder.setTransmitList(recentList);
        }
        builder.setOnItemClickListener(new MessageMultiBottomDialog.MessageMultiOperateBuilder.OnItemClickListener() {
            @Override
            public void onClick(MessageMultiBottomDialog dialog, View itemView, int position) {
                String functionName = functionList.get(position);
                if ("transmit".equals(functionName)) {
                    // 转发
                    currentMessage = imgTypeMessageList.get(pagerPosition);
                    String result = getString(R.string.baselib_share_image) + " " + currentMessage.getMsgContentMediaImage().getName();
                    Intent shareIntent = new Intent(ImagePagerNewActivity.this, ConversationSendMultiActivity.class);
                    shareIntent.putExtra(Constant.SHARE_CONTENT, result);
                    startActivityForResult(shareIntent, SHARE_MULTI_REQUEST_CODE);
                    dialog.dismiss();
                } else if ("download".equals(functionName)) {
                    // 下载
                    originalPictureDownLoadTextView.setText("0%");
                    mAdapter.getCurrentFragment().downloadImg(downLoadProgressRefreshListener);
                    dialog.dismiss();
                } else if ("location".equals(functionName)) {
                    currentMessage = imgTypeMessageList.get(pagerPosition);
                    // 定位到聊天
                    Bundle bundle = new Bundle();
                    bundle.putString(ConversationActivity.EXTRA_CID, cid);
                    bundle.putSerializable(ConversationActivity.EXTRA_POSITION_MESSAGE, new UIMessage(currentMessage));
                    IntentUtils.startActivity(ImagePagerNewActivity.this, ConversationActivity.class, bundle, true);
                    dialog.dismiss();
                }
            }

            @Override
            public void onTransmitClick(MessageMultiBottomDialog dialog, View itemView, int position) {
                Conversation conversation = recentList.get(position);
                showTransmitDialog(conversation);
                dialog.dismiss();
            }
        }).build().show();
        showOperateLl();
    }

    private void showTransmitDialog(Conversation conversation) {
        currentMessage = imgTypeMessageList.get(pagerPosition);
        final SearchModel searchModel = conversation.conversation2SearchModel();
        String shareContent = getString(R.string.baselib_share_image) + " " + currentMessage.getMsgContentMediaImage().getName();
        int defaultIcon = CommunicationUtils.getDefaultHeadUrl(this, searchModel);
        String headUrl = CommunicationUtils.getHeadUrl(searchModel);
        //分享到
        ShareDialogForDark.Builder builder = new ShareDialogForDark.Builder(ImagePagerNewActivity.this);
        builder.setUserName(searchModel.getName());
        builder.setContent(shareContent);
        builder.setDefaultResId(defaultIcon);
        builder.setHeadUrl(headUrl);
        final ShareDialogForDark dialog = builder.build();
        dialog.setCallBack(new ShareDialogForDark.CallBack() {
            @Override
            public void onConfirm(View view) {
                if (searchModel.getType().equals(SearchModel.TYPE_USER) && searchModel.getId().equals(BaseApplication.getInstance().getUid())) {
                    ToastUtils.show(R.string.do_not_select_yourself);
                } else {
                    dialog.dismiss();
                    share2Conversation(searchModel.getId(), false);
                }
            }

            @Override
            public void onCancel() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private boolean isShowOriginalImageButton(int position) {
        String url = urlList.get(position); //raw 路径
        if (imgTypeMessageList.size() > 0) {
            MsgContentMediaImage msgContentMediaImage = imgTypeMessageList.get(position).getMsgContentMediaImage();
            boolean isHaveOriginalImageCatch = ImageDisplayUtils.getInstance().isHaveCacheImage(url);//这个是判断有无原图（是否有）
            if (msgContentMediaImage.getPreviewHeight() != 0
                    && ((msgContentMediaImage.getRawHeight() != msgContentMediaImage.getPreviewHeight()) || (msgContentMediaImage.getRawWidth() != msgContentMediaImage.getPreviewWidth()))
                    && !isHaveOriginalImageCatch) {
                return true;
            }
        }
        return false;
    }

    private void setOriginalImageButtonShow(int position) {
        LogUtils.LbcDebug("imgTypeMessageList.size()" + imgTypeMessageList.size());
        if (imgTypeMessageList.size() > 0) {
            originalPictureDownLoadTextView.setVisibility(isShowOriginalImageButton(position) ? View.VISIBLE : View.GONE);
            long rawImageSize = imgTypeMessageList.get(position).getMsgContentMediaImage().getRawSize();
            originalPictureDownLoadTextView.setText(BaseApplication.getInstance().getString(R.string.chat_full_image) + "(" + FileUtils.formatFileSize(rawImageSize) + ")");
        }
    }

    private void share2Conversation(String userOrChannelId, boolean isUser) {
        if (StringUtils.isBlank(userOrChannelId)) {
            ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
        } else {
            if (isUser) {
                createDirectChannel(userOrChannelId, currentMessage);
            } else {
                transmitMsg(userOrChannelId, currentMessage);
            }
        }
    }

    /**
     * 创建单聊
     */
    private void createDirectChannel(String uid, final Message uiMessage) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(this, uid,
                    new OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            transmitMsg(conversation.getId(), uiMessage);
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        }
    }

    /**
     * 转发消息
     */
    private void transmitMsg(String cid, Message sendMessage) {
        String path = null;
        MsgContentMediaImage msgContentMediaImage = sendMessage.getMsgContentMediaImage();
        path = msgContentMediaImage.getRawMedia();
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            ChatAPIService apiService = new ChatAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.transmitFile(path, sendMessage.getChannel(), cid, "image", sendMessage);
        }
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public List<String> urlList;
        private ImageDetailFragment currentFragment;

        public ImagePagerAdapter(FragmentManager fm, List<String> urlList) {
            super(fm);
            this.urlList = urlList;
        }

        @Override
        public int getCount() {
            return urlList == null ? 0 : urlList.size();
        }

        @Override
        public Fragment getItem(int position) {
            String url = urlList.get(position); //raw 路径
            LogUtils.LbcDebug("url ::" + url);
            boolean isNeedTransformOut = (position == pageStartPosition);
            if (isNeedTransformOut && isHasTransformIn == false) {
                isNeedTransformIn = true;
                isHasTransformIn = true;
            } else {
                isNeedTransformIn = false;
            }

            MsgContentMediaImage msgContentMediaImage = (imgTypeMessageList.size() > 0) ?
                    imgTypeMessageList.get(position).getMsgContentMediaImage() : null;
            int rawHigh = msgContentMediaImage != null ? msgContentMediaImage.getRawHeight() : 0;
            int rawWidth = msgContentMediaImage != null ? msgContentMediaImage.getRawWidth() : 0;
            int preViewH = msgContentMediaImage != null ? msgContentMediaImage.getPreviewHeight() : 0;
            int preViewW = msgContentMediaImage != null ? msgContentMediaImage.getPreviewWidth() : 0;
            String imageName = msgContentMediaImage != null ? msgContentMediaImage.getName() : "";
            LogUtils.LbcDebug("rawH" + rawHigh + "rawW" + rawWidth + "preH" + preViewH + "preW" + preViewW + "name" + imageName);
            return ImageDetailFragment.newInstance(url, locationW, locationH, locationX, locationY, isNeedTransformIn,
                    isNeedTransformOut, preViewH, preViewW, rawHigh, rawWidth, imageName);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            currentFragment = (ImageDetailFragment) object;
            super.setPrimaryItem(container, position, object);
        }

        public ImageDetailFragment getCurrentFragment() {
            return currentFragment;
        }

    }

    /**
     * 转发到其他频道时调用此方法，将发送中状态的消息保存到本地数据库并发送此消息
     * 转发到其他频道时不自动跳转到此频道
     *
     * @param fakeMessage
     */
    private void sendTransmitMsg(Message fakeMessage) {
        if (fakeMessage != null) {
            fakeMessage.setSendStatus(Message.MESSAGE_SEND_ING);
            MessageCacheUtil.saveMessage(ImagePagerNewActivity.this, fakeMessage);
            MessageSendManager.getInstance().sendMessage(fakeMessage);
            ToastUtils.show(R.string.chat_message_send_success);
            // 保存最近转发到数据库
            ConversationCacheUtils.saveRecentTransmitConversation(this, new RecentTransmitModel
                    (fakeMessage.getChannel(), "", "", "", System.currentTimeMillis()));
        } else {
            ToastUtils.show(R.string.chat_message_send_fail);
        }

    }

    // 图片转发回调
    class WebService extends APIInterfaceInstance {
        @Override
        public void returnTransmitPictureSuccess(String cid, String description, Message message) {
            String path = JSONUtils.getString(description, "path", "");
            Message fakeMessage = null;
            switch (message.getType()) {
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    fakeMessage = CommunicationUtils.combineTransmitMediaImageMessage(cid, path, message.getMsgContentMediaImage());
                    break;
                case Message.MESSAGE_TYPE_MEDIA_VIDEO:
                    fakeMessage = CommunicationUtils.combineLocalVideoMessageHaveContent(cid, path, message.getMsgContentMediaVideo());
            }
            sendTransmitMsg(fakeMessage);
        }

        @Override
        public void returnTransmitPictureError(String error, int errorCode) {
            ToastUtils.show(R.string.chat_message_send_fail);
        }
    }

}
