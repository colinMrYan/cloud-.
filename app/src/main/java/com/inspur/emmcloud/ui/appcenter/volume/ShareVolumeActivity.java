package com.inspur.emmcloud.ui.appcenter.volume;

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
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;

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
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.share_volume_list)
    ListView shareVolumeListView;
    private List<Volume> shareVolumeList = new ArrayList<>();
    private Adapter adapter;
    private MyAppAPIService apiService;
    private LoadingDialog loadingDlg;
    private MyDialog createShareVolumeDlg, updateShareVolumeNameDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
        getVolumeList(true);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_share_volume;
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        adapter = new Adapter();
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.header_bg_blue), ContextCompat.getColor(getApplicationContext(), R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(this);
        shareVolumeListView.setAdapter(adapter);
        shareVolumeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<Uri> shareUriList = null;
                if (getIntent() != null && getIntent().hasExtra(Constant.SHARE_FILE_URI_LIST)) {
                    shareUriList = (List<Uri>) getIntent().getSerializableExtra(Constant.SHARE_FILE_URI_LIST);
                }
                Volume volume = shareVolumeList.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("volume", volume);
                bundle.putString("title", volume.getName());
                if (shareUriList != null && shareUriList.size() > 0) {
                    bundle.putSerializable("fileShareUriList", (Serializable) shareUriList);
                    bundle.putString("operationFileDirAbsolutePath", "/");
                    IntentUtils.startActivity(ShareVolumeActivity.this, VolumeFileLocationSelectActivity.class, bundle);
                } else {
                    IntentUtils.startActivity(ShareVolumeActivity.this, VolumeFileActivity.class, bundle);
                }
            }
        });
        shareVolumeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Volume volume = shareVolumeList.get(position);
                showVolumeOperationDlg(volume);
                return true;
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.add_img:
                showCreateShareVolumeDlg();
                break;
            default:
                break;
        }
    }


    /**
     * 弹出新建文件夹提示框
     */
    protected void showCreateShareVolumeDlg() {
        createShareVolumeDlg = new MyDialog(ShareVolumeActivity.this,
                R.layout.volume_dialog_update_name_input);
        createShareVolumeDlg.setCancelable(false);
        final EditText inputEdit = (EditText) createShareVolumeDlg.findViewById(R.id.edit);
        inputEdit.setHint(R.string.clouddriver_input_volume_name);
        inputEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MyAppConfig.VOLUME_MAX_FILE_NAME_LENGTH)});
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView) createShareVolumeDlg.findViewById(R.id.app_update_title)).setText(R.string.clouddriver_create_volume);
        Button okBtn = (Button) createShareVolumeDlg.findViewById(R.id.ok_btn);
        okBtn.setText(R.string.create);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareVolumeName = inputEdit.getText().toString();
                if (StringUtils.isBlank(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), R.string.clouddriver_input_volume_name);
                } else if (!FomatUtils.isValidFileName(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), R.string.clouddriver_volume_name_invaliad);
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
     * 弹出网盘操作框
     *
     * @param volume
     */
    private void showVolumeOperationDlg(final Volume volume) {
        boolean isOwner = volume.isOwner();
        new ActionSheetDialog.ActionListSheetBuilder(ShareVolumeActivity.this)
                .setTitle(volume.getName())
                .addItem(getString(R.string.delete), isOwner)
                .addItem(getString(R.string.clouddriver_volume_info))
                .addItem(getString(R.string.rename), isOwner)
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        String action = (String) itemView.getTag();
                        if (action.equals(getString(R.string.delete))) {
                            showVolumeDelWranibgDlg(volume);
                        } else if (action.equals(getString(R.string.clouddriver_volume_info))) {
                            Intent intent = new Intent(ShareVolumeActivity.this, ShareVolumeInfoActivity.class);
                            intent.putExtra("volume", volume);
                            startActivityForResult(intent, UPDATE_VOLUME_NAME);
                        } else if (action.equals(getString(R.string.rename))) {
                            showUpdateShareVolumeNameDlg(volume);
                        }
                        dialog.dismiss();
                    }
                }).build()
                .show();
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
     * @param volume
     */
    protected void showVolumeDelWranibgDlg(final Volume volume) {
        new CustomDialog.MessageDialogBuilder(ShareVolumeActivity.this)
                .setMessage(R.string.clouddriver_sure_delete_volume)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeShareVolume(volume);
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
        final EditText inputEdit = (EditText) updateShareVolumeNameDlg.findViewById(R.id.edit);
        inputEdit.setText(volume.getName());
        inputEdit.setSelectAllOnFocus(true);
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        inputEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MyAppConfig.VOLUME_MAX_FILE_NAME_LENGTH)});
        ((TextView) updateShareVolumeNameDlg.findViewById(R.id.app_update_title)).setText(R.string.rename);
        Button okBtn = (Button) updateShareVolumeNameDlg.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareVolumeName = inputEdit.getText().toString();
                if (StringUtils.isBlank(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), R.string.clouddriver_input_volume_name);
                } else if (!FomatUtils.isValidFileName(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), R.string.clouddriver_volume_name_invaliad);
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
                updateShareVolumeNameDlg.dismiss();
            }
        });
        updateShareVolumeNameDlg.show();
        InputMethodUtils.display(ShareVolumeActivity.this, inputEdit);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UPDATE_VOLUME_NAME) {
            Volume volume = (Volume) data.getSerializableExtra("volume");
            int index = shareVolumeList.indexOf(volume);
            if (index != -1) {
                shareVolumeList.set(index, volume);
                adapter.notifyDataSetChanged();
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
        getVolumeList(false);
    }

    /**
     * 获取云盘列表
     */
    private void getVolumeList(boolean isShowDlg) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
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
            apiService.createShareVolume(MyApplication.getInstance().getUid(), shareVolumeName);
        }
    }

    /**
     * 删除共享网盘
     *
     * @param volume
     */
    private void removeShareVolume(Volume volume) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.removeShareVolumeName(volume);
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
            convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.app_volume_share_item_view, null);
            ((TextView) convertView.findViewById(R.id.tv_name)).setText(shareVolumeList.get(position).getName());
            (convertView.findViewById(R.id.file_operation_drop_down_img)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showVolumeOperationDlg(shareVolumeList.get(position));
                }
            });
            return convertView;
        }
    }

    private class WebService extends APIInterfaceInstance {
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

            if (updateShareVolumeNameDlg != null && updateShareVolumeNameDlg.isShowing()) {
                updateShareVolumeNameDlg.dismiss();
            }
            int position = shareVolumeList.indexOf(volume);
            if (position != -1) {
                shareVolumeList.get(position).setName(name);
                sortShareVolumeList();
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void returnUpdateShareVolumeNameFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }

        @Override
        public void returnRemoveShareVolumeSuccess(Volume volume) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            shareVolumeList.remove(volume);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnRemoveShareVolumeFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
