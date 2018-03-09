package com.inspur.emmcloud.ui.appcenter.volume;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.volume.Group;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.widget.SwitchView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        Group group = (Group) getIntent().getSerializableExtra("volumeGroup");
        headerText.setText(group.getName());
        readAndWritePermissionText.setText(getString(R.string.volume_read_write_permission));
        readPermissionText.setText(getString(R.string.volume_read_permission));
        readAndWritePermissionSwitch.setOpened(group.getPrivilege() > 4 ? true : false);
        readPermissionSwitch.setOpened(true);
        readPermissionSwitch.setEnable(group.getPrivilege() > 4 ? false : true);
        readPermissionSwitch.setPaintColorOn(group.getPrivilege() > 4?0x667fc5f6:0xff7fc5f6);
        readPermissionSwitch.setPaintCircleBtnColor(group.getPrivilege() > 4?0xbb7fc5f6:0xff008cee);
        readAndWritePermissionSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                LogUtils.YfcDebug("-------------------------");
                readAndWritePermissionSwitch.toggleSwitch(true);
                readPermissionSwitch.setEnable(false);
                readPermissionSwitch.setPaintColorOn(0x667fc5f6);
                readPermissionSwitch.setPaintCircleBtnColor(0xbb7fc5f6);
            }

            @Override
            public void toggleToOff(View view) {
                readAndWritePermissionSwitch.toggleSwitch(false);
                readPermissionSwitch.setEnable(true);
                readPermissionSwitch.setPaintColorOn(0xff7fc5f6);
                readPermissionSwitch.setPaintCircleBtnColor(0xff008cee);
            }
        });

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                finish();
                break;
        }
    }
}
