package com.inspur.emmcloud.basemodule.media.selector.basic;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.selector.PictureOnlyCameraFragment;
import com.inspur.emmcloud.basemodule.media.selector.PictureSelectorSystemFragment;
import com.inspur.emmcloud.basemodule.media.selector.config.PictureConfig;
import com.inspur.emmcloud.basemodule.media.selector.config.PictureSelectionConfig;
import com.inspur.emmcloud.basemodule.media.selector.immersive.ImmersiveManager;
import com.inspur.emmcloud.basemodule.media.selector.style.SelectMainStyle;
import com.inspur.emmcloud.basemodule.media.selector.utils.StyleUtils;

/**
 * @author：luck
 * @date：2022/2/10 6:07 下午
 * @describe：PictureSelectorTransparentActivity
 */
public class PictureSelectorTransparentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        immersive();
        setContentView(R.layout.ps_empty);
        setActivitySize();
        setupFragment();
    }

    private void immersive() {
        SelectMainStyle mainStyle = PictureSelectionConfig.selectorStyle.getSelectMainStyle();
        int statusBarColor = mainStyle.getStatusBarColor();
        int navigationBarColor = mainStyle.getNavigationBarColor();
        boolean isDarkStatusBarBlack = mainStyle.isDarkStatusBarBlack();
        if (!StyleUtils.checkStyleValidity(statusBarColor)) {
            statusBarColor = ContextCompat.getColor(this, R.color.ps_color_grey);
        }
        if (!StyleUtils.checkStyleValidity(navigationBarColor)) {
            navigationBarColor = ContextCompat.getColor(this, R.color.ps_color_grey);
        }
        ImmersiveManager.immersiveAboveAPI23(this, statusBarColor, navigationBarColor, isDarkStatusBarBlack);
    }

    private void setupFragment() {
        int modeTypeSource = getIntent().getIntExtra(PictureConfig.EXTRA_MODE_TYPE_SOURCE, 0);
        String fragmentTag;
        Fragment targetFragment;
        if (modeTypeSource == PictureConfig.MODE_TYPE_SYSTEM_SOURCE) {
            fragmentTag = PictureSelectorSystemFragment.TAG;
            targetFragment = PictureSelectorSystemFragment.newInstance();
        } else {
            fragmentTag = PictureOnlyCameraFragment.TAG;
            targetFragment = PictureOnlyCameraFragment.newInstance();
        }
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        Fragment fragment = supportFragmentManager.findFragmentByTag(fragmentTag);
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
        FragmentInjectManager.injectSystemRoomFragment(supportFragmentManager, fragmentTag, targetFragment);
    }

    @SuppressLint("RtlHardcoded")
    private void setActivitySize() {
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.ps_anim_fade_out);
    }
}
