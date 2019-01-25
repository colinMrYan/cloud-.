package com.inspur.emmcloud.ui.mine.setting;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import org.xutils.view.annotation.ContentView;

/**
 * Created by chenmch on 2019/1/25.
 */

@ContentView(R.layout.activity_safe_guestur_face_setting)
public class SafeGustureFaceSettingActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_setting_safe_reset_guesture:
                break;
            case R.id.rl_setting_safe_reset_face:
                break;
        }
    }
}
