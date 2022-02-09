package com.inspur.emmcloud.web.plugin.audio;

import android.content.Intent;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.componentservice.web.WebMediaService;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.filetransfer.FilePathUtils;
import com.inspur.emmcloud.web.ui.ImpFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class AudioService extends ImpPlugin {

    private String successCb, failCb;
    private String recordAudioFilePath;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        Router router = Router.getInstance();
        if (router.getService(WebMediaService.class) == null) return;
        WebMediaService service = router.getService(WebMediaService.class);
        switch (action) {
            case "recordAudio":
                JSONObject optionObj = paramsObject.optJSONObject("options");
                String recordAction = optionObj.optString("value");
                switch (recordAction) {
                    case "start":
                        service.startAudioRecord();
                        break;
                    case "stop":
                        recordAudioFilePath = service.stopAudioRecord();
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
            case "upload":
                try {
                    final JSONObject optionsObj = paramsObject.getJSONObject("options");
                    String fileName = optionsObj.optString("id");
                    service.uploadAudioFile();
                } catch (JSONException e) {
                    e.printStackTrace();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImpFragment.REQUEST_CODE_RECORD_VIDEO) {
            if (data != null && data.getData() != null) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("path", recordAudioFilePath);
                    jsCallback(successCb, json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    JSONObject json = new JSONObject();
                    json.put("errorMessage", getFragmentContext().getString(R.string.web_video_record_fail));
                    jsCallback(failCb, json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
