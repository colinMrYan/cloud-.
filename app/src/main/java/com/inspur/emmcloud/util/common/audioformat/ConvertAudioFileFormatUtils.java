package com.inspur.emmcloud.util.common.audioformat;

import android.content.Context;

import com.inspur.emmcloud.util.common.LogUtils;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

/**
 * Created by yufuchang on 2018/9/12.
 * 音频文件转为指定格式
 * 支持如下格式
 * AAC
 * MP3
 * M4A
 * WMA
 * WAV
 * FLAC
 * 需要一个回调IConvertCallback
 */

public class ConvertAudioFileFormatUtils {

    private static ConvertAudioFileFormatUtils convertAudioFileFormatUtils;

    public static ConvertAudioFileFormatUtils getInstance(){
        if(convertAudioFileFormatUtils == null){
            synchronized (ConvertAudioFileFormatUtils.class){
                if(convertAudioFileFormatUtils == null){
                    convertAudioFileFormatUtils = new ConvertAudioFileFormatUtils();
                }
            }
        }
        return convertAudioFileFormatUtils;
    }

    private ConvertAudioFileFormatUtils(){}

    /**
     * 转为指定格式传入file
     * @param context
     * @param sourceFile
     * @param audioFormat
     * @param callback
     */
    public void convertAudioFile2SpecifiedFormat (Context context,File sourceFile,AudioFormat audioFormat,IConvertCallback callback) {
        if(!sourceFile.exists()){
            return;
        }
        startConvert(context,sourceFile,audioFormat,callback);
    }

    /**
     * 转为指定格式，传入文件路径
     * @param context
     * @param sourceFilePath
     * @param audioFormat
     * @param callback
     */
    public void convertAudioFile2SpecifiedFormat (Context context,String sourceFilePath,AudioFormat audioFormat,IConvertCallback callback) {
        File sourceFile = new File(sourceFilePath);
        if(!sourceFile.exists()){
            return;
        }
        startConvert(context,sourceFile,audioFormat,callback);
    }

    /**
     * 转为指定格式的音频文件，不用传callBack
     * @param context
     * @param sourceFilePath
     * @param audioFormat
     */
    public void convertAudioFile2SpecifiedFormat(Context context,String sourceFilePath,AudioFormat audioFormat){
        File sourceFile = new File(sourceFilePath);
        if(!sourceFile.exists()){
            return;
        }
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File file) {
                LogUtils.YfcDebug("转化成功");
            }

            @Override
            public void onFailure(Exception e) {
                LogUtils.YfcDebug("转化失败");
            }
        };
        startConvert(context,sourceFile,audioFormat,callback);
    }

    /**
     * 转为指定格式的音频文件，不用传callBack
     * @param context
     * @param sourceFile
     * @param audioFormat
     */
    public void convertAudioFile2SpecifiedFormat(Context context,File sourceFile,AudioFormat audioFormat){
        if(!sourceFile.exists()){
            return;
        }
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File file) {
                LogUtils.YfcDebug("转化成功");
            }

            @Override
            public void onFailure(Exception e) {
                LogUtils.YfcDebug("转化失败");
            }
        };
        startConvert(context,sourceFile,audioFormat,callback);
    }

    /**
     * 转换方法
     * @param context
     * @param sourceFile
     * @param audioFormat
     * @param callback
     */
    private void startConvert(Context context, File sourceFile, AudioFormat audioFormat, IConvertCallback callback) {
        AndroidAudioConverter.with(context)
                // Your current audio file
                .setFile(sourceFile)
                // Your desired audio format
                .setFormat(audioFormat)
                // An callback to know when conversion is finished
                .setCallback(callback)
                // Start conversion
                .convert();
    }
}
