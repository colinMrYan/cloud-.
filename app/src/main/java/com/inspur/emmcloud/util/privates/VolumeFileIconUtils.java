package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;


/**
 * Created by chenmch on 2017/11/22.
 */

public class VolumeFileIconUtils {
    public static int getIconResId(VolumeFile volumeFile) {
        Integer resId = -1;
        if (volumeFile.getType().equals(VolumeFile.FILE_TYPE_DIRECTORY)) {
            resId = R.drawable.ic_volume_file_typ_forder;
        } else {
            String format = volumeFile.getFormat();
            resId = getResourceIdByFormat(format);
            if (resId == null) {
                format = FileUtils.getMimeType(volumeFile.getName());
                if (!StringUtils.isBlank(format)) {
                    resId = getResourceIdByFormat(format);
                }
            }

            if (resId == null) {
                resId = R.drawable.ic_volume_file_typ_unknown;
            }
        }
        return resId;
    }

    private static Integer getResourceIdByFormat(String format) {
        Integer resId = null;
        switch (format) {
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
                }
                break;
        }
        return resId;
    }
}
