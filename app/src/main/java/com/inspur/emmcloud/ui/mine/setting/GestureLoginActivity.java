package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.ninelock.LockPatternUtil;
import com.inspur.emmcloud.util.privates.ninelock.LockPatternView;
import com.inspur.emmcloud.widget.CircleTextImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Sym on 2015/12/24.
 */
public class GestureLoginActivity extends BaseActivity {

    private static final int GESTURE_CODE_TIMES = 5;
    private static final long DELAYTIME = 600l;
    @BindView(R.id.lockPatternView)
    LockPatternView lockPatternView;
    @BindView(R.id.gestrue_message_text)
    TextView gestureMessage;
    @BindView(R.id.forget_gesture_btn)
    Button forgetGestureBtn;
    private String gesturePassword;
    private boolean isLogin = false;
    private int errorTime = 0;
    private LockPatternView.OnPatternListener patternListener = new LockPatternView.OnPatternListener() {

        @Override
        public void onPatternStart() {
            lockPatternView.removePostClearPatternRunnable();
        }

        @Override
        public void onPatternComplete(List<LockPatternView.Cell> pattern) {
            if (pattern != null) {
                if (LockPatternUtil.checkPattern(pattern, gesturePassword)) {
                    updateStatus(Status.CORRECT);
                    if (getIntent().hasExtra("gesture_code_change")) {
                        String command = getIntent().getStringExtra("gesture_code_change");
                        if (command.equals("reset")) {
                            IntentUtils.startActivity(GestureLoginActivity.this, CreateGestureActivity.class);
                            finish();
                        } else if (command.equals("login")) {
                            isLogin = true;
                            //发送解锁广播是，SchemeHandleActivity中接收处理
                            Intent intent = new Intent();
                            intent.setAction(Constant.ACTION_SAFE_UNLOCK);
                            MyApplication.getInstance().setIsActive(true);
                            LocalBroadcastManager.getInstance(GestureLoginActivity.this).sendBroadcast(intent);
                            finish();
                        } else if (command.equals("close")) {
                            clearGestureInfo();
                            finish();
                        }
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                } else {
                    errorTime = errorTime + 1;
                    updateStatus(Status.ERROR);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        init();
        ImmersionBar.with(this).statusBarColor(R.color.grey_f6f6f6).statusBarDarkFont(true, 0.2f).init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_gesture_login;
    }

    protected int getStatusType() {
        return STATUS_NO_SET;
    }



    private void init() {
        //得到当前用户的手势密码
        gesturePassword = CreateGestureActivity.getGestureCodeByUser(GestureLoginActivity.this);
        lockPatternView.setOnPatternListener(patternListener);
        updateStatus(Status.DEFAULT);
        if (getIntent().hasExtra("gesture_code_change")) {
            String command = getIntent().getStringExtra("gesture_code_change");
            if (command.equals("login")) {
                isLogin = true;
            }
        }
        String userHeadImgUri = APIUri
                .getChannelImgUrl(GestureLoginActivity.this, ((MyApplication) getApplication()).getUid());
        CircleTextImageView circleImageView = (CircleTextImageView) findViewById(R.id.gesture_login_user_head_img);
        ImageDisplayUtils.getInstance().displayImage(circleImageView,
                userHeadImgUri, R.drawable.icon_person_default);
    }

    /**
     * 更新状态
     *
     * @param status
     */
    private void updateStatus(Status status) {
        gestureMessage.setText(status.strId);
        gestureMessage.setTextColor(getResources().getColor(status.colorId));
        switch (status) {
            case DEFAULT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
            case ERROR:
                gestureMessage.setText(getString(R.string.gesture_code_error) + " " +
                        ((GESTURE_CODE_TIMES - errorTime) < 0 ? 0 : (GESTURE_CODE_TIMES - errorTime)) + " " + getString(R.string.gesture_code_time));
                findViewById(R.id.gesture_code_tips).setVisibility(View.VISIBLE);
                Animation shake = AnimationUtils.loadAnimation(this, R.anim.left_right_shake);
                gestureMessage.startAnimation(shake);
                lockPatternView.setPattern(LockPatternView.DisplayMode.ERROR);
                lockPatternView.postClearPatternRunnable(DELAYTIME);
                if ((GESTURE_CODE_TIMES - errorTime) == 0) {
                    clearGestureInfo();
                    ((MyApplication) getApplication()).signout();
                }
                break;
            case CORRECT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
        }
    }

    /**
     * 忘记手势密码（去账号登录界面）
     */
    @OnClick(R.id.forget_gesture_btn)
    public void forgetGesturePasswrod() {
        clearGestureInfo();
        ((MyApplication) getApplication()).signout();
    }

    /**
     * 清理手势信息
     */
    private void clearGestureInfo() {
        CreateGestureActivity.putGestureCodeByUser(GestureLoginActivity.this, "");
        CreateGestureActivity.putGestureCodeIsOpenByUser(GestureLoginActivity.this, false);
    }

    @Override
    public void onBackPressed() {
        if (isLogin) {
            ((MyApplication) getApplication()).exit();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }

    private enum Status {
        //默认的状态
        DEFAULT(R.string.gesture_default, R.color.grey_a5a5a5),
        //密码输入错误
        ERROR(R.string.gesture_error, R.color.red_f4333c),
        //密码输入正确
        CORRECT(R.string.gesture_correct, R.color.grey_a5a5a5);

        private int strId;
        private int colorId;

        Status(int strId, int colorId) {
            this.strId = strId;
            this.colorId = colorId;
        }
    }

}
