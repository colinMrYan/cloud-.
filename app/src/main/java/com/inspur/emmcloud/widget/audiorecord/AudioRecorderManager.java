package com.inspur.emmcloud.widget.audiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

/**
 * 录音模块代码
 */
public class AudioRecorderManager {

    //音频输入-麦克风
    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025，16000所有Android设备都支持
    public final static int AUDIO_SAMPLE_RATE = 16000;  //44.1KHz,普遍使用的频率
    //录音音频的放大倍数
    private final static int VOICE_ENLARGE_TIMES = 1;
    //管理类的引用
    private static AudioRecorderManager mInstance;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    //rawAudioFilePath ，麦克风
    private String rawAudioFilePath = "";
    //wavAudioFilePath可播放的音频文件
    private String wavAudioFilePath = "";
    //中间文件
    private String pcmAudioFilePath = "";
    //录音工具
    private AudioRecord audioRecord;
    //正在录制的标志
    private boolean isRecording = false;
    //音量
    private int volume = 0;
    //持续录制的时间
    private long duration = 0;
    //开始时间
    private long beginTime = 0;
    private AudioDataCallBack callBack;

    private AudioRecorderManager() {
    }

    public static AudioRecorderManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioRecorderManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioRecorderManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 开始录制
     *
     * @return
     */
    public void startRecord() {
        try {
            if (audioRecord != null) {
                audioRecord.startRecording();
                // 让录制状态为true
                isRecording = true;
                beginTime = System.currentTimeMillis();
                // 开启音频文件写入线程
                new Thread(new AudioRecordThread()).start();
            }
        } catch (Exception e) {
            callBack.onWavAudioPrepareState(AudioRecordErrorCode.E_ERROR);
            e.printStackTrace();
        }

    }

    /**
     * 准备
     *
     * @return
     */
    public void prepareWavAudioRecord() {
        //判断是否有外部存储设备sdcard
        if (isSdcardExit()) {
            if (isRecording) {
                callBack.onWavAudioPrepareState(AudioRecordErrorCode.E_STATE_RECODING);
                return;
            } else {
                if (audioRecord == null) {
                    createAudioRecord();
                }
                callBack.onWavAudioPrepareState(AudioRecordErrorCode.SUCCESS);
            }
        } else {
            callBack.onWavAudioPrepareState(AudioRecordErrorCode.E_NOSDCARD);
            return;
        }
    }

    /**
     * 获取持续时间
     *
     * @return
     */
    public float getDuration() {
        return duration / 1000;
    }

    /**
     * 获取录制状态
     *
     * @return
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * 停止录音和写文件
     */
    public void stopRecord() {
        close();
    }

    /**
     * 重置变量
     */
    private void reset() {
        isRecording = false;
        volume = 0;
    }

