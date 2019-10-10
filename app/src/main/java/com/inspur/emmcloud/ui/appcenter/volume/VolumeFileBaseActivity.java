package com.inspur.emmcloud.ui.appcenter.volume;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.VolumeActionData;
import com.inspur.emmcloud.baselib.widget.VolumeActionLayout;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.baselib.widget.roundbutton.CustomRoundButton;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeGroupResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeGroupContainMe;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.privates.ShareFile2OutAppUtils;
import com.inspur.emmcloud.util.privates.VolumeFilePrivilegeUtils;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManager;
import com.inspur.emmcloud.util.privates.cache.VolumeGroupContainMeCacheUtils;
import com.inspur.emmcloud.widget.tipsview.TipsView;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.PlatformName;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 云盘-文件操作基础类
 */

public class VolumeFileBaseActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String VOLUME_FROM = "volume_from";
    public static final int MY_VOLUME = 0;
    protected static final int REQUEST_MOVE_FILE = 5;
    protected static final int REQUEST_COPY_FILE = 6;
    protected static final int SHARE_IMAGE_OR_FILES = 7;
    protected static final String SORT_BY_NAME_UP = "sort_by_name_up";
    protected static final String SORT_BY_NAME_DOWN = "sort_by_name_down";
    protected static final String SORT_BY_TIME_UP = "sort_by_time_up";
    protected static final String SORT_BY_TIME_DOWN = "sort_by_time_down";
    final List<VolumeActionData> volumeActionDataList = new ArrayList<>();
    final List<VolumeActionData> volumeActionHideList = new ArrayList<>();
    public VolumeFile shareToVolumeFile;
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
    @BindView(R.id.header_text)
    TextView headerText;
    @BindView(R.id.header_operation_layout)
    RelativeLayout headerOperationLayout;
    @BindView(R.id.lv_file)
    RecyclerView fileRecycleView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.data_blank_layout)
    LinearLayout dataBlankLayout;
    @BindView(R.id.btn_upload_file)
    CustomRoundButton uploadFileBtn;
    @BindView(R.id.ll_volume_action)
    VolumeActionLayout volumeActionLayout;
    @BindView(R.id.tipview_red_point)
    TipsView redPointView;
    @BindView(R.id.rl_tip_view)
    RelativeLayout tipViewLayout;
    @BindView(R.id.tv_volume_tip)
    TextView volumeTipTextView;
    String deleteAction, downloadAction, renameAction, moveToAction, copyAction, permissionAction, shareTo, moreAction; //弹框点击状态
    CustomShareListener mShareListener;
    private List<VolumeFile> moveVolumeFileList = new ArrayList<>();//移动的云盘文件列表
    private MyAppAPIService apiServiceBase;
    private Dialog fileRenameDlg, createFolderDlg;
    private int volumeFrom = -1;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
        getVolumeFileList(true);
        UMConfigure.init(this, "59aa1f8f76661373290010d3"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        PlatformConfig.setWeixin(Constant.WECHAT_APPID, "56a0426315f1d0985a1cc1e75e96130d");
        PlatformConfig.setQQZone("1105561850", "1kaw4r1c37SUupFL");
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_volume_file;
    }
    private void initView() {
        sortType = PreferencesByUserAndTanentUtils.getString(this, Constant.PREF_VOLUME_FILE_SORT_TYPE, SORT_BY_NAME_UP);
        loadingDlg = new LoadingDialog(this);
        apiServiceBase = new MyAppAPIService(VolumeFileBaseActivity.this);
        apiServiceBase.setAPIInterface(new WebServiceBase());
        volume = (Volume) getIntent().getSerializableExtra("volume");
        volumeFrom = getIntent().getIntExtra(VOLUME_FROM, -1);
        currentDirAbsolutePath = getIntent().getExtras().getString("currentDirAbsolutePath", "/");
        fileFilterType = getIntent().getExtras().getString("fileFilterType", "");
        title = getIntent().getExtras().getString("title", "");
        headerText.setVisibility(View.VISIBLE);
        headerText.setText(title);
        redPointView.attach(tipViewLayout, new TipsView.Listener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onCancel() {

            }
        });
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
        adapter.setCurrentDirAbsolutePath(currentDirAbsolutePath);
        fileRecycleView.setAdapter(adapter);
    }


    /**
     * 弹出文件操作框
     *
     * @param volumeFile
     */
    protected void showFileOperationDlg(final VolumeFile volumeFile) {
        //我的文件那个网盘不再显示权限管理,共享网盘也要是自己的才能显示权限管理，否则不显示
        ActionSheetDialog.ActionListSheetBuilder builder = new ActionSheetDialog.ActionListSheetBuilder(this);
        for (int i = 0; i < volumeActionHideList.size(); i++) {
            builder.addItem(volumeActionHideList.get(i).getActionName(), volumeActionHideList.get(i).isShow());
        }
        builder.setTitle(volumeFile.getName())
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        String action = (String) itemView.getTag();
                        handleVolumeAction(action);
                        dialog.dismiss();
                    }
                })
                .setOnActionSheetDlgDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        setBottomOperationItemShow(adapter.getSelectVolumeFileList());
                    }
                })
                .build()
                .show();
    }


    /**
     * 分享到频道
     */
    private void shareToFriends(VolumeFile volumeFile) {
        Intent intent = new Intent();
        shareToVolumeFile = volumeFile;
        intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 0);
        intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, false);
        intent.putExtra(ContactSearchFragment.EXTRA_SHOW_COMFIRM_DIALOG_WITH_MESSAGE, volumeFile.getName());
        intent.putExtra(ContactSearchFragment.EXTRA_SHOW_COMFIRM_DIALOG, true);
        ArrayList<String> uidList = new ArrayList<>();
        uidList.add(MyApplication.getInstance().getUid());
        intent.putStringArrayListExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, uidList);
        intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.baselib_share_to));
        intent.setClass(VolumeFileBaseActivity.this,
                ContactSearchActivity.class);
        startActivityForResult(intent, SHARE_IMAGE_OR_FILES);
    }

    /**
     * 分享到微信 QQ
     **/
    public void shareFile(final String filePath, final VolumeFile volumeFile) {
        mShareListener = new CustomShareListener(this);
        ShareAction shareAction = new ShareAction(this)
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (snsPlatform.mKeyword.equals("WEIXIN")) {
                            if (!StringUtils.isBlank(filePath)) {
                                ShareFile2OutAppUtils.shareFile2WeChat(getApplicationContext(), filePath);
                            } else {
                                ToastUtils.show(getString(R.string.clouddriver_volume_frist_download));
                            }
                        } else if (snsPlatform.mKeyword.equals("QQ")) {
                            if (!StringUtils.isBlank(filePath)) {
                                ShareFile2OutAppUtils.shareFileToQQ(getApplicationContext(), filePath);
                            } else {
                                ToastUtils.show(getString(R.string.clouddriver_volume_frist_download));
                            }
                        } else if (snsPlatform.mKeyword.equals("CLOUDPLUSE")) {
                            shareToFriends(volumeFile);
                        }
                    }
                });
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), ShareFile2OutAppUtils.PACKAGE_WECHAT)) {
            shareAction.addButton(PlatformName.WEIXIN, "WEIXIN", "umeng_socialize_wechat", "umeng_socialize_wechat");
        }
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), ShareFile2OutAppUtils.PACKAGE_MOBILE_QQ)) {
            shareAction.addButton(PlatformName.QQ, "QQ", "umeng_socialize_qq", "umeng_socialize_qq");
        }
        shareAction.addButton(getString(R.string.clouddrive_internal_sharing), "CLOUDPLUSE", "ic_launcher_share", "ic_launcher_share");
        shareAction.open();

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
     * 根据所选文件的类型展示操作按钮
     */
    protected void setBottomOperationItemShow(List<VolumeFile> selectVolumeFileList) {
        permissionAction = getString(R.string.clouddriver_file_permission_manager);
        downloadAction = getString(R.string.download);
        moveToAction = getString(R.string.move);
        copyAction = getString(R.string.copy);
        deleteAction = getString(R.string.delete);
        moreAction = getString(R.string.more);
        renameAction = getString(R.string.rename);
        shareTo = getString(R.string.baselib_share_to);
        volumeActionDataList.clear();
        volumeActionHideList.clear();
        boolean isVolumeFileWriteable = true;
        boolean isVolumeFileReadable = true;
        boolean isVolumeFileDirectory = true;
        boolean isOwner = true;
        for (int i = 0; i < selectVolumeFileList.size(); i++) {
            if (isVolumeFileWriteable) {
                isVolumeFileWriteable = VolumeFilePrivilegeUtils.getVolumeFileWritable(getApplicationContext(), selectVolumeFileList.get(i));
            }
            if (isVolumeFileReadable) {
                isVolumeFileReadable = VolumeFilePrivilegeUtils.getVolumeFileReadable(getApplicationContext(), selectVolumeFileList.get(i));
            }
            if (isOwner) {
                isOwner = selectVolumeFileList.get(i).getOwner().equals(BaseApplication.getInstance().getUid());
            }
            if (isVolumeFileDirectory) {
                isVolumeFileDirectory = selectVolumeFileList.get(i).getType().equals(VolumeFile.FILE_TYPE_DIRECTORY);
            }
        }
        volumeActionDataList.add(new VolumeActionData(downloadAction, R.drawable.ic_volume_download,
                selectVolumeFileList.size() == 1 && !isVolumeFileDirectory
                        && (isVolumeFileReadable || isVolumeFileWriteable)));
        volumeActionDataList.add(new VolumeActionData(copyAction, R.drawable.ic_volume_copy, isVolumeFileWriteable));
        volumeActionDataList.add(new VolumeActionData(deleteAction, R.drawable.ic_volume_delete, isVolumeFileWriteable));
        volumeActionDataList.add(new VolumeActionData(renameAction, R.drawable.ic_volume_rename,
                isVolumeFileWriteable && selectVolumeFileList.size() == 1));
        volumeActionDataList.add(new VolumeActionData(moveToAction, R.drawable.ic_volume_move, isVolumeFileWriteable));
        volumeActionDataList.add(new VolumeActionData(shareTo, R.drawable.ic_volume_share, selectVolumeFileList.size() == 1 &&
                !isVolumeFileDirectory && (isVolumeFileWriteable || isVolumeFileReadable)));
        volumeActionDataList.add(new VolumeActionData(permissionAction, R.drawable.ic_volume_permission,
                isVolumeFileDirectory && (isVolumeFileWriteable || isVolumeFileReadable)
                        && (volumeFrom != MY_VOLUME) && isOwner && volume.getType().equals("public")));
        for (int i = 0; i < volumeActionDataList.size(); i++) {
            if (!volumeActionDataList.get(i).isShow()) {
                volumeActionDataList.remove(i);
                i--;
                continue;
            }
        }
        if (volumeActionDataList.size() > 5) {
            for (int i = volumeActionDataList.size(); i > 4; i--) {
                volumeActionHideList.add(volumeActionDataList.get(i - 1));
                volumeActionDataList.remove(i - 1);
            }
            volumeActionDataList.add(new VolumeActionData(moreAction, R.drawable.ic_volume_more, (selectVolumeFileList.size() == 1
                    && (isVolumeFileWriteable || isVolumeFileReadable))));
        }
        volumeActionLayout.setVisibility(selectVolumeFileList.size() > 0 && volumeActionDataList.size() > 0 ? View.VISIBLE : View.GONE);
        volumeActionLayout.clearView();
        volumeActionLayout.setVolumeActionData(volumeActionDataList, new VolumeActionLayout.VolumeActionClickListener() {
            @Override
            public void volumeActionSelectedListener(String actionName) {
                volumeActionLayout.setVisibility(View.GONE);
                handleVolumeAction(actionName);
            }
        });
    }

    /**
     * 处理Volume 相关的Action
     */
    private void handleVolumeAction(String action) {
        VolumeFile volumeFile = adapter.getSelectVolumeFileList().get(0);
        if (action.equals(downloadAction)) {
            downloadFile(volumeFile);
        } else if (action.equals(moveToAction)) {
            moveFile(adapter.getSelectVolumeFileList());
        } else if (action.equals(copyAction)) {
            copyFile(adapter.getSelectVolumeFileList());
        } else if (action.equals(deleteAction)) {
            if (adapter.getSelectVolumeFileList().size() > 0) {
                showFileDelWranibgDlg(adapter.getSelectVolumeFileList());
            }
        } else if (action.equals(moreAction)) {
            showFileOperationDlg(volumeFile);
        } else if (action.equals(renameAction)) {
            showFileRenameDlg(volumeFile);
        } else if (action.equals(shareTo)) {
            String fileSavePath = FileDownloadManager.getInstance().getDownloadFilePath(
                    DownloadFileCategory.CATEGORY_VOLUME_FILE, volumeFile.getId(), volumeFile.getName());
            shareFile(fileSavePath, adapter.getSelectVolumeFileList().get(0));
                adapter.clearSelectedVolumeFileList();
                adapter.notifyDataSetChanged();
                setBottomOperationItemShow(new ArrayList<VolumeFile>());
        } else if (action.equals(permissionAction)) {
            startVolumeFilePermissionManager(volumeFile);
        }
    }

    /**
     * 弹出文件删除提示框
     *
     * @param deleteVolumeFile
     */
    protected void showFileDelWranibgDlg(final List<VolumeFile> deleteVolumeFile) {
        new CustomDialog.MessageDialogBuilder(VolumeFileBaseActivity.this)
                .setMessage(R.string.clouddriver_sure_delete_file)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setBottomOperationItemShow(adapter.getSelectVolumeFileList());
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFile(deleteVolumeFile);
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
                R.layout.volume_dialog_update_name_input);
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
                R.layout.volume_dialog_update_name_input);
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
                    setBottomOperationItemShow(adapter.getSelectVolumeFileList());
                    fileRenameDlg.dismiss();
                }
            }
        });

        (fileRenameDlg.findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBottomOperationItemShow(adapter.getSelectVolumeFileList());
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
        String fileSavePath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_VOLUME_FILE, volumeFile.getId(), volumeFile.getName());
        if (!StringUtils.isBlank(fileSavePath)) {
            FileUtils.openFile(getApplicationContext(), fileSavePath);
        } else {
            Bundle bundle = new Bundle();
            bundle.putSerializable("volumeId", volume.getId());
            bundle.putSerializable("volumeFile", volumeFile);
            bundle.putSerializable("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName());
            IntentUtils.startActivity(VolumeFileBaseActivity.this, VolumeFileDownloadActivity.class, bundle);
        }
    }

    /**
     * 初始化无数据时显示的ui
     */
    protected void initDataBlankLayoutStatus() {
        dataBlankLayout.setVisibility((volumeFileList.size() == 0) ? View.VISIBLE : View.GONE);
        uploadFileBtn.setVisibility(headerOperationLayout.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
    }

    /**
     * 文件排序,可以被继承此Activity的实例重写进行排序
     */
    protected void sortVolumeFileList() {
        sortType = PreferencesByUserAndTanentUtils.getString(this, Constant.PREF_VOLUME_FILE_SORT_TYPE, SORT_BY_NAME_UP);
        List<VolumeFile> VolumeFileUploadingList = new ArrayList<>();
        List<VolumeFile> VolumeFileNormalList = new ArrayList<>();
        for (int i = 0; i < volumeFileList.size(); i++) {
            VolumeFile volumeFile = volumeFileList.get(i);
            if (!volumeFile.getStatus().equals("normal")) {
                VolumeFileUploadingList.add(volumeFile);
            } else {
                VolumeFileNormalList.add(volumeFile);
            }
        }

        Collections.sort(VolumeFileNormalList, new FileSortComparable());
        volumeFileList.clear();
        volumeFileList.addAll(VolumeFileUploadingList);
        volumeFileList.addAll(VolumeFileNormalList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_COPY_FILE:
                    adapter.clearSelectedVolumeFileList();
                    adapter.notifyDataSetChanged();
                    break;
                case REQUEST_MOVE_FILE:
                    adapter.clearSelectedVolumeFileList();
                    volumeFileList.removeAll(moveVolumeFileList);
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (adapter.getSelectVolumeFileList().size() > 0) {
                adapter.clearSelectedVolumeFileList();
                adapter.notifyDataSetChanged();
                setBottomOperationItemShow(new ArrayList<VolumeFile>());
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
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
    protected void downloadFile(VolumeFile volumeFile) {
        Bundle bundle = new Bundle();
        bundle.putString("volumeId", volume.getId());
        bundle.putSerializable("volumeFile", volumeFile);
        bundle.putString("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName());
        bundle.putBoolean("isStartDownload", true);
        IntentUtils.startActivity(VolumeFileBaseActivity.this, VolumeFileDownloadActivity.class, bundle);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
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
            setCurrentDirectoryLayoutByPrivilege();
        }
    }

    private static class CustomShareListener implements UMShareListener {

        private WeakReference<VolumeFileBaseActivity> mActivity;

        private CustomShareListener(VolumeFileBaseActivity activity) {
            mActivity = new WeakReference(activity);
        }

        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            ToastUtils.show(mActivity.get(), R.string.baselib_share_success);
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            ToastUtils.show(mActivity.get(), R.string.baselib_share_fail);
            if (t != null) {
                LogUtils.jasonDebug("throw:" + t.getMessage());
            }

        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

        }
    }

    private class FileSortComparable implements Comparator<VolumeFile> {
        @Override
        public int compare(VolumeFile volumeFileA, VolumeFile volumeFileB) {
            int sortResult = 0;
            if (volumeFileA.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY) && volumeFileB.getType().equals(VolumeFile.FILE_TYPE_REGULAR)) {
                sortResult = -1;
            } else if (volumeFileB.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY) && volumeFileA.getType().equals(VolumeFile.FILE_TYPE_REGULAR)) {
                sortResult = 1;
            } else {
                switch (sortType) {
                    case SORT_BY_NAME_UP:
                        sortResult = volumeFileA.getName().toLowerCase().compareTo(volumeFileB.getName().toLowerCase().toString());
                        break;
                    case SORT_BY_NAME_DOWN:
                        sortResult = 0 - volumeFileA.getName().toLowerCase().compareTo(volumeFileB.getName().toLowerCase().toString());
                        break;
                    case SORT_BY_TIME_DOWN:
                        if (volumeFileA.getCreationDate() == volumeFileB.getCreationDate()) {
                            sortResult = 0;
                        } else if (volumeFileA.getCreationDate() < volumeFileB.getCreationDate()) {
                            sortResult = 1;
                        } else {
                            sortResult = -1;
                        }
                        break;
                    case SORT_BY_TIME_UP:
                        if (volumeFileA.getCreationDate() == volumeFileB.getCreationDate()) {
                            sortResult = 0;
                        } else if (volumeFileA.getCreationDate() < volumeFileB.getCreationDate()) {
                            sortResult = -1;
                        } else {
                            sortResult = 1;
                        }
                        break;
                    default:
                        break;
                }
            }
            return sortResult;
        }
    }

    private class WebServiceBase extends APIInterfaceInstance {
        @Override
        public void returnVolumeFileListSuccess(GetVolumeFileListResult getVolumeFileListResult) {
            VolumeFileBaseActivity.this.getVolumeFileListResult = getVolumeFileListResult;
            //判断是否可以计算出当前目录的权限，如果不可以则获取网盘中我所属的群组信息
//            if (VolumeFilePrivilegeUtils.canGetVolumeFilePrivilege(getApplicationContext(), volume)) {
//                LoadingDialog.dimissDlg(loadingDlg);
//                setCurrentDirectoryLayoutByPrivilege();
//            } else {
//            }
            getVolumeGroupContainMe();
            swipeRefreshLayout.setRefreshing(false);
            if (StringUtils.isBlank(fileFilterType)) {
                volumeFileList = getVolumeFileListResult.getVolumeFileList();
            } else if (fileFilterType.equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
                volumeFileList = getVolumeFileListResult.getVolumeFileDirectoryList();
            } else {
                volumeFileList = getVolumeFileListResult.getVolumeFileFilterList(fileFilterType);
            }
            if (isShowFileUploading) {
                List<VolumeFile> volumeFileUploadingList = VolumeFileUploadManager.getInstance().getCurrentFolderUploadVolumeFile(volume.getId(), currentDirAbsolutePath);
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
            getVolumeGroupContainMe();
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
            adapter.clearSelectedVolumeFileList();
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
            adapter.clearSelectedVolumeFileList();
            setBottomOperationItemShow(adapter.getSelectVolumeFileList());
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
