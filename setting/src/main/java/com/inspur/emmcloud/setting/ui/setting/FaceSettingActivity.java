package com.inspur.emmcloud.setting.ui.setting;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FaceSettingActivity extends BaseActivity {

    @BindView(R.id.switch_view_setting_safe_start_guesture)
    SwitchCompat openFaceVertifyBtn;
    @BindView(R.id.rl_setting_safe_reset_face)
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
        return R.layout.activity_face_setting;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_setting_safe_reset_face:
                intentFaceVerifyActivity(true);
                break;
            default:
                break;
        }
    }
}
