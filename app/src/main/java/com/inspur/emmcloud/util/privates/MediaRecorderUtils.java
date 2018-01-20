package com.inspur.emmcloud.util.privates;

import android.media.MediaRecorder;
import android.os.Handler;

import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.common.LogUtils;

/**
 * Created by yufuchang on 2018/1/19.
 */

public class MediaRecorderUtils {
    private MediaRecorder mediaRecorder;
    private OnAudioStatusUpdateListener audioStatusUpdateListener;
    private int BASE = 1;
    private int SPACE = 100;// 间隔取样时间
    private long startTime = 0;
    public static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长1000*60*10;
    public MediaRecorderUtils(){
        mediaRecorder =new MediaRecorder();

    }

    private final Handler mHandler = new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            LogUtils.YfcDebug("更新状态");
            updateMicStatus();
        }
    };

    /**
     * 更新麦克状态
     */
    public void updateMicStatus() {
        if (mediaRecorder != null) {
            double ratio = (double)mediaRecorder.getMaxAmplitude() / BASE;
            double db = 0;// 分贝
            LogUtils.YfcDebug("原始分贝："+db);
            if (ratio > 1) {
                db = 20 * Math.log10(ratio);
                LogUtils.YfcDebug("分贝数："+db);
                if(null != audioStatusUpdateListener) {
                    audioStatusUpdateListener.onUpdate(db,System.currentTimeMillis()-startTime);
                }
            }
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
        }
    }


    /**
     * 开始录音
     */
    public void startMediaRecord(){
        LogUtils.YfcDebug("开始录音");
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//声音来源是话筒
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            String filePath = MyAppConfig.LOCAL_DOWNLOAD_PATH + System.currentTimeMillis() + ".amr" ;
            /* ③准备 */
            mediaRecorder.setOutputFile(filePath);
            mediaRecorder.setMaxDuration(MAX_LENGTH);

            mediaRecorder.prepare();
            mediaRecorder.start();
            startTime = System.currentTimeMillis();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 结束录音
     */
    public void stopMediaRecord(){
        //有一些网友反应在5.0以上在调用stop的时候会报错，翻阅了一下谷歌文档发现上面确实写的有可能会报错的情况，捕获异常清理一下就行了，感谢大家反馈！
        try {
            LogUtils.YfcDebug("结束录音");
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }catch (Exception e){
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    public interface OnAudioStatusUpdateListener {
        /**
         * 录音中...
         * @param db 当前声音分贝
         * @param time 录音时长
         */
        public void onUpdate(double db,long time);

        /**
         * 停止录音
         * @param filePath 保存路径
         */
        public void onStop(String filePath);
    }
}
