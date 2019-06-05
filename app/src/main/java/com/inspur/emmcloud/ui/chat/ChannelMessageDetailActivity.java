package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.SpannableString;
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

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.GetMessageCommentResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.InputMethodUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.LinkMovementClickMethod;
import com.inspur.emmcloud.widget.ScrollViewWithListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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
    private CircleTextImageView senderHeadImg;
    private TextView msgSendTimeText;
    private TextView senderNameText;
    private ImageView msgContentImg;
    private String cid = "";
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
        View msgDetailLayout = inflater.inflate(R.layout.msg_parent_detail,
                null);
        senderHeadImg = (CircleTextImageView) msgDetailLayout
                .findViewById(R.id.sender_photo_img);
        msgSendTimeText = (TextView) msgDetailLayout
                .findViewById(R.id.msg_send_time_text);
        senderNameText = (TextView) msgDetailLayout
                .findViewById(R.id.sender_name_text);
        commentListView = (ScrollViewWithListView) msgDetailLayout
                .findViewById(R.id.comment_list);
        msgDisplayLayout = (RelativeLayout) msgDetailLayout
                .findViewById(R.id.msg_display_layout);
        commentScrollView.addView(msgDetailLayout);
        initChatInputMenu();
    }

    private void initChatInputMenu() {
        chatInputMenu = (ECMChatInputMenu) findViewById(R.id.chat_input_menu);
        chatInputMenu.setOtherLayoutView(swipeRefreshLayout, commentListView);
        cid = getIntent().getExtras().getString("cid");
        String channelType = ConversationCacheUtils.getConversationType(MyApplication.getInstance(),
                cid);
        if (channelType.equals("GROUP")) {
            chatInputMenu.setCanMentions(true, cid);
        }
        chatInputMenu.hideAddMenuLayout();
        chatInputMenu.setChatInputMenuListener(new ECMChatInputMenu.ChatInputMenuListener() {
            @Override
            public void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap) {
                // TODO Auto-generated method stub
                sendComment(content, mentionsMap);
            }

            @Override
            public void onSendVoiceRecordMsg(String results, float seconds, String filePath) {

            }

            @Override
            public void onVoiceCommucaiton() {

            }

            @Override
            public void onChatDraftsClear() {

            }
        });
        chatInputMenu.setInputLayout("1", false);
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
        if (!message.getType().equals("media/image")) {
            msgDisplayView = DisplayRegularFileMsg.getView(ChannelMessageDetailActivity.this, message, 1, true);
            msgDisplayView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    MsgContentRegularFile msgContentFile = message.getMsgContentAttachmentFile();
                    String fileDownloadPath = MyAppConfig.LOCAL_DOWNLOAD_PATH + msgContentFile.getName();
                    if (FileUtils.isFileExist(fileDownloadPath)) {
                        FileUtils.openFile(ChannelMessageDetailActivity.this, fileDownloadPath);
                    } else {
                        Intent intent = new Intent(ChannelMessageDetailActivity.this, ChatFileDownloadActivtiy.class);
                        intent.putExtra("message", message);
                        ChannelMessageDetailActivity.this.startActivity(intent);
                    }
                }
            });
        } else {
            msgDisplayView = inflater.inflate(R.layout.msg_common_detail, null);
            msgContentImg = (ImageView) msgDisplayView
                    .findViewById(R.id.content_img);
            TextView fileNameText = (TextView) msgDisplayView
                    .findViewById(R.id.comment_filename_text);
            TextView fileSizeText = (TextView) msgDisplayView
                    .findViewById(R.id.comment_filesize_text);
            String fileName;
            String fileSize;
            MsgContentMediaImage msgContentMediaImage = message.getMsgContentMediaImage();
            fileName = msgContentMediaImage.getName();
            fileSize = FileUtils.formatFileSize(msgContentMediaImage.getRawSize());
            final String imgPath = APIUri.getChatFileResouceUrl(message.getChannel(), msgContentMediaImage.getRawMedia());
            ImageDisplayUtils.getInstance().displayImage(msgContentImg,
                    imgPath, R.drawable.default_image);
            msgContentImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayZoomImage(v, imgPath);
                }
            });
            fileNameText.setText(fileName);
            fileSizeText.setText(fileSize);
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
                ImagePagerActivity.class);
        intent.putExtra(ImagePagerActivity.PHOTO_SELECT_X_TAG, location[0]);
        intent.putExtra(ImagePagerActivity.PHOTO_SELECT_Y_TAG, location[1]);
        intent.putExtra(ImagePagerActivity.PHOTO_SELECT_W_TAG, width);
        intent.putExtra(ImagePagerActivity.PHOTO_SELECT_H_TAG, height);
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urlList);
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
        senderNameText.setText(ContactUserCacheUtils.getUserName(message.getFromUser()));
    }

    /**
     * 处理@逻辑
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED && requestCode == RESULT_MENTIONS) {
            String result = data.getStringExtra("searchResult");
            String uid = JSONUtils.getString(result, "uid", null);
            String name = JSONUtils.getString(result, "name", null);
            boolean isInputKeyWord = data.getBooleanExtra("isInputKeyWord", false);
            chatInputMenu.addMentions(uid, name, isInputKeyWord);
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
        finish();
    }


    /**
     * 发出评论
     */
    private void sendComment(String text, Map<String, String> mentionsMap) {

//        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
        Message message = CommunicationUtils.combinLocalCommentTextPlainMessage(cid, mid, text, mentionsMap);
        handleUnSendMessage(message, Message.MESSAGE_SEND_ING);
        commentList.add(message);
        if (commentAdapter == null) {
            commentAdapter = new CommentAdapter();
            commentListView.setAdapter(commentAdapter);
        } else {
            commentAdapter.notifyDataSetChanged();
        }
        // 滚动到页面最后
        commentScrollView.post(new Runnable() {
            public void run() {
                commentScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        InputMethodUtils.hide(ChannelMessageDetailActivity.this);
        WSAPIService.getInstance().sendChatCommentTextPlainMsg(message);
//        }
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
            WSAPIService.getInstance().getMessageComment(mid);
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
//                WebServiceMiddleUtils.hand(MyApplication.getInstance(), eventMessage.getContent(), eventMessage.getStatus());
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
            convertView = vi.inflate(R.layout.comment_item_view, null);
            TextView userNameText = (TextView) convertView
                    .findViewById(R.id.tv_name);
            TextView sendTimeText = (TextView) convertView
                    .findViewById(R.id.commentdetail_time_text);
            final TextView contentText = (TextView) convertView
                    .findViewById(R.id.comment_text);
            ImageView photoImg = (ImageView) convertView
                    .findViewById(R.id.msg_img);
            final Message message = commentList.get(position);
            userNameText.setText(ContactUserCacheUtils.getUserName(message.getFromUser()));
            String content = message.getMsgContentComment().getText();
            contentText.setMovementMethod(LinkMovementClickMethod.getInstance());
            SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(MyApplication.getInstance(), content, message.getMsgContentTextPlain().getMentionsMap());
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
}
