package com.inspur.emmcloud.widget.audiorecord;

import android.content.Context;

import com.inspur.emmcloud.R;

/**
 * Created by yufuchang on 2018/9/12.
 */

public class AudioRecordErrorCode {
    public final static int SUCCESS = 1000;
    public final static int E_NOSDCARD = 1001;
    public final static int E_STATE_RECODING = 1002;
    public final static int E_UNKOWN = 1003;
    public final static int E_ERROR = 1004;


    public static String getErrorInfo(Context vContext, int vType) {
        switch (vType) {
            case SUCCESS:
                return "success";
            case E_NOSDCARD:
                return vContext.getResources().getString(R.string.error_no_sdcard);
            case E_STATE_RECODING:
                return vContext.getResources().getString(R.string.error_state_record);
            case E_UNKOWN:
            default:
                return vContext.getResources().getString(R.string.error_unknown);

        }
    }

}
