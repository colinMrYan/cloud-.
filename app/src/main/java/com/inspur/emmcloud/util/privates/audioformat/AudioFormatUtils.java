package com.inspur.emmcloud.util.privates.audioformat;


import com.inspur.emmcloud.interf.ResultCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

/**
 * Created by chenmch on 2018/11/15.
 */

public class AudioFormatUtils {
    public static void Mp3ToWav(final String mp3Filepath, final String wavFilepath, final ResultCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//
//                Converter aConverter = new Converter();
//                try {
//                    aConverter.convert(mp3Filepath, wavFilepath);
//                } catch (JavaLayerException e) {
//                    e.printStackTrace();
//                }


//                AudioInputStream in = null;
//                AudioInputStream out = null;
//                try {
//                    File mp3 = new File(mp3Filepath);
//                    MpegAudioFileReader mp = new MpegAudioFileReader();
//                    in = mp.getAudioInputStream(mp3);
//                    AudioFormat targetFormat = new AudioFormat(8000, 16, 1, true, false);
//                    out = AudioSystem.getAudioInputStream(targetFormat, in);
//                    AudioSystem.write(out, AudioFileFormat.Type.WAVE, new File(wavFilepath));
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            callback.onSuccess();
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    if (callback != null) {
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                callback.onFail();
//                            }
//                        });
//
//                    }
//                } finally {
//                    try {
//                        if (in != null) {
//                            in.close();
//                        }
//                        if (out != null) {
//                            out.close();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }


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
        return buffer;
    }

}
