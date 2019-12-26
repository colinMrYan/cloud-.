package com.inspur.emmcloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.OnCreateDirectConversationListener;
import com.inspur.emmcloud.componentservice.communication.SearchModel;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.chat.mvp.view.ConversationSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/7/18.
 */

public class ShareLinkActivity extends BaseActivity {

    private static final int SHARE_LINK = 1;
    @BindView(R.id.rv_file_list)
    RecyclerView recyclerView;
    @BindView(R.id.rl_channel_share)
    RelativeLayout channelRelativeLayout;
    @BindView(R.id.rl_volume_share)
    RelativeLayout volumeRelativeLayout;
    @BindView(R.id.rl_file)
    RelativeLayout fileLayout;
    @BindView(R.id.rl_image)
    RelativeLayout imageLayout;
    @BindView(R.id.iv_file_icon)
    ImageView fileImageView;
    @BindView(R.id.tv_file_name)
    TextView fileTextView;
    @BindView(R.id.tv_file_sub_name)
    TextView fileSubNameText;
    private String shareLink = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        shareLink = getIntent().getExtras().getString(Constant.SHARE_LINK);
        initView();

    }


    void initView() {
        recyclerView.setVisibility(View.GONE);
        volumeRelativeLayout.setVisibility(View.GONE);
        showLinkLayout(shareLink);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_share_files;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_channel_share:
                if (!StringUtils.isBlank(shareLink)) {
                    shareLinkToFriends();
                } else {
                    ToastUtils.show(ShareLinkActivity.this, getString(R.string.baselib_share_fail));
                    finish();
                }
                break;
        }
    }

    private void showLinkLayout(String Uri) {
        fileLayout.setVisibility(View.VISIBLE);
        imageLayout.setVisibility(View.GONE);
        if (!StringUtils.isBlank(Uri)) {
            String title = JSONUtils.getString(shareLink, "title", shareLink);
            fileSubNameText.setText(title);
            title = getString(R.string.baselib_share_link) + title;
            fileTextView.setText(title);
            fileTextView.setVisibility(View.VISIBLE);
        } else {
            fileTextView.setVisibility(View.GONE);
            fileSubNameText.setVisibility(View.GONE);
        }
        fileImageView.setImageResource(R.drawable.ic_share_link);
    }

    /**
     * 给朋友分享图片或文件
     */
    private void shareLinkToFriends() {
        Intent intent = new Intent();
        intent.putExtra("select_content", 0);
        intent.putExtra("isMulti_select", false);
        intent.putExtra("isContainMe", true);
        intent.putExtra("title", getString(R.string.baselib_share_to));
        String title = JSONUtils.getString(shareLink, "title", shareLink);
        title = getString(R.string.baselib_share_link) + title;
        intent.putExtra(ContactSearchFragment.EXTRA_SHOW_COMFIRM_DIALOG_WITH_MESSAGE, title);
        intent.putExtra(ContactSearchFragment.EXTRA_SHOW_COMFIRM_DIALOG, true);
        intent.setClass(getApplicationContext(),
                ContactSearchActivity.class);
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            startActivityForResult(intent, SHARE_LINK);
        } else {
            Intent shareIntent = new Intent(this, ConversationSearchActivity.class);
            shareIntent.putExtra(Constant.SHARE_CONTENT, title);
            startActivityForResult(shareIntent, SHARE_LINK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            if (resultCode == RESULT_OK && requestCode == SHARE_LINK
                    && NetUtils.isNetworkConnected(getApplicationContext())) {

                String result = data.getStringExtra("searchResult");
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String userOrGroupId = "";
                    boolean isGroup = false;
                    if (jsonObject.has("people")) {
                        JSONArray peopleArray = jsonObject.getJSONArray("people");
                        if (peopleArray.length() > 0) {
                            JSONObject peopleObj = peopleArray.getJSONObject(0);
                            String uid = peopleObj.getString("pid");
                            userOrGroupId = uid;
                            isGroup = false;
                            // createDirectChannel(uid);
                        }
                    }

                    if (jsonObject.has("channelGroup")) {
                        JSONArray channelGroupArray = jsonObject
                                .getJSONArray("channelGroup");
                        if (channelGroupArray.length() > 0) {
                            JSONObject cidObj = channelGroupArray.getJSONObject(0);
                            String cid = cidObj.getString("cid");
                            userOrGroupId = cid;
                            isGroup = true;
                            //startChannelActivity(cid);
                        }
                    }
                    if (isGroup) {
                        startChannelActivity(userOrGroupId);
                    } else {
                        createDirectChannel(userOrGroupId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
                    finish();
                }
            } else {
                finish();
            }
        } else {
            if (resultCode == RESULT_OK && requestCode == SHARE_LINK
                    && NetUtils.isNetworkConnected(getApplicationContext())) {
                handleShareResult(data);
            } else {
                finish();
            }
        }
    }

    private void handleShareResult(Intent data) {
        SearchModel searchModel = (SearchModel) data.getSerializableExtra("searchModel");
        if (searchModel != null) {
            String userOrChannelId = searchModel.getId();
            boolean isUser = searchModel.getType().equals(SearchModel.TYPE_USER);
            share2Conversation(userOrChannelId, isUser);
        } else {
            finish();
        }
    }

    /**
     * 分享到聊天界面
     *
     * @param userOrChannelId
     * @param isUser
     */
    private void share2Conversation(String userOrChannelId, boolean isUser) {
        if (StringUtils.isBlank(userOrChannelId)) {
            ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
        } else {
            if (isUser) {
                createDirectChannel(userOrChannelId);
            } else {
                startChannelActivity(userOrChannelId);
            }
        }
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(ShareLinkActivity.this, uid,
                    new OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            startChannelActivity(conversation.getId());
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(ShareLinkActivity.this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            startChannelActivity(getCreateSingleChannelResult.getCid());
                        }

                        @Override
                        public void createDirectChannelFail() {
                            //ToastUtils.show(ShareLinkActivity.this,getString(R.string.news_share_fail));
                            finish();
                        }
                    });
        }

    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        bundle.putString("share_type", "link");
        bundle.putSerializable(Constant.SHARE_LINK, conbineGroupNewsContent());
        IntentUtils.startActivity(ShareLinkActivity.this, WebServiceRouterManager.getInstance().isV0VersionChat() ?
                ChannelV0Activity.class : ConversationActivity.class, bundle, true);
    }

    /**
     * 组装集团新闻内容
     *
     * @return
     */
    private String conbineGroupNewsContent() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", JSONUtils.getString(shareLink, "url", ""));
            jsonObject.put("poster", "");
            jsonObject.put("digest", JSONUtils.getString(shareLink, "digest", ""));
            jsonObject.put("title", JSONUtils.getString(shareLink, "title", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}
