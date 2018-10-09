package com.inspur.emmcloud.util.common.audioformat;

import android.content.Context;
import android.os.Handler;

import com.inspur.emmcloud.util.common.StringUtils;

import java.io.File;

import jaygoo.library.converter.Mp3Converter;

/**
 * 用于录音格式从Wav向mp3转化
 * Created by yufuchang on 2018/10/9.
 */

public class AndroidMp3ConvertUtils {
    private static final int SIMPLE_RATE = 8000;//采样率
    private static final int CHANNEL = 1;//声道数
    private static final int MODE = 0;//模式，默认0
    private static final int OUT_BIT_RATE = 32;//输入比特率
    private static final int QUALITY = 5;//音频质量0~9,0质量最好体积最大，9质量最差体积最小
    private Context context;
    private String wavPath = "", mp3Path = "";
    private long wavFileSize = 0;
    private AndroidMp3ConvertCallback callback;

    private AndroidMp3ConvertUtils(Context context) {
        this.context = context;
        Mp3Converter.init(SIMPLE_RATE, CHANNEL, MODE, SIMPLE_RATE, OUT_BIT_RATE, QUALITY);
    }

    /**
     * 仿照AndroidAudioConverter转化类的构建方式
     *
     * @param context
     * @return
     */
    public static AndroidMp3ConvertUtils with(Context context) {
        return new AndroidMp3ConvertUtils(context);
    }

    /**
     * 设置回调函数
     *
     * @param callBack
     * @return
     */
    public AndroidMp3ConvertUtils setCallBack(AndroidMp3ConvertCallback callBack) {
        this.callback = callBack;
        return this;
    }

    /**
     * 设置wav和mp3文件路径
     *
     * @param wavFilePath
     * @param mp3FilePath
     * @return
     */
    public AndroidMp3ConvertUtils setWavPathAndMp3Path(String wavFilePath, String mp3FilePath) {
        this.wavPath = wavFilePath;
        this.mp3Path = mp3FilePath;
        return this;
    }

    /**
     * wav到mp3转码
     */
    public void startConvert() {
        //检查回调，wav和mp3路径
        if (callback == null || StringUtils.isBlank(wavPath) || StringUtils.isBlank(mp3Path)) {
            Exception e = new Exception("check callback and path exception");
            callback.onFailure(e);
            return;
        }
        try {
            wavFileSize = new File(wavPath).length();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Mp3Converter.convertMp3(wavPath, mp3Path);
                }
            }).start();
            handler.postDelayed(runnable, 20);
        } catch (Exception e) {
            callback.onFailure(e);
            e.printStackTrace();
        }
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long bytes = Mp3Converter.getConvertBytes();
            float progress = (100f * bytes / wavFileSize);
            if (bytes == -1) {
                progress = 100;
                callback.onSuccess(mp3Path);
            }
            if (handler != null && progress != 100) {
                handler.postDelayed(this, 20);
            }
        }
    };

    public interface AndroidMp3ConvertCallback {
        void onSuccess(String mp3FilePath);

        void onFailure(Exception e);
    }
}
