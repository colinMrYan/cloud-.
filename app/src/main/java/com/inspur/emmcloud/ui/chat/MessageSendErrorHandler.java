package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import androidx.annotation.NonNull;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.bean.EventMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageSendErrorHandler {

    static void handlerErrorMessage(@NonNull Context context, @NonNull EventMessage eventMessage){
        if(eventMessage.getStatus() != 400){
            return;
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(eventMessage.getContent());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        String errorCode = jsonObject.optString("errorCode");
        switch (errorCode){
            case "SensitiveWordsMatchedError":
                ToastUtils.show(context.getString(R.string.sensitive_words_matched_error));
                break;
            case "ChannelSilentError":
                ToastUtils.show(context.getString(R.string.channel_silent_error));
                break;
            default:
                break;
        }
    }

}
