package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.R;

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
}
