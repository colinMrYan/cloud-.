package com.inspur.emmcloud.ui.appcenter.mail;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.widget.sildemenu.AllInterface;
import com.inspur.emmcloud.widget.sildemenu.LeftDrawerLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/12/20.
 */

public class MailHomeBaseActivity extends BaseFragmentActivity implements AllInterface.OnMenuSlideListener {

    protected LoadingDialog loadingDlg;
    @BindView(R.id.ldl_menu)
    LeftDrawerLayout leftDrawerLayout;
    @BindView(R.id.v_shadow)
    View shadowView;
    private MailLeftMenuFragment mailLeftMenuFragment;


    @Override
    public void onCreate() {
        setContentView(R.layout.activity_mail_home);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        loadingDlg = new LoadingDialog(this);
        addMailLeftMenyu();
        setStatus();
    }

//    private void loginMail(){
//        String mail = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT,"");
//        String password = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD,"");
//        if (StringUtils.isBlank(mail) || StringUtils.isBlank(password)){
//            IntentUtils.startActivity(this,MailLoginActivity.class,true);
//        }else {
//            MailLoginUtils.getInstance().loginMail();
//        }
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSimpleEventMessage(SimpleEventMessage simpleEventMessage) {
        if (simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_MAIL_LOGIN_SUCCESS)) {
            addMailLeftMenyu();
        } else if (simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_MAIL_LOGIN_FAIL)) {
            addMailLeftMenyu();
        }

    }

    private void addMailLeftMenyu() {
        FragmentManager fm = getSupportFragmentManager();
        mailLeftMenuFragment = (MailLeftMenuFragment) fm.findFragmentById(R.id.fm_container_menu);
        leftDrawerLayout.setOnMenuSlideListener(this);
        if (mailLeftMenuFragment == null) {
            fm.beginTransaction().add(R.id.fm_container_menu, mailLeftMenuFragment = new MailLeftMenuFragment()).commit();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_setting:
                openMenu();
                break;
            case R.id.ibt_menu_back:
                closeMenu();
                break;
            case R.id.ibt_mail_setting:
                IntentUtils.startActivity(this, MailSettingActivity.class);
                break;
            case R.id.ibt_mail_add:
                Bundle bundle = new Bundle();
                bundle.putString(MailSendActivity.EXTRA_MAIL_MODE, MailSendActivity.MODE_NEW);
                IntentUtils.startActivity(this, MailSendActivity.class, bundle);
                break;
            case R.id.ibt_back:
                finish();
                break;
            case R.id.v_shadow:
                closeMenu();
                break;
            case R.id.tv_mail_acount:

                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (leftDrawerLayout.isDrawerOpen()) {
            leftDrawerLayout.closeDrawer();
            shadowView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    public void openMenu() {
        leftDrawerLayout.openDrawer();
        shadowView.setVisibility(View.VISIBLE);
    }

    public void closeMenu() {
        if (leftDrawerLayout.isDrawerOpen()) {
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
