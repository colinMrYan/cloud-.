package com.inspur.emmcloud.ui.appcenter.volume;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    @ViewInject(R.id.file_list)
    protected RecyclerView fileRecycleView;

    @ViewInject(R.id.refresh_layout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @ViewInject(R.id.data_blank_layout)
    protected LinearLayout dataBlankLayout;

    @ViewInject(R.id.no_file_text)
    protected TextView noFileText;

    @ViewInject(R.id.batch_operation_delete_text)
    private TextView batchOperationDeleteText;

    @ViewInject(R.id.batch_operation_move_text)
    private TextView batchOperationMoveText;

    protected LoadingDialog loadingDlg;
    protected VolumeFileAdapter adapter;
    protected List<VolumeFile> volumeFileList = new ArrayList<>();
    private List<VolumeFile> moveVolumeFileList = new ArrayList<>();
    protected Volume volume;
    private MyAppAPIService apiServiceBase;
    protected String currentDirAbsolutePath;
    private Dialog fileRenameDlg, createFolderDlg;
    protected String sortType = "sort_by_name_up";
    protected String fileFilterType = "";  //显示的文件类型
    protected boolean isShowFileUploading = false;  //是否显示正在上传的文件
    protected GetVolumeFileListResult getVolumeFileListResult;
    private boolean isFileWriteable = false;


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
        String title = getIntent().getExtras().getString("title", "");
        headerText.setVisibility(View.VISIBLE);
        headerText.setText(title);
        initRecycleView();
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecycleView() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.header_bg), ContextCompat.getColor(getApplicationContext(), R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(this);
        fileRecycleView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VolumeFileAdapter(this, volumeFileList);
        fileRecycleView.setAdapter(adapter);
    }


    /**
     * 弹出文件操作框
     *
     * @param title
     */
    protected void showFileOperationDlg(final VolumeFile volumeFile) {
        boolean isVolumeFileDirectory = volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY);
        new ActionSheetDialog.ActionListSheetBuilder(VolumeFileBaseActivity.this)
                .setTitle(volumeFile.getName())
                .addItem("删除", getVolumeFileListResult.getHaveModifyPrivilege())
                .addItem("下载", !isVolumeFileDirectory)
                .addItem("重命名", getVolumeFileListResult.getHaveModifyPrivilege())
                .addItem("移动到", getVolumeFileListResult.getHaveModifyPrivilege())
                .addItem("复制")
                .addItem("分享", !isVolumeFileDirectory)
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
     * 弹出文件删除提示框
     *
     * @param volumeFile
     */
    protected void showFileDelWranibgDlg(final VolumeFile volumeFile) {
        new MyQMUIDialog.MessageDialogBuilder(VolumeFileBaseActivity.this)
                .setMessage("确定要删除所选文件吗？")
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
        inputEdit.setHint("请输入文件夹名称");
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView) createFolderDlg.findViewById(R.id.app_update_title)).setText("新建文件夹");
        Button okBtn = (Button) createFolderDlg.findViewById(R.id.ok_btn);
        okBtn.setText("新建");
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String forderName = inputEdit.getText().toString();
                if (StringUtils.isBlank(forderName)) {
                    ToastUtils.show(getApplicationContext(), "请输入文件夹名称");
                } else if (!FomatUtils.isValidFileName(forderName)) {
                    ToastUtils.show(getApplicationContext(), "文件名中不能包含特殊字符 / \\ \" : | * ? < >");
                } else {
                    boolean isNameDuplication = false;
                    for (int i = 0; i < volumeFileList.size(); i++) {
                        if (volumeFileList.get(i).getName().equals(forderName)) {
                            isNameDuplication = true;
                            break;
                        }
                    }

                    if (isNameDuplication) {
                        ToastUtils.show(getApplicationContext(), "已存在同名文件/文件夹");
                        return;
                    }
                    createForder(forderName);
                }
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
        inputEdit.setText(fileNameNoEx);
        inputEdit.setSelectAllOnFocus(true);
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView) fileRenameDlg.findViewById(R.id.app_update_title)).setText("文件重命名");
        Button okBtn = (Button) fileRenameDlg.findViewById(R.id.ok_btn);
        okBtn.setText("重命名");
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = inputEdit.getText().toString();
                if (StringUtils.isBlank(newName)) {
                    ToastUtils.show(getApplicationContext(), "请输入文件/文件夹名称");
                } else if (!FomatUtils.isValidFileName(newName)) {
                    ToastUtils.show(getApplicationContext(), "文件名中不能包含特殊字符 / \\ \" : | * ? < >");
                } else if (!fileNameNoEx.equals(newName)) {
                    newName = newName + fileExtension;
                    boolean isNameDuplication = false;
                    for (int i = 0; i < volumeFileList.size(); i++) {
                        VolumeFile volumeFile1 = volumeFileList.get(i);
                        if (volumeFile1 != volumeFile && volumeFile1.getName().equals(newName)) {
                            isNameDuplication = true;
                            break;
                        }
                    }

                    if (isNameDuplication) {
                        ToastUtils.show(getApplicationContext(), "已存在同名文件/文件夹");
                        return;
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


    private void getFilePrivilege() {
        VolumeGroupContainMe volumeGroupContainMe = VolumeGroupContainMeCacheUtils.getVolumeGroupContainMe(getApplicationContext(), volume.getId());
        if (volumeGroupContainMe != null) {
            List<String> groupIdList = volumeGroupContainMe.getGroupIdList();
            isFileWriteable = getVolumeFileListResult.isFileWriteable(groupIdList);
            setLayoutByPrivilege();
        }else {

        }

    }

    /**
     * 设置跟权限相关的layout
     *
     * @param haveModifyPrivilege
     */
    private void setLayoutByPrivilege() {
        LoadingDialog.dimissDlg(loadingDlg);
        //当自己没有写权限时禁止上传文件和新建文件夹
        boolean isVolumeFileLocaitionSelectActivity = VolumeFileBaseActivity.this instanceof VolumeFileLocationSelectActivity;
        headerOperationLayout.setVisibility(!isFileWriteable || isVolumeFileLocaitionSelectActivity ? View.GONE : View.VISIBLE);
        batchOperationDeleteText.setVisibility(isFileWriteable ? View.VISIBLE : View.GONE);
        batchOperationMoveText.setVisibility(isFileWriteable ? View.VISIBLE : View.GONE);

    }


    /**
     * 下载或打开文件
     *
     * @param volumeFile
     */
    protected void downloadOrOpenVolumeFile(VolumeFile volumeFile) {
        String fileSavePath = MyAppConfig.LOCAL_DOWNLOAD_PATH + volumeFile.getName();
        if (FileUtils.isFileExist(fileSavePath)) {
            FileUtils.openFile(getApplicationContext(), fileSavePath, volumeFile.getFormat());
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
     * 文件排序
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
     * 文件下载
     *
     * @param volumeFile
     */
    private void downloadFile(VolumeFile volumeFile) {
        Bundle bundle = null;
        bundle = new Bundle();
        bundle.putString("volumeId", volume.getId());
        bundle.putSerializable("volumeFile", volumeFile);
        bundle.putString("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName());
        bundle.putBoolean("isStartDownload", true);
        IntentUtils.startActivity(VolumeFileBaseActivity.this, VolumeFileDownloadActivtiy.class, bundle);
    }


    /**
     * 移动文件
     *
     * @param volumeFileList
     */
    protected void moveFile(List<VolumeFile> moveVolumeFileList) {
        this.moveVolumeFileList = moveVolumeFileList;
        if (moveVolumeFileList.size() > 0) {
            Intent intent = new Intent(getApplicationContext(), VolumeFileLocationSelectActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("volume", volume);
            bundle.putSerializable("volumeFileList", (Serializable) moveVolumeFileList);
            bundle.putString("title", "选择目标文件夹");
            bundle.putString("operationFileDirAbsolutePath", currentDirAbsolutePath);
            bundle.putBoolean("isFunctionCopy", false);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_MOVE_FILE);
        } else {
            ToastUtils.show(getApplicationContext(), "请选择文件");
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
            bundle.putString("title", "选择复制位置");
            bundle.putBoolean("isFunctionCopy", true);
            bundle.putString("operationFileDirAbsolutePath", currentDirAbsolutePath);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_COPY_FILE);
        } else {
            ToastUtils.show(getApplicationContext(), "请选择文件");
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
            getFilePrivilege();
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
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
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
    }
}
