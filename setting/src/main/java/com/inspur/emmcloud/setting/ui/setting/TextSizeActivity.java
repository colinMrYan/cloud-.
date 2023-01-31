package com.inspur.emmcloud.setting.ui.setting;

import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.widget.SwitchCompat;

import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextSizeActivity extends BaseActivity {

    @BindView(R2.id.switch_view_setting_web_scale)
    SwitchCompat webScaleSwitch;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_activity_font;
    }

    private void initView() {
        boolean isWebFollySysScale = PreferencesUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_APP_OPEN_WEB_SCALE_SWITCH, false);
        webScaleSwitch.setChecked(isWebFollySysScale);
        webScaleSwitch.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferencesUtils.putBoolean(BaseApplication.getInstance(), Constant.PREF_APP_OPEN_WEB_SCALE_SWITCH, isChecked);
            }
        });
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.font_size_set_layout) {
            IntentUtils.startActivity(TextSizeActivity.this, TextSizeSettingActivity.class);
        } else if (i == R.id.ibt_back) {
            finish();
        }
    }

}
