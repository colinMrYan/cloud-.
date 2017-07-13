package com.inspur.emmcloud.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.GetFileUploadResult;
import com.inspur.emmcloud.bean.GetMsgResult;
import com.inspur.emmcloud.bean.GetNewMsgsResult;
import com.inspur.emmcloud.bean.GetNewsImgResult;
import com.inspur.emmcloud.bean.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.GetSendMsgResult;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.bean.PVCollectModel;
import com.inspur.emmcloud.broadcastreceiver.MsgReceiver;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.app.groupnews.NewsWebDetailActivity;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.ConbineMsg;
import com.inspur.emmcloud.util.DirectChannelUtils;
import com.inspur.emmcloud.util.HandleMsgTextUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.ListViewUtils;
import com.inspur.emmcloud.util.MsgCacheUtil;
import com.inspur.emmcloud.util.MsgReadIDCacheUtils;
import com.inspur.emmcloud.util.MsgRecourceUploadUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.RobotCacheUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.URLMatcher;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.ECMChatInputMenu.ChatInputMenuListener;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;
import com.inspur.imp.plugin.camera.editimage.EditImageActivity;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.path;

/**
 * com.inspur.emmcloud.ui.ChannelActivity
 *
 * @author Fortune Yu; create at 2016年8月29日
 */
public class ChannelActivity extends BaseActivity implements OnRefreshListener {

