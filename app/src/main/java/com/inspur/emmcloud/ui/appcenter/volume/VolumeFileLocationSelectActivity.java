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
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.roundbutton.CustomRoundButton;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.util.privates.VolumeFilePrivilegeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

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
    List<VolumeFile> copyOrMoveErrorFiles = new ArrayList<>();
    private boolean isFunctionCopy;//判断是复制还是移动功能
    private MyAppAPIService apiService;
    private List<Uri> shareUriList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        isFunctionCopy = getIntent().getBooleanExtra(VolumeFileBaseActivity.EXTRA_IS_FUNCTION_COPY_OR_MOVE, true);
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
                intentResult.putExtra("copyFailedFiles", (Serializable) copyOrMoveErrorFiles);
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
                copyOrMoveFileBetweenVolume(operationFileAbsolutePath, isFunctionCopy ? "copy" : "move");
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

    private void copyOrMoveFileBetweenVolume(String fileOrgPath, String operation) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            List<VolumeFile> copyOrMoveVolumeFileList = (List<VolumeFile>) getIntent().getSerializableExtra(EXTRA_VOLUME_FILE_LIST);
            String path = currentDirAbsolutePath;
            if (currentDirAbsolutePath.length() > 1) {
                path = currentDirAbsolutePath.substring(0, currentDirAbsolutePath.length() - 1);
            }
            apiService.copyOrMoveFileBetweenVolume(copyOrMoveVolumeFileList, fromVolume.getId(), volume.getId(), operation, fileOrgPath, path);
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnMoveOrCopyFileBetweenVolumeSuccess(String operation) {
            LoadingDialog.dimissDlg(loadingDlg);
            Intent intentResult = new Intent();
            intentResult.putExtra("copyFailedFiles", (Serializable) copyOrMoveErrorFiles);
            setResult(RESULT_OK, intentResult);
            ToastUtils.show(getString(operation.equals("copy") ? R.string.volume_copy_success : R.string.volume_move_success));
            finish();
        }

        @Override
        public void returnMoveOrCopyFileBetweenVolumeFail(String error, int errorCode, String srcVolumeFilePath, String operation, List<VolumeFile> volumeFileList) {
            try {
                JSONArray arrayData = JSONUtils.getJSONArray(error, new JSONArray());
                for (int i = 0; i < 10; i++) {
                    if (arrayData.isNull(i)) {
                        if (i == 0) {
                            copyOrMoveErrorFiles.addAll(volumeFileList);
                        }
                        break;
                    }
                    JSONObject jsonObject = arrayData.getJSONObject(i);
                    boolean operationSuccess = jsonObject.getBoolean("success");
                    String operationFailFile = jsonObject.getString("source");
                    if (!operationSuccess && !StringUtils.isBlank(operationFailFile)) {
                        for (int i1 = 0; i1 < volumeFileList.size(); i1++) {
                            if (operationFailFile.equals(srcVolumeFilePath + volumeFileList.get(i1).getName())) {
                                copyOrMoveErrorFiles.add(volumeFileList.get(i1));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            LoadingDialog.dimissDlg(loadingDlg);
            Intent intentResult = new Intent();
            intentResult.putExtra("operationFailedFiles", (Serializable) copyOrMoveErrorFiles);
            setResult(RESULT_OK, intentResult);
            ToastUtils.show(getString(operation.equals("copy") ? R.string.volume_copy_fail : R.string.volume_move_fail));
            finish();
        }
    }
}
