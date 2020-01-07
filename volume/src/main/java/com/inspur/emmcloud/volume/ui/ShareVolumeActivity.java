package com.inspur.emmcloud.volume.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.FileActionData;
import com.inspur.emmcloud.baselib.widget.FileActionLayout;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.volume.Volume;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.volume.R;
import com.inspur.emmcloud.volume.R2;
import com.inspur.emmcloud.volume.api.VolumeAPIInterfaceInstance;
import com.inspur.emmcloud.volume.api.VolumeAPIService;
import com.inspur.emmcloud.volume.bean.GetVolumeListResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 云盘-共享网盘列表页面
 */

public class ShareVolumeActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int UPDATE_VOLUME_NAME = 1;
    @BindView(R2.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R2.id.share_volume_list)
    ListView shareVolumeListView;
    @BindView(R2.id.ll_volume_bottom_action)
    FileActionLayout bottomActionLayout;
    private List<Volume> shareVolumeList = new ArrayList<>();
    private List<Volume> selectedShareVolumeList = new ArrayList<>();
    private Adapter adapter;
    private VolumeAPIService apiService;
    private LoadingDialog loadingDlg;
    private String deleteVolumeAction, volumeDetailAction, renameVolumeAction;
    private MyDialog createShareVolumeDlg, updateShareVolumeNameDlg;

    private boolean isCopyOrMoveOperation = false;
    private boolean isCopy = false;
    private Volume copyFromVolume;
    private String operationFileDirAbsolutePath;
    private String title;
    private List<VolumeFile> fromVolumeVolumeFileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
        getVolumeList(true, true);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.volume_activity_share_volume;
    }

    private void initView() {
        if (getIntent().hasExtra(VolumeFileBaseActivity.EXTRA_IS_FUNCTION_COPY_OR_MOVE)) {
            isCopyOrMoveOperation = true;
            isCopy = getIntent().getBooleanExtra(VolumeFileBaseActivity.EXTRA_IS_FUNCTION_COPY_OR_MOVE, true);
            copyFromVolume = (Volume) getIntent().getSerializableExtra(VolumeFileBaseActivity.EXTRA_FROM_VOLUME);
            operationFileDirAbsolutePath = getIntent().getStringExtra(VolumeFileBaseActivity.EXTRA_OPERATION_FILE_DIR_ABS_PATH);
            title = getIntent().getStringExtra(VolumeFileBaseActivity.EXTRA_VOLUME_FILE_TITLE);
            fromVolumeVolumeFileList = (List<VolumeFile>) (getIntent().getSerializableExtra(VolumeFileBaseActivity.EXTRA_VOLUME_FILE_LIST));
        }
        loadingDlg = new LoadingDialog(this);
        apiService = new VolumeAPIService(this);
        apiService.setAPIInterface(new WebService());
        adapter = new Adapter();
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.header_bg_blue), ContextCompat.getColor(getApplicationContext(), R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(this);
        shareVolumeListView.setAdapter(adapter);
        shareVolumeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedShareVolumeList.size() > 0) {
                    if (selectedShareVolumeList.contains(shareVolumeList.get(position))) {
                        selectedShareVolumeList.clear();
                    } else {
                        selectedShareVolumeList.clear();
                        selectedShareVolumeList.add(shareVolumeList.get(position));
                    }
                    setBottomOperationItemShow(selectedShareVolumeList);
                }else {
                    List<Uri> shareUriList = null;
                    if (getIntent() != null && getIntent().hasExtra(Constant.SHARE_FILE_URI_LIST)) {
                        shareUriList = (List<Uri>) getIntent().getSerializableExtra(Constant.SHARE_FILE_URI_LIST);
                    }
                    Volume volume = shareVolumeList.get(position);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("volume", volume);
                    bundle.putString("title", volume.getName());
                    if (shareUriList != null && shareUriList.size() > 0) {
                        bundle.putSerializable(VolumeFileBaseActivity.EXTRA_FILE_SHARE_URI, (Serializable) shareUriList);
                        bundle.putString(VolumeFileBaseActivity.EXTRA_OPERATION_FILE_DIR_ABS_PATH, "/");
                        IntentUtils.startActivity(ShareVolumeActivity.this, VolumeFileLocationSelectActivity.class, bundle);
                    } else {
                        if (isCopyOrMoveOperation) {
                            Intent intent = new Intent(ShareVolumeActivity.this, VolumeFileLocationSelectActivity.class);
                            bundle.putSerializable(VolumeFileBaseActivity.EXTRA_FROM_VOLUME, copyFromVolume);
                            bundle.putSerializable(VolumeFileBaseActivity.EXTRA_VOLUME_FILE_LIST, (Serializable) fromVolumeVolumeFileList);
                            bundle.putBoolean(VolumeFileBaseActivity.EXTRA_IS_FUNCTION_COPY_OR_MOVE, isCopy);
                            bundle.putString(VolumeFileBaseActivity.EXTRA_OPERATION_FILE_DIR_ABS_PATH, operationFileDirAbsolutePath);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, isCopy ? VolumeFileBaseActivity.REQUEST_COPY_FILE : VolumeFileBaseActivity.REQUEST_MOVE_FILE);
                        } else {
                            IntentUtils.startActivity(ShareVolumeActivity.this, VolumeFileActivity.class, bundle);
                        }
                    }
                }
            }
        });
        shareVolumeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedShareVolumeList.contains(shareVolumeList.get(position))) {
                    selectedShareVolumeList.clear();
                } else {
                    selectedShareVolumeList.clear();
                    selectedShareVolumeList.add(shareVolumeList.get(position));
                }
                setBottomOperationItemShow(selectedShareVolumeList);
                return true;
            }
        });
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.add_img) {
            showCreateShareVolumeDlg();
        }
    }


    /**
     * 弹出新建文件夹提示框
     */
    protected void showCreateShareVolumeDlg() {
        createShareVolumeDlg = new MyDialog(ShareVolumeActivity.this,
                R.layout.volume_dialog_update_name_input);
        createShareVolumeDlg.setCancelable(false);
        final EditText inputEdit = createShareVolumeDlg.findViewById(R.id.edit);
        inputEdit.setHint(R.string.volume_clouddriver_input_volume_name);
        inputEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MyAppConfig.VOLUME_MAX_FILE_NAME_LENGTH)});
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView) createShareVolumeDlg.findViewById(R.id.app_update_title)).setText(R.string.volume_clouddriver_create_volume);
        Button okBtn = createShareVolumeDlg.findViewById(R.id.ok_btn);
        okBtn.setText(R.string.volume_create);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareVolumeName = inputEdit.getText().toString();
                if (StringUtils.isBlank(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), R.string.volume_clouddriver_input_volume_name);
                } else if (!FomatUtils.isValidFileName(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), R.string.volume_clouddriver_volume_name_invaliad);
                } else {
                    createShareVolume(shareVolumeName);
                }
            }
        });

        (createShareVolumeDlg.findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createShareVolumeDlg.dismiss();
            }
        });
        createShareVolumeDlg.show();
        InputMethodUtils.display(ShareVolumeActivity.this, inputEdit);
    }

    /**
     * 根据所选文件的类型展示操作按钮
     */
    protected void setBottomOperationItemShow(List<Volume> selectVolumeList) {
        adapter.notifyDataSetChanged();
        boolean isOwner = true;
        for (int i = 0; i < selectVolumeList.size(); i++) {
            if (!selectVolumeList.get(i).getOwner().equals(BaseApplication.getInstance().getUid())) {
                isOwner = false;
                break;
            }
        }
        if (isOwner == false && (selectVolumeList.size() > 1 || selectVolumeList.size() == 0)) {
            bottomActionLayout.setVisibility(View.GONE);
            return;
        }
        bottomActionLayout.setVisibility(selectVolumeList.size() > 0 ? View.VISIBLE : View.GONE);
        final List<FileActionData> volumeActionDataList = new ArrayList<>();
        volumeDetailAction = getString(R.string.detail);
        deleteVolumeAction = getString(R.string.delete);
        renameVolumeAction = getString(R.string.volume_rename);
        volumeActionDataList.add(new FileActionData(volumeDetailAction, R.drawable.volume_ic_detail, selectVolumeList.size() == 1 && true));
        volumeActionDataList.add(new FileActionData(deleteVolumeAction, R.drawable.volume_ic_delete, isOwner));
        volumeActionDataList.add(new FileActionData(renameVolumeAction, R.drawable.volume_ic_rename, selectVolumeList.size() == 1 && isOwner));
        bottomActionLayout.setFileActionData(volumeActionDataList, new FileActionLayout.FileActionClickListener() {
            @Override
            public void fileActionSelectedListener(String actionName) {
                handleVolumeAction(actionName);
            }
        });
    }

    private void handleVolumeAction(String actionName) {
        if (deleteVolumeAction.equals(actionName)) {
            showVolumeDelWranibgDlg();
        } else if (renameVolumeAction.equals(actionName)) {
            showUpdateShareVolumeNameDlg(selectedShareVolumeList.get(0));
        } else if (volumeDetailAction.equals(actionName)) {
            Intent intent = new Intent(ShareVolumeActivity.this, ShareVolumeInfoActivity.class);
            intent.putExtra("volume", selectedShareVolumeList.get(0));
            startActivityForResult(intent, UPDATE_VOLUME_NAME);
        }
    }

    /**
     * EventBus传递消息
     * @param eventMessage
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_VOLUME_FILE_LOCATION_SELECT_CLOSE)) {
            if (getIntent() != null && getIntent().hasExtra(Constant.SHARE_FILE_URI_LIST)) {
                finish();
            }

        }
    }


    /**
     * 弹出文件删除提示框
     *
     */
    protected void showVolumeDelWranibgDlg() {
        new CustomDialog.MessageDialogBuilder(ShareVolumeActivity.this)
                .setMessage(R.string.volume_clouddriver_sure_delete_volume)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        bottomActionLayout.setVisibility(View.VISIBLE);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeShareVolume();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 弹出更改共享网盘名称弹出框
     *
     * @param volume
     */
    private void showUpdateShareVolumeNameDlg(final Volume volume) {
        updateShareVolumeNameDlg = new MyDialog(ShareVolumeActivity.this,
                R.layout.volume_dialog_update_name_input);
        updateShareVolumeNameDlg.setCancelable(false);
        final EditText inputEdit = updateShareVolumeNameDlg.findViewById(R.id.edit);
        inputEdit.setText(volume.getName());
        inputEdit.setSelectAllOnFocus(true);
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        inputEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MyAppConfig.VOLUME_MAX_FILE_NAME_LENGTH)});
        ((TextView) updateShareVolumeNameDlg.findViewById(R.id.app_update_title)).setText(R.string.volume_rename);
        Button okBtn = updateShareVolumeNameDlg.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareVolumeName = inputEdit.getText().toString();
                if (StringUtils.isBlank(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), R.string.volume_clouddriver_input_volume_name);
                } else if (!FomatUtils.isValidFileName(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), R.string.volume_clouddriver_volume_name_invaliad);
                } else if (!shareVolumeName.equals(volume.getName())) {
                    updateShareVolumeName(volume, shareVolumeName);
                } else {
                    updateShareVolumeNameDlg.dismiss();
                }
            }
        });

        (updateShareVolumeNameDlg.findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomActionLayout.setVisibility(View.VISIBLE);
                updateShareVolumeNameDlg.dismiss();
            }
        });
        updateShareVolumeNameDlg.show();
        InputMethodUtils.display(ShareVolumeActivity.this, inputEdit);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case VolumeFileBaseActivity.REQUEST_COPY_FILE:
                case VolumeFileBaseActivity.REQUEST_MOVE_FILE:
                    setResult(RESULT_OK, data);
                    finish();
                    break;
                case UPDATE_VOLUME_NAME:
                    Volume volume = (Volume) data.getSerializableExtra("volume");
                    int index = shareVolumeList.indexOf(volume);
                    if (index != -1) {
                        shareVolumeList.set(index, volume);
                        adapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void sortShareVolumeList() {
        Collections.sort(shareVolumeList, new Comparator<Volume>() {
            @Override
            public int compare(Volume volume1, Volume volume2) {
                long creationDate1 = volume1.getCreationDate();
                long creationDate2 = volume2.getCreationDate();
                if (creationDate1 < creationDate2) {
                    return 1;
                }
                if (creationDate1 > creationDate2) {
                    return -1;
                }
                return 0;
            }
        });
    }

    @Override
    public void onRefresh() {
        getVolumeList(false, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getVolumeList(false, false);
    }

    /**
     * 获取云盘列表
     */
    private void getVolumeList(boolean isShowDlg, boolean isShowNetToast) {
        if (NetUtils.isNetworkConnected(getApplicationContext(), isShowNetToast)) {
            loadingDlg.show(isShowDlg);
            apiService.getVolumeList();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 创建共享网盘
     *
     * @param shareVolumeName
     */
    private void createShareVolume(String shareVolumeName) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.createShareVolume(BaseApplication.getInstance().getUid(), shareVolumeName);
        }
    }

    /**
     * 删除共享网盘
     *
     *
     */
    private void removeShareVolume() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            for (int i = 0; i < selectedShareVolumeList.size(); i++) {
                if (selectedShareVolumeList.get(i).getOwner().equals(BaseApplication.getInstance().getUid())) {
                    apiService.removeShareVolumeName(selectedShareVolumeList.get(i));
                }
            }
        }
    }

    /**
     * 修改网盘名称
     *
     * @param volume
     * @param name
     */
    private void updateShareVolumeName(Volume volume, String name) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.updateShareVolumeName(volume, name);
        }
    }

    class Holder {
        ImageView imageView;
        TextView textView;
        TextView volumeSizeView;
    }

    private class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return shareVolumeList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            Holder holder = new Holder();
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.volume_app_volume_share_item_view, null);
                holder.imageView = convertView.findViewById(R.id.file_operation_drop_down_img);
                holder.textView = convertView.findViewById(R.id.tv_name);
                holder.volumeSizeView = convertView.findViewById(R.id.tv_volume_size);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            if (selectedShareVolumeList.size() == 0) {
                holder.imageView.setImageResource(R.drawable.ic_no_selected);
            } else {
                boolean haveSelectedVolume = selectedShareVolumeList.contains(shareVolumeList.get(position));
                holder.imageView.setImageResource(haveSelectedVolume ? R.drawable.ic_select_yes : R.drawable.ic_select_no);
            }

            String volumeUsedSize = FileUtils.formatFileSize(shareVolumeList.get(position).getQuotaUsed());
            String volumeMaxSize = FileUtils.formatFileSize(shareVolumeList.get(position).getQuotaTotal());
            holder.volumeSizeView.setText(volumeUsedSize + " / " + volumeMaxSize);
            holder.textView.setText(shareVolumeList.get(position).getName());
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedShareVolumeList.contains(shareVolumeList.get(position))) {
                        selectedShareVolumeList.clear();
                    } else {
                        selectedShareVolumeList.clear();
                        selectedShareVolumeList.add(shareVolumeList.get(position));
                    }
                    setBottomOperationItemShow(selectedShareVolumeList);
                }
            });
            return convertView;
        }
    }

    private class WebService extends VolumeAPIInterfaceInstance {
        @Override
        public void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            swipeRefreshLayout.setRefreshing(false);
            shareVolumeList = getVolumeListResult.getShareVolumeList();
            sortShareVolumeList();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnVolumeListFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnCreateShareVolumeSuccess(Volume volume) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (createShareVolumeDlg != null && createShareVolumeDlg.isShowing()) {
                createShareVolumeDlg.dismiss();

            }
            shareVolumeList.add(0, volume);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnCreateShareVolumeFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnUpdateShareVolumeNameSuccess(Volume volume, String name) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            selectedShareVolumeList.clear();
            if (updateShareVolumeNameDlg != null && updateShareVolumeNameDlg.isShowing()) {
                updateShareVolumeNameDlg.dismiss();
            }
            int position = shareVolumeList.indexOf(volume);
            if (position != -1) {
                shareVolumeList.get(position).setName(name);
                sortShareVolumeList();
                adapter.notifyDataSetChanged();
            }
            setBottomOperationItemShow(selectedShareVolumeList);
        }

        @Override
        public void returnUpdateShareVolumeNameFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            setBottomOperationItemShow(selectedShareVolumeList);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnRemoveShareVolumeSuccess(Volume volume) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            shareVolumeList.remove(volume);
            selectedShareVolumeList.remove(volume);
            setBottomOperationItemShow(selectedShareVolumeList);
        }

        @Override
        public void returnRemoveShareVolumeFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            setBottomOperationItemShow(selectedShareVolumeList);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
