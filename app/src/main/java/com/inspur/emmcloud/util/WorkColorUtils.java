package com.inspur.emmcloud.util;

import android.view.View;

import com.inspur.emmcloud.R;

public class WorkColorUtils {

	public static void showDayOfWeek(View view,
			int countDown) {
		if (countDown < 0) {
			countDown = 0;
		}
		switch (countDown%7) {
		case 1:
			view.setBackgroundResource(R.drawable.sunday);
			break;
		case 2:
			view.setBackgroundResource(R.drawable.monday);
			break;
		case 3:
			view.setBackgroundResource(R.drawable.tuesday);
			break;
		case 4:
			view.setBackgroundResource(R.drawable.wednesday);
			break;
		case 5:
			view.setBackgroundResource(R.drawable.thursday);
			break;
		case 6:
			view.setBackgroundResource(R.drawable.friday);
			break;
		case 7:
			view.setBackgroundResource(R.drawable.saturday);
			break;
		default:
			view.setBackgroundResource(R.drawable.saturday);
			break;
		}
	}
}
