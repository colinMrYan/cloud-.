package com.inspur.emmcloud.ui.app.volume;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.adapter.VolumeFileFilterPopGridAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileUploadSTSTokenResult;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.VolumeFileUploadUtils;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;

import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.inspur.emmcloud.R.id.operation_layout;


/**
 * 云盘-文件列表展示
 */

public class VolumeFileActivity extends VolumeFileBaseActivity {
    private static final int REQUEST_OPEN_CEMERA = 2;
    private static final int REQUEST_OPEN_GALLERY = 3;
    private static final int REQUEST_OPEN_FILE_BROWSER = 4;

    @ViewInject(R.id.operation_sort_text)
    private TextView operationSortText;

    @ViewInject(R.id.batch_operation_bar_layout)
    private RelativeLayout batchOperationBarLayout;

    @ViewInject(R.id.batch_operation_header_layout)
    private RelativeLayout batchOprationHeaderLayout;

    @ViewInject(R.id.batch_operation_header_text)
    private TextView batchOprationHeaderText;

    @ViewInject(R.id.batch_operation_select_all_text)
    private TextView getBatchOprationSelectAllText;

    @ViewInject(operation_layout)
    protected RelativeLayout operationLayout;

    private PopupWindow sortOperationPop;
    private MyAppAPIService apiService;
    private String cameraPicFileName;
    private List<VolumeFile> moveVolumeFileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        setListIemClick();
    }

    private void setListIemClick() {
        adapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (volumeFile.getStatus().equals("normal")) {
                    if (!adapter.getMultiselect()) {
                        Bundle bundle = new Bundle();
                        if (volumeFile.getType().equals("directory")) {
                            bundle.putSerializable("volume", volume);
                            bundle.putSerializable("absolutePath", absolutePath + volumeFile.getName() + "/");
                            bundle.putSerializable("title", volumeFile.getName());
                            IntentUtils.startActivity(VolumeFileActivity.this, VolumeFileActivity.class, bundle);
                        } else {
                            bundle.putSerializable("volumeId", volume.getId());
                            bundle.putSerializable("volumeFile", volumeFile);
                            bundle.putSerializable("absolutePath", absolutePath + volumeFile.getName());
                            IntentUtils.startActivity(VolumeFileActivity.this, VolumeFileDownloadActivtiy.class, bundle);
                        }
                    } else {
                        adapter.setVolumeFileSelect(position);
                        batchOprationHeaderText.setText("已选择("+adapter.getSelectVolumeFileList().size()+")");
                    }
                }

            }
        });
        adapter.setItemDropDownImgClickListener(new VolumeFileAdapter.MyItemDropDownImgClickListener() {
            @Override
            public void onItemDropDownImgClick(View view, int position) {
                if (!adapter.getMultiselect()) {
                    showFileOperationDlg(volumeFileList.get(position));
                } else {
                    adapter.setVolumeFileSelect(position);
                }

            }
        });
    }

    /**
     * 初始化无数据时显示的ui
     */
    protected void initDataBlankLayoutStatus() {
        dataBlankLayout.setVisibility((volumeFileList.size() == 0) ? View.VISIBLE : View.GONE);
        operationLayout.setVisibility((volumeFileList.size() == 0) ? View.GONE : View.VISIBLE);
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
            case R.id.upload_img:
                showUploadFileDlg();
                break;
            case R.id.operation_sort_text:
                showSortOperationPop();
                break;
            case R.id.operation_multiselect_text:
                setMutiSelect(!adapter.getMultiselect());
                break;
            case R.id.operation_filter_text:
                showFileFilterPop(v);
                break;
            case R.id.sort_by_time_up_layout:
                sortType = SORT_BY_TIME_UP;
                sortOperationPop.dismiss();
                break;
            case R.id.sort_by_time_down_layout:
                sortType = SORT_BY_TIME_DOWN;
                sortOperationPop.dismiss();
                break;
            case R.id.sort_by_name_up_layout:
                sortType = SORT_BY_NAME_UP;
                sortOperationPop.dismiss();
                break;
            case R.id.sort_by_name_down_layout:
                sortType = SORT_BY_NAME_DOWN;
                sortOperationPop.dismiss();
                break;
            case R.id.batch_operation_delete_text:
                break;
            case R.id.batch_operation_copy_text:
                List<VolumeFile> copyVolumeFileList = adapter.getSelectVolumeFileList();
                GoCopyFile(copyVolumeFileList);
                break;
            case R.id.batch_operation_move_text:
                moveVolumeFileList = adapter.getSelectVolumeFileList();
                GomoveFile(moveVolumeFileList);
                break;
            case R.id.batch_operation_cancel_text:
                setMutiSelect(false);
                break;
            case R.id.batch_operation_select_all_text:
                boolean isSelectAllStatus = getBatchOprationSelectAllText.getText().toString().equals("全选");
                setselectAll(isSelectAllStatus);
                break;

            default:
                break;
        }
    }

    /**
     * 弹出选择上传文件提示框
     */
    private void showUploadFileDlg() {
        new ActionSheetDialog.ActionListSheetBuilder(VolumeFileActivity.this)
                .addItem("拍照")
                .addItem("选择照片")
                .addItem("选择文件")
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        switch (position) {
                            case 0:
                                cameraPicFileName = System.currentTimeMillis() + ".jpg";
                                AppUtils.openCamera(VolumeFileActivity.this, cameraPicFileName, REQUEST_OPEN_CEMERA);
                                break;
                            case 1:
                                AppUtils.openGallery(VolumeFileActivity.this, 1, REQUEST_OPEN_GALLERY);
                                break;
                            case 2:
                                AppUtils.openFileSystem(VolumeFileActivity.this, REQUEST_OPEN_FILE_BROWSER);
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
     * 弹出文件排序选择框
     */
    private void showSortOperationPop() {
        View contentView = LayoutInflater.from(VolumeFileActivity.this)
                .inflate(R.layout.app_volume_file_sort_operation_pop, null);
        sortOperationPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        ((TextView) contentView.findViewById(R.id.sort_by_time_up_text)).setTextColor(Color.parseColor(sortType.equals(SORT_BY_TIME_UP) ? "#2586CD" : "#666666"));
        ((TextView) contentView.findViewById(R.id.sort_by_time_down_text)).setTextColor(Color.parseColor(sortType.equals(SORT_BY_TIME_DOWN) ? "#2586CD" : "#666666"));
        ((TextView) contentView.findViewById(R.id.sort_by_name_up_text)).setTextColor(Color.parseColor(sortType.equals(SORT_BY_NAME_UP) ? "#2586CD" : "#666666"));
        ((TextView) contentView.findViewById(R.id.sort_by_name_down_text)).setTextColor(Color.parseColor(sortType.equals(SORT_BY_NAME_DOWN) ? "#2586CD" : "#666666"));
        (contentView.findViewById(R.id.sort_by_time_up_select_img)).setVisibility(sortType.equals(SORT_BY_TIME_UP)? View.VISIBLE : View.INVISIBLE);
        (contentView.findViewById(R.id.sort_by_time_down_select_img)).setVisibility(sortType.equals(SORT_BY_TIME_DOWN)? View.VISIBLE : View.INVISIBLE);
        (contentView.findViewById(R.id.sort_by_name_up_select_img)).setVisibility(sortType.equals(SORT_BY_NAME_UP)? View.VISIBLE : View.INVISIBLE);
        (contentView.findViewById(R.id.sort_by_name_down_select_img)).setVisibility(sortType.equals(SORT_BY_NAME_DOWN)? View.VISIBLE : View.INVISIBLE);
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
                setOperationSort();
            }
        });

    }

    /**
     * 设置排序显示
     */
    private void setOperationSort(){
        Drawable drawable1 = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_volume_menu_drop_down);
        drawable1.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 14), DensityUtil.dip2px(getApplicationContext(), 14));
        operationSortText.setCompoundDrawables(null, null, drawable1, null);
        String sortTypeShowTxt;
        switch (sortType){
            case SORT_BY_NAME_DOWN:
                sortTypeShowTxt = "名称降序";
                break;
            case SORT_BY_TIME_UP:
                sortTypeShowTxt = "时间升序";
                break;
            case SORT_BY_TIME_DOWN:
                sortTypeShowTxt = "时间降序";
                break;
            default:
                sortTypeShowTxt = "名称升序";
                break;
        }
        operationSortText.setText(sortTypeShowTxt);
        sortVolumeFileList();
    }

    /**
     * 弹出文件筛选框
     */
    private void showFileFilterPop(View v) {
        View contentView = LayoutInflater.from(VolumeFileActivity.this)
                .inflate(R.layout.app_volume_file_filter_pop, null);
        final PopupWindow fileFilterPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        GridView fileFilterGrid = (GridView) contentView.findViewById(R.id.file_filter_type_grid);
        fileFilterGrid.setAdapter(new VolumeFileFilterPopGridAdapter(VolumeFileActivity.this));
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
     * 弹出文件操作框
     *
     * @param title
     */
    protected void showFileOperationDlg(final VolumeFile volumeFile) {
        if (volumeFile.getType().equals("directory")){
            new ActionSheetDialog.ActionListSheetBuilder(VolumeFileActivity.this)
                    .setTitle(volumeFile.getName())
                    .addItem("删除")
                    .addItem("重命名")
                    .addItem("移动到")
                    .addItem("复制")
                    .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                        @Override
                        public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                            switch (position) {
                                case 0:
                                    showFileDelWranibgDlg(volumeFile);
                                    break;
                                case 1:
                                    showFileRenameDlg(volumeFile);
                                    break;
                                case 2:
                                    moveVolumeFileList.clear();
                                    moveVolumeFileList.add(volumeFile);
                                    GomoveFile(moveVolumeFileList);
                                    break;
                                case 3:
                                    List<VolumeFile> copyVolumeFileList = new ArrayList<VolumeFile>();
                                    copyVolumeFileList.add(volumeFile);
                                    GoCopyFile(copyVolumeFileList);
                                    break;
                                default:
                                    break;
                            }
                            dialog.dismiss();
                        }
                    })
                    .build()
                    .show();
        }else {
            new ActionSheetDialog.ActionListSheetBuilder(VolumeFileActivity.this)
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
                            Bundle bundle = null;
                            switch (position) {
                                case 0:
                                    showFileDelWranibgDlg(volumeFile);
                                    break;
                                case 1:
                                    bundle = new Bundle();
                                    bundle.putString("volumeId", volume.getId());
                                    bundle.putSerializable("volumeFile", volumeFile);
                                    bundle.putString("absolutePath", absolutePath + volumeFile.getName());
                                    bundle.putBoolean("isStartDownload", true);
                                    IntentUtils.startActivity(VolumeFileActivity.this, VolumeFileDownloadActivtiy.class, bundle);
                                    break;
                                case 2:
                                    showFileRenameDlg(volumeFile);
                                    break;
                                case 3:
                                    moveVolumeFileList.clear();
                                    moveVolumeFileList.add(volumeFile);
                                    GomoveFile(moveVolumeFileList);
                                    break;
                                case 4:
                                    List<VolumeFile> copyVolumeFileList = new ArrayList<VolumeFile>();
                                    copyVolumeFileList.add(volumeFile);
                                    GoCopyFile(copyVolumeFileList);
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

    }

    /**
     * 设置是否是多选状态
     *
     * @param isMutiselect
     */
    private void setMutiSelect(boolean isMutiselect) {
        getBatchOprationSelectAllText.setText("全选");
        batchOprationHeaderText.setText("已选择(0)");
        batchOperationBarLayout.setVisibility(isMutiselect ? View.VISIBLE : View.GONE);
        batchOprationHeaderLayout.setVisibility(isMutiselect ? View.VISIBLE : View.GONE);
        adapter.setShowFileOperationDropDownImg(!isMutiselect);
        adapter.setMultiselect(isMutiselect);
    }

    private void setselectAll(boolean isSelectAll){
        getBatchOprationSelectAllText.setText(isSelectAll?"全不选":"全选");
        adapter.setSelectAll(isSelectAll);
        batchOprationHeaderText.setText("已选择("+adapter.getSelectVolumeFileList().size()+")");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_OPEN_FILE_BROWSER) {  //文件浏览器选择文件返回
                Uri uri = data.getData();
                LogUtils.jasonDebug("uri=" + uri.toString());
                String filePath = GetPathFromUri4kitkat.getPathByUri(getApplicationContext(), uri);
                uploadFile(filePath);
            } else if (requestCode == REQUEST_OPEN_CEMERA //拍照返回
                    && NetUtils.isNetworkConnected(getApplicationContext())) {
                String filePath = Environment.getExternalStorageDirectory() + "/DCIM/" + cameraPicFileName;
                uploadFile(filePath);
            }else if (requestCode == REQUEST_MOVE_FILE){  //移动文件
                volumeFileList.removeAll(moveVolumeFileList);
                adapter.notifyDataSetChanged();
            }
        } else if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {  // 图库选择图片返回
            if (data != null && requestCode == REQUEST_OPEN_GALLERY) {
                ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data
                        .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                String filePath = imageItemList.get(0).path;
                uploadFile(filePath);
            }
        }

    }

    /**
     * 上传文件
     *
     * @param filePath
     */
    private void uploadFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            getVolumeFileUploadSTSToken(file);
        } else {
            ToastUtils.show(getApplicationContext(), "选择的文件不存在！");
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


    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnVolumeFileUploadSTSTokenSuccess(GetVolumeFileUploadSTSTokenResult getVolumeFileUploadSTSTokenResult, String filePath) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            long time = System.currentTimeMillis();
            File file = new File(filePath);
            VolumeFile volumeFile = new VolumeFile();
            volumeFile.setType("regular");
            volumeFile.setId(time + "");
            volumeFile.setCreationDate(time);
            volumeFile.setName(file.getName());
            volumeFile.setStatus("downloading");
            volumeFile.setFormat("." + FileUtils.getFileExtension(filePath));
            VolumeFileUploadUtils.getInstance().startUpload(getApplicationContext(), getVolumeFileUploadSTSTokenResult, null, volumeFile, filePath, volume.getId() + absolutePath);
            volumeFileList.add(0, volumeFile);
            initDataBlankLayoutStatus();
            adapter.setVolumeFileList(volumeFileList);
            adapter.notifyItemInserted(0);
        }

        @Override
        public void returnVolumeFileUploadSTSTokenFail(String error, int errorCode, String filePath) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
