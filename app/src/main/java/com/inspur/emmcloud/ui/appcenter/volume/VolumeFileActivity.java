package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.adapter.VolumeFileFilterPopGridAdapter;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.VolumeFilePrivilegeUtils;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManagerUtils;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.mycamera.MyCameraActivity;
import com.inspur.imp.util.compressor.Compressor;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;


/**
 * 云盘-文件列表展示
 */

public class VolumeFileActivity extends VolumeFileBaseActivity {
    private static final int REQUEST_OPEN_CEMERA = 2;
    private static final int REQUEST_OPEN_GALLERY = 3;
    private static final int REQUEST_OPEN_FILE_BROWSER = 4;
    private static final int REQUEST_SHOW_FILE_FILTER = 5;
    @BindView(R.id.operation_layout)
    RelativeLayout operationLayout;
    @BindView(R.id.operation_sort_text)
    TextView operationSortText;
    @BindView(R.id.batch_operation_bar_layout)
    RelativeLayout batchOperationBarLayout;
    @BindView(R.id.batch_operation_header_layout)
    RelativeLayout batchOprationHeaderLayout;
    @BindView(R.id.batch_operation_header_text)
    TextView batchOprationHeaderText;
    @BindView(R.id.batch_operation_select_all_text)
    TextView getBatchOprationSelectAllText;
    @BindView(R.id.batch_operation_delete_text)
    TextView batchOperationDeleteText;
    @BindView(R.id.batch_operation_move_text)
    TextView batchOperationMoveText;
    private PopupWindow sortOperationPop;
    private String cameraPicFileName;
    private BroadcastReceiver broadcastReceiver;
    private boolean isOpenFromParentDirectory = false;//是否从父级目录打开，如果是的话关闭时直接finish，否则需要打开父级页面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.isShowFileUploading = true;
        isOpenFromParentDirectory = getIntent().getBooleanExtra("isOpenFromParentDirectory", false);
        setListIemClick();
        registerReceiver();
        handleFileShareToVolume();
    }

    /**
     * 处理文件分享
     */
    private void handleFileShareToVolume() {
        List<String> fileShareUriList = (List<String>) getIntent().getSerializableExtra(Constant.SHARE_FILE_URI_LIST);
        if (fileShareUriList != null && NetUtils.isNetworkConnected(this)) {
            for (int i = 0; i < fileShareUriList.size(); i++) {
                uploadFile(fileShareUriList.get(i));
            }
        }
    }

    private void setListIemClick() {
        adapter.setItemClickListener(new VolumeFileAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (volumeFile.getStatus().equals("normal")) {
                    if (!adapter.getMultiselect()) {
                        Bundle bundle = new Bundle();
                        if (volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
                            bundle.putSerializable("volume", volume);
                            bundle.putSerializable("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName() + "/");
                            bundle.putSerializable("title", volumeFile.getName());
                            bundle.putBoolean("isOpenFromParentDirectory", true);
                            IntentUtils.startActivity(VolumeFileActivity.this, VolumeFileActivity.class, bundle);
                        } else {
                            downloadOrOpenVolumeFile(volumeFile);
                        }
                    } else {
                        adapter.setVolumeFileSelect(position);
                        batchOprationHeaderText.setText(getString(R.string.clouddriver_has_selected, adapter.getSelectVolumeFileList().size()));
                        setBatchOprationLayoutByPrivilege();
                    }
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (volumeFile.getStatus().equals("normal") && !adapter.getMultiselect()) {
                    showFileOperationDlg(volumeFileList.get(position));
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

    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String directoryId = intent.getStringExtra("directoryId");
                if (directoryId != null && directoryId.equals(getVolumeFileListResult.getId())) {
                    String command = intent.getStringExtra("command");
                    if (command != null && command.equals("refresh")) {
                        getVolumeFileList(true);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter("broadcast_volume");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * 初始化无数据时显示的ui
     */
    protected void initDataBlankLayoutStatus() {
        super.initDataBlankLayoutStatus();
        operationLayout.setVisibility((volumeFileList.size() == 0) ? View.GONE : View.VISIBLE);
        if (adapter.getMultiselect()) {
            setMutiSelect(false);
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                onBackPressed();
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
                deleteFile(adapter.getSelectVolumeFileList());
                break;
            case R.id.batch_operation_copy_text:
                copyFile(adapter.getSelectVolumeFileList());
                break;
            case R.id.batch_operation_move_text:
                moveFile(adapter.getSelectVolumeFileList());
                break;
            case R.id.batch_operation_cancel_text:
                setMutiSelect(false);
                break;
            case R.id.batch_operation_select_all_text:
                boolean isSelectAllStatus = getBatchOprationSelectAllText.getText().toString().equals(getString(R.string.clouddriver_select_all));
                setselectAll(isSelectAllStatus);
                setBatchOprationLayoutByPrivilege();
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
                .addItem(getString(R.string.take_photo))
                .addItem(getString(R.string.clouddriver_select_photo))
                .addItem(getString(R.string.clouddriver_select_file))
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
        (contentView.findViewById(R.id.sort_by_time_up_select_img)).setVisibility(sortType.equals(SORT_BY_TIME_UP) ? View.VISIBLE : View.INVISIBLE);
        (contentView.findViewById(R.id.sort_by_time_down_select_img)).setVisibility(sortType.equals(SORT_BY_TIME_DOWN) ? View.VISIBLE : View.INVISIBLE);
        (contentView.findViewById(R.id.sort_by_name_up_select_img)).setVisibility(sortType.equals(SORT_BY_NAME_UP) ? View.VISIBLE : View.INVISIBLE);
        (contentView.findViewById(R.id.sort_by_name_down_select_img)).setVisibility(sortType.equals(SORT_BY_NAME_DOWN) ? View.VISIBLE : View.INVISIBLE);
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
    private void setOperationSort() {
        Drawable drawable1 = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_volume_menu_drop_down);
        drawable1.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 14), DensityUtil.dip2px(getApplicationContext(), 14));
        operationSortText.setCompoundDrawables(null, null, drawable1, null);
        String sortTypeShowTxt;
        switch (sortType) {
            case SORT_BY_NAME_DOWN:
                sortTypeShowTxt = getString(R.string.clouddriver_sort_by_name_dasc);
                break;
            case SORT_BY_TIME_UP:
                sortTypeShowTxt = getString(R.string.clouddriver_sort_by_time_asc);
                break;
            case SORT_BY_TIME_DOWN:
                sortTypeShowTxt = getString(R.string.clouddriver_sort_by_time_dasc);
                break;
            default:
                sortTypeShowTxt = getString(R.string.clouddriver_sort_by_name_asc);
                break;
        }
        operationSortText.setText(sortTypeShowTxt);
        sortVolumeFileList();
        adapter.setVolumeFileList(volumeFileList);
        adapter.notifyDataSetChanged();
    }


    /**
     * 文件排序
     */
    protected void sortVolumeFileList() {
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
                String[] fileFilterTypes = {VolumeFile.FILTER_TYPE_DOCUNMENT, VolumeFile.FILTER_TYPE_IMAGE, VolumeFile.FILTER_TYPE_AUDIO, VolumeFile.FILTER_TYPE_VIDEO, VolumeFile.FILTER_TYPE_OTHER};
                Intent intent = new Intent(VolumeFileActivity.this, VolumeFileFilterActvity.class);
                Bundle bundle = getIntent().getExtras();
                bundle.putString("title", getString(R.string.app_classification));
                bundle.putString("fileFilterType", fileFilterTypes[position]);
                bundle.putString("currentDirAbsolutePath", currentDirAbsolutePath);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_SHOW_FILE_FILTER);
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
     * 设置当前目录权限有关的layout展示
     */
    @Override
    protected void setCurrentDirectoryLayoutByPrivilege() {
        boolean isCurrentDirectoryWriteable = VolumeFilePrivilegeUtils.getVolumeFileWriteable(getApplicationContext(), getVolumeFileListResult);
        headerOperationLayout.setVisibility(isCurrentDirectoryWriteable ? View.VISIBLE : View.GONE);
    }

    private void setBatchOprationLayoutByPrivilege() {
        List<VolumeFile> selectVolumeFileList = adapter.getSelectVolumeFileList();
        if (selectVolumeFileList.size() > 0) {
            batchOperationBarLayout.setVisibility(View.VISIBLE);
            boolean isFileListWriteable = VolumeFilePrivilegeUtils.getVolumeFileListWriteable(getApplicationContext(), selectVolumeFileList);
            batchOperationDeleteText.setVisibility(isFileListWriteable ? View.VISIBLE : View.GONE);
            batchOperationMoveText.setVisibility(isFileListWriteable ? View.VISIBLE : View.GONE);
        } else {
            batchOperationBarLayout.setVisibility(View.GONE);
        }

    }

    /**
     * 设置是否是多选状态
     *
     * @param isMutiselect
     */
    private void setMutiSelect(boolean isMutiselect) {
        getBatchOprationSelectAllText.setText(R.string.clouddriver_select_all);
        batchOprationHeaderText.setText(getString(R.string.clouddriver_has_selected, 0));
        if (!isMutiselect) {
            batchOperationBarLayout.setVisibility(View.GONE);
        }
        batchOprationHeaderLayout.setVisibility(isMutiselect ? View.VISIBLE : View.GONE);
        adapter.setShowFileOperationDropDownImg(!isMutiselect);
        adapter.setMultiselect(isMutiselect);
    }

    private void setselectAll(boolean isSelectAll) {
        getBatchOprationSelectAllText.setText(isSelectAll ? R.string.clouddriver_select_nothing : R.string.clouddriver_select_all);
        adapter.setSelectAll(isSelectAll);
        batchOprationHeaderText.setText(getString(R.string.clouddriver_has_selected, adapter.getSelectVolumeFileList().size()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_OPEN_FILE_BROWSER) {  //文件浏览器选择文件返回
                Uri uri = data.getData();
                String filePath = GetPathFromUri4kitkat.getPathByUri(getApplicationContext(), uri);
                uploadFile(filePath);
            } else if (requestCode == REQUEST_OPEN_CEMERA //拍照返回
                    && NetUtils.isNetworkConnected(getApplicationContext())) {
                String imgPath = data.getExtras().getString(MyCameraActivity.OUT_FILE_PATH);
                try {
                    File file = new Compressor(VolumeFileActivity.this).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                            .compressToFile(new File(imgPath));
                    imgPath = file.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                uploadFile(imgPath);
            } else if (requestCode == REQUEST_SHOW_FILE_FILTER) {  //移动文件
                getVolumeFileList(false);
            }
        } else if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {  // 图库选择图片返回
            if (data != null && requestCode == REQUEST_OPEN_GALLERY) {
                ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data
                        .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                String imgPath = imageItemList.get(0).path;
                try {
                    File file = new Compressor(VolumeFileActivity.this).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                            .compressToFile(new File(imgPath));
                    imgPath = file.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                uploadFile(imgPath);
            }
        }

    }

    /**
     * 上传文件
     *
     * @param filePath
     */
    private void uploadFile(String filePath) {
        if (filePath == null) {
            ToastUtils.show(getApplicationContext(), R.string.clouddriver_file_no_exist);
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            ToastUtils.show(getApplicationContext(), R.string.clouddriver_file_no_exist);
            return;

        }
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            VolumeFile mockVolumeFile = getMockVolumeFileData(file);
            VolumeFileUploadManagerUtils.getInstance().uploadFile(mockVolumeFile, filePath, currentDirAbsolutePath);
            volumeFileList.add(0, mockVolumeFile);
            initDataBlankLayoutStatus();
            adapter.setVolumeFileList(volumeFileList);
            adapter.notifyItemInserted(0);
            //解决RecyclerView当数据添加到第一位置，显示位置不正确的系统bug
            fileRecycleView.scrollToPosition(0);
        }
    }

    /**
     * 生成一个用于上传展示的数据
     *
     * @param file
     * @return
     */
    private VolumeFile getMockVolumeFileData(File file) {
        long time = System.currentTimeMillis();
        VolumeFile volumeFile = new VolumeFile();
        volumeFile.setType(VolumeFile.FILE_TYPE_REGULAR);
        volumeFile.setId(time + "");
        volumeFile.setCreationDate(time);
        volumeFile.setName(file.getName());
        volumeFile.setStatus("downloading");
        volumeFile.setVolume(volume.getId());
        volumeFile.setFormat("");
        return volumeFile;
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        if (isOpenFromParentDirectory || currentDirAbsolutePath.equals("/")) {
            finish();
        } else {
            //当从外部分享完成后进入到相应界面，返回时按目录结构逐级回退
            String[] forders = currentDirAbsolutePath.split("/");
            String parentForderName = forders[forders.length - 1];
            String parentDirAbsolutePath = currentDirAbsolutePath.substring(0, currentDirAbsolutePath.length() - 1 - parentForderName.length());
            Bundle bundle = new Bundle();
            bundle.putSerializable("volume", volume);
            bundle.putSerializable("currentDirAbsolutePath", parentDirAbsolutePath);
            bundle.putSerializable("title", parentForderName);
            IntentUtils.startActivity(VolumeFileActivity.this, VolumeFileActivity.class, bundle, true);
        }

    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        super.onDestroy();
    }

    private class FileSortComparable implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            VolumeFile volumeFileA = (VolumeFile) o1;
            VolumeFile volumeFileB = (VolumeFile) o2;
            int sortResult = 0;
            if (volumeFileA.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY) && volumeFileB.getType().equals(VolumeFile.FILE_TYPE_REGULAR)) {
                sortResult = -1;
            } else if (volumeFileB.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY) && volumeFileA.getType().equals(VolumeFile.FILE_TYPE_REGULAR)) {
                sortResult = 1;
            } else {
                switch (sortType) {
                    case SORT_BY_NAME_UP:
                        sortResult = Collator.getInstance(Locale.CHINA).compare(volumeFileA.getName(), volumeFileB.getName());
                        break;
                    case SORT_BY_NAME_DOWN:
                        sortResult = 0 - Collator.getInstance(Locale.CHINA).compare(volumeFileA.getName(), volumeFileB.getName());
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


}
