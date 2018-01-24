package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * 云盘-共享网盘列表页面
 */

@ContentView(R.layout.activity_share_volume)
public class ShareVolumeActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int UPDATE_VOLUME_NAME = 1;
    private List<Volume> shareVolumeList = new ArrayList<>();

    @ViewInject(R.id.share_volume_list)
    private ListView shareVolumeListView;

    @ViewInject(R.id.refresh_layout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    private Adapter adapter;
    private MyAppAPIService apiService;
    private LoadingDialog loadingDlg;
    private MyDialog createShareVolumeDlg,updateShareVolumeNameDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shareVolumeList = (List<Volume>) getIntent().getExtras().getSerializable("shareVolumeList");
        initView();
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        apiService = new MyAppAPIService(this);
        apiService.setAPIInterface(new WebService());
        adapter = new Adapter();
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.header_bg), ContextCompat.getColor(getApplicationContext(), R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(this);
        shareVolumeListView.setAdapter(adapter);
        shareVolumeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Volume volume = shareVolumeList.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("volume", volume);
                bundle.putSerializable("title", volume.getName());
                IntentUtils.startActivity(ShareVolumeActivity.this, VolumeFileActivity.class, bundle);
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
            case R.id.back_layout:
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
                R.layout.dialog_my_app_approval_password_input, R.style.userhead_dialog_bg);
        createShareVolumeDlg.setCancelable(false);
        final EditText inputEdit = (EditText) createShareVolumeDlg.findViewById(R.id.edit);
        inputEdit.setHint("请输入网盘名称");
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView) createShareVolumeDlg.findViewById(R.id.app_update_title)).setText("新建网盘");
        Button okBtn = (Button) createShareVolumeDlg.findViewById(R.id.ok_btn);
        okBtn.setText("新建");
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareVolumeName = inputEdit.getText().toString();
                if (StringUtils.isBlank(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), "请输入网盘名称");
                } else if (!FomatUtils.isValidFileName(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), "网盘名中不能包含特殊字符 / \\ \" : | * ? < >");
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
                .addItem("删除", isOwner)
                .addItem("网盘详情")
                .addItem("重命名", isOwner)
                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(ActionSheetDialog dialog, View itemView, int position) {
                        switch (position) {
                            case 0:
                                removeShareVolume(volume);
                                break;
                            case 1:
                                Intent intent = new Intent(ShareVolumeActivity.this, ShareVolumeInfoActivity.class);
                                intent.putExtra("volume", volume);
                                startActivityForResult(intent,UPDATE_VOLUME_NAME);
                                break;
                            case 2:
                                showUpdateShareVolumeNameDlg(volume);
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                }).build()
                .show();
    }

    /**
     * 弹出更改共享网盘名称弹出框
     *
     * @param volume
     */
    private void showUpdateShareVolumeNameDlg(final Volume volume) {
        updateShareVolumeNameDlg = new MyDialog(ShareVolumeActivity.this,
                R.layout.dialog_my_app_approval_password_input, R.style.userhead_dialog_bg);
        updateShareVolumeNameDlg.setCancelable(false);
        final EditText inputEdit = (EditText) updateShareVolumeNameDlg.findViewById(R.id.edit);
        inputEdit.setText(volume.getName());
        inputEdit.setSelectAllOnFocus(true);
        inputEdit.setInputType(InputType.TYPE_CLASS_TEXT);
        ((TextView) updateShareVolumeNameDlg.findViewById(R.id.app_update_title)).setText("新建网盘");
        Button okBtn = (Button) updateShareVolumeNameDlg.findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareVolumeName = inputEdit.getText().toString();
                if (StringUtils.isBlank(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), "请输入网盘名称");
                } else if (!FomatUtils.isValidFileName(shareVolumeName)) {
                    ToastUtils.show(getApplicationContext(), "网盘名中不能包含特殊字符 / \\ \" : | * ? < >");
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
            ((TextView) convertView.findViewById(R.id.name_text)).setText(shareVolumeList.get(position).getName());
            (convertView.findViewById(R.id.file_operation_drop_down_img)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showVolumeOperationDlg(shareVolumeList.get(position));
                }
            });
            return convertView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.jasonDebug("resultCode="+resultCode);
        LogUtils.jasonDebug("requestCode="+requestCode);
        if (resultCode == RESULT_OK && requestCode == UPDATE_VOLUME_NAME){
            Volume volume = (Volume) data.getSerializableExtra("volume");
            LogUtils.jasonDebug("volumeMafdfdf="+volume.getName());
            int index= shareVolumeList.indexOf(volume);
            if (index !=-1){
                shareVolumeList.set(index,volume);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRefresh() {
        getVolumeList();
    }

    /**
     * 获取云盘列表
     */
    private void getVolumeList() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
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
     * @param volume
     * @param name
     */
    private void updateShareVolumeName(Volume volume, String name) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.updateShareVolumeName(volume, name);
        }
    }


    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult) {
            swipeRefreshLayout.setRefreshing(false);
            shareVolumeList = getVolumeListResult.getShareVolumeList();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnVolumeListFail(String error, int errorCode) {
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
            shareVolumeList.add(volume);
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

            if (updateShareVolumeNameDlg != null && updateShareVolumeNameDlg.isShowing()){
                updateShareVolumeNameDlg.dismiss();
            }
            int position = shareVolumeList.indexOf(volume);
            if (position != -1) {
                shareVolumeList.get(position).setName(name);
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
        public void retrunRemoveShareVolumeSuccess(Volume volume) {
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
