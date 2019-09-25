package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeFileAdapter;
import com.inspur.emmcloud.adapter.VolumeFileFilterPopGridAdapter;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.basemodule.util.compressor.Compressor;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImagePicker;
import com.inspur.emmcloud.basemodule.util.imagepicker.bean.ImageItem;
import com.inspur.emmcloud.basemodule.util.imagepicker.ui.ImageGridActivity;
import com.inspur.emmcloud.basemodule.util.mycamera.MyCameraActivity;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.VolumeFilePrivilegeUtils;
import com.inspur.emmcloud.util.privates.VolumeFileUploadManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * 云盘-文件列表展示
 */

public class VolumeFileActivity extends VolumeFileBaseActivity {
    private static final int REQUEST_OPEN_CEMERA = 2;
    private static final int REQUEST_OPEN_GALLERY = 3;
    private static final int REQUEST_OPEN_FILE_BROWSER = 4;
    private static final int REQUEST_SHOW_FILE_FILTER = 5;
    @BindView(R.id.operation_sort_text)
    TextView operationSortText;
    @BindView(R.id.batch_operation_header_layout)
    RelativeLayout batchOprationHeaderLayout;
    @BindView(R.id.batch_operation_header_text)
    TextView batchOprationHeaderText;
    @BindView(R.id.batch_operation_select_all_text)
    TextView getBatchOprationSelectAllText;


    private PopupWindow sortOperationPop;
    private String cameraPicFileName;
    private BroadcastReceiver broadcastReceiver;
    private boolean isOpenFromParentDirectory = false;//是否从父级目录打开，如果是的话关闭时直接finish，否则需要打开父级页面
    private PopupWindow popupWindow;


    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        this.isShowFileUploading = true;
        isOpenFromParentDirectory = getIntent().getBooleanExtra("isOpenFromParentDirectory", false);
        setOperationSortText();
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
            public void onSelectedItemClick(View view, int position) {
                adapter.setVolumeFileSelect(position);
                batchOprationHeaderText.setText(getString(R.string.clouddriver_has_selected, adapter.getSelectVolumeFileList().size()));
                setBottomOperationItemShow(adapter.getSelectVolumeFileList());
                getBatchOprationSelectAllText.setText((volumeFileList.size() == adapter.getSelectVolumeFileList().size()) ? R.string.clouddriver_select_nothing : R.string.clouddriver_select_all);
                batchOprationHeaderText.setText(getString(R.string.clouddriver_has_selected, adapter.getSelectVolumeFileList().size()));
            }

