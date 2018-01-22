package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ToastUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * Created by yufuchang on 2018/1/18.
 */

public class Voice2StringMessageUtils {
    // 语音听写对象
    private SpeechRecognizer speechRecognizer;
    // 用HashMap存储听写结果
    private HashMap<String, String> iatResultMap = new LinkedHashMap<String, String>();
    // 引擎类型
    private String engineType = SpeechConstant.TYPE_CLOUD;
    //上下文
    private Activity activity;
    //结果监听
    private RecognizerListener recognizerListener;
    //结果回调
    private OnVoiceResultCallback onVoiceResultCallback;

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener;

    public Voice2StringMessageUtils(Activity activity){
        this.activity = activity;
        initListeners();
    }

    /**
     * 启动听写
     */
    public void startVoiceListening(){
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        speechRecognizer = SpeechRecognizer.createRecognizer(activity, mInitListener);
        setParam();
        speechRecognizer.startListening(recognizerListener);
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        speechRecognizer.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        speechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, engineType);
        // 设置返回结果格式
        speechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
        String lag = PreferencesUtils.getString(activity,"iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "en_us");
            speechRecognizer.setParameter(SpeechConstant.ACCENT, null);
        } else {
            // 设置语言
            speechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            speechRecognizer.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        speechRecognizer.setParameter(SpeechConstant.VAD_BOS, PreferencesUtils.getString(activity,"iat_vadbos_preference", "3000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        speechRecognizer.setParameter(SpeechConstant.VAD_EOS, PreferencesUtils.getString(activity,"iat_vadeos_preference", "3000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        speechRecognizer.setParameter(SpeechConstant.ASR_PTT, PreferencesUtils.getString(activity,"iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        speechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        speechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
        mInitListener = new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    LogUtils.YfcDebug("初始化失败："+code);
                }else{
                    LogUtils.YfcDebug("初始化成功");
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
                showTip(error.getPlainDescription(true));
            }

            @Override
            public void onEndOfSpeech() {
                // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
                onVoiceResultCallback.onVoiceFinish();
            }

            @Override
            public void onResult(RecognizerResult results, boolean isLast) {
                parseIatAndReturnStringResult(results);
                if (isLast) {
                    //最后的结果
                    onVoiceResultCallback.onVoiceResult(parseIatAndReturnStringResult(results),isLast);
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
     * 解析结果
     * @param results
     */
    private String parseIatAndReturnStringResult(RecognizerResult results) {
        String text = XFJsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (Exception e) {
            e.printStackTrace();
        }
        iatResultMap.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : iatResultMap.keySet()) {
            resultBuffer.append(iatResultMap.get(key));
        }
        ToastUtils.show(activity,text);
        return resultBuffer.toString();
    }

    /**
     * 在调用处回收资源的方法
     * @return
     */
    public SpeechRecognizer getSpeechRecognizer() {
        return speechRecognizer;
    }

    /**
     * 处理结果的回调接口，返回开始，结束，音量，解析文字四个结果
     * @param onVoiceResultCallback
     */
    public void setOnVoiceResultCallback(OnVoiceResultCallback onVoiceResultCallback) {
        this.onVoiceResultCallback = onVoiceResultCallback;
    }

    /**
     * 提示信息
     * @param str
     */
    private void showTip(final String str) {
        ToastUtils.show(activity,str);
    }

}
