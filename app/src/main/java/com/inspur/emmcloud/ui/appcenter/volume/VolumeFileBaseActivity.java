package com.inspur.emmcloud.ui.appcenter.volume;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeGroupResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeGroupContainMe;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.VolumeFilePrivilegeUtils;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManagerUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.VolumeGroupContainMeCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 云盘-文件操作基础类
 */

@ContentView(R.layout.activity_volume_file)
public class VolumeFileBaseActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    protected static final int REQUEST_MOVE_FILE = 5;
    protected static final int REQUEST_COPY_FILE = 6;
    protected static final String SORT_BY_NAME_UP = "sort_by_name_up";
    protected static final String SORT_BY_NAME_DOWN = "sort_by_name_down";
    protected static final String SORT_BY_TIME_UP = "sort_by_time_up";
    protected static final String SORT_BY_TIME_DOWN = "sort_by_time_down";

    @ViewInject(R.id.header_text)
    protected TextView headerText;

    @ViewInject(R.id.header_operation_layout)
    protected RelativeLayout headerOperationLayout;

    @ViewInject(R.id.lv_file)
    protected RecyclerView fileRecycleView;

    @ViewInject(R.id.refresh_layout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @ViewInject(R.id.data_blank_layout)
    protected LinearLayout dataBlankLayout;

    protected LoadingDialog loadingDlg;
    protected VolumeFileAdapter adapter;
    protected List<VolumeFile> volumeFileList = new ArrayList<>();//云盘列表
    protected Volume volume;
    protected String currentDirAbsolutePath;//当前文件夹路径
    protected String sortType = "sort_by_name_up";
    protected String fileFilterType = "";  //显示的文件类型
    protected boolean isShowFileUploading = false;  //是否显示正在上传的文件
    protected GetVolumeFileListResult getVolumeFileListResult;
    protected String title = "";
    private List<VolumeFile> moveVolumeFileList = new ArrayList<>();//移动的云盘文件列表
    private MyAppAPIService apiServiceBase;
    private Dialog fileRenameDlg, createFolderDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getVolumeFileList(true);

    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiServiceBase = new MyAppAPIService(VolumeFileBaseActivity.this);
        apiServiceBase.setAPIInterface(new WebServiceBase());
        volume = (Volume) getIntent().getSerializableExtra("volume");
        currentDirAbsolutePath = getIntent().getExtras().getString("currentDirAbsolutePath", "/");
        fileFilterType = getIntent().getExtras().getString("fileFilterType", "");
        title = getIntent().getExtras().getString("title", "");
        headerText.setVisibility(View.VISIBLE);
        headerText.setText(title);
        initRecycleView();
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecycleView() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.header_bg_blue), ContextCompat.getColor(getApplicationContext(), R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(this);
        fileRecycleView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VolumeFileAdapter(this, volumeFileList);
        fileRecycleView.setAdapter(adapter);
    }


    /**
     * 弹出文件操作框
     *
     * @param volumeFile
     */
    protected void showFileOperationDlg(final VolumeFile volumeFile) {
        boolean isVolumeFileWriteable = VolumeFilePrivilegeUtils.getVolumeFileWriteable(getApplicationContext(), volumeFile);
        boolean isVolumeFileDirectory = volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY);
        new ActionSheetDialog.ActionListSheetBuilder(VolumeFileBaseActivity.this)
                .setTitle(volumeFile.getName())
                .addItem(getString(R.string.delete), isVolumeFileWriteable)
                .addItem(getString(R.string.download), !isVolumeFileDirectory)
                .addItem(getString(R.string.rename), isVolumeFileWriteable)
                .addItem(getString(R.string.move_to), isVolumeFileWriteable)
                .addItem(getString(R.string.copy))
                .addItem(getString(R.string.clouddriver_file_permission_manager), isVolumeFileWriteable)
                // .addItem("分享", !isVolumeFileDirectory)
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        switch (position) {
                            case 0:
                                showFileDelWranibgDlg(volumeFile);
                                break;
                            case 1:
                                downloadFile(volumeFile);
                                break;
                            case 2:
                                showFileRenameDlg(volumeFile);
                                break;
                            case 3:
                                moveVolumeFileList.clear();
                                moveVolumeFileList.add(volumeFile);
                                moveFile(moveVolumeFileList);
                                break;
                            case 4:
                                List<VolumeFile> copyVolumeFileList = new ArrayList<VolumeFile>();
                                copyVolumeFileList.add(volumeFile);
                                copyFile(copyVolumeFileList);
                                break;
                            case 5:
                                startVolumeFilePermissionManager(volumeFile);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .build()
                .show();
    }

    /**
     * 打开权限管理
     */
    private void startVolumeFilePermissionManager(VolumeFile volumeFile) {
        Bundle bundle = new Bundle();
        bundle.putString("volume", volumeFile.getVolume());
        bundle.putString("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName());
        IntentUtils.startActivity(VolumeFileBaseActivity.this, VolumeFilePermissionManagerActivity.class, bundle);
    }


    /**
     * 弹出文件删除提示框
     *
     * @param volumeFile
     */
    protected void showFileDelWranibgDlg(final VolumeFile volumeFile) {
        new MyQMUIDialog.MessageDialogBuilder(VolumeFileBaseActivity.this)
                .setMessage(R.string.clouddriver_sure_delete_file)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        List<VolumeFile> deleteVolumeFileList = new ArrayList<>();
                        deleteVolumeFileList.add(volumeFile);
                        deleteFile(deleteVolumeFileList);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 弹出新建文件夹提示框
     */
    protected void showCreateFolderDlg() {
        createFolderDlg = new MyDialog(VolumeFileBaseActivity.this,
                R.layout.dialog_my_app_approval_password_input, R.style.userhead_dialog_bg);
        createFolderDlg.setCancelable(false);
        final EditText inputEdit = (EditText) createFolderDlg.findViewById(R.id.edit);
        inputEdit.setHint(getString(R.string.clouddriver_input_directory_name));
        inputEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MyAppConfig.VOLUME_MAX_FILE_NAME_LENGTH)});
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView) createFolderDlg.findViewById(R.id.app_update_title)).setText(getString(R.string.clouddriver_create_folder));
        Button okBtn = (Button) createFolderDlg.findViewById(R.id.ok_btn);
        okBtn.setText(R.string.create);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String forderName = inputEdit.getText().toString().trim();
                if (StringUtils.isBlank(forderName)) {
                    ToastUtils.show(getApplicationContext(), R.string.clouddriver_input_directory_name);
                    return;
                }
                if (!FomatUtils.isValidFileName(forderName)) {
                    ToastUtils.show(getApplicationContext(), R.string.clouddriver_directory_name_invaliad);
                    return;
                }

                for (int i = 0; i < volumeFileList.size(); i++) {
                    if (volumeFileList.get(i).getName().equals(forderName)) {
                        ToastUtils.show(getApplicationContext(), R.string.clouddriver_exists_same_name);
                        return;
                    }
                }
                createForder(forderName);
            }
        });

        (createFolderDlg.findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFolderDlg.dismiss();
            }
        });
        createFolderDlg.show();
        InputMethodUtils.display(VolumeFileBaseActivity.this, inputEdit);
    }


    /**
     * 弹出文件重命名提示框
     */
    protected void showFileRenameDlg(final VolumeFile volumeFile) {
        String fileName = volumeFile.getName();
        final String fileNameNoEx = volumeFile.getType().equals(VolumeFile.FILE_TYPE_REGULAR) ? FileUtils.getFileNameWithoutExtension(fileName) : fileName;
        final String fileExtension = fileName.replace(fileNameNoEx, "");
        fileRenameDlg = new MyDialog(VolumeFileBaseActivity.this,
                R.layout.dialog_my_app_approval_password_input, R.style.userhead_dialog_bg);
        fileRenameDlg.setCancelable(false);
        final EditText inputEdit = (EditText) fileRenameDlg.findViewById(R.id.edit);
        inputEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MyAppConfig.VOLUME_MAX_FILE_NAME_LENGTH)});
        inputEdit.setText(fileNameNoEx);
        inputEdit.setSelectAllOnFocus(true);
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView) fileRenameDlg.findViewById(R.id.app_update_title)).setText(
                volumeFile.getType().equals(VolumeFile.FILE_TYPE_REGULAR) ? R.string.file_rename : R.string.folder_rename);
        Button okBtn = (Button) fileRenameDlg.findViewById(R.id.ok_btn);
        okBtn.setText(R.string.rename);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = inputEdit.getText().toString().trim();
                if (StringUtils.isBlank(newName)) {
                    ToastUtils.show(getApplicationContext(), volumeFile.getType().equals(
                            VolumeFile.FILE_TYPE_REGULAR) ? R.string.clouddriver_input_file_name : R.string.clouddriver_input_directory_name);
                    return;
                }
                if (!FomatUtils.isValidFileName(newName)) {
                    ToastUtils.show(getApplicationContext(), R.string.clouddriver_file_name_invaliad);
                    return;
                }
                if (!fileNameNoEx.equals(newName)) {
                    newName = newName + fileExtension;
                    for (int i = 0; i < volumeFileList.size(); i++) {
                        VolumeFile volumeFile1 = volumeFileList.get(i);
                        if (volumeFile1 != volumeFile && volumeFile1.getName().equals(newName)) {
                            ToastUtils.show(getApplicationContext(), R.string.clouddriver_exists_same_name);
                            return;
                        }
                    }
                    renameFile(volumeFile, newName);
                } else {
                    fileRenameDlg.dismiss();
                }
            }
        });

        (fileRenameDlg.findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileRenameDlg.dismiss();
            }
        });
        fileRenameDlg.show();
        InputMethodUtils.display(VolumeFileBaseActivity.this, inputEdit);
    }


    /**
     * 设置跟权限相关的layout,可以被继承此Activity的实例重写控制当前页面的layout
     */
    protected void setCurrentDirectoryLayoutByPrivilege() {
    }


    /**
     * 下载或打开文件
     *
     * @param volumeFile
     */
    protected void downloadOrOpenVolumeFile(VolumeFile volumeFile) {
        String fileSavePath = MyAppConfig.LOCAL_DOWNLOAD_PATH + volumeFile.getName();
        if (FileUtils.isFileExist(fileSavePath)) {
            FileUtils.openFile(getApplicationContext(), fileSavePath);
        } else {
            Bundle bundle = new Bundle();
            bundle.putSerializable("volumeId", volume.getId());
            bundle.putSerializable("volumeFile", volumeFile);
            bundle.putSerializable("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName());
            IntentUtils.startActivity(VolumeFileBaseActivity.this, VolumeFileDownloadActivtiy.class, bundle);
        }
    }

    /**
     * 初始化无数据时显示的ui
     */
    protected void initDataBlankLayoutStatus() {
        dataBlankLayout.setVisibility((volumeFileList.size() == 0) ? View.VISIBLE : View.GONE);
    }

    /**
     * 文件排序,可以被继承此Activity的实例重写进行排序
     */
    protected void sortVolumeFileList() {
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_MOVE_FILE) {
            volumeFileList.removeAll(moveVolumeFileList);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 发送刷新目录广播
     *
     * @param directoryId
     */
    protected void sendVolumeFileRefreshBroadcast(String directoryId) {
        Intent intent = new Intent();
        intent.putExtra("directoryId", directoryId);
        intent.putExtra("command", "refresh");
        intent.setAction("broadcast_volume");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    /**
     * 文件下载
     *
     * @param volumeFile
     */
    private void downloadFile(VolumeFile volumeFile) {
        Bundle bundle = new Bundle();
        bundle.putString("volumeId", volume.getId());
        bundle.putSerializable("volumeFile", volumeFile);
        bundle.putString("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName());
        bundle.putBoolean("isStartDownload", true);
        IntentUtils.startActivity(VolumeFileBaseActivity.this, VolumeFileDownloadActivtiy.class, bundle);
    }


    /**
     * 移动文件
     *
     * @param moveVolumeFileList
     */
    protected void moveFile(List<VolumeFile> moveVolumeFileList) {
        this.moveVolumeFileList = moveVolumeFileList;
        if (moveVolumeFileList.size() > 0) {
            Intent intent = new Intent(getApplicationContext(), VolumeFileLocationSelectActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("volume", volume);
            bundle.putSerializable("volumeFileList", (Serializable) moveVolumeFileList);
            bundle.putString("title", getString(R.string.clouddriver_select_move_position));
            bundle.putString("operationFileDirAbsolutePath", currentDirAbsolutePath);
            bundle.putBoolean("isFunctionCopy", false);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_MOVE_FILE);
        }

    }

    /***
     * 复制文件
     * @param volumeFileList
     */
    protected void copyFile(List<VolumeFile> volumeFileList) {
        if (volumeFileList.size() > 0) {
            Intent intent = new Intent(getApplicationContext(), VolumeFileLocationSelectActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("volume", volume);
            bundle.putSerializable("volumeFileList", (Serializable) volumeFileList);
            bundle.putString("title", getString(R.string.clouddriver_select_copy_position));
            bundle.putBoolean("isFunctionCopy", true);
            bundle.putString("operationFileDirAbsolutePath", currentDirAbsolutePath);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_COPY_FILE);
        }
    }


    /**
     * 获取网盘下包含自己的组
     */
    private void getVolumeGroupContainMe() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            apiServiceBase.getVolumeGroupContainMe(volume.getId());
        } else {
            LoadingDialog.dimissDlg(loadingDlg);
        }
    }

    /**
     * 创建文件夹
     *
     * @param forderName
     */
    private void createForder(String forderName) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiServiceBase.createForder(volume.getId(), forderName, currentDirAbsolutePath);
        }
    }

    /**
     * 删除文件
     *
     * @param deleteVolumeFile
     */
    protected void deleteFile(List<VolumeFile> deleteVolumeFile) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiServiceBase.volumeFileDelete(volume.getId(), deleteVolumeFile, currentDirAbsolutePath);
        }
    }

    /**
     * 文件重命名
     *
     * @param volumeFile
     * @param fileNewName
     */
    private void renameFile(VolumeFile volumeFile, String fileNewName) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiServiceBase.volumeFileRename(volume.getId(), volumeFile, currentDirAbsolutePath, fileNewName);
        }
    }


    @Override
    public void onRefresh() {
        getVolumeFileList(false);
    }

    /**
     * 获取文件列表
     */
    protected void getVolumeFileList(boolean isShowDlg) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show(isShowDlg);
            String path = currentDirAbsolutePath;
            if (currentDirAbsolutePath.length() > 1) {
                path = currentDirAbsolutePath.substring(0, currentDirAbsolutePath.length() - 1);
            }
            apiServiceBase.getVolumeFileList(volume.getId(), path);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private class WebServiceBase extends APIInterfaceInstance {
        @Override
        public void returnVolumeFileListSuccess(GetVolumeFileListResult getVolumeFileListResult) {
            VolumeFileBaseActivity.this.getVolumeFileListResult = getVolumeFileListResult;
            //判断是否可以计算出当前目录的权限，如果不可以则获取网盘中我所属的群组信息
            if (VolumeFilePrivilegeUtils.canGetVolumeFilePrivilege(getApplicationContext(), volume)) {
                LoadingDialog.dimissDlg(loadingDlg);
                setCurrentDirectoryLayoutByPrivilege();
            } else {
                getVolumeGroupContainMe();
            }
            swipeRefreshLayout.setRefreshing(false);
            if (StringUtils.isBlank(fileFilterType)) {
                volumeFileList = getVolumeFileListResult.getVolumeFileList();
            } else if (fileFilterType.equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
                volumeFileList = getVolumeFileListResult.getVolumeFileDirectoryList();
            } else {
                volumeFileList = getVolumeFileListResult.getVolumeFileFilterList(fileFilterType);
            }

            if (isShowFileUploading) {
                List<VolumeFile> volumeFileUploadingList = VolumeFileUploadManagerUtils.getInstance().getCurrentForderUploadingVolumeFile(volume.getId(), currentDirAbsolutePath);
                volumeFileList.addAll(0, volumeFileUploadingList);
            }
            sortVolumeFileList();
            adapter.setVolumeFileList(volumeFileList);
            adapter.notifyDataSetChanged();
            initDataBlankLayoutStatus();
        }

        @Override
        public void returnVolumeFileListFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnCreateForderSuccess(VolumeFile volumeFile) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (createFolderDlg != null && createFolderDlg.isShowing()) {
                createFolderDlg.dismiss();
            }
            volumeFileList.add(volumeFile);
            sortVolumeFileList();
            adapter.setVolumeFileList(volumeFileList);
            adapter.notifyDataSetChanged();
            initDataBlankLayoutStatus();
            if (VolumeFileBaseActivity.this instanceof VolumeFileLocationSelectActivity) {
                sendVolumeFileRefreshBroadcast(getVolumeFileListResult.getId());
            }
        }

        @Override
        public void returnCreateForderFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnVolumeFileDeleteSuccess(List<VolumeFile> deleteVolumeFileList) {
            LoadingDialog.dimissDlg(loadingDlg);
            volumeFileList.removeAll(deleteVolumeFileList);
            adapter.setVolumeFileList(volumeFileList);
            adapter.notifyDataSetChanged();
            initDataBlankLayoutStatus();
        }

        @Override
        public void returnVolumeFileDeleteFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnVolumeFileRenameSuccess(VolumeFile oldVolumeFile, String fileNewName) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (fileRenameDlg != null && fileRenameDlg.isShowing()) {
                fileRenameDlg.dismiss();
            }
            int index = volumeFileList.indexOf(oldVolumeFile);
            if (index != -1) {
                volumeFileList.get(index).setName(fileNewName);
                sortVolumeFileList();
                adapter.setVolumeFileList(volumeFileList);
                adapter.notifyDataSetChanged();
            }


        }

        @Override
        public void returnVolumeFileRenameFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (fileRenameDlg != null && fileRenameDlg.isShowing()) {
                fileRenameDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnVolumeGroupContainMeSuccess(GetVolumeGroupResult getVolumeGroupResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            List<String> groupIdList = getVolumeGroupResult.getGroupIdList();
            VolumeGroupContainMe volumeGroupContainMe = new VolumeGroupContainMe(volume.getId(), groupIdList);
            VolumeGroupContainMeCacheUtils.saveVolumeGroupContainMe(getApplicationContext(), volumeGroupContainMe);
            setCurrentDirectoryLayoutByPrivilege();
        }

        @Override
        public void returnVolumeGroupContainMeFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            setCurrentDirectoryLayoutByPrivilege();
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
