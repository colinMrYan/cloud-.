package com.inspur.emmcloud.util;

import android.widget.ImageView;

import com.inspur.emmcloud.R;

public class TagColorUtils {
	public static void setTagColorImg(ImageView imageView,String color){
		if (color.equals("ORANGE")) {
			imageView.setImageResource(R.drawable.icon_mession_orange);
		} else if (color.equals("BLUE")) {
			imageView.setImageResource(R.drawable.icon_mession_blue);
		} else if (color.equals("GREEN")) {
			imageView.setImageResource(R.drawable.icon_mession_green);
		} else if (color.equals("PINK")) {
			imageView.setImageResource(R.drawable.icon_mession_red);
		} else if (color.equals("YELLOW")) {
			imageView.setImageResource(R.drawable.icon_mession_yellow);
		} else if (color.equals("PURPLE")) {
			imageView.setImageResource(R.drawable.icon_mession_purple);
		}
	}
}
