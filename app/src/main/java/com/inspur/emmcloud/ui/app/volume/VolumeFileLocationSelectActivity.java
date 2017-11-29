package com.inspur.emmcloud.ui.app.volume;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;

import org.xutils.view.annotation.ViewInject;

import java.util.List;


/**
 * 云盘-复制到、移动到
 */


public class VolumeFileLocationSelectActivity extends VolumeFileBaseActivity {

    @ViewInject(R.id.header_operation_layout)
    private RelativeLayout headerOperationLayout;

    @ViewInject(R.id.location_select_cancel_text)
    private TextView locationSelectCancelText;

    @ViewInject(R.id.location_select_to_text)
    private TextView locationSelectToText;

    @ViewInject(R.id.location_select_bar_layout)
    private RelativeLayout locationSelectBarLayout;

    private boolean isFunctionCopy;//判断是复制还是移动功能

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置此界面只显示文件夹
        this.isShowDirectoryOnly = true;
        isFunctionCopy = getIntent().getBooleanExtra("isFunctionCopy",true);
        initViews();

    }

    private void initViews(){
        locationSelectToText.setText(isFunctionCopy?"复制到":"移动到当前目录");
        headerOperationLayout.setVisibility(View.GONE);
        locationSelectCancelText.setVisibility(View.VISIBLE);
        locationSelectBarLayout.setVisibility(View.VISIBLE);
        adapter.setShowFileOperationDropDownImg(false);
        adapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("volume", volume);
                bundle.putString("absolutePath", absolutePath + volumeFile.getName() + "/");
                bundle.putString("title", volumeFile.getName());
                bundle.putBoolean("isFunctionCopy",true);
                IntentUtils.startActivity(VolumeFileLocationSelectActivity.this, VolumeFileLocationSelectActivity.class, bundle);

            }
        });
    }

    /**
     * 初始化无数据时显示的ui
     */
    protected void initDataBlankLayoutStatus() {
        dataBlankLayout.setVisibility((volumeFileList.size() == 0) ? View.VISIBLE : View.GONE);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.new_forder_img:
                showCreateFolderDlg();
                break;
            case R.id.refresh_btn:
                getVolumeFileList(true);
                break;

            case R.id.location_select_cancel_text:
                closeAllThisActivityInstance();
                break;
            case R.id.location_select_new_forder_text:
                showCreateFolderDlg();
                break;
            case R.id.location_select_to_text:
                if (isFunctionCopy){
                    copyFile();
                }else {
                    moveFile();
                }
                break;

            default:
                break;
        }
    }

    /**
     * 关闭此Activity所有的instance
     */
    private void closeAllThisActivityInstance(){
        List<Activity> activityList = ((MyApplication)getApplicationContext()).getActivityList();
        for (int i=0;i<activityList.size();i++){
            Activity activity = activityList.get(i);
            if (activity != null && activity instanceof  VolumeFileLocationSelectActivity){
                activity.finish();
            }
        }
    }

    /**
     * 复制文件
     */
    private void copyFile(){
        if (NetUtils.isNetworkConnected(getApplicationContext())){

        }
    }

    /**
     * 移动文件
     */
    private void moveFile(){
        if (NetUtils.isNetworkConnected(getApplicationContext())){

        }
    }
}
