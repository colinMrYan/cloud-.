package com.inspur.emmcloud.application.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.inspur.emmcloud.application.R;

/**
 * Created by: yufuchang
 * Date: 2019/12/16
 */
public class ApplicationTestActivity extends AppCompatActivity {
    MyAppFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_test);
        fragment = new MyAppFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commitAllowingStateLoss();
    }
}
