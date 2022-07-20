package com.inspur.emmcloud.web.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.R2;
import com.inspur.emmcloud.web.plugin.audio.IMPAudioService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AudioDialogChooseManager {
    /**
     * 以下为dialog的初始化控件，包括其中的布局文件
     */
    private Dialog dialog;
    private ImageView recorderImg;
    private TextView recorderText;
    private TextView cancelText;
    private TextView uploadText;
    private Context mContext;
    private boolean isStatusRecording = false;
    private float recordTime = 0;
    private IMPAudioService audioService;

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

    public AudioDialogChooseManager(Context context, IMPAudioService mediaService) {
        mContext = context;
        audioService = mediaService;
    }

    /**
     * 展示录制音频dialog
     */
    public void showRecordingDialog() {
        dialog = new Dialog(mContext);
        // 用layoutinflater来引用布局
        dialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        dialog.getWindow().setDimAmount(0);
        View view = LayoutInflater.from(mContext).inflate(R.layout.web_imp_dialog_voice_record_manager, null);
        dialog.setContentView(view);
        recorderImg = (ImageView) dialog.findViewById(R.id.iv_recorder);
        recorderText = (TextView) dialog.findViewById(R.id.tv_recorder);
        uploadText = (TextView) dialog.findViewById(R.id.tv_upload);
        cancelText = (TextView) dialog.findViewById(R.id.tv_cancel);
        uploadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioService != null) audioService.upload();
                dismissRecordingDialog();
            }
        });
        cancelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioService != null) audioService.cancel();
                dismissRecordingDialog();
            }
        });
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
        }
    }

    /**
     * 取消界面
     */
    public void wantToCancel() {
        if (dialog != null && dialog.isShowing()) {
            isStatusRecording = false;
            recorderImg.setImageResource(R.drawable.ic_recorder_cancel);
            recorderText.setText(R.string.release_to_cancel);
            recorderText.setBackgroundResource(R.drawable.bg_record_dialog_text);
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
            recorderText.setText(R.string.recording_too_short);
            recorderText.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.transparent));
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
        recordTime = time;
        if (dialog != null && dialog.isShowing() && isStatusRecording) {
            //通过level来找到图片的id，也可以用switch来寻址，但是代码可能会比较长
            int resId = mContext.getResources().getIdentifier("ic_recorder_volume_level_v" + level,
                    "drawable", mContext.getPackageName());
            recorderImg.setImageResource(resId);
            if (recordTime >= 51) {
                recorderText.setText(mContext.getString(R.string.record_count_down_text, (60 - (int) recordTime)));
            }
        }
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
