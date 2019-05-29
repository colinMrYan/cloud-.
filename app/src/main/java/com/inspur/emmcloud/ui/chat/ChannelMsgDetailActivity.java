package com.inspur.emmcloud.ui.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Comment;
import com.inspur.emmcloud.bean.chat.CommentBodyBean;
import com.inspur.emmcloud.bean.chat.GetMsgCommentResult;
import com.inspur.emmcloud.bean.chat.GetMsgResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.ECMChatInputMenuV0;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ScrollViewWithListView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
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


/**
 * 消息详情页面
 *
 * @author Administrator
 */
public class ChannelMsgDetailActivity extends BaseActivity implements
        SwipeRefreshLayout.OnRefreshListener {

    private static final int RESULT_MENTIONS = 5;
    private ScrollViewWithListView commentListView;
    private Msg msg;
    private ChatAPIService apiService;
    private List<Comment> commentList;
    private BaseAdapter commentAdapter;
    private LoadingDialog loadingDialog;
    private ScrollView commentScrollView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CircleTextImageView senderHeadImg;
    private TextView msgSendTimeText;
    private TextView senderNameText;
    private ImageView msgContentImg;
    private String cid = "";
    private RelativeLayout msgDisplayLayout;
    private LayoutInflater inflater;
    private ECMChatInputMenuV0 chatInputMenu;

    @Override
    public void onCreate() {
        initView();
        initData();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_channel_msg_detail;
    }

    /**
     * 初始化Views
     */
    private void initView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        loadingDialog = new LoadingDialog(this);
        apiService = new ChatAPIService(this);
        apiService.setAPIInterface(new WebService());
        commentList = new ArrayList<Comment>();
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
        chatInputMenu = (ECMChatInputMenuV0) findViewById(R.id.chat_input_menu);
        cid = getIntent().getExtras().getString("cid");
        String channelType = ChannelCacheUtils.getChannelType(getApplicationContext(),
                cid);
        if (channelType.equals("GROUP")) {
            chatInputMenu.setCanMentions(true, cid);
        }
        chatInputMenu.hideAddMenuLayout();
        chatInputMenu.setChatInputMenuListener(new ECMChatInputMenuV0.ChatInputMenuListener() {
            @Override
            public void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap) {
                // TODO Auto-generated method stub
                sendComment(content, mentionsUidList, urlList);
            }

            @Override
            public void onVoiceCommucaiton() {

            }

            @Override
            public void onChatDraftsClear() {

            }
        });
        chatInputMenu.setInputLayout("1");
    }


    /**
     * 初始化数据源
     */
    private void initData() {
        String mid = getIntent().getStringExtra("mid");
        msg = MsgCacheUtil.getCacheMsg(getApplicationContext(), mid);
        if (msg != null) {
            handMsgData();
        } else {
            getMsgById(mid);
        }
    }

    /**
     * 处理数据
     */
    private void handMsgData() {
        cid = msg.getCid();
        getComment();
        displayMsgDetail();
    }

    /**
     * 展示消息详情
     */
    private void displayMsgDetail() {
        disPlayCommonInfo();
        View msgDisplayView = null;
        if (msg.getType().equals("res_file")) {
            msgDisplayView = DisplayResFileMsg.displayResFileMsg(ChannelMsgDetailActivity.this, msg, true);
        } else {
            msgDisplayView = inflater.inflate(R.layout.msg_common_detail, null);
            msgContentImg = (ImageView) msgDisplayView
                    .findViewById(R.id.content_img);
            TextView fileNameText = (TextView) msgDisplayView
                    .findViewById(R.id.comment_filename_text);
            TextView fileSizeText = (TextView) msgDisplayView
                    .findViewById(R.id.comment_filesize_text);
            final CommentBodyBean commentBodyBean = new CommentBodyBean(
                    msg.getBody());
            displayImage(commentBodyBean.getKey());
            fileNameText.setText(commentBodyBean.getName());
            LogUtils.jasonDebug("size=" + commentBodyBean.getSize());
            fileSizeText.setText(FileUtils.formatFileSize(commentBodyBean
                    .getSize()));
            msgContentImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (msg.getType().equals("image")
                            || msg.getType().equals("res_image")) {
                        displayZoomImage(v, commentBodyBean.getKey());
                    }
                }
            });
        }
        msgDisplayView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        msgDisplayLayout.addView(msgDisplayView);
    }

    /**
     * 展示图片或者文件图标
     *
     * @param fileName
     */
    private void displayImage(String fileName) {
        if (msg.getType().equals("res_image")) {
            ImageDisplayUtils.getInstance().displayImage(msgContentImg,
                    APIUri.getPreviewUrl(fileName), R.drawable.icon_photo_default);
        } else {
            ImageDisplayUtils.getInstance().displayImage(msgContentImg, "drawable://" + FileUtils.getIconResId(fileName));
        }
    }

    /**
     * 展示可以缩放的Image
     *
     * @param path
     */
    protected void displayZoomImage(View view, String path) {

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        view.invalidate();
        int width = view.getWidth();
        int height = view.getHeight();
        String url = path;
        if (!path.startsWith("file:") && !path.startsWith("content:")
                && !path.startsWith("drawable")) {
            url = APIUri.getPreviewUrl(path);
        }
        ArrayList<String> urlList = new ArrayList<String>();
        urlList.add(url);
        Intent intent = new Intent(getApplicationContext(),
                ImagePagerV0Activity.class);
        intent.putExtra(ImagePagerV0Activity.PHOTO_SELECT_X_TAG, location[0]);
        intent.putExtra(ImagePagerV0Activity.PHOTO_SELECT_Y_TAG, location[1]);
        intent.putExtra(ImagePagerV0Activity.PHOTO_SELECT_W_TAG, width);
        intent.putExtra(ImagePagerV0Activity.PHOTO_SELECT_H_TAG, height);
        intent.putExtra(ImagePagerV0Activity.EXTRA_IMAGE_URLS, urlList);
        startActivity(intent);
    }

    /**
     * 展示通用的部分
     */
    private void disPlayCommonInfo() {
        //机器人进群修改处
        String iconUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), msg.getUid());
        ImageDisplayUtils.getInstance().displayImage(senderHeadImg, iconUrl, R.drawable.icon_photo_default);
        senderHeadImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserInfo(msg.getUid());
            }
        });
        String msgSendTime = TimeUtils.getDisplayTime(getApplicationContext(),
                msg.getTime());
        msgSendTimeText.setText(msgSendTime);
        senderNameText.setText(msg.getTitle());
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
        InputMethodUtils.hide(ChannelMsgDetailActivity.this);
        //将最新的评论数返回给ImagePagerActivity
        if (getIntent().hasExtra("from") && getIntent().getStringExtra("from").equals("imagePager")) {
            Intent intent = new Intent();
            intent.putExtra("mid", msg.getMid());
            intent.putExtra("commentCount", commentList.size());
            setResult(RESULT_OK, intent);
        }
        finish();
    }


    /**
     * 发出评论
     */
    private void sendComment(String commentContent, List<String> mentionsUidList, List<String> urlList) {

        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            String commentConbineResult = getConbineComment(commentContent, mentionsUidList, urlList);
            apiService.sendMsg(cid, commentConbineResult, "txt_comment",
                    msg.getMid(), "");
            Comment newComment = combineComment(commentConbineResult);
            commentList.add(newComment);
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
            InputMethodUtils.hide(ChannelMsgDetailActivity.this);
        }
    }

    /**
     * 拼装评论
     */
    private Comment combineComment(String content) {
        String uid = ((MyApplication) getApplicationContext()).getUid();
        String title = PreferencesUtils.getString(
                ChannelMsgDetailActivity.this, "userRealName");
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
     * 打开个人信息
     *
     * @param uid
     */
    private void openUserInfo(String uid) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        //机器人进群修改处
        if (uid.startsWith("BOT")) {
            IntentUtils.startActivity(ChannelMsgDetailActivity.this, RobotInfoActivity.class, bundle);
        } else {
            IntentUtils.startActivity(ChannelMsgDetailActivity.this,
                    UserInfoActivity.class, bundle);
        }
    }

    @Override
    public void onRefresh() {
        getComment();
    }

    public String getConbineComment(String content, List<String> mentionsUidList, List<String> urlList) {
        JSONObject richTextObj = new JSONObject();
        JSONArray mentionArray = JSONUtils.toJSONArray(mentionsUidList);
        JSONArray urlArray = JSONUtils.toJSONArray(urlList);
        try {
            richTextObj.put("source", content);
            richTextObj.put("mentions", mentionArray);
            richTextObj.put("urlList", urlArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return richTextObj.toString();
    }

    /**
     * 获取消息
     *
     * @param mid
     */
    private void getMsgById(String mid) {
        if (NetUtils.isNetworkConnected(ChannelMsgDetailActivity.this)) {
            loadingDialog.show();
            apiService.getMsg(mid);
        }
    }

    /**
     * 获取消息的评论
     */
    private void getComment() {
        if (NetUtils.isNetworkConnected(ChannelMsgDetailActivity.this)) {
            apiService.getComment(msg.getMid());
        } else {
            swipeRefreshLayout.setRefreshing(false);
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
            final Comment comment = commentList.get(position);
            userNameText.setText(comment.getTitle());
            contentText.setMovementMethod(LinkMovementMethod.getInstance());
            SpannableString spannableString = MentionsAndUrlShowUtils.getMsgContentSpannableString(comment.getMsgBody());
            contentText.setText(spannableString);
            TransHtmlToTextUtils.stripUnderlines(contentText,
                    Color.parseColor("#0f7bca"));
            String time = TimeUtils.getDisplayTime(getApplicationContext(),
                    comment.getTime());
            sendTimeText.setText(time);

            //机器人进群修改处
            String iconUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), comment.getUid());
            ImageDisplayUtils.getInstance().displayImage(photoImg, iconUrl, R.drawable.icon_person_default);
            photoImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uid = comment.getUid();
                    openUserInfo(uid);
                }
            });
            return convertView;
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnMsgSuccess(GetMsgResult getMsgResult) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            msg = getMsgResult.getMsg();
            if (msg != null) {
                handMsgData();
            }
        }

        @Override
        public void returnMsgFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(ChannelMsgDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnMsgCommentSuccess(
                GetMsgCommentResult getMsgCommentResult, String mid) {
            swipeRefreshLayout.setRefreshing(false);
            commentList = getMsgCommentResult.getCommentList();
            if (commentList != null && commentList.size() > 0) {
                commentAdapter = new CommentAdapter();
                commentListView.setAdapter(commentAdapter);
                commentAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void returnMsgCommentFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(ChannelMsgDetailActivity.this, error, errorCode);
        }

        @Override
        public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
                                         String fakeMessageId) {
        }

        @Override
        public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelMsgDetailActivity.this, error, errorCode);
        }
    }

}
