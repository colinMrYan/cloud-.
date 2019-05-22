package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.inspur.emmcloud.bean.system.VoiceResult;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import static com.iflytek.cloud.ErrorCode.ERROR_AUDIO_RECORD;


/**
 * Created by yufuchang on 2018/1/18.
 * 讯飞听写转写的不同
 * ①使用场景：      听写主要应用于人机对话，可支持实时及已录制音频，如输入法、语音搜索，转写更自然地贴近日常的对话和演讲，仅支持已录制音频；
 * ②SDK平台：      听写可支持Android\IOS\Linux\Windows\Java，转写只支持Java；
 * ③支持语言：      听写可支持中英文及部分方言，转写暂时仅支持中文普通话；
 * ④支持的音频时长： 听写为一分钟以内，转写为五小时以内；
 * ⑤音频格式：      听写必须是采样率为8KHz或16KHz，位长16bit，单声道的wav或pcm，转写支持的音频格式更多，具体请看官网
 * ⑥收费方式：      听写按照交互次数收费，前期提供一定的免费次数供试用 ，转写及按时时长收费，前期为每个帐号提供5个小时的免费时长供试用
 * 目前使用的方式为听写
 */

public class Voice2StringMessageUtils {
    public static final int MSG_FROM_XUNFEI = 1;
    public static final int MSG_FROM_CUSTOM = 2;
    public static final int MSG_XUNFEI_PERMISSION_ERROR = 3;
    public static final int MSG_XUNFEI_ERROR = 4;
    private static final int VOICE_FROM_XUNFEI = 0;
    private static final int VOICE_FROM_LOCAL_FILE = 1;
    // 语音听写对象
    private SpeechRecognizer speechRecognizer;
    // 用HashMap存储听写结果
    private HashMap<String, String> iatResultMap = new LinkedHashMap<String, String>();
    // 引擎类型
    private String engineType = SpeechConstant.TYPE_CLOUD;
    //上下文
    private Context context;
    //结果监听
    private RecognizerListener recognizerListener;
    //结果回调
    private OnVoiceResultCallback onVoiceResultCallback;
    //初始化监听器，监听是否初始化成功
    private InitListener initListener;

    private float durationTime = 0;
    private String voiceFilePath = "";
    private int voiceState = -1;
    private int audioSimpleRate = 16000;

    public Voice2StringMessageUtils(Context context) {
        this.context = context;
        initListeners();
    }

    /**
     * 启动听写
     */
    public void startVoiceListening() {
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        speechRecognizer = SpeechRecognizer.createRecognizer(context, initListener);
        setParam(VOICE_FROM_XUNFEI);
        speechRecognizer.startListening(recognizerListener);
        voiceState = MSG_FROM_XUNFEI;
    }

    public void setAudioSimpleRate(int audioSimpleRate) {
        this.audioSimpleRate = audioSimpleRate;
    }

    /**
     * 通过音频文件启动听写
     * 以后需要发送语音时可以单独录制一段语音存到sd卡当做文件发送
     */
    public void startVoiceListeningByVoiceFile(float seconds, String voiceFilePath) {
        this.durationTime = seconds;
        this.voiceFilePath = voiceFilePath;
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        speechRecognizer = SpeechRecognizer.createRecognizer(context, initListener);
        setParam(VOICE_FROM_LOCAL_FILE);
        //注释掉的这几句是读取文件听写文字的
        speechRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        speechRecognizer.setParameter(SpeechConstant.ASR_PTT, "1");
        speechRecognizer.startListening(recognizerListener);
        byte[] audioData = FileUtils.readAudioFileFromSDcard(voiceFilePath);
        if (audioData != null) {
            speechRecognizer.writeAudio(audioData, 0, audioData.length);
        }
        speechRecognizer.stopListening();
        voiceState = MSG_FROM_CUSTOM;
    }


    /**
     * 参数设置
     *
     * @return
     */
    private void setParam(int type) {
//        // 清空参数
        speechRecognizer.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        speechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, engineType);
        // 设置返回结果格式
        speechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");

        //IOS未设置
//        //网络转写超时设置
//        speechRecognizer.setParameter(SpeechConstant.NET_TIMEOUT, "8000");

