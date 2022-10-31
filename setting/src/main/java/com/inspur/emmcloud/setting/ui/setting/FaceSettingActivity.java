package com.inspur.emmcloud.setting.ui.setting;

import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FaceSettingActivity extends BaseActivity {

    @BindView(R2.id.switch_view_setting_safe_start_guesture)
    SwitchCompat openFaceVertifyBtn;
    @BindView(R2.id.rl_setting_safe_reset_face)
    RelativeLayout faceRelativeLayout;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openFaceVertifyBtn.setChecked(FaceVerifyActivity.getFaceVerifyIsOpenByUser(BaseApplication.getInstance()));
        faceRelativeLayout.setVisibility(FaceVerifyActivity.getFaceVerifyIsOpenByUser(BaseApplication.getInstance()) ? View.VISIBLE : View.GONE);
    }

    private void initViews() {
        openFaceVertifyBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                if (FaceVerifyActivity.getFaceVerifyIsOpenByUser(getApplication()) != isChecked) {
                    intentFaceVerifyActivity(isChecked);
                }
            }
        });
    }


    /**
     * 打开设置人脸识别页面
     *
     * @param isFaceSettingOpen
     */
    private void intentFaceVerifyActivity(boolean isFaceSettingOpen) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFaceSettingOpen", isFaceSettingOpen);
        IntentUtils.startActivity(FaceSettingActivity.this, FaceVerifyActivity.class, bundle);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_face_setting_activity;
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.rl_setting_safe_reset_face) {
            intentFaceVerifyActivity(true);
        }
    }
}
