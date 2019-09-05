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
            resId = R.drawable.baselib_file_type_folder;
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
                resId = R.drawable.baselib_file_type_unkown;
            }
        }
        return resId;
    }

    private static Integer getResourceIdByFormat(String format) {
        Integer resId = null;
        switch (format) {
            case "application/zip":
                resId = R.drawable.baselib_file_type_zip;
                break;
            case "text/plain":
                resId = R.drawable.baselib_file_type_txt;
                break;
            case "application/pdf":
                resId = R.drawable.baselib_file_type_pdf;
                break;
            case "text/html":
                resId = R.drawable.ic_volume_file_typ_html;
                break;
            case "text/xml":
                resId = R.drawable.ic_volume_file_typ_xsl;
                break;
            default:
                if (format.startsWith("image/")) {
                    resId = R.drawable.baselib_file_type_img;
                } else if (format.startsWith("video/")) {
                    resId = R.drawable.baselib_file_type_video;
                } else if (format.startsWith("audio/")) {
                    resId = R.drawable.baselib_file_type_audio;
                } else if (format.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml")) {
                    resId = R.drawable.baselib_file_type_word;
                } else if (format.startsWith("application/vnd.openxmlformats-officedocument.presentationml")) {
                    resId = R.drawable.baselib_file_type_ppt;
                } else if (format.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml")) {
                    resId = R.drawable.baselib_file_type_excel;
                }
                break;
        }
        return resId;
    }
}
