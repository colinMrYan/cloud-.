package com.inspur.emmcloud.ui.mine.setting;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.privates.ninelock.LockPatternIndicator;
import com.inspur.emmcloud.util.privates.ninelock.LockPatternUtil;
import com.inspur.emmcloud.util.privates.ninelock.LockPatternView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * create gesture activity
 * Created by Sym on 2015/12/23.
 */
public class CreateGestureActivity extends BaseActivity {

    public static final String GESTURE_CODE = "gesture_code";
    public static final String GESTURE_CODE_ISOPEN = "gesture_code_isopen";
    public static final String CREATE_GESTURE_CODE_SUCCESS = "create_gesture_code_success";
    public static final String EXTRA_FORCE_SET = "extra_force_set";
    private static final long DELAYTIME = 600L;
    @BindView(R.id.lockPatterIndicator)
    LockPatternIndicator lockPatternIndicator;
    @BindView(R.id.lockPatternView)
    LockPatternView lockPatternView;
    @BindView(R.id.gesture_reset_btn)
    Button resetBtn;
    @BindView(R.id.gesture_message_text)
    TextView gestrueMessage;
    @BindView(R.id.tv_force_gesture_create)
    TextView forceGestureCreate;
    private List<LockPatternView.Cell> mChosenPattern = null;
    /**
     * 手势监听
     */
    private LockPatternView.OnPatternListener patternListener = new LockPatternView.OnPatternListener() {

        @Override
        public void onPatternStart() {
            lockPatternView.removePostClearPatternRunnable();
            //updateStatus(Status.DEFAULT, null);
            lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
        }

        @Override
        public void onPatternComplete(List<LockPatternView.Cell> pattern) {
            //Log.e(TAG, "--onPatternDetected--");
            if (mChosenPattern == null && pattern.size() >= 4) {
                mChosenPattern = new ArrayList<LockPatternView.Cell>(pattern);
                updateStatus(Status.CORRECT, pattern);
            } else if (mChosenPattern == null && pattern.size() < 4) {
                updateStatus(Status.LESSERROR, pattern);
            } else if (mChosenPattern != null) {
                if (mChosenPattern.equals(pattern)) {
                    updateStatus(Status.CONFIRMCORRECT, pattern);
                } else {
                    updateStatus(Status.CONFIRMERROR, pattern);
                }
            }
        }
    };

    /**
     * 根据用户获取gesturecode
     *
     * @param context
     * @return
     */
    public static String getGestureCodeByUser(Context context) {
        return PreferencesByUsersUtils.getString(context, CreateGestureActivity.GESTURE_CODE);
    }

    /**
     * 根据用户获取是否打开了gesturecode
     *
     * @param context
     * @return
     */
    public static boolean getGestureCodeIsOpenByUser(Context context) {
        return PreferencesByUsersUtils.getBoolean(context, CreateGestureActivity.GESTURE_CODE_ISOPEN, false);
    }

    /**
     * 根据用户存储gesturecode
     *
     * @param context
     */
    public static void putGestureCodeByUser(Context context, String gestureCode) {
        PreferencesByUsersUtils.putString(context, GESTURE_CODE, gestureCode);
    }

    /**
     * 根据用户存储gesturecode是否打开
     *
     * @param context
     */
    public static void putGestureCodeIsOpenByUser(Context context, boolean isGestureCodeOpen) {
        PreferencesByUsersUtils.putBoolean(context, GESTURE_CODE_ISOPEN, isGestureCodeOpen);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_create_gesture;
    }

    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }
    /**
     * 初始化
     */
    private void init() {
        lockPatternView.setOnPatternListener(patternListener);
        if (getIntent().getBooleanExtra(EXTRA_FORCE_SET, false)) {
            forceGestureCreate.setVisibility(View.VISIBLE);
        } else {
            forceGestureCreate.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 更新状态
     *
     * @param status
     * @param pattern
     */
    private void updateStatus(Status status, List<LockPatternView.Cell> pattern) {
        gestrueMessage.setTextColor(getResources().getColor(status.colorId));
        gestrueMessage.setText(status.strId);
        switch (status) {
            case DEFAULT:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
            case CORRECT:
                updateLockPatternIndicator();
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
            case LESSERROR:
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                break;
            case CONFIRMERROR:
                lockPatternView.setPattern(LockPatternView.DisplayMode.ERROR);
                lockPatternView.postClearPatternRunnable(DELAYTIME);
                break;
            case CONFIRMCORRECT:
                saveChosenPattern(pattern);
                lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
                setLockPatternSuccess();
                break;
        }
    }

    /**
     * 更新 Indicator
     */
    private void updateLockPatternIndicator() {
        if (mChosenPattern == null)
            return;
        lockPatternIndicator.setIndicator(mChosenPattern);
    }

    /**
     * 重新设置手势
     */
    @OnClick(R.id.gesture_reset_btn)
    public void resetGesture() {
        mChosenPattern = null;
        lockPatternIndicator.setDefaultIndicator();
        updateStatus(Status.DEFAULT, null);
        lockPatternView.setPattern(LockPatternView.DisplayMode.DEFAULT);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    /**
     * 成功设置了手势密码(跳到首页)
     */
    private void setLockPatternSuccess() {
        ToastUtils.show(this, getString(R.string.create_gesture_confirm_correct));
        putGestureCodeIsOpenByUser(CreateGestureActivity.this, true);
        EventBus.getDefault().post(CREATE_GESTURE_CODE_SUCCESS);
        setResult(RESULT_OK);
        finish();
    }

    /**
     * 保存手势密码
     */
    private void saveChosenPattern(List<LockPatternView.Cell> cells) {
        String gestureCode = LockPatternUtil.patternToString(cells);
        putGestureCodeByUser(CreateGestureActivity.this, gestureCode);
    }

    private enum Status {
        //默认的状态，刚开始的时候（初始化状态）
        DEFAULT(R.string.create_gesture_default, R.color.grey_a5a5a5),
        //第一次记录成功
        CORRECT(R.string.create_gesture_correct, R.color.grey_a5a5a5),
        //连接的点数小于4（二次确认的时候就不再提示连接的点数小于4，而是提示确认错误）
        LESSERROR(R.string.create_gesture_less_error, R.color.red_f4333c),
        //二次确认错误
        CONFIRMERROR(R.string.create_gesture_confirm_error, R.color.red_f4333c),
        //二次确认正确
        CONFIRMCORRECT(R.string.create_gesture_confirm_correct, R.color.grey_a5a5a5);

        private int strId;
        private int colorId;

        Status(int strId, int colorId) {
            this.strId = strId;
            this.colorId = colorId;
        }
    }
}