    private static final int HAND_CALLBACK_MESSAGE = 1;
    private static final int GELLARY_RESULT = 2;
    private static final int CAMERA_RESULT = 3;
    private static final int MENTIONS_RESULT = 5;
    private static final int CHOOSE_FILE = 4;
    private PullableListView msgListView;
    private List<Msg> msgList;
    private Handler handler;
    private MsgReceiver msgResvier;
    private ChatAPIService apiService;
    private PullToRefreshLayout pullToRefreshLayout;
    private BroadcastReceiver refreshNameReceiver;
    private Channel channel;
    private String cid;
    private ECMChatInputMenu chatInputMenu;
    private LoadingDialog loadingDlg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        ((MyApplication) getApplicationContext()).addActivity(this);
        init();
        registeRefreshNameReceiver();
        recordUserClickChannel();
    }

    // Activity在SingleTask的启动模式下多次打开传递Intent无效，用此方法解决
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        init();
        //当从群成员选择进入沟通频道的时候执行这里的记录
        recordUserClickChannel();
    }

    /**
     * 记录用户点击的频道，修改不是云+客服的时候才记录频道点击事件170629
     */
    private void recordUserClickChannel() {
        String from = "";
        if(getIntent().hasExtra("from")){
            from = getIntent().getStringExtra("from");
            if(!from.equals("customer")){
                PVCollectModel pvCollectModel = new PVCollectModel("channel","communicate");
                PVCollectModelCacheUtils.saveCollectModel(ChannelActivity.this,pvCollectModel);
            }
        }
    }

    private void init() {
        loadingDlg = new LoadingDialog(this);
        apiService = new ChatAPIService(ChannelActivity.this);
        apiService.setAPIInterface(new WebService());
        cid = getIntent().getExtras().getString("cid");
        channel = ChannelCacheUtils.getChannel(this, cid);
        if (channel == null) {
            getChannelInfo();
        }else if(getIntent().hasExtra("get_new_msg")){//通过scheme打开的频道
            getNewMsgOfChannel(true);
        }else {
            initViews();
        }
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);
        pullToRefreshLayout.setOnRefreshListener(this);
        handleChatInputMenu();
        setChannelTitle();
        initMsgListView();
        handMessage();
        registeMsgReceiver();
    }

    /**
     * 显示聊天频道的title
     */
    private void setChannelTitle() {
        String title = channel.getTitle();
        if (channel.getType().equals("DIRECT")) {
            String myUid = ((MyApplication) getApplicationContext()).getUid();
            if (title.contains(myUid) && title.contains("-")) {
                title = DirectChannelUtils.getDirectChannelTitle(
                        getApplicationContext(), title);
            }
        } else if (channel.getType().equals("SERVICE")) {
            title = DirectChannelUtils.getRobotInfo(getApplicationContext(),
                    title).getName();
        }
        ((TextView) findViewById(R.id.header_text)).setText(title);
    }

    /**
     * 处理chatInputMenu是否显示，以及显示几个Menu上的item
     */
    private void handleChatInputMenu() {
        chatInputMenu = (ECMChatInputMenu) findViewById(R.id.chat_input_menu);
        if (channel.getType().equals("GROUP")) {
            chatInputMenu.setIsChannelGroup(true, cid);
        }
        chatInputMenu.setChatInputMenuListener(new ChatInputMenuListener() {

            @Override
            public void onSetContentViewHeight(boolean isLock) {
                // TODO Auto-generated method stub
                final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) pullToRefreshLayout
                        .getLayoutParams();
                if (isLock) {
                    params.height = pullToRefreshLayout.getHeight();
                    params.weight = 0.0F;
                } else {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            params.weight = 1.0F;
                        }
                    });
                }
            }

            @Override
            public void onSendMsg(String content, List<String> mentionsUidList,
                                  List<String> mentionsUserNameList) {
                // TODO Auto-generated method stub
                sendTextMessage(content, mentionsUidList, mentionsUserNameList);
            }
        });
        if ((channel != null) && channel.getInputs().equals("0")) {
            chatInputMenu.setVisibility(View.GONE);
        } else {
            chatInputMenu.updateMenuGrid(handleShowItems());
        }
    }


    /**
     * 计算inputs的二进制
     */
    private String handleShowItems() {
        String result = "-1";
        String inputs = channel.getInputs();
        if (!StringUtils.isBlank(inputs)) {
            result = Integer.toBinaryString(Integer.parseInt(inputs));
        }
        return result;
    }

    /**
     * 注册更改频道名称广播
     */
    private void registeRefreshNameReceiver() {
        refreshNameReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getExtras().getString("name");
                ((TextView) findViewById(R.id.header_text)).setText(name);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("update_channel_name");
        registerReceiver(refreshNameReceiver, filter);
    }

    /**
     * 初始化消息列表UI
     */
    private void initMsgListView() {
        msgListView = (PullableListView) findViewById(R.id.msg_list);
        msgListView.setCanSelectBottom(true);
        msgList = MsgCacheUtil.getHistoryMsgList(getApplicationContext(),
                cid, "", 15);
        // 如果没有消息就不让ListView刷新
        if (msgList.size() == 0) {
            msgListView.setCanPullDown(false);
        }
        msgListView.setAdapter(adapter);
        msgListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                msgListView.setSelection(adapter.getCount()-1);
            }
        },30);
        pullToRefreshLayout.setOnRefreshListener(ChannelActivity.this);
        msgListView.smoothScrollToPosition(adapter.getCount());
        // 设置点击每个Item时跳转到详情
        msgListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Bundle bundle = new Bundle();
                Msg msg = msgList.get(position);
                String msgType = msg.getType();
                String mid = "";
                //当消息处于发送中状态时无法点击
                if (msg.getSendStatus() != 1) {
                    return;
                }

                if (msgType.equals("res_file")) {
                    mid = msg.getMid();
                    bundle.putString("mid", mid);
                    bundle.putString("cid", msg.getCid());
                    IntentUtils.startActivity(ChannelActivity.this,
                            ChannelMsgDetailActivity.class, bundle);
                } else if (msgType.equals("comment")
                        || msgType.equals("text_comment")) {
                    mid = msg.getCommentMid();
                    bundle.putString("mid", mid);
                    bundle.putString("cid", msg.getCid());
                    IntentUtils.startActivity(ChannelActivity.this,
                            ChannelMsgDetailActivity.class, bundle);
                } else if (msgType.equals("res_link")) {
                    String msgBody = msg.getBody();
                    String linkTitle = JSONUtils.getString(msgBody, "title", "");
                    String linkDigest = JSONUtils.getString(msgBody, "digest", "");
                    String linkUrl = JSONUtils.getString(msgBody, "url", "");
                    String linkPoster = JSONUtils.getString(msgBody, "poster", "");
                    bundle.putString("url", linkUrl);
                    bundle.putString("title", linkTitle);
                    bundle.putString("digest", linkDigest);
                    bundle.putString("poster", linkPoster);
                    bundle.putBoolean("tran", true);
                    IntentUtils.startActivity(ChannelActivity.this,
                            NewsWebDetailActivity.class, bundle);
                }

            }
        });
        /**
         * 当触摸消息list时把输入法和添加选项layout隐藏
         */
        msgListView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (!chatInputMenu.hideAddMenuLayout()) {
                    chatInputMenu.hideSoftInput();
                }
                return false;
            }
        });

    }

    /**
     * 设置ListView的刷新和滚动到最下方
     */
    private void setListViewNotifyAndScrollEnd(){
        adapter.notifyDataSetChanged();
        msgListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                msgListView.setSelection(adapter.getCount()-1);
            }
        },30);
    }


    /**
     * 注册消息接收广播,传入一个Handler用于接收到消息后把消息发回到主线程
     */
    private void registeMsgReceiver() {
        // TODO Auto-generated method stub
        if (msgResvier == null){
            msgResvier = new MsgReceiver(ChannelActivity.this, handler);
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.inspur.msg");
            registerReceiver(msgResvier, filter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    final Intent data) {
        if (resultCode == RESULT_OK) {
            // 文件管理器返回
            if (requestCode == CHOOSE_FILE
                    && NetUtils.isNetworkConnected(getApplicationContext())) {
                Msg localMsg = MsgRecourceUploadUtils.uploadImgFile(
                        ChannelActivity.this, data, apiService);
                addLocalMessage(localMsg);
                //拍照返回
            } else if (requestCode == CAMERA_RESULT
                    && NetUtils.isNetworkConnected(getApplicationContext())) {
                String cameraImgPath = Environment.getExternalStorageDirectory() + "/DCIM/" + PreferencesUtils.getString(ChannelActivity.this, "capturekey");
                refreshGallery(ChannelActivity.this,cameraImgPath);
                EditImageActivity.start(ChannelActivity.this, cameraImgPath, MyAppConfig.LOCAL_IMG_CREATE_PATH);
                //拍照后图片编辑返回
            } else if (requestCode == EditImageActivity.ACTION_REQUEST_EDITIMAGE) {
                String imgPath = data.getExtras().getString("save_file_path");
                Msg localMsg = MsgRecourceUploadUtils.uploadMsgImg(
                        ChannelActivity.this, imgPath, apiService);
                addLocalMessage(localMsg);
            } else if (requestCode == MENTIONS_RESULT) {
                // @返回
                chatInputMenu.setMentionData(data);
            }
        } else {
            // 图库选择图片返回
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS)
                if (data != null && requestCode == GELLARY_RESULT) {
                    ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data
                            .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                    for (int i = 0;i<imageItemList.size();i++){
                        Msg localMsg = MsgRecourceUploadUtils.uploadMsgImg(
                                ChannelActivity.this, imageItemList.get(i).path, apiService);
                        addLocalMessage(localMsg);
                    }
                }
        }
    }

    /**
     * 保存并显示把图片展示出来
     *
     * @param context
     * @param cameraPath
     */
    private  void refreshGallery(Context context, String cameraPath) {
        File file = new File(cameraPath);
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }

    /**
     * 处理子线程返回消息
     */
    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HAND_CALLBACK_MESSAGE: // 接收推送的消息·
                        Msg pushMsg = (Msg) msg.obj;
                        if (cid.equals(pushMsg.getCid())){
                            MsgReadIDCacheUtils.saveReadedMsg(ChannelActivity.this,
                                    pushMsg.getCid(), pushMsg.getMid());
                            if (!msgList.contains(pushMsg)&& !pushMsg.getTmpId().equals(AppUtils.getMyUUID(getApplicationContext()))) {
                                msgList.add(pushMsg);
                                msgListView.setCanPullDown(true);
                                setListViewNotifyAndScrollEnd();
                            }
                        }
                        break;

                    default:
                        break;
                }

            }

        };
    }

    /**
     * 发送消息
     *
     * @param content
     * @param type
     * @param fakeMessageId
     */
    protected void sendMsg(String content, String type, String fakeMessageId) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            apiService.sendMsg(cid, content, type, fakeMessageId);
        }
    }

    /**
     * 当推送消息是自己的消息时修改消息id
     *
     * @param fakeMessageId
     * @param realMsg
     */
    private void replaceWithRealMsg(String fakeMessageId, Msg realMsg) {
        if (StringUtils.isBlank(fakeMessageId)) {
            return;
        }
        Msg fakeMsg = new Msg();
        fakeMsg.setMid(fakeMessageId);
        // 如果list中已经有了这个真实的消息，就要去掉假消息，防止重复
        if (msgList.contains(realMsg)) {
            msgList.remove(fakeMsg);
            adapter.notifyDataSetChanged();
        } else { // 如果list中没有这真是的消息，就要替换成真实消息
            int fakeMsgIndex = msgList.indexOf(fakeMsg);
            if (fakeMsgIndex != -1) {
                msgList.remove(fakeMsgIndex);
                msgList.add(fakeMsgIndex, realMsg);
                /**第一个可见的位置**/
                int firstVisiblePosition = msgListView.getFirstVisiblePosition();
                /**最后一个可见的位置**/
                int lastVisiblePosition = msgListView.getLastVisiblePosition();
                if (fakeMsgIndex >= firstVisiblePosition && fakeMsgIndex <= lastVisiblePosition) {
                    View childAt = msgListView.getChildAt(fakeMsgIndex
                            - firstVisiblePosition);
                    if (childAt != null) {
                        ImageView refreshingImg = (ImageView) childAt.findViewById(R.id.refreshing_img);
                        refreshingImg.clearAnimation();
                        refreshingImg.setVisibility(View.GONE);
                    }
                }

            } else {
                msgList.add(realMsg);
                adapter.notifyDataSetChanged();
            }
        }


    }

    /**
     * 设置消息发送失败
     *
     * @param fakeMessageId
     */
    private void setMsgSendFail(String fakeMessageId) {
        Msg fakeMsg = new Msg();
        fakeMsg.setMid(fakeMessageId);
        int fakeMsgIndex = msgList.indexOf(fakeMsg);
        if (fakeMsgIndex != -1) {
            msgList.get(fakeMsgIndex).setSendStatus(2);
            adapter.notifyDataSetChanged();
        }

    }

    /**
     * 控件点击事件
     *
     * @param v
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finishActivity();
                break;

            case R.id.channel_info_img:
                showChannelInfo();
                break;
            default:
                break;
        }
    }

    /**
     * 关闭此页面
     */
    private void finishActivity(){
        if (loadingDlg != null && loadingDlg.isShowing()){
            loadingDlg.dismiss();
        }
        finish();
    }

    /**
     * 展示群组或个人信息
     */
    private void showChannelInfo() {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        if (channel.getType().equals("GROUP")) {
            IntentUtils.startActivity(ChannelActivity.this,
                    ChannelInfoActivity.class, bundle);
        } else if (channel.getType().equals("SERVICE")) {
            String botUid = DirectChannelUtils.getRobotInfo(getApplicationContext(),
                    channel.getTitle()).getId();
            bundle.putString("uid", botUid);
            IntentUtils.startActivity(ChannelActivity.this,
                    RobotInfoActivity.class, bundle);
        } else {
            IntentUtils.startActivity(ChannelActivity.this,
                    UserInfoActivity.class, bundle);
        }
    }

    /**
     * 点击发送按钮后发送消息的逻辑
     */
    private void sendTextMessage(String content, List<String> mentionsUidList,
                                 List<String> mentionsUserNameList) {

        ArrayList<String> urlList = URLMatcher.getUrls(content);
        JSONObject richTextObj = new JSONObject();
        String source = HandleMsgTextUtils.handleMentionAndURL(chatInputMenu.getEdit(), content,
                mentionsUserNameList, mentionsUidList);
        JSONArray mentionArray = JSONUtils.toJSONArray(mentionsUidList);
        JSONArray urlArray = JSONUtils.toJSONArray(urlList);
        try {
            richTextObj.put("source", source);
            richTextObj.put("mentions", mentionArray);
            richTextObj.put("urls", urlArray);
            richTextObj.put("tmpId", AppUtils.getMyUUID(ChannelActivity.this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String fakeMessageId = System.currentTimeMillis() + "";
        Msg localMsg = ConbineMsg.conbineMsg(ChannelActivity.this,
                richTextObj.toString(), "", "txt_rich", fakeMessageId);
        addLocalMessage(localMsg);
        sendMsg(richTextObj.toString(), "txt_rich", fakeMessageId);

    }

    /**
     * 消息发送完成后在本地添加一条消息
     *
     * @param msg
     */
    private void addLocalMessage(Msg msg) {
        if (msg != null) {
            //本地添加的消息设置为正在发送状态
            msg.setSendStatus(0);
            msgList.add(msg);
            setListViewNotifyAndScrollEnd();
            msgListView.setCanPullDown(true);
        }
    }

    /**
     * 显示adapter
     */
    private BaseAdapter adapter = new BaseAdapter() {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Msg msg = msgList.get(position);
            String type = msg.getType();
            LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.chat_msg_card_parent_view, null);
            RelativeLayout cardLayout = (RelativeLayout) convertView
                    .findViewById(R.id.card_layout);
            showCommonView(convertView, position, cardLayout);
            View childView = null;
            if (type.equals("txt_comment") || type.equals("comment")) {
                childView = vi.inflate(
                        R.layout.chat_msg_card_child_text_comment_view, null);
                DisplayTxtCommentMsg.displayCommentMsg(ChannelActivity.this,
                        childView, msg, apiService);
                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Bundle bundle = new Bundle();
                        bundle.putString("cid", msg.getCid());
                        bundle.putString("mid", msg.getCommentMid());
                        IntentUtils.startActivity(ChannelActivity.this,
                                ChannelMsgDetailActivity.class, bundle);
                    }
                });
            } else if (type.equals("res_image") || type.equals("image")) {
                childView = vi.inflate(
                        R.layout.chat_msg_card_child_res_img_view, null);
                DisplayResImageMsg.displayResImgMsg(ChannelActivity.this,
                        childView, msg);
            } else if (type.equals("res_link")) {
                TextView newsCommentText = (TextView) convertView
                        .findViewById(R.id.news_comment_text);
                // newsCommentText.setVisibility(View.VISIBLE);
                newsCommentText.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Bundle bundle = new Bundle();
                        bundle.putString("mid", msg.getMid());
                        bundle.putString("cid", msg.getCid());
                        IntentUtils.startActivity(ChannelActivity.this,
                                ChannelMsgDetailActivity.class, bundle);
                    }
                });
                childView = vi.inflate(
                        R.layout.chat_msg_card_child_res_link_view, null);
                DisplayResLinkMsg.displayResLinkMsg(ChannelActivity.this,
                        childView, msg);
            } else if (type.equals("res_file")) {
                childView = vi.inflate(
                        R.layout.chat_msg_card_child_res_file_view, null);
                DisplayResFileMsg.displayResFileMsg(ChannelActivity.this,
                        childView, msg);
            } else if (type.equals("txt_rich")) {
                childView = vi.inflate(
                        R.layout.chat_msg_card_child_text_rich_view, null);
                DisplayTxtRichMsg.displayRichTextMsg(ChannelActivity.this,
                        childView, msg);
            } else {
                childView = vi.inflate(
                        R.layout.chat_msg_card_child_res_unknown_view, null);
                DisplayResUnknownMsg.displayResUnknownMsg(ChannelActivity.this,
                        childView, msg);
            }
            cardLayout.addView(childView);
            return convertView;
        }

        /**
         * 显示公共的View
         * @param convertView
         * @param position
         * @param cardLayout
         */
        private void showCommonView(View convertView, int position,
                                    RelativeLayout cardLayout) {
            final Msg msg = msgList.get(position);
            showUserName(convertView, msg);
            showMsgSendTime(convertView, msg, position);
            showUserPhoto(convertView, msg);
            showRefreshingImg(convertView, msg);
            showCardLayout(convertView, cardLayout, msg);
        }

        /**
         * 显示正在发送的标志
         * @param convertView
         * @param msg
         */
        private void showRefreshingImg(View convertView, Msg msg) {
            ImageView refreshingImg = (ImageView) convertView.findViewById(R.id.refreshing_img);
            if (msg.getSendStatus() == 0) {
                RotateAnimation refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
                        getApplicationContext(), R.anim.pull_rotating);
                // 添加匀速转动动画
                LinearInterpolator lir = new LinearInterpolator();
                refreshingAnimation.setInterpolator(lir);
                refreshingImg.setVisibility(View.VISIBLE);
                refreshingImg.startAnimation(refreshingAnimation);
            } else if (msg.getSendStatus() == 2) {
                refreshingImg.setVisibility(View.VISIBLE);
                refreshingImg.setImageResource(R.drawable.ic_chat_msg_send_fail);
            }

        }

        /**
         * 显示卡片的内容
         * @param convertView
         * @param cardLayout
         * @param msg
         */
        private void showCardLayout(View convertView,
                                    RelativeLayout cardLayout, Msg msg) {
            // TODO Auto-generated method stub
            boolean isMyMsg = msg.getUid().equals(
                    ((MyApplication) getApplicationContext()).getUid());
            ((View) convertView.findViewById(R.id.card_cover_view)).setBackgroundResource(isMyMsg ? R.drawable.ic_chat_msg_img_cover_arrow_right : R.drawable.ic_chat_msg_img_cover_arrow_left);
            LayoutParams params = (LayoutParams) cardLayout.getLayoutParams();
            params.addRule(isMyMsg ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_LEFT);
            cardLayout.setLayoutParams(params);
        }


        /**
         * 展示消息发送时间
         *
         * @param convertView
         * @param msg
         * @param position
         */
        private void showMsgSendTime(View convertView, Msg msg, int position) {
            // TODO Auto-generated method stub
            TextView sendTimeText = (TextView) convertView
                    .findViewById(R.id.send_time_text);
            long msgTimeLong = TimeUtils.UTCString2Long(msg.getTime());
            long lastMsgTimelong = 0;
            if (position != 0) {
                lastMsgTimelong = TimeUtils.UTCString2Long(msgList.get(
                        position - 1).getTime());
            }
            long duration = msgTimeLong - lastMsgTimelong;
            if (duration >= 180000) {
                sendTimeText.setVisibility(View.VISIBLE);
                String msgSendTime = TimeUtils.getChannelMsgDisplayTime(
                        getApplicationContext(), msg.getTime());
                sendTimeText.setText(msgSendTime);
            } else {
                sendTimeText.setVisibility(View.GONE);
            }
        }

        /**
         * 展示用户名称
         *
         * @param convertView
         * @param msg
         */
        private void showUserName(View convertView, Msg msg) {
            // TODO Auto-generated method stub
            TextView senderNameText = (TextView) convertView
                    .findViewById(R.id.sender_name_text);
            if (channel.getType().equals("GROUP") && !isMyMsg(msg)) {
                senderNameText.setVisibility(View.VISIBLE);
                senderNameText.setText(msg.getTitle());
            } else {
                senderNameText.setVisibility(View.GONE);
            }
        }

        /**
         * 展示用户头像
         *
         * @param convertView
         * @param msg
         */
        private void showUserPhoto(View convertView, final Msg msg) {
            // TODO Auto-generated method stub
            ImageView senderPhotoImg = (ImageView) convertView
                    .findViewById(R.id.sender_photo_img);
            if (msg.getUid().equals(
                    ((MyApplication) getApplicationContext()).getUid())) {
                senderPhotoImg.setVisibility(View.INVISIBLE);
            } else {
                senderPhotoImg.setVisibility(View.VISIBLE);
                String iconUrl = UriUtils.getChannelImgUri(ChannelActivity.this, msg.getUid());
                if (channel.getType().equals("SERVICE")) {
                    iconUrl = UriUtils.getRobotIconUri(RobotCacheUtils
                            .getRobotById(ChannelActivity.this, msg.getUid())
                            .getAvatar());
                }
                new ImageDisplayUtils(ChannelActivity.this,
                        R.drawable.icon_person_default).display(senderPhotoImg,
                        iconUrl);
                senderPhotoImg.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        String uid = msg.getUid();
                        bundle.putString("uid", uid);
                        if (channel.getType().endsWith("SERVICE")) {
                            IntentUtils.startActivity(ChannelActivity.this,
                                    RobotInfoActivity.class, bundle);
                        } else {
                            IntentUtils.startActivity(ChannelActivity.this,
                                    UserInfoActivity.class, bundle);
                        }
                    }
                });
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return msgList.size();
        }
    };

    /**
     * 判断是否自己的消息
     *
     * @param msg
     * @return
     */
    private boolean isMyMsg(Msg msg) {
        String uid = ((MyApplication) getApplication()).getUid();
        return msg.getUid().equals(uid);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (!chatInputMenu.hideAddMenuLayout()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (handler != null) {
            handler = null;
        }
        if (msgResvier != null) {
            unregisterReceiver(msgResvier);
        }
        if (refreshNameReceiver != null) {
            unregisterReceiver(refreshNameReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        if (MsgCacheUtil.isDataInLocal(ChannelActivity.this, cid, msgList
                .get(0).getMid(), 15)) {
            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
            List<Msg> historyMsgList = MsgCacheUtil.getHistoryMsgList(
                    ChannelActivity.this, cid, msgList.get(0).getMid(),
                    15);
            msgList.addAll(0, historyMsgList);
            adapter.notifyDataSetChanged();
            ListViewUtils.setSelection(msgListView, historyMsgList.size() - 1);
        } else {
            getNewsMsg();
        }
    }

    /**
     * 获取新消息
     */
    private void getNewsMsg() {
        apiService.getNewMsgs(cid, msgList.get(0).getMid(), 15);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
    }

    /**
     * 获取频道信息
     */
    private void getChannelInfo() {
        if (NetUtils.isNetworkConnected(this)){
            loadingDlg.show();
            String[] cidArray ={cid};
            apiService.getChannelGroupList(cidArray);
        }else{
            finishActivity();
        }

    }

    /**
     * 获取此频道的最新消息
     */
    private void getNewMsgOfChannel(boolean isShowDlg){
        if (NetUtils.isNetworkConnected(this,false)){
            loadingDlg.show(isShowDlg);
            apiService.getNewMsgs(cid,"",15);
        }else {
            initViews();
        }
    }


    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
                                         String fakeMessageId) {
            replaceWithRealMsg(fakeMessageId, getSendMsgResult.getMsg());
        }

        @Override
        public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelActivity.this, error, errorCode);
            setMsgSendFail(fakeMessageId);
        }

        @Override
        public void returnUploadMsgImgSuccess(
                GetNewsImgResult getNewsImgResult, String fakeMessageId) {
            String newsImgBody = getNewsImgResult.getImgMsgBody();
            sendMsg(newsImgBody, "res_image", fakeMessageId);
        }

        @Override
        public void returnUploadMsgImgFail(String error, int errorCode) {
            if (pullToRefreshLayout == null){
                if (loadingDlg != null && loadingDlg.isShowing()){
                    loadingDlg.dismiss();
                }
                initViews();
            }

            WebServiceMiddleUtils.hand(ChannelActivity.this, error, errorCode);
        }

        @Override
        public void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult) {
            if (pullToRefreshLayout == null){
                if (loadingDlg != null && loadingDlg.isShowing()){
                    loadingDlg.dismiss();
                }
                List<Msg> msgList = getNewMsgsResult.getNewMsgList(cid);
                MsgCacheUtil.saveMsgList(ChannelActivity.this,msgList,"");
                initViews();
            }else {
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                final List<Msg> historyMsgList = getNewMsgsResult
                        .getNewMsgList(cid);
                MsgCacheUtil.saveMsgList(ChannelActivity.this, historyMsgList,
                        msgList.get(0).getMid());
                if (historyMsgList != null && historyMsgList.size() > 1) {
                    msgList.addAll(0, historyMsgList);
                    adapter.notifyDataSetChanged();
                    ListViewUtils.setSelection(msgListView,
                            historyMsgList.size() - 1);
                } else {
                    msgListView.setCanPullDown(false);
                }
            }

        }

        @Override
        public void returnNewMsgsFail(String error,int errorCode) {
            if (pullToRefreshLayout == null){
                if (loadingDlg != null && loadingDlg.isShowing()){
                    loadingDlg.dismiss();
                }
                initViews();
            }else {
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
                WebServiceMiddleUtils.hand(ChannelActivity.this, error,errorCode);
            }
        }

        @Override
        public void returnMsgSuccess(GetMsgResult getMsgResult) {
            Msg msg = getMsgResult.getMsg();
            if (msg != null && ChannelActivity.this != null) {
                MsgCacheUtil.saveMsg(ChannelActivity.this, msg);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void returnMsgFail(String error,int errorCode) {
            WebServiceMiddleUtils.hand(ChannelActivity.this, error,errorCode);
        }

        @Override
        public void returnFileUpLoadSuccess(
                GetFileUploadResult getFileUploadResult, String fakeMessageId) {
            String fileMsgBody = getFileUploadResult.getFileMsgBody();
            sendMsg(fileMsgBody, "res_file", fakeMessageId);
        }

        @Override
        public void returnFileUpLoadFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelActivity.this, error, errorCode);
        }

        @Override
        public void returnSearchChannelGroupSuccess(
                GetSearchChannelGroupResult getSearchChannelGroupResult) {
            List<ChannelGroup> channelGroupList = getSearchChannelGroupResult.getSearchChannelGroupList();
            if (channelGroupList.size() != 0){
                channel = new Channel(channelGroupList.get(0));
                getNewMsgOfChannel(false);
            }else{
                finishActivity();
            }

        }

        @Override
        public void returnSearchChannelGroupFail(String error,int errorCode) {
            channel = ChannelCacheUtils.getChannel(getApplicationContext(),cid);
            if(channel == null){
                finishActivity();
            }else {
                getNewMsgOfChannel(false);
            }
        }
    }

}