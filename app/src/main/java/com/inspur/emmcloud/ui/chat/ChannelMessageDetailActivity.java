package com.inspur.emmcloud.ui.chat;

import static com.inspur.emmcloud.basemodule.media.record.activity.CommunicationRecordActivity.VIDEO_PATH;
import static com.inspur.emmcloud.basemodule.media.record.activity.CommunicationRecordActivity.VIDEO_THUMBNAIL_PATH;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.ImageViewRound;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.media.player.VideoPlayerActivity;
import com.inspur.emmcloud.basemodule.media.player.basic.PlayerGlobalConfig;
import com.inspur.emmcloud.basemodule.media.selector.demo.GlideEngine;
import com.inspur.emmcloud.basemodule.media.selector.thread.PictureThreadUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.GetMessageCommentResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVideo;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.MessageSendManager;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.LinkMovementClickMethod;
import com.tencent.rtmp.TXLiveConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 消息详情页面
 *
 * @author Administrator
 */
public class ChannelMessageDetailActivity extends BaseActivity implements
        SwipeRefreshLayout.OnRefreshListener {

    private static final int RESULT_MENTIONS = 5;
    private ScrollViewWithListView commentListView;
    private Message message;
    private List<Message> commentList;
    private BaseAdapter commentAdapter;
    private ScrollView commentScrollView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageViewRound senderHeadImg;
    private TextView msgSendTimeText;
    private TextView countTv;
    private TextView senderNameText;
    private ImageView msgContentImg;
    private TextView msgContent;
    private String cid = "";
    private String membersDetail; // 群成员昵称
    private RelativeLayout msgDisplayLayout;
    private LayoutInflater inflater;
    private ECMChatInputMenu chatInputMenu;
    private String mid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate() {
        initView();
        initData();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_channel_message_detail;
    }

    /**
     * 初始化Views
     */
    private void initView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        commentList = new ArrayList<>();
        commentScrollView = (ScrollView) findViewById(R.id.scrollview);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View msgDetailLayout = inflater.inflate(R.layout.design3_msg_parent_detail,
                null);
        senderHeadImg = (ImageViewRound) msgDetailLayout
                .findViewById(R.id.sender_photo_img);
        msgSendTimeText = (TextView) msgDetailLayout
                .findViewById(R.id.msg_send_time_text);
        senderNameText = (TextView) msgDetailLayout
                .findViewById(R.id.sender_name_text);
        commentListView = (ScrollViewWithListView) msgDetailLayout
                .findViewById(R.id.comment_list);
        msgDisplayLayout = (RelativeLayout) msgDetailLayout
                .findViewById(R.id.msg_display_layout);
        countTv = (TextView) msgDetailLayout.findViewById(R.id.tv_count);
        commentScrollView.addView(msgDetailLayout);
        initChatInputMenu();
    }

    private void initChatInputMenu() {
        chatInputMenu = (ECMChatInputMenu) findViewById(R.id.chat_input_menu);
        chatInputMenu.setOtherLayoutView(swipeRefreshLayout, commentListView);
        cid = getIntent().getExtras().getString("cid");
        membersDetail = getIntent().getExtras().getString("membersDetail");
        String channelType = ConversationCacheUtils.getConversationType(MyApplication.getInstance(),
                cid);
        if (channelType.equals("GROUP")) {
            chatInputMenu.setIsGroup(true, cid);
        }
        chatInputMenu.hideAddMenuLayout();
        chatInputMenu.setChatInputMenuListener(new ECMChatInputMenu.ChatInputMenuListener() {
            @Override
            public void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap) {
                // TODO Auto-generated method stub
                sendComment(content, mentionsMap);
            }

            @Override
            public void onSendReplyMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap, String mid) {

            }

            @Override
            public void onSendVoiceRecordMsg(String results, float seconds, String filePath) {

            }

            @Override
            public void onVoiceCommucaiton() {

            }

            @Override
            public void onVideoCommucaiton() {

            }

            @Override
            public void onChatDraftsClear() {

            }

            @Override
            public void onNoSmallWindowPermission() {

            }
        });
        chatInputMenu.setInputLayout("1", true);
    }


    /**
     * 初始化数据源
     */
    private void initData() {
        mid = getIntent().getStringExtra("mid");
        message = MessageCacheUtil.getMessageByMid(MyApplication.getInstance(), mid);
        if (message != null) {
            handMsgData();
        } else {
            WSAPIService.getInstance().getMessageById(mid);
        }
    }

    /**
     * 处理数据
     */
    private void handMsgData() {
        cid = message.getChannel();
        getComment();
        displayMsgDetail();
    }

    /**
     * 展示消息详情
     */
    private void displayMsgDetail() {
        disPlayCommonInfo();
        View msgDisplayView = null;
        if (message.getType().equals("media/image")) {
            msgDisplayView = inflater.inflate(R.layout.msg_common_detail, null);
            msgContentImg = (ImageView) msgDisplayView
                    .findViewById(R.id.content_img);
            TextView fileNameText = (TextView) msgDisplayView
                    .findViewById(R.id.comment_filename_text);
            TextView fileSizeText = (TextView) msgDisplayView
                    .findViewById(R.id.comment_filesize_text);
//            String fileName;
//            String fileSize;
//            fileName = msgContentMediaImage.getName();
//            fileSize = FileUtils.formatFileSize(msgContentMediaImage.getRawSize());
            MsgContentMediaImage msgContentMediaImage = message.getMsgContentMediaImage();
            //判断是否有Preview 图片如果有的话用preview ，否则用原图
            int w = msgContentMediaImage.getRawWidth();
            int h = msgContentMediaImage.getRawHeight();
            if (msgContentMediaImage.getPreviewHeight() > 0 && msgContentMediaImage.getPreviewWidth() > 0) {
                h = msgContentMediaImage.getPreviewHeight();
                w = msgContentMediaImage.getPreviewWidth();
            }
            setImgViewSize(this, msgContentImg, w, h);
            String imgPathResult = APIUri.getChatFileResouceUrl(message.getChannel(), msgContentMediaImage.getRawMedia());
            boolean isHaveOriginalImage = ImageDisplayUtils.getInstance().isHaveCacheImage(imgPathResult);
            if (!isHaveOriginalImage) {
                imgPathResult = imgPathResult + "&resize=true&w=" + message.getMsgContentMediaImage().getPreviewWidth() +
                        "&h=" + message.getMsgContentMediaImage().getPreviewHeight();
            }
            final String imgPath = imgPathResult;
            ImageDisplayUtils.getInstance().displayImage(msgContentImg,
                    imgPath, R.drawable.default_image);
            msgContentImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayZoomImage(v, imgPath);
                }
            });
