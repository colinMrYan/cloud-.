package com.inspur.emmcloud.util.privates;


import android.os.Handler;
import android.os.Looper;

import com.inspur.emmcloud.interf.ResultCallback;
import com.inspur.emmcloud.util.common.LogUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

/**
 * Created by chenmch on 2018/11/15.
 */

public class AudioFormatUtils {
    public static void Mp3ToWav(final String mp3filepath, final String pcmfilepath, final ResultCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayInputStream bais = null;
                AudioInputStream sourceAIS = null;
                AudioInputStream mp3AIS = null;
                AudioInputStream pcmAIS = null;
                try {

                    File mp3 = new File(mp3filepath);

                    AudioFormat targetFormat = null;
                    try {
                        MpegAudioFileReader mp = new MpegAudioFileReader();
                        AudioInputStream in = mp.getAudioInputStream(mp3);
                        //targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 16, 1, 2, 16000, false);
                        AudioFormat sourceFormat = in.getFormat();
                        //AudioFormat mp3tFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 16, sourceFormat.getChannels(), sourceFormat.getChannels() * 2, sourceFormat.getSampleRate(), false);


                        targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 16, 1, 2, sourceFormat.getSampleRate(), false);
                        sourceAIS = AudioSystem.getAudioInputStream(targetFormat, in);
                        AudioSystem.write(sourceAIS, AudioFileFormat.Type.WAVE, new File(pcmfilepath));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFail();
                            }
                        });

                    }
                } finally {
                    try {
                        if (bais != null) {
                            bais.close();
                        }
                        if (sourceAIS != null) {
                            sourceAIS.close();
                        }
                        if (mp3AIS != null) {
                            mp3AIS.close();
                        }
                        if (pcmAIS != null) {
                            pcmAIS.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }

    /**
     * 将文件转成字节流
     *
     * @param filePath
     * @return
     */
    private static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.jasonDebug("size=" + buffer.length);
        return buffer;
    }

}
