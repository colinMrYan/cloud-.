package com.inspur.emmcloud.util;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Volume.VolumeFile;

/**
 * Created by chenmch on 2017/11/22.
 */

public class VolumeFileIconUtils {
    public static int getIconResId(VolumeFile volumeFile) {
        int resId = -1;
        if (volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
            resId = R.drawable.ic_volume_file_typ_forder;
        } else {
            String format = volumeFile.getFormat();
            switch (format){
                case "application/zip":
                    resId = R.drawable.ic_volume_file_typ_zip;
                    break;
                case "text/plain":
                    resId = R.drawable.ic_volume_file_typ_txt;
                    break;
                case "application/pdf":
                    resId = R.drawable.ic_volume_file_typ_pdf;
                    break;
                case "text/html":
                    resId = R.drawable.ic_volume_file_typ_html;
                    break;
                case "text/xml":
                    resId = R.drawable.ic_volume_file_typ_xsl;
                    break;
                default:
                    if (format.startsWith("image/")) {
                        resId = R.drawable.ic_volume_file_typ_img;
                    } else if (format.startsWith("video/")) {
                        resId = R.drawable.ic_volume_file_typ_video;
                    } else if (format.startsWith("audio/")) {
                        resId = R.drawable.ic_volume_file_typ_audio;
                    } else if (format.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml")) {
                        resId = R.drawable.ic_volume_file_typ_word;
                    } else if (format.startsWith("application/vnd.openxmlformats-officedocument.presentationml")) {
                        resId = R.drawable.ic_volume_file_typ_ppt;
                    } else if (format.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml")) {
                        resId = R.drawable.ic_volume_file_typ_excel;
                    } else {
                        resId = R.drawable.ic_volume_file_typ_unknown;
                    }
                    break;
            }

        }
        return resId;
    }
}
