package com.inspur.emmcloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResourceUtils;
import com.inspur.emmcloud.util.privates.LanguageManager;

public abstract class BaseActivity extends Activity {
    protected final int STATUS_NORMAL = 1;
    protected final int STATUS_WHITE = 2;
    protected final int STATUS_WHITE_DARK_FONT = 3;
    protected final int STATUS_TRANSPARENT = 4;
    protected final int STATUS_NO_SET = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        setTheme();
        super.onCreate(savedInstanceState);
        int layoutResId = getLayoutResId();
        if (layoutResId != 0) {
            setContentView(layoutResId);
        }
        setStatus(getStatusType());
    }

    public abstract int getLayoutResId();

    protected int getStatusType() {
        return STATUS_NORMAL;
    }

    //解决调用系统应用后会弹出手势解锁的问题
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyApplication.getInstance().setEnterSystemUI(false);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.getInstance().attachBaseContext(newBase));
    }

    public void setTheme() {
        int currentThemeNo = PreferencesUtils.getInt(MyApplication.getInstance(), Constant.PREF_APP_THEME, 0);
        switch (currentThemeNo) {
            case 1:
                setTheme(R.style.AppTheme_1);
                break;
            case 2:
                setTheme(R.style.AppTheme_2);
                break;
            default:
                setTheme(R.style.AppTheme_0);
                break;
        }
    }

    private void setStatus(int statusType) {
        int navigationBarColor = android.R.color.white;
        boolean isStatusBarDarkFont = ResourceUtils.getBoolenOfAttr(this, R.attr.status_bar_dark_font);
        switch (statusType) {
            case STATUS_NORMAL:
                int statusBarColor = ResourceUtils.getResValueOfAttr(BaseActivity.this, R.attr.header_bg_color);
                ImmersionBar.with(this).statusBarColor(statusBarColor).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).statusBarDarkFont(isStatusBarDarkFont, 0.2f).init();
                break;
            case STATUS_WHITE:
                ImmersionBar.with(this).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).init();
                break;
            case STATUS_WHITE_DARK_FONT:
                ImmersionBar.with(this).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).statusBarColor(android.R.color.white).statusBarDarkFont(true, 0.2f).init();
                break;
            case STATUS_TRANSPARENT:
                ImmersionBar.with(this).transparentStatusBar().statusBarDarkFont(isStatusBarDarkFont, 0.2f).navigationBarColor(navigationBarColor).navigationBarDarkIcon(true, 1.0f).init();
                break;
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }
}
