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

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.appcenter.volume.VolumeHomePageActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/5/12.
 */
@ContentView(R.layout.activity_share_files)
public class ShareFilesActivity extends BaseActivity{

    @ViewInject(R.id.rv_file_list)
    private RecyclerView recyclerView;
    @ViewInject(R.id.img_file_icon)
    private ImageView imageView;
    @ViewInject(R.id.rl_channel_share)
    private RelativeLayout channelRelativeLayout;
    @ViewInject(R.id.rl_volume_share)
    private RelativeLayout volumeRelativeLayout;
    private List<Uri> uriArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(isLogin()){
            handleShareIntent();
        }else {
            MyApplication.getInstance().signout();
        }
        initViews();
    }

    private void initViews() {
        recyclerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(ShareFilesActivity.this, 11)));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ShareFilesActivity.this, 5);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new ShareFilesAdapter());
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
        }
    }

    /**
     * 判断是ImageUri
     * @param uriList
     * @return
     */
    private boolean isImageUriList(List<Uri> uriList){
        for (int i = 0; i < uriList.size(); i++) {
            if(!uriList.get(i).toString().contains("images")){
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是FileUri
     * @param uriList
     * @return
     */
    private boolean isFileUriList(List<Uri> uriList){
        for (int i = 0; i < uriList.size(); i++) {
            if(!uriList.get(i).toString().contains("file")){
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否已经在登录状态
     * @return
     */
    private boolean isLogin(){
        String accessToken = PreferencesUtils.getString(
                ShareFilesActivity.this, "accessToken", "");
        return !StringUtils.isBlank(accessToken);
    }

    /**
     * 处理带分享功能的Action
     */
    private void handleShareIntent() {
        String action = getIntent().getAction();
        List<Uri> uriList = new ArrayList<>();
        if (Intent.ACTION_SEND.equals(action)) {
            Uri uri = FileUtils.getShareFileUri(getIntent());
            if (uri != null) {
                uriList.add(uri);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            List<Uri> fileUriList = FileUtils.getShareFileUriList(getIntent());
            uriList.addAll(fileUriList);
        }
        for (int i = 0; i < uriList.size() ; i++) {
            LogUtils.YfcDebug("分享的文件路径："+uriList.get(i));
        }
        uriArrayList.addAll(uriList);
//        LogUtils.YfcDebug("分享文件的列表长度："+uriList.size());
//        if (uriList.size() > 0) {
//            startVolumeShareActivity(uriList);
//        }
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

    class ShareFilesAdapter extends RecyclerView.Adapter<FileHolder>{
        LayoutInflater inflater;
        public ShareFilesAdapter(){
            inflater = LayoutInflater.from(ShareFilesActivity.this);
        }

        @Override
        public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.app_center_recommand_app_item, null);
            FileHolder holder = new FileHolder(view);
            holder.imageView = (ImageView) view.findViewById(R.id.img_share_file);
            return holder;
        }

        @Override
        public void onBindViewHolder(FileHolder holder, int position) {
            ImageDisplayUtils.getInstance().displayImage(holder.imageView, uriArrayList.get(position).toString(), R.drawable.ic_app_default);
        }

        @Override
        public int getItemCount() {
            return uriArrayList.size();
        }
    }

    class FileHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        public FileHolder(View itemView) {
            super(itemView);
        }
    }
}
