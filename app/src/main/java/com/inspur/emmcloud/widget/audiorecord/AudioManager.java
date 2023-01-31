package com.inspur.emmcloud.widget.audiorecord;

import android.media.MediaRecorder;

import com.inspur.emmcloud.basemodule.config.MyAppConfig;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AudioManager {

    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    /**
     * 单例化这个类
     */
    private static AudioManager mInstance;
    public AudioStageListener mListener;
    private MediaRecorder mRecorder;
    private String mDirString;
    private String mCurrentFilePathString;

    /**
     * 单例化的方法 1 先声明一个static 类型的变量a 2 在声明默认的构造函数 3 再用public synchronized static
     * 类名 getInstance() { if(a==null) { a=new 类();} return a; } 或者用以下的方法
     */
    private int BASE = 1;
    private boolean isPrepared;// 是否准备好了

    private AudioManager(String dir) {
        mDirString = dir;
    }

    public static AudioManager getInstance(String dir) {
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager(dir);

                }
            }
        }
        return mInstance;

    }

    public void setOnAudioStageListener(AudioStageListener listener) {
        mListener = listener;
    }

    // 准备方法
    public void prepareAudio() {
        try {
            // 一开始应该是false的
            isPrepared = false;

            File dir = new File(MyAppConfig.LOCAL_CACHE_VOICE_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileNameString = generalFileName();
            File file = new File(dir, fileNameString);

            mCurrentFilePathString = file.getAbsolutePath();
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                //mRecorder.setAudioSource(AUDIO_SAMPLE_RATE);
                // 设置输出文件
                mRecorder.setOutputFile(file.getAbsolutePath());
                // 设置meidaRecorder的音频源是麦克风
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                // 设置文件音频的输出格式为amr
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                // 设置音频的编码格式为amr
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }

            // 严格遵守google官方api给出的mediaRecorder的状态流程图
            mRecorder.prepare();
            mRecorder.start();
            // 准备结束
            isPrepared = true;
            // 已经准备好了，可以录制了
            if (mListener != null) {
                mListener.wellPrepared();
            }

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 随机生成文件的名称
     *
     * @return
     */
    private String generalFileName() {
        // TODO Auto-generated method stub

        return UUID.randomUUID().toString() + ".amr";
    }

    // 获得声音的level
    public int getVoiceLevel() {
        // mRecorder.getMaxAmplitude()这个是音频的振幅范围，值域是1-32767
        if (isPrepared && mRecorder != null) {
            int ratio = mRecorder.getMaxAmplitude() / BASE;
            int db = 0;// 分贝
            if (ratio > 1)
                db = (int) (20 * Math.log10(ratio));
            db = db / 15;
            if (db == 0) {
                db++;
            } else if (db > 6) {
                db = 6;
            }
            return db;
        }

        return 1;
    }

    // 释放资源
    public void release() {
        // 严格按照api流程进行
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

    }

    // 取消,因为prepare时产生了一个文件，所以cancel方法应该要删除这个文件，
    // 这是与release的方法的区别
    public void cancel() {
        release();
        if (mCurrentFilePathString != null) {
            deleteAudioRecorderFile(mCurrentFilePathString);
            mCurrentFilePathString = null;
        }

    }

    /***
     * 删除录音文件
     * @param filePath
     */
    public void deleteAudioRecorderFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public String getCurrentFilePath() {
        // TODO Auto-generated method stub
        return mCurrentFilePathString;
    }

    /**
     * 回调函数，准备完毕，准备好后，button才会开始显示录音框
     *
     * @author nickming
     */
    public interface AudioStageListener {
        void wellPrepared();
    }

}