    /**
     * 关闭，对内使用
     */
    private void close() {
        try {
            reset();
            if (audioRecord != null) {
                isRecording = false;//停止文件写入
                if (audioRecord.getState() != AudioRecord.STATE_UNINITIALIZED) {
                    audioRecord.release();//释放资源
                }
                audioRecord = null;
            }
            beginTime = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建AudioRecord
     */
    private void createAudioRecord() {
        // 获取音频文件路径
        String fileName = AppUtils.generalFileName();
        rawAudioFilePath = getRawFilePath() + fileName + ".raw";
        pcmAudioFilePath = getRawFilePath() + fileName + ".pcm";
        wavAudioFilePath = getWavFilePath() + fileName + ".wav";
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        // 创建AudioRecord对象
        audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
    }

    /**
     * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
     * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
     * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
     */
    private void writeData2File() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audioData = new byte[bufferSizeInBytes];
        FileOutputStream fos = null;
        int readSize = 0;
        try {
            File dir = new File(MyAppConfig.LOCAL_CACHE_VOICE_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(rawAudioFilePath);
            if (file.exists()) {
                file.delete();
            }
            File pcmfile = new File(pcmAudioFilePath);
            if (pcmfile.exists()) {
                pcmfile.delete(); //如果存在删除
            }

            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
            boolean isHasData = false;
            while (isRecording == true) {
                if (audioRecord == null) {
                    callBack.onWavAudioPrepareState(AudioRecordErrorCode.E_ERROR);
                    return;
                }
                readSize = audioRecord.read(audioData, 0, bufferSizeInBytes);
                volume = getVolumeLevel(audioData);
                for (int i = 0; i < audioData.length; i++) {
                    audioData[i] = (byte) (audioData[i] * VOICE_ENLARGE_TIMES);
                }
                if (AudioRecord.ERROR_INVALID_OPERATION != readSize && fos != null) {
                    isHasData = true;
                    fos.write(audioData);
                } else {
                    if (!isHasData) {
                        isRecording = false;
                        callBack.onWavAudioPrepareState(AudioRecordErrorCode.E_ERROR);
                    }
                }
                duration = System.currentTimeMillis() - beginTime;
                DecimalFormat decimalFormat = new DecimalFormat("##0.0");
                String time = decimalFormat.format(duration / 1000f);
                callBack.onDataChange(volume, Float.parseFloat(time));
            }
            if (fos != null) {
                fos.close();// 关闭写入流
            }
            isHasData = false;
        } catch (Exception e) {
            LogUtils.YfcDebug("发生异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取音量的等级非分贝分级算法
     *
     * @param audioData
     * @return
     */
    private int getVolumeLevel(byte[] audioData) {
        int voiceLevel = 0;
        if (audioData != null && audioData.length > 0) {
            voiceLevel = calculateVolume(audioData);
        }
        voiceLevel = voiceLevel / 3;
        if (voiceLevel == 0) {
            voiceLevel++;
        } else if (voiceLevel > 6) {
            voiceLevel = 6;
        }
        return voiceLevel;
    }

    /**
     * 计算音量
     *
     * @param buffer
     * @return
     */
    private int calculateVolume(byte[] buffer) {
        double sumVolume = 0.0;
        double avgVolume = 0.0;
        int volume = 0;
        for (int i = 0; i < buffer.length; i += 2) {
            int v1 = buffer[i] & 0xFF;
            int v2 = buffer[i + 1] & 0xFF;
            int temp = v1 + (v2 << 8);// 小端
            if (temp >= 0x8000) {
                temp = 0xffff - temp;
            }
            sumVolume += Math.abs(temp);
        }
        avgVolume = sumVolume / buffer.length / 2;
        volume = (int) Math.log10(1 + avgVolume) * 10;
        return volume;
    }

    // 这里得到可播放的音频文件
    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = AUDIO_SAMPLE_RATE;
        int channels = 1;
        long byteRate = 16 * AUDIO_SAMPLE_RATE * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
     * 自己特有的头文件。
     */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate) throws Exception {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);

    }

    /**
     * 获取麦克风输入的原始音频流文件路径
     *
     * @return
     */
    private String getRawFilePath() {
        return MyAppConfig.LOCAL_CACHE_VOICE_PATH + "/";
    }

    /**
     * 获取编码后的WAV格式音频文件路径
     *
     * @return
     */
    private String getWavFilePath() {
        return MyAppConfig.LOCAL_CACHE_VOICE_PATH + "/";
    }

    /**
     * 判断是否有外部存储设备sdcard
     *
     * @return true | false
     */
    private boolean isSdcardExit() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取wav文件路径
     *
     * @return
     */
    public String getCurrentFilePath() {
        // TODO Auto-generated method stub
        return rawAudioFilePath;
    }

    /**
     * 持续时间，音量回调
     *
     * @param callBack
     */
    public void setCallBack(AudioDataCallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * 给AudioRecordButton返回数据的回调接口
     */
    public interface AudioDataCallBack {
        void onDataChange(int volume, float duration);

        void onWavAudioPrepareState(int state);
    }

    /**
     * 准备一个线程执行数据操作
     */
    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeData2File();//往文件中写入裸数据
            /**初始文件的语音降噪和语音增强算法在此添加**/
            // copyWaveFile(rawAudioFilePath, wavAudioFilePath);//给裸数据加上头文件
        }
    }
}
