package com.inspur.emmcloud.ui.mine.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.ninelock.LockPatternUtil;
import com.inspur.emmcloud.util.ninelock.LockPatternView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Sym on 2015/12/24.
 */
public class GestureLoginActivity extends Activity {

    private static final String TAG = "LoginGestureActivity";

    @Bind(R.id.lockPatternView)
    LockPatternView lockPatternView;
    @Bind(R.id.messageTv)
    TextView messageTv;
    @Bind(R.id.forgetGestureBtn)
    Button forgetGestureBtn;

    private static final long DELAYTIME = 600l;
    private String gesturePassword;
    private boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarColor.changeStateBarColor(this, R.color.grey_f6f6f6);
        setContentView(R.layout.activity_gesture_login);
        ButterKnife.bind(this);
        this.init();
    }

    private void init() {
        //得到当前用户的手势密码
        gesturePassword = PreferencesByUserAndTanentUtils.getString(GestureLoginActivity.this, "gesture_code");
        lockPatternView.setOnPatternListener(patternListener);
        updateStatus(Status.DEFAULT);
        if (getIntent().hasExtra("gesture_code_change")) {
            String command = getIntent().getStringExtra("gesture_code_change");
            if(command.equals("login")){
                isLogin = true;
            }
        }
    }

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
                        LogUtils.YfcDebug("command："+command);
                        if (command.equals("reset")) {
                            IntentUtils.startActivity(GestureLoginActivity.this, CreateGestureActivity.class);
                            finish();
                        } else if (command.equals("login")) {
                            isLogin = true;
                            finish();
                        } else if(command.equals("close")){
                            LogUtils.YfcDebug("进入LoginClose");
                            PreferencesByUserAndTanentUtils.putBoolean(GestureLoginActivity.this,"gesture_code_isopen",false);
                            PreferencesByUserAndTanentUtils.putString(GestureLoginActivity.this, "gesture_code","");
                            finish();
                        }
                    }
                } else {
                    updateStatus(Status.ERROR);
                }
            }
        }
    };

    /**
     * 更新状态
     *
     * @param status
     */
    private void updateStatus(Status status) {
        messageTv.setText(status.strId);
        messageTv.setTextColor(getResources().getColor(status.colorId));
        switch (status) {
            case DEFAULT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
            case ERROR:
                lockPatternView.setPattern(LockPatternView.DisplayMode.ERROR);
                lockPatternView.postClearPatternRunnable(DELAYTIME);
                break;
            case CORRECT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                loginGestureSuccess();
                break;
        }
    }

    /**
     * 手势登录成功（去首页）
     */
    private void loginGestureSuccess() {
        Toast.makeText(GestureLoginActivity.this, "success", Toast.LENGTH_SHORT).show();
    }

    /**
     * 忘记手势密码（去账号登录界面）
     */
    @OnClick(R.id.forgetGestureBtn)
    void forgetGesturePasswrod() {
        Intent intent = new Intent(GestureLoginActivity.this, CreateGestureActivity.class);
        startActivity(intent);
        this.finish();
    }

    private enum Status {
        //默认的状态
        DEFAULT(R.string.gesture_default, R.color.grey_a5a5a5),
        //密码输入错误
        ERROR(R.string.gesture_error, R.color.red_f4333c),
        //密码输入正确
        CORRECT(R.string.gesture_correct, R.color.grey_a5a5a5);

        private Status(int strId, int colorId) {
            this.strId = strId;
            this.colorId = colorId;
        }
        private int strId;
        private int colorId;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        LogUtils.YfcDebug("点返回键的状态："+isLogin);
        if(!isLogin){
            finish();
        }
    }
}
