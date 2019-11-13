package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.roundbutton.CustomRoundButton;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.util.privates.VolumeFilePrivilegeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 云盘-复制到、移动到页面
 */


public class VolumeFileLocationSelectActivity extends VolumeFileBaseActivity {

    @BindView(R.id.bottom_layout)
    RelativeLayout locationSelectBarLayout;
    @BindView(R.id.btn_location_select_to)
    CustomRoundButton locationSelectToBtn;
    @BindView(R.id.btn_location_select_upload_to)
    CustomRoundButton locationSelectUploadToBtn;
    @BindView(R.id.header_operation_layout)
    RelativeLayout headerOperationLayout;
    @BindView(R.id.location_select_cancel_text)
    TextView locationSelectCancelText;
    @BindView(R.id.path_text)
    TextView pathText;
    List<VolumeFile> copyErrorFiles = new ArrayList<>();
    private boolean isFunctionCopy;//判断是复制还是移动功能
    private MyAppAPIService apiService;
    private List<Uri> shareUriList = new ArrayList<>();
    private int copyFileSize = 0;
    private int copyReturnFileSize = 0;
    private int returnErrorFileSize = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        isFunctionCopy = getIntent().getBooleanExtra("isFunctionCopy", true);
        initViews();
    }

    private void initViews() {
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        uploadFileBtn.setVisibility(View.GONE);
        locationSelectToBtn.setText(isFunctionCopy ? R.string.clouddriver_copy2_current_directory : R.string.clouddriver_move2_current_directory);
        headerOperationLayout.setVisibility(View.GONE);
        locationSelectCancelText.setVisibility(View.VISIBLE);
        locationSelectBarLayout.setVisibility(View.VISIBLE);
        adapter.setShowFileOperationSelcteImage(false);
        adapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
                    boolean isVolumeFileWritable = VolumeFilePrivilegeUtils.getVolumeFileWritable(getApplicationContext(), volumeFile);
                    if (isVolumeFileWritable) {
                        Intent intent = new Intent(getApplicationContext(), VolumeFileLocationSelectActivity.class);
                        Bundle bundle = getIntent().getExtras();
                        bundle.putString("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName() + "/");
                        bundle.putString("title", volumeFile.getName());
                        intent.putExtras(bundle);
                        startActivityForResult(intent, isFunctionCopy ? REQUEST_COPY_FILE : REQUEST_MOVE_FILE);
                    } else {
                        ToastUtils.show(R.string.volume_no_permission);
                    }

                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onItemDropDownImgClick(View view, int position) {

            }

            @Override
            public void onItemOperationTextClick(View view, int position) {

            }

            @Override
            public void onSelectedItemClick(View view, int position) {

            }
        });
        pathText.setVisibility(View.VISIBLE);
        pathText.setText(getString(R.string.clouddriver_current_directory_hint, currentDirAbsolutePath));
        List<Uri> fileShareUriList = (List<Uri>) getIntent().getSerializableExtra(Constant.SHARE_FILE_URI_LIST);
        if (fileShareUriList != null) {
            shareUriList.addAll(fileShareUriList);
        }
        locationSelectUploadToBtn.setVisibility(shareUriList.size() > 0 ? View.VISIBLE : View.GONE);
        locationSelectToBtn.setVisibility(shareUriList.size() > 0 ? View.GONE : View.VISIBLE);
    }

    @OnClick({R.id.btn_location_select_to, R.id.btn_location_select_upload_to})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.iv_head_operation:
                showCreateFolderDlg();
                break;

            case R.id.location_select_cancel_text:
                Intent intentResult = new Intent();
                intentResult.putExtra("copyFailedFiles", (Serializable) copyErrorFiles);
                setResult(RESULT_OK, intentResult);
                finish();
                break;
            case R.id.location_select_new_forder_text:
                showCreateFolderDlg();
                break;
            case R.id.btn_location_select_to:
                if (fromVolume == null) {
                    fromVolume = volume;
                }
                String operationFileAbsolutePath = getIntent().getStringExtra(EXTRA_OPERATION_FILE_DIR_ABS_PATH);
                if (!isFunctionCopy && operationFileAbsolutePath.equals(currentDirAbsolutePath) && volume.getId().equals(fromVolume.getId())) {
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
                    if (fromVolume != null && !fromVolume.getId().equals(volume.getId())) {
                        copyFileBetweenVolume(operationFileAbsolutePath);
                    } else {
                        copyFile(operationFileAbsolutePath);
                    }
                } else {
                    moveFile(operationFileAbsolutePath);
                }
                break;
            case R.id.btn_location_select_upload_to:
                goUploadPage();
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_FILE_LOCATION_SELECT_CLOSE)) {
            finish();
        }
    }

    /**
     * 返回上传页面
     */
    private void goUploadPage() {
        if (NetUtils.isNetworkConnected(this)) {
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
        boolean isCurrentDirectoryWritable = VolumeFilePrivilegeUtils.getVolumeFileWritable(getApplicationContext(), getVolumeFileListResult, volume);
        locationSelectBarLayout.setVisibility(isCurrentDirectoryWritable ? View.VISIBLE : View.GONE);
    }

    /**
     * 关闭此Activity所有的instance
     */
    private void closeAllThisActivityInstance() {
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_VOLUME_FILE_LOCATION_SELECT_CLOSE));
//        List<Activity> activityList = ((MyApplication) getApplicationContext()).getActivityList();
//        for (int i = 0; i < activityList.size(); i++) {
//            Activity activity = activityList.get(i);
//            if (activity != null && activity instanceof VolumeFileLocationSelectActivity) {
//                activity.finish();
//            }
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_MOVE_FILE || requestCode == REQUEST_COPY_FILE) {
                setResult(RESULT_OK, data);
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
     * 夸网盘复制
     **/
    private void copyFileBetweenVolume(String fileOrgPath) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            copyReturnFileSize = 0;
            returnErrorFileSize = 0;
            loadingDlg.show();
            List<VolumeFile> moveVolumeFileList = (List<VolumeFile>) getIntent().getSerializableExtra(EXTRA_VOLUME_FILE_LIST);
            copyFileSize = moveVolumeFileList.size();
            String path = currentDirAbsolutePath;
            if (currentDirAbsolutePath.length() > 1) {
                path = currentDirAbsolutePath.substring(0, currentDirAbsolutePath.length() - 1);
            }
            for (int i = 0; i < moveVolumeFileList.size(); i++) {
                String srcVolumeFilePath = fileOrgPath + moveVolumeFileList.get(i).getName();
                apiService.copyFileBetweenVolume(moveVolumeFileList.get(i), fromVolume.getId(), volume.getId(), srcVolumeFilePath, path);
            }
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

    /**
     * 复制异常提醒Toast
     **/
    private void copyErrorToastShow() {
        if (returnErrorFileSize != 0) {
            String showErrorDetail = String.format(getString(R.string.volume_copy_between_volume_error_toast), returnErrorFileSize);
            ToastUtils.show(showErrorDetail);
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
            Intent intentResult = new Intent();
            intentResult.putExtra("copyFailedFiles", (Serializable) copyErrorFiles);
            setResult(RESULT_OK, intentResult);
            finish();
        }

        @Override
        public void returnCopyFileFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            returnErrorFileSize = ((List<VolumeFile>) getIntent().getSerializableExtra("volumeFileList")).size();
            copyErrorToastShow();
            Intent intentResult = new Intent();
            intentResult.putExtra("copyFailedFiles", getIntent().getSerializableExtra("volumeFileList"));
            setResult(RESULT_OK, intentResult);
            finish();
        }

        @Override
        public void returnCopyFileBetweenVolumeSuccess() {
            copyReturnFileSize++;
            if (copyReturnFileSize == copyFileSize) {
                copyErrorToastShow();
                LoadingDialog.dimissDlg(loadingDlg);
                Intent intentResult = new Intent();
                intentResult.putExtra("copyFailedFiles", (Serializable) copyErrorFiles);
                setResult(RESULT_OK, intentResult);
                finish();
            }
        }

        @Override
        public void returnCopyFileBetweenVolumeFail(String error, int errorCode, VolumeFile volumeFile) {
            copyReturnFileSize++;
            returnErrorFileSize++;
            copyErrorFiles.add(volumeFile);
            if (copyReturnFileSize == copyFileSize) {
                copyErrorToastShow();
                LoadingDialog.dimissDlg(loadingDlg);
                Intent intentResult = new Intent();
                intentResult.putExtra("copyFailedFiles", (Serializable) copyErrorFiles);
                setResult(RESULT_OK, intentResult);
                finish();
            }
        }
    }
}
