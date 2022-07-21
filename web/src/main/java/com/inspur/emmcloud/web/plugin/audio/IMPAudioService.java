package com.inspur.emmcloud.web.plugin.audio;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.componentservice.web.WebMediaCallbackImpl;
import com.inspur.emmcloud.componentservice.web.WebMediaService;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.ui.AudioDialogChooseManager;

import org.json.JSONException;
import org.json.JSONObject;

public class IMPAudioService extends ImpPlugin {
    WebMediaService service = null;
    private String successCal, failCal;
    private AudioDialogChooseManager audioDialogChooseManager;
    private static final int VOICE_MESSAGE = 4;
    private boolean saveToLocal = false;
    private String uploadPath;
    //录音时间
    private float durationTime = 0;
    private int volumeSize = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VOICE_MESSAGE:
                    if (durationTime < 60.0) {
                        audioDialogChooseManager.updateVoiceLevelAndDurationTime(msg.arg1, msg.arg2);
                    } else {
                        service.stopAudioRecord(null);
                        audioDialogChooseManager.dismissRecordingDialog();
                        upload();
                    }
                    break;
            }

        }
    };
    @Override
    public void execute(String action, JSONObject paramsObject) {
        successCal = JSONUtils.getString(paramsObject, "success", "");
        failCal = JSONUtils.getString(paramsObject, "fail", "");
        Router router = Router.getInstance();
        if (router.getService(WebMediaService.class) == null) return;
        if (service == null) {
            service = router.getService(WebMediaService.class);
        }
        if (audioDialogChooseManager == null) {
            audioDialogChooseManager = new AudioDialogChooseManager(getActivity(), this);
        }
        switch (action) {
            case "recordingAudio":
                JSONObject optionObj = paramsObject.optJSONObject("options");
                uploadPath = optionObj.optString("id");
                saveToLocal = optionObj.optBoolean("local");
                if (TextUtils.isEmpty(uploadPath)) {
                    return;
                }
                audioDialogChooseManager.showRecordingDialog();
                service.startAudioRecord(handler);
                audioDialogChooseManager.recording();
                break;
            case "playAudio":
                // 音频的播放、停止
                JSONObject optionObjPlay = paramsObject.optJSONObject("options");
                String path = optionObjPlay.optString("path");
                String playAction = optionObjPlay.optString("value");
                // 是否绝对路径，true: 绝对路径，false: 相对路径
                Boolean absolute = optionObjPlay.optBoolean("absolute");
                switch (playAction) {
                    case "start":
                        if (StringUtils.isBlank(path)) {
                            return;
                        }
                        if (!absolute) {
                            path = getRelativeFilePath();
                        }
                        service.playAudio(path);
                        break;
                    case "stop":
                        service.stopAudio(path);
                        break;
                }
                break;
            default:
                showCallIMPMethodErrorDlg();
                break;

        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return null;
    }

    @Override
    public void onDestroy() {
        service.stopAudio(null);
        durationTime = 0;
    }

    public void cancel() {
        service.stopAudioRecord(null);
        durationTime = 0;
    }

    private String getRelativeFilePath() {
        return MyAppConfig.LOCAL_CACHE_VOICE_PATH + "/";
    }

    public void upload() {
        durationTime = 0;
        if (service == null) return;
        service.stopAudioRecord(new WebMediaCallbackImpl() {
            @Override
            public void onRecordEnd(String resourceLocalPath) throws JSONException {
                if (saveToLocal) {
                    JSONObject json = new JSONObject();
                    json.put("status", 1);
                    JSONObject result = new JSONObject();
                    result.put("path", resourceLocalPath);
                    json.put("result", result);
                    jsCallback(successCal, json);
                } else {
                    if (resourceLocalPath != null) {
                        service.uploadAudioFile(uploadPath, resourceLocalPath, new WebMediaCallbackImpl() {
                                    @Override
                                    public void onSuccess(String webPath) {
                                        try {
                                            JSONObject json = new JSONObject();
                                            JSONObject result = new JSONObject();
                                            result.put("path", webPath);
                                            json.put("status", 1);
                                            json.put("result", result);
                                            jsCallback(successCal, json);
                                        } catch (JSONException e) {
                                            jsCallback(failCal, e.getMessage());
                                        }
                                    }


                                    @Override
                                    public void onFail() {
                                        jsCallback(failCal, getFragmentContext().getString(R.string.web_video_record_fail));
                                    }
                                }
                        );
                    }
                }

            }
        });
    }
}
