package com.inspur.emmcloud.ui.app.volume;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.adapter.VolumeFileFilterPopGridAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileUploadSTSTokenResult;
import com.inspur.emmcloud.bean.Volume.Volume;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.InputMethodUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.oss.OssService;
import com.inspur.emmcloud.util.oss.PauseableUploadTask;
import com.inspur.emmcloud.util.oss.STSGetter;
import com.inspur.emmcloud.util.oss.UIDisplayer;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * 云盘-我的文件
 */

@ContentView(R.layout.activity_volume_my_file)
public class VolumeMyFileActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_OPEN_GALLERY = 3;
    private static final int REQUEST_OPEN_FILE_BROWSER = 4;

    @ViewInject(R.id.header_text)
    private TextView headerText;

    @ViewInject(R.id.operation_sort_text)
    private TextView operationSortText;

    @ViewInject(R.id.file_list)
    private RecyclerView fileRecycleView;

    @ViewInject(R.id.refresh_layout)
    private SwipeRefreshLayout swipeRefreshLayout;

    @ViewInject(R.id.data_blank_layout)
    private LinearLayout dataBlankLayout;

    @ViewInject(R.id.batch_operation_bar_layout)
    private RelativeLayout batchOperationBarLayout;

    @ViewInject(R.id.batch_operation_header_layout)
    private RelativeLayout batchOprationHeaderLayout;


    private PopupWindow sortOperationPop;
    private LoadingDialog loadingDlg;
    private boolean isSortByTime = true;
    private TextView sortByTimeText, sortByNameText;
    private ImageView sortByTimeSelectImg, sortByNameSelectImg;
    private VolumeFileAdapter adapter;
    private List<VolumeFile> volumeFileList = new ArrayList<>();
    private Volume volume;
    private MyAppAPIService apiService;
    private String absolutePath;
    private OssService ossService;
    private WeakReference<PauseableUploadTask> task;
    private UIDisplayer UIDisplayer;
    private Dialog fileRenameDlg, createFolderDlg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getVolumeFileList(true);

    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiService = new MyAppAPIService(VolumeMyFileActivity.this);
        apiService.setAPIInterface(new NetService());
        volume = (Volume) getIntent().getSerializableExtra("volume");
        absolutePath = getIntent().getExtras().getString("absolutePath","/");
        String title = getIntent().getExtras().getString("title","");
        headerText.setText(title);
        initRecycleView();
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecycleView() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(),R.color.header_bg), ContextCompat.getColor(getApplicationContext(),R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(this);
        fileRecycleView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VolumeFileAdapter(this, volumeFileList);
        fileRecycleView.setAdapter(adapter);
        adapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("volume", volume);
                if (volumeFile.getType().equals("directory")){
                    bundle.putSerializable("absolutePath", absolutePath+volumeFile.getName()+"/");
                    bundle.putSerializable("title", volumeFile.getName());
                    IntentUtils.startActivity(VolumeMyFileActivity.this, VolumeMyFileActivity.class,bundle);
                }else {
                    IntentUtils.startActivity(VolumeMyFileActivity.this, VolumeFileInfoActivtiy.class,bundle);
                }
            }
        });
        adapter.setItemDropDownImgClickListener(new VolumeFileAdapter.MyItemDropDownImgClickListener() {
            @Override
            public void onItemDropDownImgClick(View view, int position) {
                showFileOperationDlg(volumeFileList.get(position));
            }
        });
    }

    /**
     * 弹出文件操作框
     *
     * @param title
     */
    private void showFileOperationDlg(final VolumeFile volumeFile) {
        new ActionSheetDialog.ActionListSheetBuilder(VolumeMyFileActivity.this)
                .setTitle(volumeFile.getName())
                .addItem("删除")
                .addItem("下载")
                .addItem("重命名")
                .addItem("移动到")
                .addItem("复制")
                .addItem("分享")
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        switch (position) {
                            case 0:
                                showFileDelWranibgDlg(volumeFile);
                                break;
                            case 2:
                                showFileRenameDlg(volumeFile);
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
     * 弹出选择上传文件提示框
     */
    private void showUploadFileDlg() {
        new ActionSheetDialog.ActionListSheetBuilder(VolumeMyFileActivity.this)
                .addItem("拍照")
                .addItem("选择照片")
                .addItem("选择文件")
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        switch (position) {
                            case 0:
                                break;
                            case 1:
                                AppUtils.openGallery(VolumeMyFileActivity.this, 1, REQUEST_OPEN_GALLERY);
                                break;
                            case 2:
                                AppUtils.openFileSystem(VolumeMyFileActivity.this, REQUEST_OPEN_FILE_BROWSER);
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
     */
    private void showFileDelWranibgDlg(final VolumeFile volumeFile) {
        new MyQMUIDialog.MessageDialogBuilder(VolumeMyFileActivity.this)
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
                        deleteFileByName(volumeFile);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 弹出新建文件夹提示框
     */
    private void showCreateFolderDlg() {

        createFolderDlg = new MyDialog(VolumeMyFileActivity.this,
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
        InputMethodUtils.display(VolumeMyFileActivity.this, inputEdit);
    }


    /**
     * 弹出文件重命名提示框
     */
    private void showFileRenameDlg(final VolumeFile volumeFile) {
        String fileName = volumeFile.getName();
        String fileNameNoEx = fileName;
        if (volumeFile.getType().equals("regular")) {
            fileNameNoEx = FileUtils.getFileNameWithoutExtension(fileName);
        }
        final String fileExtension = fileName.replace(fileNameNoEx, "");
        fileRenameDlg = new MyDialog(VolumeMyFileActivity.this,
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
                } else {
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
                    if (!newName.equals(volumeFile.getName())) {
                        renameFile(volumeFile, newName);
                    } else {
                        fileRenameDlg.dismiss();
                    }
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
        InputMethodUtils.display(VolumeMyFileActivity.this, inputEdit);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.new_forder_img:
                showCreateFolderDlg();
                break;
            case R.id.upload_btn:
            case R.id.upload_img:
                showUploadFileDlg();
                break;
            case R.id.operation_sort_text:
                showSortOperationPop();
                break;
            case R.id.operation_multiselect_text:
                setMutiselect(true);
                break;
            case R.id.operation_filter_text:
                showFileFilterPop(v);
                break;
            case R.id.sort_by_time_layout:
                isSortByTime = true;
                sortOperationPop.dismiss();
                break;
            case R.id.sort_by_name_layout:
                isSortByTime = false;
                sortOperationPop.dismiss();
                break;
            case R.id.batch_operation_delete_text:
                break;
            case R.id.batch_operation_copy_text:
                break;
            case R.id.batch_operation_move_text:
                break;
            case R.id.batch_operation_cancel_text:
                setMutiselect(false);
                break;
            case R.id.batch_operation_select_all_text:
                break;

            default:
                break;
        }
    }

    /**
     * 弹出文件排序选择框
     */
    private void showSortOperationPop() {
        View contentView = LayoutInflater.from(VolumeMyFileActivity.this)
                .inflate(R.layout.app_volume_file_sort_operation_pop, null);
        sortOperationPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        sortByTimeText = (TextView) contentView.findViewById(R.id.sort_by_time_text);
        sortByNameText = (TextView) contentView.findViewById(R.id.sort_by_name_text);
        sortByTimeSelectImg = (ImageView) contentView.findViewById(R.id.sort_by_time_select_img);
        sortByNameSelectImg = (ImageView) contentView.findViewById(R.id.sort_by_name_select_img);
        sortByTimeText.setTextColor(Color.parseColor(isSortByTime ? "#2586CD" : "#666666"));
        sortByNameText.setTextColor(Color.parseColor(isSortByTime ? "#666666" : "#2586CD"));
        sortByTimeSelectImg.setVisibility(isSortByTime ? View.VISIBLE : View.INVISIBLE);
        sortByNameSelectImg.setVisibility(isSortByTime ? View.INVISIBLE : View.VISIBLE);
        sortOperationPop.setTouchable(true);
        sortOperationPop.setBackgroundDrawable(ContextCompat.getDrawable(
                getApplicationContext(), R.drawable.pop_window_view_tran));
        sortOperationPop.setOutsideTouchable(true);
        sortOperationPop.showAsDropDown(operationSortText);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_volume_menu_drop_up);
        drawable.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 14), DensityUtil.dip2px(getApplicationContext(), 14));
        operationSortText.setCompoundDrawables(null, null, drawable, null);
        sortOperationPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                operationSortText.setText(isSortByTime ? "时间排序" : "名称排序");
                Drawable drawable1 = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_volume_menu_drop_down);
                drawable1.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 14), DensityUtil.dip2px(getApplicationContext(), 14));
                operationSortText.setCompoundDrawables(null, null, drawable1, null);
            }
        });

    }

    /**
     * 弹出文件筛选框
     */
    private void showFileFilterPop(View v) {
        View contentView = LayoutInflater.from(VolumeMyFileActivity.this)
                .inflate(R.layout.app_volume_file_filter_pop, null);
        final PopupWindow fileFilterPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        GridView fileFilterGrid = (GridView) contentView.findViewById(R.id.file_filter_type_grid);
        fileFilterGrid.setAdapter(new VolumeFileFilterPopGridAdapter(VolumeMyFileActivity.this));
        fileFilterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fileFilterPop.dismiss();
            }
        });
        fileFilterPop.setTouchable(true);
        fileFilterPop.setBackgroundDrawable(ContextCompat.getDrawable(
                getApplicationContext(), R.drawable.pop_window_view_tran));
        fileFilterPop.setOutsideTouchable(true);
        fileFilterPop.showAsDropDown(v);
    }

    /**
     * 设置是否是多选状态
     *
     * @param isMutiselect
     */
    private void setMutiselect(boolean isMutiselect) {
        batchOperationBarLayout.setVisibility(isMutiselect ? View.VISIBLE : View.GONE);
        batchOprationHeaderLayout.setVisibility(isMutiselect ? View.VISIBLE : View.GONE);
        adapter.setMultiselect(isMutiselect);
    }

    /**
     * 初始化无数据时显示的ui
     */
    private void initDataBlankLayoutStatus() {
        dataBlankLayout.setVisibility((volumeFileList.size() == 0) ? View.VISIBLE : View.GONE);
    }




    /**
     * 删除本地列表的文件
     *
     * @param fileName
     */
    private void deleteFileInLocal(VolumeFile volumeFile) {
        int deletePositon = volumeFileList.indexOf(volumeFile);
        if (deletePositon != -1) {
            volumeFileList.remove(deletePositon);
            adapter.setVolumeFileList(volumeFileList);
            adapter.notifyItemRemoved(deletePositon);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_OPEN_FILE_BROWSER) {
                Uri uri = data.getData();
                String filePath = GetPathFromUri4kitkat.getPathByUri(getApplicationContext(), uri);
                File file = new File(filePath);
                getVolumeFileUploadSTSToken(file);
            }
        }

    }

    //初始化一个OssService用来上传下载
    public OssService initOSS(UIDisplayer displayer, GetVolumeFileUploadSTSTokenResult getVolumeFileUploadSTSTokenResult) {
        OSSCredentialProvider credentialProvider = new STSGetter(getVolumeFileUploadSTSTokenResult);
        ;
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(getApplicationContext(), getVolumeFileUploadSTSTokenResult.getEndpoint(), credentialProvider, conf);
        return new OssService(oss, getVolumeFileUploadSTSTokenResult, displayer);

    }

    /**
     * 创建文件夹
     *
     * @param forderName
     */
    private void createForder(String forderName) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            apiService.createForder(volume.getId(), forderName, absolutePath);
        }
    }

    private void deleteFileByName(VolumeFile volumeFile) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            apiService.deleteFile(volume.getId(), volumeFile, absolutePath);
        }
    }

    /**
     * 文件重命名
     * @param volumeFile
     * @param newName
     */
    private void renameFile(VolumeFile volumeFile, String newName) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {

        }
    }

    /**
     * 获取云盘上传STS Token
     *
     * @param file
     */
    private void getVolumeFileUploadSTSToken(File file) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            String volumeFilePath = absolutePath + file.getName();
            apiService.getVolumeFileUploadSTSToken(volume.getId(), file.getName(), volumeFilePath, file.getPath());
        }
    }

    @Override
    public void onRefresh() {
        getVolumeFileList(false);
    }

    /**
     * 获取文件列表
     */
    private void getVolumeFileList(boolean isShowDlg) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show(isShowDlg);
            String path = absolutePath;
            if (absolutePath.length()>1){
                path =absolutePath.substring(0,absolutePath.length()-1);
            }
            apiService.getVolumeFileList(volume.getId(), path);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private class NetService extends APIInterfaceInstance {
        @Override
        public void returnVolumeFileListSuccess(GetVolumeFileListResult getVolumeFileListResult) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            swipeRefreshLayout.setRefreshing(false);
            volumeFileList = getVolumeFileListResult.getVolumeFileList();
            initDataBlankLayoutStatus();
            adapter.setVolumeFileList(volumeFileList);
            adapter.notifyDataSetChanged();
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
        public void returnVolumeFileUploadSTSTokenSuccess(GetVolumeFileUploadSTSTokenResult getVolumeFileUploadSTSTokenResult, String filePath) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ossService = initOSS(null, getVolumeFileUploadSTSTokenResult);
            ossService.asyncPutImage(getVolumeFileUploadSTSTokenResult.getFileName(), filePath);
//           WeakReference<PauseableUploadTask> task = new WeakReference<>(ossService.asyncMultiPartUpload(getVolumeFileUploadSTSTokenResult, filePath));
        }

        @Override
        public void returnVolumeFileUploadSTSTokenFail(String error, int errorCode, String filePath) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnCreateForderSuccess(VolumeFile volumeFile) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (createFolderDlg != null && createFolderDlg.isShowing()) {
                createFolderDlg.dismiss();
            }
            volumeFileList.add(volumeFile);
            adapter.setVolumeFileList(volumeFileList);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnCreateForderFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnDeleteFileSuccess(VolumeFile volumeFile) {
            deleteFileInLocal(volumeFile);
        }

        @Override
        public void returnDeleteFileFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
