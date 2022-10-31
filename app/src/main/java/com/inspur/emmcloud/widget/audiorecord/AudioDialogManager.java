package com.inspur.emmcloud.widget.audiorecord;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import androidx.core.content.ContextCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AudioDialogManager {

    /**
     * 以下为dialog的初始化控件，包括其中的布局文件
     */
    private Dialog dialog;
    private ImageView recorderImg;
    private TextView labelText;
    private Context mContext;
    private boolean isStatusRecording = false;
    private float recordTime = 0;

    /**
     * 以下为语音转字动画的dialog
     */
    private Timer timer;
    private TimerTask timerTask;
    private int count = 0;
    private List<ImageView> imageViewList = new ArrayList<>();
    /**
     * 控制动画的handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (imageViewList != null && imageViewList.size() > 0) {
                imageViewList.get(0).setVisibility(View.VISIBLE);
                imageViewList.get(1).setVisibility((count % 2 == 0 || count % 3 == 0) ? View.VISIBLE : View.INVISIBLE);
                imageViewList.get(2).setVisibility((count % 3 == 0) ? View.VISIBLE : View.INVISIBLE);
            }
        }
    };

    public AudioDialogManager(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    /**
     * 展示录制音频dialog
     */
    public void showRecordingDialog() {
        dialog = new Dialog(mContext);
        // 用layoutinflater来引用布局
        dialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        dialog.getWindow().setDimAmount(0);
        View view = LayoutInflater.from(mContext).inflate(R.layout.communication_dialog_record_manager, null);
        dialog.setContentView(view);
        recorderImg = (ImageView) dialog.findViewById(R.id.iv_recorder);
        labelText = (TextView) dialog.findViewById(R.id.tv_recorder);
        dialog.setCancelable(false);
        setDialogWidth(0.5f);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        isStatusRecording = true;
    }

    private void setDialogWidth(float size) {
        WindowManager m = dialog.getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        int width = d.getWidth();
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        p.x = 0;
        p.y = 0;
        p.width = (int) (width * size);
        dialog.getWindow().setAttributes(
                p);
    }

    /**
     * 设置正在录音时的dialog界面
     */
    public void recording() {
        if (dialog != null && dialog.isShowing()) {
            isStatusRecording = true;
            recorderImg.setImageResource(R.drawable.ic_recorder_volume_level_v1);
            if (recordTime >= 51) {
                labelText.setText(mContext.getString(R.string.record_count_down_text, (60 - (int) recordTime)));
            } else {
                labelText.setText(R.string.slide_up_to_cancel);
            }
            labelText.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.transparent));
        }
    }

    /**
     * 取消界面
     */
    public void wantToCancel() {
        // TODO Auto-generated method stub
        if (dialog != null && dialog.isShowing()) {
            isStatusRecording = false;
            recorderImg.setImageResource(R.drawable.ic_recorder_cancel);
            labelText.setText(R.string.release_to_cancel);
            labelText.setBackgroundResource(R.drawable.bg_record_dialog_text);
        }
    }

    /**
     * 录音过短
     */
    public void tooShort() {
        // TODO Auto-generated method stub
        if (dialog != null && dialog.isShowing()) {
            isStatusRecording = false;
            recorderImg.setImageResource(R.drawable.ic_recorder_too_short);
            labelText.setText(R.string.recording_too_short);
            labelText.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.transparent));
        }
    }

    /**
     * 隐藏dialog
     */
    public void dismissRecordingDialog() {
        // TODO Auto-generated method stub
        recordTime = 0;
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    /**以下是语音转字动画UI部分**/

    /**
     * 更新音量和持续时间
     *
     * @param level
     * @param time
     */
    public void updateVoiceLevelAndDurationTime(int level, float time) {
        // TODO Auto-generated method stub
        recordTime = time;
        if (dialog != null && dialog.isShowing() && isStatusRecording) {
            //通过level来找到图片的id，也可以用switch来寻址，但是代码可能会比较长
            int resId = mContext.getResources().getIdentifier("ic_recorder_volume_level_v" + level,
                    "drawable", mContext.getPackageName());
            recorderImg.setImageResource(resId);
            if (recordTime >= 51) {
                labelText.setText(mContext.getString(R.string.record_count_down_text, (60 - (int) recordTime)));
            }
        }
    }

    /**
     * 展示dialog
     */
    public void showVoice2WordProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                count = count + 1;
                handler.sendEmptyMessage(count);
            }
        };
        dialog = new Dialog(mContext);
        // 用layoutinflater来引用布局
        dialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        dialog.getWindow().setDimAmount(0);
        View view = LayoutInflater.from(mContext).inflate(R.layout.communication_dialog_voice_word_manager, null);
        dialog.setContentView(view);
        imageViewList.add((ImageView) dialog.findViewById(R.id.iv_arrow_first));
        imageViewList.add((ImageView) dialog.findViewById(R.id.iv_arrow_second));
        imageViewList.add((ImageView) dialog.findViewById(R.id.iv_arrow_third));
        dialog.setCancelable(false);
        setDialogWidth(0.75f);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        timer.schedule(timerTask, 10, 300);
    }

    /**
     * 取消dialog
     */
    public void dismissVoice2WordProgressDialog() {
        if (dialog != null) {
            dismissRecordingDialog();
        }
        destroyTimerAndData();
    }

    /**
     * 销毁timer和timertask，清空count
     */
    private void destroyTimerAndData() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        count = 0;
    }
}
