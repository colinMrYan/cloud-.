package com.inspur.emmcloud.ui.appcenter.volume;

import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeGroupPermissionResult;
import com.inspur.emmcloud.bean.appcenter.volume.Group;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SwitchView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/3/8.
 */
public class VolumeGroupChangePermissionActivity extends BaseActivity {

    private static final int VOLUME_WRITE_PERMISSION = 6;
    private static final int VOLUME_READ_PERMISSION = 4;
    private static final int VOLUME_NO_PERMISSION = 0;
    @BindView(R.id.header_text)
    TextView headerText;
    @BindView(R.id.tv_volume_write_permission)
    TextView writePermissionText;
    @BindView(R.id.tv_volume_read_permission)
    TextView readPermissionText;
    @BindView(R.id.swv_volume_write_permission)
    SwitchView writePermissionSwitch;
    @BindView(R.id.swv_volume_read_permission)
    SwitchView readPermissionSwitch;
    private MyAppAPIService myAppAPIService;
    private LoadingDialog loadingDialog;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_volume_group_change_permission;
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
        writePermissionText.setText(getString(R.string.volume_read_write_permission));
        readPermissionText.setText(getString(R.string.volume_read_permission));
        writePermissionSwitch.setOpened(group.getPrivilege() > VOLUME_READ_PERMISSION);
        readPermissionSwitch.setOpened(true);
        readPermissionSwitch.setEnable(group.getPrivilege() <= VOLUME_READ_PERMISSION);
        readPermissionSwitch.setPaintColorOn(group.getPrivilege() > 4 ? 0x667fc5f6 : 0xff7fc5f6);
        readPermissionSwitch.setPaintCircleBtnColor(group.getPrivilege() > 4 ? 0xbb7fc5f6 : 0xff008cee);
        final String currentVolumePath = getIntent().getStringExtra("volumeFilePath");
        writePermissionSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                updateVolumeGroupPermission(group, currentVolumePath, VOLUME_WRITE_PERMISSION, true);
            }

            @Override
            public void toggleToOff(View view) {
                updateVolumeGroupPermission(group, currentVolumePath, VOLUME_READ_PERMISSION, true);
            }
        });
        readPermissionSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                updateVolumeGroupPermission(group, currentVolumePath, VOLUME_READ_PERMISSION, true);
            }

            @Override
            public void toggleToOff(View view) {
                updateVolumeGroupPermission(group, currentVolumePath, VOLUME_NO_PERMISSION, true);
            }
        });
    }

    /**
     * 更改文件夹组权限
     *
     * @param group
     * @param currentVolumePath
     * @param i
     * @param b
     */
    private void updateVolumeGroupPermission(Group group, String currentVolumePath, int i, boolean b) {
        if (NetUtils.isNetworkConnected(VolumeGroupChangePermissionActivity.this)) {
            loadingDialog.show();
            myAppAPIService.updateVolumeFileGroupPermission(group.getVolume(), currentVolumePath, group.getId(), i, b);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnUpdateVolumeGroupPermissionSuccess(GetVolumeGroupPermissionResult getVolumeGroupPermissionResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            if (getVolumeGroupPermissionResult.getPrivilege() >= VOLUME_WRITE_PERMISSION) {
                writePermissionSwitch.toggleSwitch(true);
                readPermissionSwitch.setIsCodeManual(true, true);
                readPermissionSwitch.setOpened(true);
                readPermissionSwitch.setEnable(false);
                readPermissionSwitch.setPaintColorOn(0x667fc5f6);
                readPermissionSwitch.setPaintCircleBtnColor(0xbb7fc5f6);
            } else {
                writePermissionSwitch.toggleSwitch(false);
                readPermissionSwitch.setEnable(true);
                if (getVolumeGroupPermissionResult.getPrivilege() == VOLUME_NO_PERMISSION) {
                    readPermissionSwitch.toggleSwitch(false);
                } else if (getVolumeGroupPermissionResult.getPrivilege() == VOLUME_READ_PERMISSION) {
                    readPermissionSwitch.toggleSwitch(true);
                    readPermissionSwitch.setIsCodeManual(true, true);
                }
                readPermissionSwitch.setPaintColorOn(0xff7fc5f6);
                readPermissionSwitch.setPaintCircleBtnColor(0xff008cee);
            }
            //发送到VolumeFilePermissionManagerActivity
            EventBus.getDefault().post(getVolumeGroupPermissionResult);
        }

        @Override
        public void returnUpdateVolumeGroupPermissionFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
