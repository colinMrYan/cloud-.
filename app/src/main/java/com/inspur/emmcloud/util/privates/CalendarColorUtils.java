package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.LogUtils;

public class CalendarColorUtils {
    public static int getColor(Context context, String color) {
        int displayColor = -1;
        if (color.equals("PINK")) {
            displayColor = context.getResources().getColor(R.color.cal_pink);
        } else if (color.equals("ORANGE")) {
            displayColor = context.getResources().getColor(R.color.cal_orange);
        } else if (color.equals("YELLOW")) {
            displayColor = context.getResources().getColor(R.color.cal_yellow);
        } else if (color.equals("GREEN")) {
            displayColor = context.getResources().getColor(R.color.cal_green);
        } else if (color.equals("BLUE")) {
            displayColor = context.getResources().getColor(R.color.cal_blue);
        } else if (color.equals("PURPLE")) {
            displayColor = context.getResources().getColor(R.color.cal_pink);
        } else if (color.equals("BROWN")) {
            displayColor = context.getResources().getColor(R.color.cal_purple);
        } else {
            displayColor = context.getResources().getColor(R.color.transparent);
        }
        return displayColor;
    }

	public static int getColorCircleImage( String color) {
		int displayColor = -1;
		if (color.equals( "ORANGE" )) {
			displayColor = R.drawable.icon_orange_circle;
		} else if (color.equals( "YELLOW" )) {
			displayColor = R.drawable.icon_yellow_circle;
		} else if (color.equals( "GREEN" )) {
			displayColor = R.drawable.icon_green_circle;
		} else if (color.equals( "BLUE" )) {
			displayColor = R.drawable.icon_blue_circle;
		} else if (color.equals( "PURPLE" )) {
			displayColor = R.drawable.icon_purple_circle;
		} else if (color.equals( "BROWN" )) {
			displayColor = R.drawable.icon_brown_circle;
		} else {
			displayColor = R.drawable.icon_blue_circle;
		}
		LogUtils.LbcDebug( "displayColor"+displayColor );
		return displayColor;
	}
}