        String language = LanguageManager.getInstance().getCurrentAppLanguage();
        switch (language) {
            case "zh-Hans":
                // 设置语言
                speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                // 设置语言区域
                speechRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin");
                break;
            case "en":
                // 设置语言
                speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "en_us");
                break;
            default:
                // 设置语言
                speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                // 设置语言区域
                speechRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin");
                break;
        }
        //来自本地录音文件时，前后端时间都设置为60s，其他情况使用默认值
        if (type == VOICE_FROM_LOCAL_FILE) {
            // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
            speechRecognizer.setParameter(SpeechConstant.VAD_BOS, "60000");
            // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
            speechRecognizer.setParameter(SpeechConstant.VAD_EOS, "60000");
        }
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        speechRecognizer.setParameter(SpeechConstant.ASR_PTT, "0");
        //根据IOS参数新加参数
        speechRecognizer.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "-1");
        speechRecognizer.setParameter(SpeechConstant.SAMPLE_RATE, audioSimpleRate + "");
        speechRecognizer.setParameter(SpeechConstant.DOMAIN, "iat");
        speechRecognizer.setParameter(SpeechConstant.PARAMS, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        speechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        speechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "");
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
        initListener = new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    //初始化失败，停止录音
                    stopListening();
                }
            }
        };

        recognizerListener = new RecognizerListener() {
            @Override
            public void onBeginOfSpeech() {
                // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
                onVoiceResultCallback.onVoiceStart();
            }

            @Override
            public void onError(SpeechError error) {
                LogUtils.YfcDebug("错误：" + error.getErrorCode() + error.getErrorDescription());
                VoiceResult voiceResult = new VoiceResult();
                voiceResult.setXunFeiError(MSG_XUNFEI_ERROR);
                if (error.getErrorCode() == ERROR_AUDIO_RECORD) {
                    voiceResult.setXunFeiPermissionError(MSG_XUNFEI_PERMISSION_ERROR);
                }
                voiceResult.setMsgState(voiceState);
                voiceResult.setResults("");
                voiceResult.setSeconds(durationTime);
                voiceResult.setFilePath(voiceFilePath);
                onVoiceResultCallback.onVoiceResultError(voiceResult);
                //返回错误停止录音
                stopListening();
            }

            @Override
            public void onEndOfSpeech() {
                // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
                onVoiceResultCallback.onVoiceFinish();
            }

            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                addListeningResult2Map(results);
                if (isLast) {
                    String voiceWords = getLastListeningResult();
                    VoiceResult voiceResult = new VoiceResult();
                    voiceResult.setResults(voiceWords);
                    voiceResult.setMsgState(voiceState);
                    voiceResult.setSeconds(durationTime);
                    voiceResult.setFilePath(voiceFilePath);
                    //最后的结果
                    onVoiceResultCallback.onVoiceResultSuccess(voiceResult, isLast);
                }
            }

            //音量值0~30
            @Override
            public void onVolumeChanged(int volume, byte[] data) {
                onVoiceResultCallback.onVoiceLevelChange(volume);
            }

            @Override
            public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
                // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
                // 若使用本地能力，会话id为null
                //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                //		Log.d(TAG, "session id =" + sid);
                //	}
            }
        };
    }

    /**
     * 向map中加入一个解析结果
     *
     * @param results
     */
    private void addListeningResult2Map(RecognizerResult results) {
        String content = XFJsonParser.parseIatResult(results.getResultString());
        String sn = JSONUtils.getString(results.getResultString(), "sn", "");
        iatResultMap.put(sn, content);
    }

    /**
     * 获取最终解析结果
     */
    private String getLastListeningResult() {
        StringBuffer resultBuffer = new StringBuffer();
        Set<String> iatResultSet = iatResultMap.keySet();
        if (iatResultSet != null) {
            for (String key : iatResultMap.keySet()) {
                resultBuffer.append(iatResultMap.get(key));
            }
        }
        iatResultMap.clear();
        return resultBuffer.toString();
    }

    /**
     * 在调用处回收资源的方法
     *
     * @return
     */
    public SpeechRecognizer getSpeechRecognizer() {
        return speechRecognizer;
    }

    /**
     * 处理结果的回调接口，返回开始，结束，音量，解析文字四个结果
     *
     * @param onVoiceResultCallback
     */
    public void setOnVoiceResultCallback(OnVoiceResultCallback onVoiceResultCallback) {
        this.onVoiceResultCallback = onVoiceResultCallback;
    }

    /**
     * 停止监听
     */
    public void stopListening() {
        if (speechRecognizer != null && speechRecognizer.isListening()) {
            speechRecognizer.stopListening();
        }
    }

}

