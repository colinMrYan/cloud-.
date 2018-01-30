package com.inspur.emmcloud.ui.login;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

/**
 * Created by yufuchang on 2018/1/30.
 */

public class LoginMoreActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_more);
    }

    /**
     * onclick
     * @param view
     */
    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
        }
    }
}
