package com.inspur.emmcloud.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.appcenter.groupnews.NewsWebDetailActivity;
import com.inspur.emmcloud.ui.appcenter.volume.VolumeHomePageActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.TabAndAppExistUtils;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

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
    @ViewInject(R.id.img_volume_share_icon)
    private ImageView volumeShareIcon;
    private List<Uri> uriList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isLogin()) {
//            handleShareIntent();
            this.uriList.addAll((List<Uri>) getIntent().getSerializableExtra("fileShareUriList"));
        } else {
            MyApplication.getInstance().signout();
        }
//        initSharingMode();
        if(isFileUriList(uriList) && uriList.size() > 1){
            ToastUtils.show(ShareFilesActivity.this,getString(R.string.share_mutil_only_support_image));
            startIndexActivity();
        }else if(isImageUriList(uriList) && uriList.size() > 5){
            ToastUtils.show(ShareFilesActivity.this,getString(R.string.share_no_more_than_five));
            startIndexActivity();
        }else{
            initViews();
        }
    }

    /**
     * 启动index
     */
    private void startIndexActivity() {
        Intent intent = new Intent();
        intent.setClass(ShareFilesActivity.this,IndexActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 分享方式
     */
    private void initSharingMode() {
        boolean isCommunicateExist = TabAndAppExistUtils.isTabExist(ShareFilesActivity.this,"communicate");
        boolean isVolumeAppExist = TabAndAppExistUtils.isAppExist(ShareFilesActivity.this,"9eb097d0-d994-11e7-a6dd-8f4ea6776516");
        channelRelativeLayout.setVisibility(isCommunicateExist?View.VISIBLE:View.GONE);
        volumeRelativeLayout.setVisibility(isVolumeAppExist?View.VISIBLE:View.GONE);
        viewLineVolume.setVisibility((isCommunicateExist&&isVolumeAppExist)?View.VISIBLE:View.GONE);
        if(!(isCommunicateExist || isVolumeAppExist)){
            ToastUtils.show(ShareFilesActivity.this,getString(R.string.share_no_share_way));
        }
    }

    private void initViews() {
        ImageDisplayUtils.getInstance().displayImage(imageView, TabAndAppExistUtils.getVolumeImgUrl(ShareFilesActivity.this,
                "9eb097d0-d994-11e7-a6dd-8f4ea6776516"), R.drawable.ic_app_default);
        int uriListSize = uriList.size();
        switch (uriListSize) {
            case 0:
                finish();
                break;
            case 1:
                imageView.setVisibility(View.VISIBLE);
                if(isFileUriList(uriList)){
                    String filePath = GetPathFromUri4kitkat.getPathByUri(ShareFilesActivity.this,uriList.get(0));
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
    private boolean isImageUriList(List<Uri> uriList) {
        for (int i = 0; i < uriList.size(); i++) {
            if (!uriList.get(i).toString().contains("images")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是FileUri
     *
     * @param uriList
     * @return
     */
    private boolean isFileUriList(List<Uri> uriList) {
        for (int i = 0; i < uriList.size(); i++) {
            if (!uriList.get(i).toString().contains("file")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否已经在登录状态
     *
     * @return
     */
    private boolean isLogin() {
        String accessToken = PreferencesUtils.getString(
                ShareFilesActivity.this, "accessToken", "");
        return !StringUtils.isBlank(accessToken);
    }

    /**
     * 处理带分享功能的Action
     */
    private void handleShareIntent() {
//        String action = getIntent().getAction();
//        List<Uri> uriList = new ArrayList<>();
//        if (Intent.ACTION_SEND.equals(action)) {
//            Uri uri = FileUtils.getShareFileUri(getIntent());
//            if (uri != null) {
//                uriList.add(uri);
//            }
//        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
//            List<Uri> fileUriList = FileUtils.getShareFileUriList(getIntent());
//            uriList.addAll(fileUriList);
//        }
//        for (int i = 0; i < uriList.size(); i++) {
//            LogUtils.YfcDebug("分享的文件路径：" + uriList.get(i));
//        }
        this.uriList.addAll((List<Uri>) getIntent().getSerializableExtra("fileShareUriList"));
    }

    /**
     * @param uriList
     */
    private void startVolumeShareActivity(List<Uri> uriList) {
        Intent intent = new Intent();
        intent.setClass(ShareFilesActivity.this, VolumeHomePageActivity.class);
        intent.putExtra("fileShareUriList", (Serializable) uriList);
        startActivity(intent);
        finish();
    }

    /**
     * 给朋友分享图片或文件
     */
    private void shareNewsToFrinds() {
        Intent intent = new Intent();
        intent.putExtra("select_content", 0);
        intent.putExtra("isMulti_select", false);
        intent.putExtra("isContainMe", true);
        intent.putExtra("title", getString(R.string.news_share));
        intent.setClass(getApplicationContext(),
                ContactSearchActivity.class);
        startActivityForResult(intent, SHARE_IMAGE_OR_FILES);
        dialog.dismiss();
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
                        sendMsg(cid);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.show(ShareFilesActivity.this,getString(R.string.news_share_fail));
            }
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
