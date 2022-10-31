package com.inspur.emmcloud.setting.ui.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.NotificationSetUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationSettingActivity extends BaseActivity {
    @BindView(R2.id.switch_setting_notification)
    SwitchCompat notificationSwitch;
    private LoadingDialog loadingDlg;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationSwitch.setChecked(getSwitchOpen());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_mine_setting_notification;
    }

    private void initView() {
        if (loadingDlg == null) loadingDlg = new LoadingDialog(this);
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {    //代表上升沿 先检测
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        showNotificationDlg(isChecked);
                    } else {
                        PreferencesByUserAndTanentUtils.putBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, true);
                        switchPush();
                        notificationSwitch.setChecked(true);
                    }
                } else {
                    if (NotificationSetUtils.isNotificationEnabled(NotificationSettingActivity.this)) {
                        showNotificationCloseDlg();
                    } else {
                        PreferencesByUserAndTanentUtils.putBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, false);
                        notificationSwitch.setChecked(false);
                    }
                }
            }
        });
    }

    /**
     * 弹出注销提示框
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showNotificationDlg(boolean isChecked) {
        if (!NotificationSetUtils.isNotificationEnabled(NotificationSettingActivity.this)) {
            PreferencesByUserAndTanentUtils.putBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, false);
            notificationSwitch.setChecked(false);
            new CustomDialog.MessageDialogBuilder(NotificationSettingActivity.this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.notification_switch_open_setting))
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            NotificationSetUtils.openNotificationSetting(NotificationSettingActivity.this);
                        }
                    })
                    .show();
        } else {
            PreferencesByUserAndTanentUtils.putBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, isChecked);
            PushManagerUtils.getInstance().startPush();
            notificationSwitch.setChecked(isChecked);
        }
    }

    /**
     * 开关push并向服务器发出信号
     */
    private void switchPush() {
        boolean switchFlag = PreferencesByUserAndTanentUtils.getBoolean(this,
                Constant.PUSH_SWITCH_FLAG, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setPushStatus(NotificationSetUtils.isNotificationEnabled(this) && switchFlag);
        } else {
            setPushStatus(switchFlag);
        }
    }

    private void setPushStatus(boolean openPush) {
        if (openPush) {
            PushManagerUtils.getInstance().startPush();
            PushManagerUtils.getInstance().registerPushId2Emm();
        } else {
            PushManagerUtils.getInstance().stopPush();
            PushManagerUtils.getInstance().unregisterPushId2Emm();
        }
    }

    private void showNotificationCloseDlg() {
        new CustomDialog.MessageDialogBuilder(NotificationSettingActivity.this)
                .setMessage(R.string.notification_switch_cant_recive)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notificationSwitch.setChecked(true);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        notificationSwitch.setChecked(false);
                        PreferencesByUserAndTanentUtils.putBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, false);
                        switchPush();
                    }
                })
                .show();
    }

    private boolean getSwitchOpen() {
        boolean isOpen = PreferencesByUserAndTanentUtils.getBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !NotificationSetUtils.isNotificationEnabled(this)) {
            isOpen = false;
        }
        return isOpen;
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();
        }
        // 通知指南
        if (i == R.id.notification_system_layout) {
            IntentUtils.startActivity(NotificationSettingActivity.this, NotificationSystemSettingGuideActivity.class);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
