package com.inspur.emmcloud.ui.app.volume;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;

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
    private MyAppAPIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置此界面只显示文件夹
        this.fileFilterType = VolumeFile.FILE_TYPE_DIRECTORY;
        isFunctionCopy = getIntent().getBooleanExtra("isFunctionCopy", true);
        initViews();

    }

    private void initViews() {
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        noFileText.setText("暂无文件夹");
        locationSelectToText.setText(isFunctionCopy ? "复制到" : "移动到当前目录");
        headerOperationLayout.setVisibility(View.GONE);
        locationSelectCancelText.setVisibility(View.VISIBLE);
        locationSelectBarLayout.setVisibility(View.VISIBLE);
        adapter.setShowFileOperationDropDownImg(false);
        adapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getApplicationContext(), VolumeFileLocationSelectActivity.class);
                VolumeFile volumeFile = volumeFileList.get(position);
                Bundle bundle = getIntent().getExtras();
                bundle.putString("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName() + "/");
                bundle.putString("title",volumeFile.getName() );
                intent.putExtras(bundle);
                if (!isFunctionCopy) {
                    startActivityForResult(intent, REQUEST_MOVE_FILE);
                } else {
                    startActivity(intent);
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
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
                String operationFileAbsolutePath = getIntent().getStringExtra("operationFileDirAbsolutePath");
                if (operationFileAbsolutePath.equals(currentDirAbsolutePath)) {
                    ToastUtils.show(getApplicationContext(), "该文件已在当前文件夹");
                    return;
                }

                List<VolumeFile> operationFileList = (List<VolumeFile>) getIntent().getSerializableExtra("volumeFileList");
                for (int i = 0;i<operationFileList.size();i++){
                    String volumeFilePath = operationFileAbsolutePath+operationFileList.get(i).getName();
                    if (currentDirAbsolutePath.startsWith(volumeFilePath)){
                        ToastUtils.show(getApplicationContext(), isFunctionCopy?"不能将文件复制到自身或其子目录下":"不能将文件移动到自身或其子目录下");
                        return;
                    }
                }
                if (isFunctionCopy) {
                    copyFile(operationFileAbsolutePath);
                } else {
                    moveFile(operationFileAbsolutePath);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 关闭此Activity所有的instance
     */
    private void closeAllThisActivityInstance() {
        List<Activity> activityList = ((MyApplication) getApplicationContext()).getActivityList();
        for (int i = 0; i < activityList.size(); i++) {
            Activity activity = activityList.get(i);
            if (activity != null && activity instanceof VolumeFileLocationSelectActivity) {
                activity.finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_MOVE_FILE) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    /**
     * 复制文件
     */
    private void copyFile(String operationFileAbsolutePath) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {

        }
    }

    /**
     * 移动文件
     *
     * @param operationFileAbsolutePath
     */
    private void moveFile(String operationFileAbsolutePath) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            String path = currentDirAbsolutePath;
            if (currentDirAbsolutePath.length() > 1) {
                path = currentDirAbsolutePath.substring(0, currentDirAbsolutePath.length() - 1);
            }
            List<VolumeFile> moveVolumeFileList = (List<VolumeFile>) getIntent().getSerializableExtra("volumeFileList");
            apiService.moveVolumeFile(volume.getId(), operationFileAbsolutePath, moveVolumeFileList, path);
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMoveFileSuccess(List<VolumeFile> movedVolumeFileList) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            //将移动的位置传递回去，以便于当前页面刷新数据
            Intent intent = new  Intent();
            intent.putExtra("path", currentDirAbsolutePath);
            intent.putExtra("command","refresh");
            intent.setAction("broadcast_volume");
            sendBroadcast(intent);
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void returnMoveFileFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
