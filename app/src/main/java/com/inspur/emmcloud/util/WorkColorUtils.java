package com.inspur.emmcloud.util;

import android.widget.ImageView;

import com.inspur.emmcloud.R;

public class WorkColorUtils {

	public static void showDayOfWeek(ImageView imageView, 
			int countDown) {
		if (countDown < 0) {
			countDown = 0;
		}
		switch (countDown%7) {
		case 1:
			imageView.setImageResource(R.drawable.sunday);
			break;
		case 2:
			imageView.setImageResource(R.drawable.monday);
			break;
		case 3:
			imageView.setImageResource(R.drawable.tuesday);
			break;
		case 4:
			imageView.setImageResource(R.drawable.wednesday);
			break;
		case 5:
			imageView.setImageResource(R.drawable.thursday);
			break;
		case 6:
			imageView.setImageResource(R.drawable.friday);
			break;
		case 7:
			imageView.setImageResource(R.drawable.saturday);
			break;
		default:
			imageView.setImageResource(R.drawable.saturday);
			break;
		}
	}
}
