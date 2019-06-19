package com.inspur.emmcloud.baselib.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.inspur.baselib.R;

import java.lang.reflect.Field;

/**
 * Created by libaochao on 2019/6/19.
 */

public class TimePickerDialog {
    private Context context;
    private android.app.TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private int hoursOfDay;
    private int minutes;
    private boolean is24HoursOfDay;
    private AlertDialog.Builder alertDialog;
    private TimePicker timePicker;

    public TimePickerDialog(Context context, android.app.TimePickerDialog.OnTimeSetListener onTimeSetListener,
                            int hourOfDay, int minutes, boolean is24HoursOfDay) {
        this.context = context;
        this.onTimeSetListener = onTimeSetListener;
        this.hoursOfDay = hourOfDay;
        this.minutes = minutes;
        this.is24HoursOfDay = is24HoursOfDay;
    }

    public void showTimePickerDialog() {
        View view = initPicker();
        initPickerTimeDialog(view);
        getNumSpinnerPicker();
        alertDialog.show();
    }

    private void getNumSpinnerPicker() {
        Resources systemResources = Resources.getSystem();
        int hourNumberPickerId = systemResources.getIdentifier("hour", "id", "android");
        int minuteNumberPickerId = systemResources.getIdentifier("minute", "id", "android");
        NumberPicker hourNumberPicker = timePicker.findViewById(hourNumberPickerId);
        NumberPicker minuteNumberPicker = timePicker.findViewById(minuteNumberPickerId);
        setNumberPickerDivider(hourNumberPicker);
        setNumberPickerDivider(minuteNumberPicker);

    }

    private void setNumberPickerDivider(NumberPicker numberPicker) {
        NumberPicker picker = numberPicker;
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {  //设置颜色
                pf.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(
                        ContextCompat.getColor(context, R.color.blue_00aaee)); //选择自己喜欢的颜色
                try {
                    pf.set(numberPicker, colorDrawable);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
//            if (pf.getName().equals("mSelectionDividerHeight")) {   //设置高度
//                pf.setAccessible(true);
//                try {
//                    int result = 3;  //要设置的高度
//                    pf.set(picker, result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            }
            picker.invalidate();
        }
    }

    /**
     * 初始化Dialog
     */
    private View initPicker() {
        View inflate = LayoutInflater.from(context).inflate(R.layout.basewidget_dialog_time_picker, null);
        timePicker = inflate.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(is24HoursOfDay);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(hoursOfDay);
            timePicker.setMinute(minutes);
        }

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {  //获取当前选择的时间
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                hoursOfDay = hourOfDay;
                minutes = minute;
            }
        });
        return inflate;
    }

    private void initPickerTimeDialog(View view) {
        alertDialog = new AlertDialog.Builder(context, R.style.DateTimeAlertDialog);
        alertDialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                onTimeSetListener.onTimeSet(timePicker, hoursOfDay, minutes);
                dialog.dismiss();
            }
        });
        alertDialog.setView(view);
    }

}
