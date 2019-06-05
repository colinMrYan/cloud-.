package com.inspur.emmcloud.ui.appcenter.volume;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.system.ClearShareDataBean;
import com.inspur.emmcloud.util.privates.VolumeFilePrivilegeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * 云盘-复制到、移动到页面
 */


public class VolumeFileLocationSelectActivity extends VolumeFileBaseActivity {

    @BindView(R.id.location_select_bar_layout)
    RelativeLayout locationSelectBarLayout;
    @BindView(R.id.location_select_to_text)
    TextView locationSelectToText;
    @BindView(R.id.tv_location_select_upload_to)
    TextView locationSelectUploadToText;
    @BindView(R.id.header_operation_layout)
    RelativeLayout headerOperationLayout;
    @BindView(R.id.location_select_cancel_text)
    TextView locationSelectCancelText;
    @BindView(R.id.path_text)
    TextView pathText;

    private boolean isFunctionCopy;//判断是复制还是移动功能
    private MyAppAPIService apiService;

    private List<Uri> shareUriList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        isFunctionCopy = getIntent().getBooleanExtra("isFunctionCopy", true);
        initViews();
    }

    private void initViews() {
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        locationSelectToText.setText(isFunctionCopy ? R.string.clouddriver_copy2_current_directory : R.string.clouddriver_move2_current_directory);
        headerOperationLayout.setVisibility(View.GONE);
        locationSelectCancelText.setVisibility(View.VISIBLE);
        locationSelectBarLayout.setVisibility(View.VISIBLE);
        adapter.setShowFileOperationDropDownImg(false);
        adapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
                    Intent intent = new Intent(getApplicationContext(), VolumeFileLocationSelectActivity.class);
                    Bundle bundle = getIntent().getExtras();
                    bundle.putString("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName() + "/");
                    bundle.putString("title", volumeFile.getName());
                    intent.putExtras(bundle);
                    startActivityForResult(intent, isFunctionCopy ? REQUEST_COPY_FILE : REQUEST_MOVE_FILE);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        pathText.setVisibility(View.VISIBLE);
        pathText.setText(getString(R.string.clouddriver_current_directory_hint, currentDirAbsolutePath));
        List<Uri> fileShareUriList = (List<Uri>) getIntent().getSerializableExtra(Constant.SHARE_FILE_URI_LIST);
        if (fileShareUriList != null) {
            shareUriList.addAll(fileShareUriList);
        }
        locationSelectUploadToText.setVisibility(shareUriList.size() > 0 ? View.VISIBLE : View.GONE);
        locationSelectToText.setVisibility(shareUriList.size() > 0 ? View.GONE : View.VISIBLE);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
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
                    ToastUtils.show(getApplicationContext(), R.string.file_exist_current_directory);
                    return;
                }

                List<VolumeFile> operationFileList = (List<VolumeFile>) getIntent().getSerializableExtra("volumeFileList");
                for (int i = 0; i < operationFileList.size(); i++) {
                    String volumeFilePath = operationFileAbsolutePath + operationFileList.get(i).getName();
                    if (currentDirAbsolutePath.startsWith(volumeFilePath)) {
                        ToastUtils.show(getApplicationContext(), isFunctionCopy ? R.string.file_cannot_copy_here : R.string.file_cannot_move_here);
                        return;
                    }
                }
                if (isFunctionCopy) {
                    copyFile(operationFileAbsolutePath);
                } else {
                    moveFile(operationFileAbsolutePath);
                }
                break;
            case R.id.tv_location_select_upload_to:
                goUploadPage();
                break;
            default:
                break;
        }
    }

    /**
     * 返回上传页面
     */
    private void goUploadPage() {
        if (NetUtils.isNetworkConnected(this)) {
            //发送到ShareVolume页面和VolumeHomePage页面
            EventBus.getDefault().post(new ClearShareDataBean());
            Bundle bundle = new Bundle();
            bundle.putSerializable("volume", volume);
            bundle.putSerializable("currentDirAbsolutePath", currentDirAbsolutePath);
            bundle.putSerializable("title", title);
            bundle.putSerializable(Constant.SHARE_FILE_URI_LIST, (Serializable) shareUriList);
            IntentUtils.startActivity(VolumeFileLocationSelectActivity.this, VolumeFileActivity.class, bundle);
            closeAllThisActivityInstance();
        }
    }

    /**
     * 设置当前目录权限有关的layout展示
     */
    @Override
    protected void setCurrentDirectoryLayoutByPrivilege() {
        boolean isCurrentDirectoryWriteable = VolumeFilePrivilegeUtils.getVolumeFileWriteable(getApplicationContext(), getVolumeFileListResult);
        locationSelectBarLayout.setVisibility(isCurrentDirectoryWriteable ? View.VISIBLE : View.GONE);
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
            if (requestCode == REQUEST_MOVE_FILE || requestCode == REQUEST_COPY_FILE) {
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
            loadingDlg.show();
            String path = currentDirAbsolutePath;
            if (currentDirAbsolutePath.length() > 1) {
                path = currentDirAbsolutePath.substring(0, currentDirAbsolutePath.length() - 1);
            }
            List<VolumeFile> moveVolumeFileList = (List<VolumeFile>) getIntent().getSerializableExtra("volumeFileList");
            apiService.copyVolumeFile(volume.getId(), operationFileAbsolutePath, moveVolumeFileList, path);
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
            LoadingDialog.dimissDlg(loadingDlg);
            //将移动的位置传递回去，以便于当前页面刷新数据
            sendVolumeFileRefreshBroadcast(getVolumeFileListResult.getId());
            sendVolumeFileRefreshBroadcast(movedVolumeFileList.get(0).getParent());
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void returnMoveFileFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnCopyFileSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            //将移动的位置传递回去，以便于当前页面刷新数据
            sendVolumeFileRefreshBroadcast(getVolumeFileListResult.getId());
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void returnCopyFileFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