//            fileNameText.setText(fileName);
//            fileSizeText.setText(fileSize);
        } else if (message.getType().equals("media/video")) {
            msgDisplayView = inflater.inflate(R.layout.msg_video_detail, null);
            ImageView videoIv = (ImageView) msgDisplayView
                    .findViewById(R.id.iv_video);
            MsgContentMediaVideo msgContentMediaVideo = message.getMsgContentMediaVideo();
            setImgViewSize(this, videoIv, msgContentMediaVideo.getImageWidth(),
                    msgContentMediaVideo.getImageHeight());
            loadVideoMessage(videoIv);
            videoIv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayerGlobalConfig config = PlayerGlobalConfig.getInstance();
                    config.renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION;
                    Intent intentVideo = new Intent(ChannelMessageDetailActivity.this, VideoPlayerActivity.class);
                    String path = message.getMsgContentMediaVideo().getMedia();
                    String videoPath;
                    videoPath = APIUri.getECMChatUrl() + "/api/v1/channel/" + cid + "/file/request?path=" + StringUtils.encodeURIComponent(path) + "&inlineContent=true";
                    MsgContentMediaVideo msgContentMediaVideo = message.getMsgContentMediaVideo();
                    String imagePath = msgContentMediaVideo.getImagePath();
                    String mediaPath = StringUtils.isEmpty(msgContentMediaVideo.getOriginMediaPath()) ? message.getLocalPath() : msgContentMediaVideo.getOriginMediaPath();
                    intentVideo.putExtra(VIDEO_THUMBNAIL_PATH, !StringUtils.isEmpty(imagePath) && imagePath.startsWith("http") ? imagePath : mediaPath);
                    intentVideo.putExtra(VIDEO_PATH, videoPath);
                    startActivity(intentVideo);
                }
            });
        } else if (message.getType().equals("text/plain")) {
            msgDisplayView = inflater.inflate(R.layout.msg_common_text_detail, null);
            msgContent = (TextView) msgDisplayView
                    .findViewById(R.id.content_text);
            String originText = message.getMsgContentTextPlain().getText();
            String originSpannableString = ChatMsgContentUtils.getMentions(originText, message.getMsgContentTextPlain().getMentionsMap(), JSONUtils.getJSONArray(membersDetail, new JSONArray()));
            Spannable originSpan = EmotionUtil.getInstance(this).getSmiledText(originSpannableString, msgContent.getTextSize());
            msgContent.setText(originSpan);

        } else {
            msgDisplayView = DisplayRegularFileMsg.getView(ChannelMessageDetailActivity.this, message, 1, true);
            msgDisplayView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    MsgContentRegularFile msgContentFile = message.getMsgContentAttachmentFile();
                    String fileDownloadPath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_MESSAGE, message.getId(), msgContentFile.getName());
                    ;
                    if (!StringUtils.isBlank(fileDownloadPath)) {
                        FileUtils.openFile(ChannelMessageDetailActivity.this, fileDownloadPath);
                    } else {
                        Intent intent = new Intent(ChannelMessageDetailActivity.this, ChatFileDownloadActivtiy.class);
                        intent.putExtra("message", message);
                        ChannelMessageDetailActivity.this.startActivity(intent);
                    }
                }
            });
        }
        msgDisplayView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        msgDisplayLayout.addView(msgDisplayView);
    }

    /**
     * 展示可以缩放的Image
     */
    protected void displayZoomImage(View view, String url) {

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        view.invalidate();
        int width = view.getWidth();
        int height = view.getHeight();
        ArrayList<String> urlList = new ArrayList<>();
        urlList.add(url);
        Intent intent = new Intent(getApplicationContext(),
                ImagePagerNewActivity.class);
        intent.putExtra(ImagePagerActivity.PHOTO_SELECT_X_TAG, location[0]);
        intent.putExtra(ImagePagerActivity.PHOTO_SELECT_Y_TAG, location[1]);
        intent.putExtra(ImagePagerActivity.PHOTO_SELECT_W_TAG, width);
        intent.putExtra(ImagePagerActivity.PHOTO_SELECT_H_TAG, height);
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urlList);
        intent.putExtra(ImagePagerActivity.EXTRA_CHANNEL_ID, cid);
        startActivity(intent);
    }

    /**
     * 展示通用的部分
     */
    private void disPlayCommonInfo() {
        String photoUrl = APIUri.getChannelImgUrl(MyApplication.getInstance(), message.getFromUser());
        ImageDisplayUtils.getInstance().displayImage(senderHeadImg, photoUrl, R.drawable.icon_photo_default);
        senderHeadImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserInfo(message.getFromUser());
            }
        });
        String msgSendTime = TimeUtils.getDisplayTime(ChannelMessageDetailActivity.this,
                message.getCreationDate());
        msgSendTimeText.setText(msgSendTime);
        if (!TextUtils.isEmpty(membersDetail)) {
            senderNameText.setText(ChatMsgContentUtils.getUserNicknameOrName(JSONUtils.getJSONArray(membersDetail, new JSONArray()), message.getFromUser()));
        } else {
            senderNameText.setText(ContactUserCacheUtils.getUserName(message.getFromUser()));
        }
    }

    /**
     * 处理@逻辑
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED && requestCode == RESULT_MENTIONS) {
            String result = data.getStringExtra("searchResult");
            JSONArray jsonArray = JSONUtils.getJSONArray(result, new JSONArray());
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    String uid = JSONUtils.getString(jsonArray.getString(i), "uid", null);
                    String name = JSONUtils.getString(jsonArray.getString(i), "name", null);
                    String nickname = JSONUtils.getString(jsonArray.getString(i), "nickname", null);
                    boolean isInputKeyWord = data.getBooleanExtra("isInputKeyWord", false);
                    chatInputMenu.addMentions(uid, name, isInputKeyWord, nickname);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 控件的点击逻辑
     **/
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                onBackPressed();
                break;
            case R.id.iv_location:
                // 定位到聊天
                Bundle bundle = new Bundle();
                bundle.putString(ConversationActivity.EXTRA_CID, cid);
                bundle.putSerializable(ConversationActivity.EXTRA_POSITION_MESSAGE, new UIMessage(message));
                IntentUtils.startActivity(this, ConversationActivity.class, bundle, true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        InputMethodUtils.hide(ChannelMessageDetailActivity.this);
        //将最新的评论数返回给ImagePagerActivity
        if (getIntent().hasExtra("from") && getIntent().getStringExtra("from").equals("imagePager")) {
            Intent intent = new Intent();
            intent.putExtra("mid", message.getId());
            intent.putExtra("commentCount", commentList.size());
            setResult(RESULT_OK, intent);
        }

        if (chatInputMenu.isVoiceInput()) {
            chatInputMenu.stopVoiceInput();
            if (chatInputMenu.isVoiceInputLayoutShow()) {
                chatInputMenu.hideVoiceInputLayout();
            }
            return;
        }
        finish();
    }


    /**
     * 发出评论
     */
    private void sendComment(String text, Map<String, String> mentionsMap) {
        Message message = CommunicationUtils.combinLocalCommentTextPlainMessage(cid, mid, text, mentionsMap);
        handleUnSendMessage(message, Message.MESSAGE_SEND_ING);
        commentList.add(message);
        if (commentAdapter == null) {
            commentAdapter = new CommentAdapter();
            commentListView.setAdapter(commentAdapter);
        } else {
            commentAdapter.notifyDataSetChanged();
        }
        countTv.setVisibility(View.VISIBLE);
        countTv.setText(getString(R.string.comment_count, commentList.size()));
        // 滚动到页面最后
        commentScrollView.post(new Runnable() {
            public void run() {
                commentScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        InputMethodUtils.hide(ChannelMessageDetailActivity.this);
        MessageSendManager.getInstance().sendMessage(message);
    }

    /**
     * 处理未发送成功的消息，存储临时消息
     *
     * @param message
     * @param status
     */
    private void handleUnSendMessage(Message message, int status) {
        //发送中，无网,发送消息失败
        message.setSendStatus(status);
        message.setRead(Message.MESSAGE_READ);
        MessageCacheUtil.saveMessage(ChannelMessageDetailActivity.this, message);
        SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_COMMENT_MESSAGE, message);
        EventBus.getDefault().post(simpleEventMessage);
    }

    /**
     * 打开个人信息
     *
     * @param uid
     */
    private void openUserInfo(String uid) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        //机器人进群修改处
        if (uid.startsWith("BOT")) {
            IntentUtils.startActivity(ChannelMessageDetailActivity.this, RobotInfoActivity.class, bundle);
        } else {
            IntentUtils.startActivity(ChannelMessageDetailActivity.this,
                    UserInfoActivity.class, bundle);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
        getComment();
    }

    /**
     * 获取消息的评论
     */
    private void getComment() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            WSAPIService.getInstance().getMessageComment(mid, cid);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessageComment(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_MESSAGE_COMMENT)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                GetMessageCommentResult getMessageCommentResult = new GetMessageCommentResult(content);
                commentList = getMessageCommentResult.getCommentList();
                if (commentList != null && commentList.size() > 0) {
                    commentAdapter = new CommentAdapter();
                    commentListView.setAdapter(commentAdapter);
                    commentAdapter.notifyDataSetChanged();
                    countTv.setVisibility(View.VISIBLE);
                    countTv.setText(getString(R.string.comment_count, commentList.size()));
                } else {
                    countTv.setVisibility(View.GONE);
                }
            } else {
//                WebServiceMiddleUtils.hand(MyApplication.getInstance(), eventMessage.getContent(), eventMessage.getStatus());
            }
        }
    }

    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessageById(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_MESSAGE_BY_ID)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                JSONObject contentobj = JSONUtils.getJSONObject(content);
                Message message = new Message(contentobj);
                if (message.getId().equals(mid)) {
                    handMsgData();
                }
            } else {
                ToastUtils.show(R.string.message_get_fail);
                finish();
            }

        }

    }

    class CommentAdapter extends BaseAdapter {
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
            LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.comment_item_view_new, null);
            TextView userNameText = (TextView) convertView
                    .findViewById(R.id.tv_name);
            TextView sendTimeText = (TextView) convertView
                    .findViewById(R.id.commentdetail_time_text);
            final TextView contentText = (TextView) convertView
                    .findViewById(R.id.comment_text);
            ImageViewRound photoImg = (ImageViewRound) convertView
                    .findViewById(R.id.msg_img);
            photoImg.setType(ImageViewRound.TYPE_ROUND);
            photoImg.setRoundRadius(photoImg.dpTodx(6));
            final Message message = commentList.get(position);
            if (!TextUtils.isEmpty(membersDetail)) {
                userNameText.setText(ChatMsgContentUtils.getUserNicknameOrName(JSONUtils.getJSONArray(membersDetail, new JSONArray()), message.getFromUser()));
            } else {
                userNameText.setText(ContactUserCacheUtils.getUserName(message.getFromUser()));
            }
            String content = message.getMsgContentComment().getText();
            contentText.setMovementMethod(LinkMovementClickMethod.getInstance());
            SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(content, message.getMsgContentTextPlain().getMentionsMap(), JSONUtils.getJSONArray(membersDetail, new JSONArray()));
            contentText.setText(spannableString);
            TransHtmlToTextUtils.stripUnderlines(contentText,
                    Color.parseColor("#0f7bca"));
            String time = TimeUtils.getDisplayTime(ChannelMessageDetailActivity.this,
                    message.getCreationDate());
            sendTimeText.setText(time);
            String photoUrl = APIUri.getChannelImgUrl(MyApplication.getInstance(), message.getFromUser());
            ImageDisplayUtils.getInstance().displayImage(photoImg, photoUrl, R.drawable.icon_person_default);
            photoImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openUserInfo(message.getFromUser());
                }
            });
            return convertView;
        }
    }

    // 加载视频消息类型View
    private void loadVideoMessage(final ImageView videoIv) {
        MsgContentMediaVideo msgContentMediaVideo = message.getMsgContentMediaVideo();
        final int chatImgBg = ResourceUtils.getResValueOfAttr(this, R.attr.bg_chat_img);
        String imagePath = msgContentMediaVideo.getImagePath();
        String localPath = message.getLocalPath();
        // 视频压缩时，获取原路径，Glide缓存key为原路径
        if (!TextUtils.isEmpty(msgContentMediaVideo.getOriginMediaPath())) {
            localPath = msgContentMediaVideo.getOriginMediaPath();
        }
        // 先使用缓存，再使用本地加载，再请求网络
        if (imagePath.startsWith("http")) {
            GlideEngine.createGlideEngine().loadVideoThumbnailImage(this, imagePath, 0, 0, videoIv, chatImgBg, new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    if (e != null && e.getMessage().contains("FileNotFoundException")) {
                        // 防止Glide缓存失效，重新请求
                        JSONObject object = JSONUtils.getJSONObject(message.getContent());
                        JSONObject picInfo = JSONUtils.getJSONObject(object, "thumbnail", new JSONObject());
                        try {
                            picInfo.put("media", "");
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                        message.setContent(object.toString());
                        MessageCacheUtil.saveMessage(ChannelMessageDetailActivity.this, message);
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            });
        } else {
            // 防止图片本地被删除，不为空再加载本地资源
            if (!TextUtils.isEmpty(localPath)) {
                GlideEngine.createGlideEngine().loadVideoThumbnailImage(this, localPath, 0, 0, videoIv, chatImgBg, new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        message.setLocalPath("");
                        loadVideoCover(ChannelMessageDetailActivity.this, videoIv, chatImgBg, message);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                });
            } else {
                loadVideoCover(this, videoIv, chatImgBg, message);
            }
        }
    }

    private static void loadVideoCover(final Activity context, final ImageView imageView, final int chatImgBg, final Message message) {
        imageView.setImageResource(chatImgBg);
        BaseModuleApiService appAPIService = new BaseModuleApiService(context);
        appAPIService.setAPIInterface(new BaseModuleAPIInterfaceInstance() {
            @Override
            public void returnVideoSuccess(final String url) {
                PictureThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject object = JSONUtils.getJSONObject(message.getContent());
                        JSONObject picInfo = JSONUtils.getJSONObject(object, "thumbnail", new JSONObject());
                        try {
                            picInfo.put("media", url);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        message.setContent(object.toString());
                        MessageCacheUtil.saveMessage(context, message);
                        GlideEngine.createGlideEngine().loadVideoThumbnailImage(context, url, 0, 0, imageView, chatImgBg, null);
                    }
                });
            }

            @Override
            public void returnVideoFail(String error, int errorCode) {
            }

        });
        String originUlr = APIUri.getECMChatUrl() + "/api/v1/channel/" + message.getChannel() + "/file/request?path=" + StringUtils.encodeURIComponent(message.getMsgContentMediaVideo().getMedia()) + "&inlineContent=true";
        appAPIService.getVideoUrl(originUlr);
    }

    private static boolean setImgViewSize(Activity context, ImageView imageView, int w, int h) {
        if (w == 0 || h == 0) {
            return false;
        }
        int minW = DensityUtil.dip2px(context, 120);
        int minH = DensityUtil.dip2px(context, 120);
        int maxW = DensityUtil.dip2px(context, 260);
        int maxH = DensityUtil.dip2px(context, 260);
        LayoutParams params = imageView.getLayoutParams();
        if (w == h) {
            params.width = maxW;
            params.height = maxH;
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else if (h > w) {
            params.height = maxH;
            params.width = (int) (maxH * 1.0 * w / h);
            if (params.width < minW) {
                params.width = minW;
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            params.width = maxW;
            params.height = (int) (maxW * 1.0 * h / w);
            if (params.height < minH) {
                params.height = minH;
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        imageView.setLayoutParams(params);
        return true;
    }

}
