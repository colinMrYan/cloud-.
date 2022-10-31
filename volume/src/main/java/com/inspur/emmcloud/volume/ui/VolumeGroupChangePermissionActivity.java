package com.inspur.emmcloud.volume.ui;

import android.content.res.ColorStateList;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.volume.R;
import com.inspur.emmcloud.volume.R2;
import com.inspur.emmcloud.volume.api.VolumeAPIInterfaceInstance;
import com.inspur.emmcloud.volume.api.VolumeAPIService;
import com.inspur.emmcloud.volume.bean.GetVolumeGroupPermissionResult;
import com.inspur.emmcloud.volume.bean.Group;

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
    @BindView(R2.id.header_text)
    TextView headerText;
    @BindView(R2.id.tv_volume_write_permission)
    TextView writePermissionText;
    @BindView(R2.id.tv_volume_read_permission)
    TextView readPermissionText;
    @BindView(R2.id.swv_volume_write_permission)
    SwitchCompat writePermissionSwitch;
    @BindView(R2.id.swv_volume_read_permission)
    SwitchCompat readPermissionSwitch;
    private VolumeAPIService myAppAPIService;
    private LoadingDialog loadingDialog;
    private boolean isShouldChangePermission = true;

    public void setSwitchColor(int thumbColor, int trackColor, SwitchCompat v) {
        // thumb color
        // int thumbColor = 0x1A666666;

        // trackColor
        // int trackColor = 0x7E000000;

        // set the thumb color
        DrawableCompat.setTintList(v.getThumbDrawable(), new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        thumbColor,
                        trackColor
                }));

        // set the track color
        DrawableCompat.setTintList(v.getTrackDrawable(), new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        0x7E000000,
                        0x1A666666
                }));
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.volume_activity_volume_group_change_permission;
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        myAppAPIService = new VolumeAPIService(VolumeGroupChangePermissionActivity.this);
        myAppAPIService.setAPIInterface(new WebService());
        loadingDialog = new LoadingDialog(VolumeGroupChangePermissionActivity.this);
        final Group group = (Group) getIntent().getSerializableExtra("volumeGroup");
        headerText.setText(group.getName());
        writePermissionText.setText(getString(R.string.volume_read_write_permission));
        readPermissionText.setText(getString(R.string.volume_read_permission));
        writePermissionSwitch.setChecked(group.getPrivilege() > VOLUME_READ_PERMISSION);
        readPermissionSwitch.setChecked(group.getPrivilege() > 0);
        readPermissionSwitch.setEnabled(group.getPrivilege() <= VOLUME_READ_PERMISSION);
//        setSwitchColor(group.getPrivilege() > 4 ? 0xbb7fc5f6 : 0xff008cee,
//                group.getPrivilege() > 4 ? 0x667fc5f6 : 0xff7fc5f6, readPermissionSwitch);
        final String currentVolumePath = getIntent().getStringExtra("volumeFilePath");
        writePermissionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateVolumeGroupPermission(group, currentVolumePath, b ? VOLUME_WRITE_PERMISSION : VOLUME_READ_PERMISSION, true);
            }
        });
        readPermissionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isShouldChangePermission) {
                    updateVolumeGroupPermission(group, currentVolumePath, b ? VOLUME_READ_PERMISSION : VOLUME_NO_PERMISSION, true);
                }
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

    public void onClick(View v) {
        if (v.getId() == R.id.ibt_back) {
            finish();
        }
    }

    class WebService extends VolumeAPIInterfaceInstance {
        @Override
        public void returnUpdateVolumeGroupPermissionSuccess(GetVolumeGroupPermissionResult getVolumeGroupPermissionResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            if (getVolumeGroupPermissionResult.getPrivilege() >= VOLUME_WRITE_PERMISSION) {
                isShouldChangePermission = false;
                writePermissionSwitch.setChecked(true);
                // readPermissionSwitch.setIsCodeManual(true, true);
                readPermissionSwitch.setChecked(true);
                readPermissionSwitch.setEnabled(false);
                //readPermissionSwitch.setPaintColorOn(0x667fc5f6);
                //readPermissionSwitch.setPaintCircleBtnColor(0xbb7fc5f6);
            } else {
                isShouldChangePermission = true;
                writePermissionSwitch.setChecked(false);
                readPermissionSwitch.setEnabled(true);
                if (getVolumeGroupPermissionResult.getPrivilege() == VOLUME_NO_PERMISSION) {
                    readPermissionSwitch.setChecked(false);
                } else if (getVolumeGroupPermissionResult.getPrivilege() == VOLUME_READ_PERMISSION) {
                    readPermissionSwitch.setChecked(true);
                    //readPermissionSwitch.setIsCodeManual(true, true);
                }
                // readPermissionSwitch.setPaintColorOn(0xff7fc5f6);
                // readPermissionSwitch.setPaintCircleBtnColor(0xff008cee);
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
