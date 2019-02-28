package com.inspur.emmcloud.util.privates;

import android.view.View;

import com.inspur.emmcloud.R;

public class WorkColorUtils {

    public static void showDayOfWeek(View imageView,
                                     int countDown) {
        if (countDown < 0) {
            countDown = 0;
        }
        switch (countDown % 7) {
            case 1:
                imageView.setBackgroundResource(R.drawable.sunday);
                break;
            case 2:
                imageView.setBackgroundResource(R.drawable.monday);
                break;
            case 3:
                imageView.setBackgroundResource(R.drawable.tuesday);
                break;
            case 4:
                imageView.setBackgroundResource(R.drawable.wednesday);
                break;
            case 5:
                imageView.setBackgroundResource(R.drawable.thursday);
                break;
            case 6:
                imageView.setBackgroundResource(R.drawable.friday);
                break;
            case 7:
                imageView.setBackgroundResource(R.drawable.saturday);
                break;
            default:
                imageView.setBackgroundResource(R.drawable.saturday);
                break;
        }
    }
}
