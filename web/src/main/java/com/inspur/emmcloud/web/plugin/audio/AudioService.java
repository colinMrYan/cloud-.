package com.inspur.emmcloud.web.plugin.audio;

import android.util.Log;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.componentservice.web.WebMediaCallbackImpl;
import com.inspur.emmcloud.componentservice.web.WebMediaService;
import com.inspur.emmcloud.componentservice.web.WebMediaCallback;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

public class AudioService extends ImpPlugin {
    WebMediaService service = null;
    private String successCb, failCb;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        Router router = Router.getInstance();
        if (router.getService(WebMediaService.class) == null) return;
        if (service == null) {
            service = router.getService(WebMediaService.class);
        }
        Log.e("printf", "service.id : " + service.getClass());
        switch (action) {
            case "recordAudio":
                JSONObject optionObj = paramsObject.optJSONObject("options");
                String recordAction = optionObj.optString("value");
                //上传接口
                switch (recordAction) {
                    case "start":
                        service.startAudioRecord();
                        break;
                    case "stop":
                        //录制完成后进行上传
                        String uploadPath = optionObj.optString("path");
                        service.stopAudioRecord(new WebMediaCallbackImpl() {
                            @Override
                            public void onRecordEnd(String resourceLocalPath) {
                                if (resourceLocalPath != null) {
                                    service.uploadAudioFile("", resourceLocalPath, new WebMediaCallbackImpl() {
                                                @Override
                                                public void onSuccess(VolumeFile volumeFile) {
                                                    try {
                                                        JSONObject json = new JSONObject();
                                                        json.put("path", JSONUtils.getString(volumeFile.getResource(), "url", ""));
                                                        Log.e("printf","uploadPath : " + JSONUtils.getString(volumeFile.getResource(), "url", ""));
                                                        jsCallback(successCb, json);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                public void onFail() {
                                                    try {
                                                        JSONObject json = new JSONObject();
                                                        json.put("errorMessage", getFragmentContext().getString(R.string.web_video_record_fail));
                                                        jsCallback(failCb, json);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                    );
                                }
                            }
                        });
                        break;
                }
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

    }
}
