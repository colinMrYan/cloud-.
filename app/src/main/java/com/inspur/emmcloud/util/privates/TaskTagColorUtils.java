package com.inspur.emmcloud.util.privates;

import android.widget.ImageView;

import com.inspur.emmcloud.R;

public class TaskTagColorUtils {
    public static final String TASK_TAG_COLOR_ORANGE = "ORANGE";
    public static final String TASK_TAG_COLOR_BLUE = "BLUE";
    public static final String TASK_TAG_COLOR_GREEN = "GREEN";
    public static final String TASK_TAG_COLOR_PINK = "PINK";
    public static final String TASK_TAG_COLOR_YELLOW = "YELLOW";
    public static final String TASK_TAG_COLOR_PURPLE = "PURPLE";

    public static void setTagColorImg(ImageView imageView, String color) {
        if (color.equals(TASK_TAG_COLOR_ORANGE)) {
            imageView.setImageResource(R.drawable.icon_orange_circle);
        } else if (color.equals(TASK_TAG_COLOR_BLUE)) {
            imageView.setImageResource(R.drawable.icon_blue_circle);
        } else if (color.equals(TASK_TAG_COLOR_GREEN)) {
            imageView.setImageResource(R.drawable.icon_green_circle);
        } else if (color.equals(TASK_TAG_COLOR_PINK)) {
            imageView.setImageResource(R.drawable.icon_brown_circle);
        } else if (color.equals(TASK_TAG_COLOR_YELLOW)) {
            imageView.setImageResource(R.drawable.icon_yellow_circle);
        } else if (color.equals(TASK_TAG_COLOR_PURPLE)) {
            imageView.setImageResource(R.drawable.icon_purple_circle);
        }
    }
}
