package com.inspur.emmcloud.ui.contact;

import android.os.Bundle;

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.R;

/**
 * 通讯录选择界面
 *
 * @author Administrator
 */
public class ContactSearchActivity extends BaseFragmentActivity {

    private ContactSearchFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_search_hold);
        //必需继承FragmentActivity,嵌套fragment只需要这行代码
        fragment = new ContactSearchFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (!fragment.onBackPressedConsumeByUI()) {
            super.onBackPressed();
        }
    }

}
