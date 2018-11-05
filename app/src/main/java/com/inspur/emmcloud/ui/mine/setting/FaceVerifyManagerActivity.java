package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.SwitchView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * 刷脸认证管理
 */

@ContentView(R.layout.activity_gesture_manager)
public class FaceVerifyManagerActivity extends BaseActivity {

    @ViewInject(R.id.switch_gesture_switchview)
    private SwitchView switchView;

    @ViewInject(R.id.header_text)
    private TextView headerText;

    @ViewInject(R.id.tv_name_tips)
    private TextView switchText;

    @ViewInject(R.id.text)
    private TextView experienceText;

    @ViewInject(R.id.switch_gesture_change_code_layout)
    private RelativeLayout FaceExperienceLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FaceExperienceLayout.setVisibility(FaceVerifyActivity.getFaceVerifyIsOpenByUser(this)
                ? View.VISIBLE
                : View.GONE);
        switchView.setOpened(FaceVerifyActivity.getFaceVerifyIsOpenByUser(this));
    }


    private void init() {
        headerText.setText(R.string.face_enter);
        switchText.setText(R.string.face_login);
        experienceText.setText(getString(R.string.experience_face_verify));
        switchView.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
            @Override
            public void toggleToOn(View view) {
                Intent intent = new Intent();
                intent.putExtra("isFaceSettingOpen", true);
                intent.setClass(FaceVerifyManagerActivity.this, FaceVerifyActivity.class);
                startActivity(intent);
            }

            @Override
            public void toggleToOff(View view) {
                Intent intent = new Intent();
                intent.putExtra("isFaceSettingOpen", false);
                intent.setClass(FaceVerifyManagerActivity.this, FaceVerifyActivity.class);
                startActivity(intent);
            }
        });
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.switch_gesture_change_code_layout:
                Intent intent = new Intent();
                intent.putExtra("isFaceVerifyExperience", true);
                intent.setClass(FaceVerifyManagerActivity.this, FaceVerifyActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
