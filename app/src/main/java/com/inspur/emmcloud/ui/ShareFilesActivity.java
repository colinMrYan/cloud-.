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
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.ui.appcenter.volume.VolumeHomePageActivity;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.chat.ChannelV0Activity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TabAndAppExistUtils;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/5/12.
 */
@ContentView(R.layout.activity_share_files)
public class ShareFilesActivity extends BaseActivity {

    private final static int SHARE_IMAGE_OR_FILES = 0;
    @ViewInject(R.id.rv_file_list)
    private RecyclerView recyclerView;
    @ViewInject(R.id.img_file_icon)
    private ImageView imageView;
    @ViewInject(R.id.rl_channel_share)
    private RelativeLayout channelRelativeLayout;
    @ViewInject(R.id.rl_volume_share)
    private RelativeLayout volumeRelativeLayout;
    @ViewInject(R.id.view_line_volume)
    private View viewLineVolume;
    private List<String> uriList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.uriList.addAll((List<String>) getIntent().getSerializableExtra("fileShareUriList"));
        initSharingMode();
        if(!isImageUriList(uriList)){
            if(uriList.size() > 1){
                ToastUtils.show(ShareFilesActivity.this,getString(R.string.share_mutil_only_support_image));
                finish();
            }
            File file = new File(uriList.get(0));
            if(StringUtils.isBlank(FileUtils.getSuffix(file))){
                ToastUtils.show(ShareFilesActivity.this,getString(R.string.share_no_suffix));
                finish();
            }
        }else if(isImageUriList(uriList) && uriList.size() > 5){
            ToastUtils.show(ShareFilesActivity.this,getString(R.string.share_no_more_than_five));
            finish();
        }
        initViews();
    }


    /**
     * 分享方式
     */
    private void initSharingMode() {
        boolean isCommunicateExist = TabAndAppExistUtils.isTabExist(ShareFilesActivity.this,"communicate");
        boolean isVolumeAppExist = TabAndAppExistUtils.isAppExist(ShareFilesActivity.this,"emm://volume");
        channelRelativeLayout.setVisibility(isCommunicateExist?View.VISIBLE:View.GONE);
        volumeRelativeLayout.setVisibility(isVolumeAppExist?View.VISIBLE:View.GONE);
        viewLineVolume.setVisibility((isCommunicateExist&&isVolumeAppExist)?View.VISIBLE:View.GONE);
        if(!(isCommunicateExist || isVolumeAppExist)){
            ToastUtils.show(ShareFilesActivity.this,getString(R.string.share_no_share_way));
        }
    }

    private void initViews() {
        ImageDisplayUtils.getInstance().displayImage(imageView, TabAndAppExistUtils.getVolumeImgUrl(ShareFilesActivity.this,
                "emm://volume"), R.drawable.ic_app_default);
        int uriListSize = uriList.size();
        switch (uriListSize) {
            case 0:
                finish();
                break;
            case 1:
                imageView.setVisibility(View.VISIBLE);
                if(!isImageUriList(uriList)){
                    String filePath = uriList.get(0);
                    ImageDisplayUtils.getInstance().displayImage(imageView, "drawable://" + FileUtils.getRegularFileIconResId(filePath));
                }else{
                    ImageDisplayUtils.getInstance().displayImage(imageView, uriList.get(0).toString(), R.drawable.ic_app_default);
                }
                break;
            default:
                recyclerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(ShareFilesActivity.this, 11)));
                GridLayoutManager gridLayoutManager = new GridLayoutManager(ShareFilesActivity.this, 3);
                recyclerView.setLayoutManager(gridLayoutManager);
                recyclerView.setAdapter(new ShareFilesAdapter());
                break;
        }

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
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
     * @param uriList
     */
    private void startVolumeShareActivity(List<String> uriList) {
        Intent intent = new Intent();
        intent.setClass(ShareFilesActivity.this, VolumeHomePageActivity.class);
        intent.putExtra("fileShareUriList", (Serializable) uriList);
        startActivity(intent);
        finish();
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
                ToastUtils.show(ShareFilesActivity.this,getString(R.string.news_share_fail));
            }
        }
    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid){
            Bundle bundle = new Bundle();
            bundle.putString("cid",cid);
            bundle.putString("share_type",isImageUriList(uriList)?"image":"file");
            bundle.putSerializable("share_paths", (Serializable) uriList);
            IntentUtils.startActivity(ShareFilesActivity.this, APIUri.isV0VersionChat()?
                    ChannelV0Activity.class: ChannelActivity.class,bundle,true);
    }



    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        new ChatCreateUtils().createDirectChannel(ShareFilesActivity.this, uid,
                new ChatCreateUtils.OnCreateDirectChannelListener() {
                    @Override
                    public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                        startChannelActivity(getCreateSingleChannelResult.getCid());
                    }

                    @Override
                    public void createDirectChannelFail() {
                        ToastUtils.show(ShareFilesActivity.this,getString(R.string.news_share_fail));
                    }
                });
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
