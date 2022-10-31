package com.inspur.emmcloud.volume.ui;

import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.volume.R;
import com.inspur.emmcloud.volume.R2;
import com.inspur.emmcloud.volume.adapter.VolumeGroupPermissionManagerAdapter;
import com.inspur.emmcloud.volume.api.VolumeAPIInterfaceInstance;
import com.inspur.emmcloud.volume.api.VolumeAPIService;
import com.inspur.emmcloud.volume.bean.GetVolumeGroupPermissionResult;
import com.inspur.emmcloud.volume.bean.GetVolumeResultWithPermissionResult;
import com.inspur.emmcloud.volume.bean.Group;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 2018/03/01数据
 * {
 * "id": "61786f76-4b15-4f2f-9bf1-f5df9121745f",
 * "name": "拥有上传/查看权限",
 * "enterprise": "10000",
 * "creationDate": 1519696313994,
 * "lastUpdate": 1519696313994,
 * "volume": "1f401063-a36e-40ed-8186-5afeae716908",
 * "owner": "99999",
 * "privilege": 6,
 * "type": "default",
 * "members": ["12240", "233825", "66666", "99999"]
 * }
 * Created by yufuchang on 2018/2/28.
 */
public class VolumeFilePermissionManagerActivity extends BaseActivity {

    @BindView(R2.id.rv_volume_file_permission)
    RecyclerView groupRecyclerView;

    private VolumeGroupPermissionManagerAdapter volumeGroupPermissionManagerAdapter;

    private LoadingDialog loadingDialog;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
        getVolumeFileGroup();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.volume_activity_volume_file_permission_manager;
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(VolumeFilePermissionManagerActivity.this));
        volumeGroupPermissionManagerAdapter = new VolumeGroupPermissionManagerAdapter(VolumeFilePermissionManagerActivity.this);
        volumeGroupPermissionManagerAdapter.setVolumeGroupPermissionManagerInterfaceListener(new VolumeGroupPermissionManagerAdapter.VolumeGroupPermissionManagerInterface() {
            @Override
            public void onVolumeGroupClickListener(Group group) {
                Intent intent = new Intent();
                intent.setClass(VolumeFilePermissionManagerActivity.this, VolumeGroupChangePermissionActivity.class);
                intent.putExtra("volumeGroup", group);
                intent.putExtra("volumeFilePath", getIntent().getStringExtra("currentDirAbsolutePath"));
                startActivity(intent);
            }
        });
        groupRecyclerView.setAdapter(volumeGroupPermissionManagerAdapter);
        EventBus.getDefault().register(this);
        loadingDialog = new LoadingDialog(this);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.ibt_back) {
            finish();
        }
    }

    /**
     * 来自VolumeGroupChangePermissionActivity
     *
     * @param getVolumeGroupPermissionResult
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateVolumeGroupPermission(GetVolumeGroupPermissionResult getVolumeGroupPermissionResult) {
        getVolumeFileGroup();
    }


    /**
     * 获取文件夹对应的群组及权限
     */
    private void getVolumeFileGroup() {
        if (NetUtils.isNetworkConnected(VolumeFilePermissionManagerActivity.this)) {
            loadingDialog.show();
            String volumeId = getIntent().getStringExtra("volume");
            String volumeFilePath = getIntent().getStringExtra("currentDirAbsolutePath");
            VolumeAPIService myAppAPIService = new VolumeAPIService(VolumeFilePermissionManagerActivity.this);
            myAppAPIService.setAPIInterface(new WebService());
            myAppAPIService.getVolumeFileGroup(volumeId, volumeFilePath);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    class WebService extends VolumeAPIInterfaceInstance {
        @Override
        public void returnVolumeGroupSuccess(GetVolumeResultWithPermissionResult getVolumeResultWithPermissionResult) {
            super.returnVolumeGroupSuccess(getVolumeResultWithPermissionResult);
            LoadingDialog.dimissDlg(loadingDialog);
            volumeGroupPermissionManagerAdapter.setVolumeGroupPermissionList(getVolumeResultWithPermissionResult.getVolumeGroupList());
        }

        @Override
        public void returnVolumeGroupFail(String error, int errorCode) {
            super.returnVolumeGroupFail(error, errorCode);
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
