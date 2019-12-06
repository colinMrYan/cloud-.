package com.inspur.emmcloud.web.plugin.screenshot;

import android.content.Intent;

import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/12/6.
 */

public class ScreenshotService extends ImpPlugin {
    @Override
    public void execute(String action, JSONObject paramsObject) {
        if (action.equals("do")) {
            screenshot();
        } else {
            showCallIMPMethodErrorDlg();
        }
    }


    private void screenshot() {
        String screenshotImgPath = ScreenshotUtil.screenshot(getActivity());
        if (screenshotImgPath != null) {
            getActivity().startActivity(new Intent(getActivity(), IMGEditActivity.class)
                    .putExtra(IMGEditActivity.EXTRA_IS_COVER_ORIGIN, true)
                    .putExtra(IMGEditActivity.EXTRA_IMAGE_PATH, screenshotImgPath));
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
