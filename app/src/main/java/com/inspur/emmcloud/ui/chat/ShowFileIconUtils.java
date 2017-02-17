package com.inspur.emmcloud.ui.chat;

import android.widget.ImageView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.ImageDisplayUtils;

public class ShowFileIconUtils {

	/**
	 * 展示文件卡片上的图标
	 * @param imageDisplayUtils
	 * @param fileName
	 * @param convertView
	 */
	public static void showFileIcon(ImageDisplayUtils imageDisplayUtils,String fileName,ImageView iconImageView) {
		int imageName = 0;
		if (fileName.endsWith("doc") || fileName.endsWith("docx")) {
			imageName = R.drawable.icon_file_word;
		} else if (fileName.endsWith("xls") || fileName.endsWith("xlsx")) {
			imageName = R.drawable.icon_file_excel;
		} else if (fileName.endsWith("ppt") || fileName.endsWith("pptx")) {
			imageName = R.drawable.icon_file_ppt;
		} else if (fileName.endsWith("pdf")) {
			imageName = R.drawable.icon_file_pdf;
		} else if (fileName.endsWith("txt")) {
			imageName = R.drawable.icon_txt;
		} else if (fileName.endsWith("zip")) {
			imageName = R.drawable.icon_file_zip;
		} else if (fileName.endsWith("rar")) {
			imageName = R.drawable.icon_file_rar;
		} else if (fileName.contains("jpg") || fileName.contains("png")) {
			imageName = R.drawable.icon_file_photos;
		} else {
			imageName = R.drawable.icon_file_unknown;
		}
		imageDisplayUtils.display(iconImageView, "drawable://"+imageName);
	}
}
