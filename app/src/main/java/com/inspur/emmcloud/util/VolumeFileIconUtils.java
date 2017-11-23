package com.inspur.emmcloud.util;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Volume.VolumeFile;

/**
 * Created by chenmch on 2017/11/22.
 */

public class VolumeFileIconUtils {
    public static int getIconResId(VolumeFile volumeFile) {
        int resId = -1;
        if (volumeFile.getType().equals("directory")) {
            resId = R.drawable.ic_volume_file_typ_forder;
        } else {
            switch (volumeFile.getFormat()) {
                case ".png":
                case ".jpg":
                    resId = R.drawable.ic_volume_file_typ_img;
                    break;

                default:
                    resId = R.drawable.ic_volume_file_typ_unknown;
                    break;
            }
        }
        return resId;
    }
}
