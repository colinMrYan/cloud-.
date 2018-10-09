package com.inspur.emmcloud.util.common.audioformat;

import android.content.Context;
import android.os.Handler;

import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.StringUtils;

import java.io.File;

import jaygoo.library.converter.Mp3Converter;

/**
 * 用于录音格式从Wav向mp3转化
 * Created by yufuchang on 2018/10/9.
 */

public class AndroidMp3ConvertUtils {
    private static final int SIMPLE_RATE = 16000;
    private static final int CHANNEL = 2;
    private static final int MODE = 0;
    private static final int OUT_BIT_RATE = 96;
    private static final int QUALITY = 7;
    private Context context;
    private String wavPath = "", mp3Path = "";
    private long wavFileSize = 0;
    private AndroidMp3ConvertCallback callback;

    private AndroidMp3ConvertUtils(Context context) {
        this.context = context;
        Mp3Converter.init(SIMPLE_RATE, CHANNEL, MODE, SIMPLE_RATE, OUT_BIT_RATE, QUALITY);
    }

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
                LogUtils.YfcDebug("转化完成");
                callback.onSuccess(new File(mp3Path));
            }
            if (handler != null && progress != 100) {
                handler.postDelayed(this, 20);
            }
        }
    };

    public interface AndroidMp3ConvertCallback {
        void onSuccess(File mp3File);

        void onFailure(Exception e);
    }
}
