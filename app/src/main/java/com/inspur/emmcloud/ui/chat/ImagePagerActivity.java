package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetMessageCommentCountResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.MessageSendManager;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenuImgComment;
import com.inspur.emmcloud.widget.HackyViewPager;
import com.inspur.emmcloud.widget.SoftKeyboardStateHelper;

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
@Route(path = Constant.AROUTER_CLASS_COMMUNICATION_IMAGEPAGER)
public class ImagePagerActivity extends BaseFragmentActivity {
    public static final String EXTRA_IMAGE_INDEX = "image_index";
    public static final String EXTRA_IMAGE_URLS = "image_urls";
    public static final String EXTRA_CURRENT_IMAGE_MSG = "channel_current_image_msg";
    public static final String EXTRA_IMAGE_MSG_LIST = "channel_image_msg_list";
    public static final String PHOTO_SELECT_X_TAG = "PHOTO_SELECT_X_TAG";
    public static final String PHOTO_SELECT_Y_TAG = "PHOTO_SELECT_Y_TAG";
    public static final String PHOTO_SELECT_W_TAG = "PHOTO_SELECT_W_TAG";
    public static final String PHOTO_SELECT_H_TAG = "PHOTO_SELECT_H_TAG";
    private static final int RESULT_MENTIONS = 5;
    private static final int CHECK_IMG_COMMENT = 1;
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
    private RelativeLayout functionLayout;
    private TextView commentCountText;
    private TextView originalPictureDownLoadTextView;
    private Map<String, Integer> commentCountMap = new ArrayMap<>();
    private Boolean isNeedTransformIn;
    private boolean isHasTransformIn = false;
    private View mainView;
    private LinearLayout inputLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setNavigationBarColor(android.R.color.black);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //全屏显示
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
        setContentView(R.layout.activity_image_pager);
        EventBus.getDefault().register(this);
        init();
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
        functionLayout = (RelativeLayout) findViewById(R.id.function_layout);
        if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG)) {
            (findViewById(R.id.comment_count_text)).setVisibility(View.VISIBLE);
            (findViewById(R.id.enter_channel_imgs_img)).setVisibility(View.VISIBLE);
            (findViewById(R.id.write_comment_layout)).setVisibility(View.VISIBLE);
            cid = imgTypeMessageList.get(0).getChannel();
            commentCountText = (TextView) findViewById(R.id.comment_count_text);
        }

        initEcmChatInputMenu();
        mPager = (HackyViewPager) findViewById(R.id.pager);
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urlList);
        mPager.setAdapter(mAdapter);
        initIndicatorView();
        pagerPosition = pageStartPosition;
        mPager.setCurrentItem(pagerPosition);
        //解决当只有一张图片无法显示评论数的问题
        if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG) && pagerPosition == 0) {
            setCommentCount();
        }

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
                IntentUtils.startActivity(ImagePagerActivity.this,
                        GroupAlbumActivity.class, bundle);
                break;
            case R.id.comment_count_text:
                bundle = new Bundle();
                bundle.putString("mid", imgTypeMessageList.get(pagerPosition).getId());
                bundle.putString("cid", imgTypeMessageList.get(pagerPosition).getChannel());
                bundle.putString("from", "imagePager");
                //能看到的消息应在上一层控制都是发送成功的消息
                Intent intent = new Intent(ImagePagerActivity.this, ChannelMessageDetailActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, CHECK_IMG_COMMENT);
                break;
            case R.id.write_comment_layout:
                inputLayout.setVisibility(View.VISIBLE);
                ecmChatInputMenu.setAddMenuLayoutShow(true);
                InputMethodUtils.display(this, ecmChatInputMenu.getChatInputEdit());
                break;
            case R.id.tv_original_picture_download_progress:
                LogUtils.LbcDebug("下载图片");
                if (imgTypeMessageList.size() > 0) {
                    originalPictureDownLoadTextView.setText("0%");
                    ImageDetailFragment imageDetailFragment = mAdapter.getCurrentFragment();
                    imageDetailFragment.loadingImage(downLoadProgressRefreshListener);
                }
                break;
            case R.id.rl_blank:     //点击空白
                inputLayout.setVisibility(View.GONE);
                if (ecmChatInputMenu != null) {
                    ecmChatInputMenu.showSoftInput(false);
                    ecmChatInputMenu.getChatInputEdit().setText("");
                    ecmChatInputMenu.getChatInputEdit().clearInsertModelList();
                    ecmChatInputMenu.setAddMenuLayoutShow(true);
                }
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
        inputLayout = findViewById(R.id.ll_input);
        ecmChatInputMenu = findViewById(R.id.chat_input_menu);
        ecmChatInputMenu.setAddMenuLayoutShow(true);
//        ecmChatInputMenu.showSoftInput(true);
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
                inputLayout.setVisibility(View.GONE);
                ecmChatInputMenu.showSoftInput(false);
            }

            @Override
            public void hideChatInputMenu() {
                inputLayout.setVisibility(View.GONE);
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
        final TextView indicator = (TextView) findViewById(R.id.indicator);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                CharSequence text = getString(R.string.meeting_viewpager_indicator, position + 1, mPager.getAdapter().getCount());
                indicator.setText(text);
                pagerPosition = position;
                LogUtils.LbcDebug("选择图片位置::" + pagerPosition);
                setOriginalImageButtonShow(position);
                if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG)) {
                    setCommentCount();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 设置评论数目
     */
    private void setCommentCount() {
        String mid = imgTypeMessageList.get(pagerPosition).getId();
        if (commentCountMap.containsKey(mid)) {
            int count = commentCountMap.get(mid);
            commentCountText.setText("" + count);
        } else {
            commentCountText.setText("0");
            getImgCommentCount(mid);
        }
    }


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
    }


    public void setPhotoTap() {
        if (functionLayout.getVisibility() == View.VISIBLE) {
            functionLayout.setVisibility(View.GONE);
        } else {
            functionLayout.setVisibility(View.VISIBLE);
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
            commentCountText.setText(commentCount + "");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void getImgCommentCount(String mid) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            WSAPIService.getInstance().getMessageCommentCount(mid);
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
            commentCountText.setText((commentCount + 1) + "");
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
                        commentCountText.setText(number + "");
                    }
                }
            }
        }
    }

    //当包含的fragment发来OnPhotoTap信号
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotoTab(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_ON_PHOTO_TAB)) {
            setPhotoTap();
        } else if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_ON_PHOTO_CLOSE)) {
            if (functionLayout.getVisibility() == View.VISIBLE) {
                functionLayout.setVisibility(View.GONE);
            }
        }

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

}
