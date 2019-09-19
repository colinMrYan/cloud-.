package com.inspur.emmcloud.ui.appcenter.volume;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.PopupWindow;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;

import java.io.Serializable;
import java.util.List;

/**
 * Created by libaochao on 2019/9/19.
 */

public class VolumeOpeartionUtils {

    private Context context;
    private PopupWindow popupWindow;

    public VolumeOpeartionUtils(Context context) {
        this.context = context;
    }

    public void showPopupWindow() {

    }

    public void copyFile(List<VolumeFile> volumeFileList, int requestCode, String currentDirAbsolutePath, Volume volume) {
        if (volumeFileList.size() > 0) {
            Intent intent = new Intent(context, VolumeFileLocationSelectActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("volume", volume);
            bundle.putSerializable("volumeFileList", (Serializable) volumeFileList);
            bundle.putString("title", context.getString(R.string.clouddriver_select_copy_position));
            bundle.putBoolean("isFunctionCopy", true);
            bundle.putString("operationFileDirAbsolutePath", currentDirAbsolutePath);
            intent.putExtras(bundle);
            ((Activity) context).startActivityForResult(intent, requestCode);
        }
    }
}