            @Override
            public void onItemClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (volumeFile.getStatus().equals("normal") && adapter.getSelectVolumeFileList().size() == 0) {
                    if (!adapter.getMultiselect()) {
                        Bundle bundle = new Bundle();
                        if (volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
                            boolean isVolumeFileWriteable = VolumeFilePrivilegeUtils.getVolumeFileWriteable(getApplicationContext(), volumeFile);
                            boolean isVolumeFileReadable = VolumeFilePrivilegeUtils.getVolumeFileReadable(getApplicationContext(), volumeFile);
                            if (isVolumeFileWriteable || isVolumeFileReadable) {
                                bundle.putSerializable("volume", volume);
                                bundle.putSerializable("currentDirAbsolutePath", currentDirAbsolutePath + volumeFile.getName() + "/");
                                bundle.putSerializable("title", volumeFile.getName());
                                bundle.putBoolean("isOpenFromParentDirectory", true);
                                IntentUtils.startActivity(VolumeFileActivity.this, VolumeFileActivity.class, bundle);
                            } else {
                                ToastUtils.show(R.string.volume_no_permission);
                            }
                        } else {
                            downloadOrOpenVolumeFile(volumeFile);
                        }
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

            @Override
            public void onItemDropDownImgClick(View view, int position) {
                if (!adapter.getMultiselect()) {
                    showFileOperationDlg(volumeFileList.get(position));
                } else {
                    adapter.setVolumeFileSelect(position);
                }

            }

            @Override
            public void onItemOperationTextClick(View view, int position) {
                VolumeFile volumeFile = volumeFileList.get(position);
                if (volumeFile.getStatus().equals(VolumeFile.STATUS_UPLOADIND)) {
                    //取消上传
                    VolumeFileUploadManager.getInstance().cancelVolumeFileUploadService(volumeFile);
                    volumeFileList.remove(position);
                    adapter.notifyItemRemoved(position);
                } else if (NetUtils.isNetworkConnected(VolumeFileActivity.this)) {
                    //重新上传
                    volumeFile.setStatus(VolumeFile.STATUS_UPLOADIND);
                    VolumeFileUploadManager.getInstance().reUploadFile(volumeFile);
                    adapter.notifyItemChanged(position);
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
    @Override
    protected void initDataBlankLayoutStatus() {
        super.initDataBlankLayoutStatus();
        if (adapter.getMultiselect()) {
            setMutiSelect(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMessage(SimpleEventMessage simpleEventMessage) {
        if (simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_FILE_SORT_TIME_CHANGED)) {
            sortType = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_VOLUME_FILE_SORT_TYPE, SORT_BY_NAME_UP);
            setOperationSort();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                onBackPressed();
                break;
            case R.id.iv_head_operation:
                showUploadOperationPopWindow(new ArrayList<VolumeFile>());
                break;
            case R.id.btn_upload_file:
                break;
            case R.id.iv_down_up_list:
                break;
            case R.id.operation_sort_text:
                showSortOperationPop();
                break;
            case R.id.operation_multiselect_text:
                setMutiSelect(true);
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
            case R.id.ll_volume_upload_image_pop:
                AppUtils.openGallery(VolumeFileActivity.this, 10, REQUEST_OPEN_GALLERY, true);
                break;
            case R.id.ll_volume_new_folder_pop:
                showCreateFolderDlg();
                break;
            case R.id.ll_volume_upload_file_pop:
                Bundle bundle = new Bundle();
                bundle.putInt("extra_maximum", 10);
                ARouter.getInstance().
                        build(Constant.AROUTER_CLASS_WEB_FILEMANAGER).with(bundle).
                        navigation(VolumeFileActivity.this, REQUEST_OPEN_FILE_BROWSER);
                break;
            case R.id.ll_volume_take_phone_pop:
                cameraPicFileName = System.currentTimeMillis() + ".jpg";
                AppUtils.openCamera(VolumeFileActivity.this, cameraPicFileName, REQUEST_OPEN_CEMERA);
                break;
            case R.id.batch_operation_cancel_text:
                setMutiSelect(true);
                break;
            case R.id.batch_operation_select_all_text:
                boolean isSelectAllStatus = getBatchOprationSelectAllText.getText().toString().equals(getString(R.string.clouddriver_select_all));
                setselectAll(isSelectAllStatus);
                setBottomOperationItemShow(adapter.getSelectVolumeFileList());
                break;
            default:
                break;
        }
    }

    /**
     * 设置pop框
     */
    private void showUploadOperationPopWindow(List<VolumeFile> selectVolumeFileList) {
        View view = LayoutInflater.from(VolumeFileActivity.this)
                .inflate(R.layout.volume_top_operation_layout, null);
        popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        view.findViewById(R.id.ll_volume_download_pop).setVisibility(selectVolumeFileList.size() == 1 &&
                !selectVolumeFileList.get(0).getType().equals(VolumeFile.FILE_TYPE_DIRECTORY) ?
                View.VISIBLE : View.GONE);
        view.findViewById(R.id.ll_volume_move_pop).setVisibility(selectVolumeFileList.size() > 0 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.ll_volume_copy_pop).setVisibility(selectVolumeFileList.size() > 0 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.ll_volume_delete_pop).setVisibility(selectVolumeFileList.size() > 0 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.ll_volume_more_pop).setVisibility(selectVolumeFileList.size() == 1 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.ll_volume_rename_pop).setVisibility(selectVolumeFileList.size() == 1 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.ll_volume_upload_image_pop).setVisibility(selectVolumeFileList.size() > 0 ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.ll_volume_new_folder_pop).setVisibility(selectVolumeFileList.size() > 0 ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.ll_volume_upload_file_pop).setVisibility(selectVolumeFileList.size() > 0 ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.ll_volume_take_phone_pop).setVisibility(selectVolumeFileList.size() > 0 ? View.GONE : View.VISIBLE);
        View parentView = LayoutInflater.from(this).inflate(R.layout.activity_volume_file, null);
        popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);      //相对父布局

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
                Drawable drawable1 = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_volume_menu_drop_down);
                drawable1.setBounds(0, 0, DensityUtil.dip2px(getApplicationContext(), 14), DensityUtil.dip2px(getApplicationContext(), 14));
                operationSortText.setCompoundDrawables(null, null, drawable1, null);
                String sortTypeOld = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_VOLUME_FILE_SORT_TYPE, SORT_BY_NAME_UP);
                if (!sortTypeOld.equals(sortType)) {
                    PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), Constant.PREF_VOLUME_FILE_SORT_TYPE, sortType);
                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_VOLUME_FILE_SORT_TIME_CHANGED));
                }

            }
        });

    }


    /**
     * 设置排序显示
     */
    private void setOperationSort() {
        setOperationSortText();
        sortVolumeFileList();
        adapter.setVolumeFileList(volumeFileList);
        adapter.notifyDataSetChanged();

    }

    private void setOperationSortText() {
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

    /**
     * 设置是否是多选状态
     *
     * @param isMutiselect
     */
    private void setMutiSelect(boolean isMutiselect) {
        getBatchOprationSelectAllText.setText(R.string.clouddriver_select_all);
        batchOprationHeaderText.setText(getString(R.string.clouddriver_has_selected, 0));
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
                if (data.hasExtra("pathList")) {
                    ArrayList<String> pathList;
                    pathList = data.getStringArrayListExtra("pathList");
                    if (pathList == null) {
                        pathList = new ArrayList<>();
                    }
                    for (int i = 0; i < pathList.size(); i++) {
                        uploadFile(pathList.get(i));
                    }
                }
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
                Boolean originalPicture = data.getBooleanExtra(ImageGridActivity.EXTRA_ORIGINAL_PICTURE, false);
                ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data
                        .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                for (int i = 0; i < imageItemList.size(); i++) {
                    String imgPath = imageItemList.get(i).path;
                    if (!originalPicture) {
                        try {
                            File file = new Compressor(VolumeFileActivity.this).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                    .compressToFile(new File(imgPath));
                            imgPath = file.getAbsolutePath();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    uploadFile(imgPath);
                }
            }
        } else if (requestCode == SHARE_IMAGE_OR_FILES) {
            // shareToVolumeFile
            String result = data.getStringExtra("searchResult");
            try {
                String userOrChannelId = "";
                boolean isGroup = false;
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("people")) {
                    JSONArray peopleArray = jsonObject.getJSONArray("people");
                    if (peopleArray.length() > 0) {
                        JSONObject peopleObj = peopleArray.getJSONObject(0);
                        userOrChannelId = peopleObj.getString("pid");
                        isGroup = false;
                    }
                }

                if (jsonObject.has("channelGroup")) {
                    JSONArray channelGroupArray = jsonObject
                            .getJSONArray("channelGroup");
                    if (channelGroupArray.length() > 0) {
                        JSONObject cidObj = channelGroupArray.getJSONObject(0);
                        userOrChannelId = cidObj.getString("cid");
                        isGroup = true;
                    }
                }
                if (StringUtils.isBlank(userOrChannelId)) {
                    ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
                } else {
                    if (isGroup) {
                        startChannelActivity(userOrChannelId);
                    } else {
                        createDirectChannel(userOrChannelId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
            }
        }

    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        bundle.putString("share_type", "file");
        bundle.putString("path", currentDirAbsolutePath); //currentDirAbsolutePath
        bundle.putSerializable("share_obj_form_volume", (Serializable) shareToVolumeFile);
        IntentUtils.startActivity(this, ConversationActivity.class, bundle, true);
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(this, uid,
                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            startChannelActivity(conversation.getId());
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            startChannelActivity(getCreateSingleChannelResult.getCid());
                        }

                        @Override
                        public void createDirectChannelFail() {
                        }
                    });
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
        VolumeFile mockVolumeFile = getMockVolumeFileData(file);
        VolumeFileUploadManager.getInstance().uploadFile(mockVolumeFile, filePath, currentDirAbsolutePath);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSimpleEventMessage(SimpleEventMessage simpleEventMessage) {
        if (simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_UPLOAD)) {
            getVolumeFileList(false);
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
        volumeFile.setStatus(VolumeFile.STATUS_UPLOADIND);
        volumeFile.setVolume(volume.getId());
        volumeFile.setFormat(FileUtils.getMimeType(file.getName()));
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
            if (forders.length < 2) {
                finish();
                return;
            }
            String parentForderName = forders[forders.length - 2];
            String parentDirAbsolutePath = currentDirAbsolutePath.substring(0, currentDirAbsolutePath.length() - 1 - forders[forders.length - 1].length());
            Bundle bundle = new Bundle();
            bundle.putSerializable("volume", volume);
            bundle.putSerializable("currentDirAbsolutePath", parentDirAbsolutePath);
            if (parentDirAbsolutePath.equals("/")) {
                if (volume.getType().equals("private")) {
                    parentForderName = getString(R.string.clouddriver_my_file);
                } else {
                    parentForderName = volume.getName();
                }
            }
            bundle.putSerializable("title", parentForderName);
            IntentUtils.startActivity(VolumeFileActivity.this, VolumeFileActivity.class, bundle, true);
        }

    }

    @Override
    protected void onResume() {
        setBottomOperationItemShow(adapter.getSelectVolumeFileList());
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        super.onDestroy();
    }

}
