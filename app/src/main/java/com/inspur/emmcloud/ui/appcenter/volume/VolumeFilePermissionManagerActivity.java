package com.inspur.emmcloud.ui.appcenter.volume;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VolumeGroupPermissionManagerAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeGroupPermissionResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeResultWithPermissionResult;
import com.inspur.emmcloud.bean.appcenter.volume.Group;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.widget.ECMRecyclerViewLinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 *
 * 2018/03/01数据
 * {
    "id": "61786f76-4b15-4f2f-9bf1-f5df9121745f",
    "name": "拥有上传/查看权限",
    "enterprise": "10000",
    "creationDate": 1519696313994,
    "lastUpdate": 1519696313994,
    "volume": "1f401063-a36e-40ed-8186-5afeae716908",
    "owner": "99999",
    "privilege": 6,
    "type": "default",
    "members": ["12240", "233825", "66666", "99999"]
   }
 * Created by yufuchang on 2018/2/28.
 */
@ContentView(R.layout.activity_volume_permission)
public class VolumeFilePermissionManagerActivity extends BaseActivity{

    @ViewInject(R.id.volume_file_permission_rv)
    protected RecyclerView groupRecyclerView;

    private VolumeGroupPermissionManagerAdapter volumeGroupPermissionManagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        getVolumeFileGroup();
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        ECMRecyclerViewLinearLayoutManager layoutManager = new ECMRecyclerViewLinearLayoutManager(VolumeFilePermissionManagerActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setCanScrollHorizontally(false);
        groupRecyclerView.setLayoutManager(layoutManager);
        volumeGroupPermissionManagerAdapter = new VolumeGroupPermissionManagerAdapter(VolumeFilePermissionManagerActivity.this);
        volumeGroupPermissionManagerAdapter.setVolumeGroupPermissionManagerInterfaceListener(new VolumeGroupPermissionManagerAdapter.VolumeGroupPermissionManagerInterface() {
            @Override
            public void onVolumeGroupClickListener(Group group) {
                Intent intent = new Intent();
                intent.setClass(VolumeFilePermissionManagerActivity.this,VolumeGroupChangePermissionActivity.class);
                intent.putExtra("volumeGroup",group);
                intent.putExtra("volumePath",getIntent().getStringExtra("currentDirAbsolutePath"));
                startActivity(intent);
            }
        });
        groupRecyclerView.setAdapter(volumeGroupPermissionManagerAdapter);
        EventBus.getDefault().register(this);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateVolumeGroupPermission(GetVolumeGroupPermissionResult getVolumeGroupPermissionResult) {
        getVolumeFileGroup();
    }


    /**
     * 获取文件夹对应的群组及权限
     */
    private void getVolumeFileGroup(){
        if(NetUtils.isNetworkConnected(VolumeFilePermissionManagerActivity.this)){
            String volumeId = getIntent().getStringExtra("volume");
            String volumePath = getIntent().getStringExtra("currentDirAbsolutePath");
            MyAppAPIService myAppAPIService = new MyAppAPIService(VolumeFilePermissionManagerActivity.this);
            myAppAPIService.setAPIInterface(new WebService());
            myAppAPIService.getVolumeFileGroup(volumeId,volumePath);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnVolumeGroupSuccess(GetVolumeResultWithPermissionResult getVolumeResultWithPermissionResult) {
            super.returnVolumeGroupSuccess(getVolumeResultWithPermissionResult);
            volumeGroupPermissionManagerAdapter.setVolumeGroupPermissionList(getVolumeResultWithPermissionResult.getVolumeGroupList());
        }

        @Override
        public void returnVolumeGroupFail(String error, int errorCode) {
            super.returnVolumeGroupFail(error, errorCode);
        }
    }
}
