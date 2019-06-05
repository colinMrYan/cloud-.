package com.inspur.emmcloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.ui.appcenter.volume.VolumeHomePageActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.TabAndAppExistUtils;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/5/12.
 */
public class ShareFilesActivity extends BaseActivity {

    private final static int SHARE_IMAGE_OR_FILES = 0;
    @BindView(R.id.rv_file_list)
    RecyclerView recyclerView;
    @BindView(R.id.img_file_icon)
    ImageView imageView;
    @BindView(R.id.rl_channel_share)
    RelativeLayout channelRelativeLayout;
    @BindView(R.id.rl_volume_share)
    RelativeLayout volumeRelativeLayout;
    @BindView(R.id.view_line_volume)
    View viewLineVolume;
    private List<String> uriList = new ArrayList<>();

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        this.uriList.addAll((List<String>) getIntent().getSerializableExtra(Constant.SHARE_FILE_URI_LIST));
        if (!isImageUriList(uriList)) {
            if (uriList.size() <= 1) {
                File file = new File(uriList.get(0));
                if (StringUtils.isBlank(FileUtils.getSuffix(file))) {
                    ToastUtils.show(ShareFilesActivity.this, getString(R.string.share_no_suffix));
                    finish();
                }
            } else {
                ToastUtils.show(ShareFilesActivity.this, getString(R.string.share_mutil_only_support_image));
                finish();
            }
        } else if (isImageUriList(uriList) && uriList.size() > 5) {
            ToastUtils.show(ShareFilesActivity.this, getString(R.string.share_no_more_than_five));
            finish();
        }
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_share_files;
    }

    /**
     * 分享方式
     */
    private void initSharingMode() {
        boolean isCommunicateExist = TabAndAppExistUtils.isTabExist(MyApplication.getInstance(), Constant.APP_TAB_BAR_COMMUNACATE);
        boolean isVolumeAppExist = TabAndAppExistUtils.isAppExist(MyApplication.getInstance(), "emm://volume");
        channelRelativeLayout.setVisibility(isCommunicateExist ? View.VISIBLE : View.GONE);
        volumeRelativeLayout.setVisibility(isVolumeAppExist ? View.VISIBLE : View.GONE);
        viewLineVolume.setVisibility((isCommunicateExist && isVolumeAppExist) ? View.VISIBLE : View.GONE);
        if (!(isCommunicateExist || isVolumeAppExist)) {
            ToastUtils.show(MyApplication.getInstance(), getString(R.string.share_no_share_way));
        }
    }

    private void initViews() {
        initSharingMode();
        ImageDisplayUtils.getInstance().displayImage(imageView, TabAndAppExistUtils.getVolumeIconUrl(MyApplication.getInstance(),
                "emm://volume"), R.drawable.ic_app_default);
        int uriListSize = uriList.size();
        switch (uriListSize) {
            case 0:
                finish();
                break;
            case 1:
                imageView.setVisibility(View.VISIBLE);
                if (!isImageUriList(uriList)) {
                    String filePath = uriList.get(0);
                    ImageDisplayUtils.getInstance().displayImage(imageView, "drawable://" + FileUtils.getRegularFileIconResId(filePath));
                } else {
                    ImageDisplayUtils.getInstance().displayImage(imageView, uriList.get(0).toString(), R.drawable.ic_app_default);
                }
                break;
            default:
                recyclerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(MyApplication.getInstance(), 11)));
                GridLayoutManager gridLayoutManager = new GridLayoutManager(MyApplication.getInstance(), 3);
                recyclerView.setLayoutManager(gridLayoutManager);
                recyclerView.setAdapter(new ShareFilesAdapter());
                break;
        }

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_channel_share:
                shareFilesToFriends();
                break;
            case R.id.rl_volume_share:
                startVolumeShareActivity(uriList);
                break;
        }
    }

    /**
     * 判断是ImageUri
     *
     * @param uriList
     * @return
     */
    private boolean isImageUriList(List<String> uriList) {
        for (int i = 0; i < uriList.size(); i++) {
            String uriString = uriList.get(i).toString().toLowerCase();
            if (!uriString.endsWith("png") && !uriString.endsWith("jpg") && !uriString.endsWith("jpeg")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 分享功能中不再判断文件来源（例如来自文件系统还是图库）
     *
     * @param uriList
     */
    private void startVolumeShareActivity(List<String> uriList) {
        if (FileUtils.isFileInListExist(uriList)) {
            Intent intent = new Intent();
            intent.setClass(ShareFilesActivity.this, VolumeHomePageActivity.class);
            intent.putExtra(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
            startActivity(intent);
        } else {
            ToastUtils.show(MyApplication.getInstance(), getString(R.string.share_has_not_exist_file));
        }
        finish();
    }

    /**
     * 给朋友分享图片或文件
     */
    private void shareFilesToFriends() {
        Intent intent = new Intent();
        intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 0);
        intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, false);
        ArrayList<String> uidList = new ArrayList<>();
        uidList.add(MyApplication.getInstance().getUid());
        intent.putStringArrayListExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, uidList);
        intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.news_share));
        intent.setClass(getApplicationContext(),
                ContactSearchActivity.class);
        startActivityForResult(intent, SHARE_IMAGE_OR_FILES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHARE_IMAGE_OR_FILES && resultCode == RESULT_OK
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
                ToastUtils.show(MyApplication.getInstance(), getString(R.string.news_share_fail));
            }
        }
    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        bundle.putString("share_type", isImageUriList(uriList) ? "image" : "file");
        bundle.putSerializable("share_paths", (Serializable) uriList);
        IntentUtils.startActivity(ShareFilesActivity.this, WebServiceRouterManager.getInstance().isV0VersionChat() ?
                ChannelV0Activity.class : ConversationActivity.class, bundle, true);
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(ShareFilesActivity.this, uid,
                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            startChannelActivity(conversation.getId());
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(ShareFilesActivity.this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            startChannelActivity(getCreateSingleChannelResult.getCid());
                        }

                        @Override
                        public void createDirectChannelFail() {
                        }
                    });
        }

    }


    class ShareFilesAdapter extends RecyclerView.Adapter<FileHolder> {
        LayoutInflater inflater;

        public ShareFilesAdapter() {
            inflater = LayoutInflater.from(ShareFilesActivity.this);
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.share_file_item, null);
            FileHolder holder = new FileHolder(view);
            holder.imageView = (ImageView) view.findViewById(R.id.img_share_file);
            return holder;
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            ImageDisplayUtils.getInstance().displayImage(holder.imageView, uriList.get(position).toString(), R.drawable.ic_app_default);
        }

        @Override
        public int getItemCount() {
            return uriList.size();
        }
    }

    class FileHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public FileHolder(View itemView) {
            super(itemView);
        }
    }
}
