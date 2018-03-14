package com.inspur.emmcloud.ui.appcenter.volume;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeGroupPermissionResult;
import com.inspur.emmcloud.bean.appcenter.volume.Group;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SwitchView;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by yufuchang on 2018/3/8.
 */
@ContentView(R.layout.activity_volume_change_permission)
public class VolumeGroupChangePermissionActivity extends BaseActivity {

    @ViewInject(R.id.header_text)
    protected TextView headerText;

    @ViewInject(R.id.volume_read_write_permission_tv)
    protected TextView readAndWritePermissionText;

    @ViewInject(R.id.volume_read_permission_tv)
    protected TextView readPermissionText;

    @ViewInject(R.id.volume_read_write_permission_switch)
    protected SwitchView readAndWritePermissionSwitch;

    @ViewInject(R.id.volume_read_permission_switch)
    protected SwitchView readPermissionSwitch;

    private MyAppAPIService myAppAPIService;

    private LoadingDialog loadingDialog;

    private static final int VOLUME_READ_WRITE_PERMISSION = 6;
    private static final int VOLUME_READ_PERMISSION = 4;
    private static final int VOLUME_NO_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        myAppAPIService = new MyAppAPIService(VolumeGroupChangePermissionActivity.this);
        myAppAPIService.setAPIInterface(new WebService());
        loadingDialog = new LoadingDialog(VolumeGroupChangePermissionActivity.this);
        final Group group = (Group) getIntent().getSerializableExtra("volumeGroup");
        headerText.setText(group.getName());
        readAndWritePermissionText.setText(getString(R.string.volume_read_write_permission));
        readPermissionText.setText(getString(R.string.volume_read_permission));
        readAndWritePermissionSwitch.setOpened(group.getPrivilege() > VOLUME_READ_PERMISSION ? true : false);
        readPermissionSwitch.setOpened(true);
        readPermissionSwitch.setEnable(group.getPrivilege() > VOLUME_READ_PERMISSION ? false : true);
        readPermissionSwitch.setPaintColorOn(group.getPrivilege() > 4?0x667fc5f6:0xff7fc5f6);
        readPermissionSwitch.setPaintCircleBtnColor(group.getPrivilege() > 4?0xbb7fc5f6:0xff008cee);
        final String currentVolumePath = getIntent().getStringExtra("volumePath");
        readAndWritePermissionSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                updateVolumeGroupPermission(group,currentVolumePath,VOLUME_READ_WRITE_PERMISSION,true);
            }

            @Override
            public void toggleToOff(View view) {
                updateVolumeGroupPermission(group,currentVolumePath,VOLUME_READ_PERMISSION,true);
            }
        });
        readPermissionSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                updateVolumeGroupPermission(group,currentVolumePath,VOLUME_READ_PERMISSION,true);
            }

            @Override
            public void toggleToOff(View view) {
                updateVolumeGroupPermission(group,currentVolumePath,VOLUME_NO_PERMISSION,true);
            }
        });
    }

    /**
     * 更改文件夹组权限
     * @param group
     * @param currentVolumePath
     * @param i
     * @param b
     */
    private void updateVolumeGroupPermission(Group group, String currentVolumePath, int i, boolean b) {
        if(NetUtils.isNetworkConnected(VolumeGroupChangePermissionActivity.this)){
            loadingDialog.show();
            myAppAPIService.updateVolumeFileGroupPermission(group.getVolume(),currentVolumePath,group.getId(),i,b);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                finish();
                break;
        }
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnUpdateVolumeGroupPermissionSuccess(GetVolumeGroupPermissionResult getVolumeGroupPermissionResult) {
            if(loadingDialog != null){
                loadingDialog.dismiss();
            }
            if(getVolumeGroupPermissionResult.getPrivilege() >= VOLUME_READ_WRITE_PERMISSION){
                readAndWritePermissionSwitch.toggleSwitch(true);
                readPermissionSwitch.setIsCodeManual(true,true);
                readPermissionSwitch.setOpened(true);
                readPermissionSwitch.setEnable(false);
                readPermissionSwitch.setPaintColorOn(0x667fc5f6);
                readPermissionSwitch.setPaintCircleBtnColor(0xbb7fc5f6);
            }else {
                readAndWritePermissionSwitch.toggleSwitch(false);
                readPermissionSwitch.setEnable(true);
                if(getVolumeGroupPermissionResult.getPrivilege() == VOLUME_NO_PERMISSION){
                    readPermissionSwitch.toggleSwitch(false);
                }else if(getVolumeGroupPermissionResult.getPrivilege() == VOLUME_READ_PERMISSION){
                    readPermissionSwitch.setIsCodeManual(true,true);
                }
                readPermissionSwitch.setPaintColorOn(0xff7fc5f6);
                readPermissionSwitch.setPaintCircleBtnColor(0xff008cee);
            }
            EventBus.getDefault().post(getVolumeGroupPermissionResult);
        }

        @Override
        public void returnUpdateVolumeGroupPermissionFail(String error, int errorCode) {
            if(loadingDialog != null){
                loadingDialog.dismiss();
            }
        }
    }
}
