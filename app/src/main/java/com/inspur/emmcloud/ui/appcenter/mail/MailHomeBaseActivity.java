package com.inspur.emmcloud.ui.appcenter.mail;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.sildemenu.AllInterface;
import com.inspur.emmcloud.widget.sildemenu.LeftDrawerLayout;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by chenmch on 2018/12/20.
 */

@ContentView(R.layout.activity_mail_home)
public class MailHomeBaseActivity extends BaseFragmentActivity implements AllInterface.OnMenuSlideListener{

    @ViewInject(R.id.ldl_menu)
    private LeftDrawerLayout leftDrawerLayout;

    @ViewInject(R.id.v_shadow)
    private View shadowView;

    private MailLeftMenuFragment mailLeftMenuFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        x.view().inject(this);
        FragmentManager fm = getSupportFragmentManager();
        mailLeftMenuFragment = (MailLeftMenuFragment) fm.findFragmentById(R.id.fm_container_menu);
        leftDrawerLayout.setOnMenuSlideListener(this);
        if (mailLeftMenuFragment == null) {
            fm.beginTransaction().add(R.id.fm_container_menu, mailLeftMenuFragment = new MailLeftMenuFragment()).commit();
        }
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.ibt_setting:
               openMenu();
                break;
            case R.id.rl_back:
                closeMenu();
                break;
            case R.id.bt_mail_add:
                break;
            case R.id.bt_close:
               finish();
                break;
            case R.id.v_shadow:
                closeMenu();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (leftDrawerLayout.isDrawerOpen()){
            leftDrawerLayout.closeDrawer();
            shadowView.setVisibility(View.GONE);
        }else {
            super.onBackPressed();
        }
    }

    public void openMenu() {
        leftDrawerLayout.openDrawer();
        shadowView.setVisibility(View.VISIBLE);
    }

    public void closeMenu() {
        if (leftDrawerLayout.isDrawerOpen()){
            leftDrawerLayout.closeDrawer();
            shadowView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onMenuSlide(float offset) {
        shadowView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
        int alpha = (int) Math.round(offset * 255 * 0.4);
        shadowView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
