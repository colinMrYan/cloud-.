package com.inspur.emmcloud.mail.ui;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import android.view.View;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.R2;
import com.inspur.emmcloud.mail.widget.sildemenu.AllInterface;
import com.inspur.emmcloud.mail.widget.sildemenu.LeftDrawerLayout;

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
    @BindView(R2.id.ldl_menu)
    LeftDrawerLayout leftDrawerLayout;
    @BindView(R2.id.v_shadow)
    View shadowView;
    private MailLeftMenuFragment mailLeftMenuFragment;
    private ContactUser contactUser;


    @Override
    public void onCreate() {
        setContentView(R.layout.mail_home_activity);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        loadingDlg = new LoadingDialog(this);
        addMailLeftMenyu();
        setStatus();
        ContactService mailService = Router.getInstance().getService(ContactService.class);
        if (mailService != null) {
            contactUser = mailService.getContactUserByUid(BaseApplication.getInstance().getUid());
        }
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
        int i = v.getId();
        if (i == R.id.ibt_setting) {
            openMenu();

        } else if (i == R.id.ibt_menu_back) {
            closeMenu();

        } else if (i == R.id.ibt_mail_setting) {
            IntentUtils.startActivity(this, MailSettingActivity.class);

        } else if (i == R.id.ibt_mail_add) {
            Bundle bundle = new Bundle();
            bundle.putString(MailSendActivity.EXTRA_MAIL_MODE, MailSendActivity.MODE_NEW);
            IntentUtils.startActivity(this, MailSendActivity.class, bundle);

        } else if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.v_shadow) {
            closeMenu();

        } else if (i == R.id.tv_mail_acount) {
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
