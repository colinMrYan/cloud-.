package com.inspur.emmcloud.ui;

import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.GetNewsImgResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.MsgRecourceUploadUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/7/18.
 */

public class ShareLinkActivity extends BaseActivity {

    private static final int SHARE_LINK = 1;
    private ChatAPIService apiService;
    private String shareLink = "";
    private String key = "";
    private LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarUtils.changeStateBarColor(this);
        apiService = new ChatAPIService(ShareLinkActivity.this);
        apiService.setAPIInterface(new WebService());
        shareLink = getIntent().getExtras().getString(Constant.SHARE_LINK);
        loadingDialog = new LoadingDialog(ShareLinkActivity.this);
        if(!StringUtils.isBlank(shareLink)){
            loadingDialog.show();
            MsgRecourceUploadUtils.uploadResImg(ShareLinkActivity.this,JSONUtils.getString(shareLink,"poster",""),apiService);
        }else{
            ToastUtils.show(ShareLinkActivity.this,getString(R.string.news_share_fail));
            finish();
        }
    }

    /**
     * 给朋友分享图片或文件
     */
    private void shareFilesToFriends() {
        Intent intent = new Intent();
        intent.putExtra("select_content", 0);
        intent.putExtra("isMulti_select", false);
        intent.putExtra("isContainMe", true);
        intent.putExtra("title", getString(R.string.news_share));
        intent.setClass(getApplicationContext(),
                ContactSearchActivity.class);
        startActivityForResult(intent, SHARE_LINK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SHARE_LINK
                && NetUtils.isNetworkConnected(getApplicationContext())) {
            String result = data.getStringExtra("searchResult");
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("people")) {
                    JSONArray peopleArray = jsonObject.getJSONArray("people");
                    if (peopleArray.length() > 0) {
                        JSONObject peopleObj = peopleArray.getJSONObject(0);
                        String uid = peopleObj.getString("pid");
                        createDirectChannel(uid);
                    }
                }

                if (jsonObject.has("channelGroup")) {
                    JSONArray channelGroupArray = jsonObject
                            .getJSONArray("channelGroup");
                    if (channelGroupArray.length() > 0) {
                        JSONObject cidObj = channelGroupArray.getJSONObject(0);
                        String cid = cidObj.getString("cid");
                        startChannelActivity(cid);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.show(MyApplication.getInstance(),getString(R.string.news_share_fail));
            }
        }else{
            finish();
        }
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        new ChatCreateUtils().createDirectChannel(ShareLinkActivity.this, uid,
                new ChatCreateUtils.OnCreateDirectChannelListener() {
                    @Override
                    public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                        startChannelActivity(getCreateSingleChannelResult.getCid());
                    }

                    @Override
                    public void createDirectChannelFail() {
                        ToastUtils.show(ShareLinkActivity.this,getString(R.string.news_share_fail));
                    }
                });
    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid){
        Bundle bundle = new Bundle();
        bundle.putString("cid",cid);
        bundle.putString("share_type","link");
        bundle.putSerializable(Constant.SHARE_LINK, conbineGroupNewsContent());
        IntentUtils.startActivity(ShareLinkActivity.this, MyApplication.getInstance().isV0VersionChat()?
                ChannelV0Activity.class: ChannelActivity.class,bundle,true);
    }

    /**
     * 组装集团新闻内容
     * @return
     */
    private String conbineGroupNewsContent() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("url", JSONUtils.getString(shareLink,"url",""));
            jsonObject.put("poster", key);
            jsonObject.put("digest", JSONUtils.getString(shareLink,"digest",""));
            jsonObject.put("title", JSONUtils.getString(shareLink,"title",""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    class WebService extends APIInterfaceInstance{

        @Override
        public void returnUploadResImgSuccess(
                GetNewsImgResult getNewsImgResult, String fakeMessageId) {
            LoadingDialog.dimissDlg(loadingDialog);
            String newsImgBody = getNewsImgResult.getImgMsgBody();
            key = JSONUtils.getString(newsImgBody,"key","");
            shareFilesToFriends();
        }

        @Override
        public void returnUploadResImgFail(String error, int errorCode, String fakeMessageId) {
            LoadingDialog.dimissDlg(loadingDialog);
            ToastUtils.show(ShareLinkActivity.this,getString(R.string.news_share_fail));
            finish();
        }
    }
}
