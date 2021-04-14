package com.inspur.emmcloud.baselib.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.NumberPicker;

import com.inspur.baselib.R;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;

import java.lang.reflect.Field;
import java.util.Calendar;

/**
 * Created by libaochao on 2019/7/16.
 */

public class DatePickerSpinnerDialog {
    private Context context;
    private AlertDialog.Builder alertDialog;
    private DatePicker datePicker;
    private DatePickerDialogInterface datePickerDialogInterface;
    private Calendar calendar;

    public DatePickerSpinnerDialog(Context context) {
        this.context = context;
        calendar = Calendar.getInstance();
    }

    public void setDataTimePickerDialogListener(DatePickerDialogInterface timePickerDialogListener) {
        datePickerDialogInterface = timePickerDialogListener;
    }

    /**
     * 初始化Dialog
     */
    private View initPicker() {
        View inflate = LayoutInflater.from(context).inflate(PreferencesUtils.getInt(context, "app_theme_num_v1", 0) != 3 ? R.layout.basewidget_dialog_date_picker : R.layout.basewidget_dialog_date_picker_dark, null);
        datePicker = inflate.findViewById(R.id.date_picker);
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                calendar.set(Calendar.YEAR, i);
                calendar.set(Calendar.MONTH, i1);
                calendar.set(Calendar.DAY_OF_MONTH, i2);
            }
        });
        return inflate;
    }

    /**
     * 展示Dialog
     */
    public void showDatePickerDialog() {
        View view = initPicker();
        initPickerTimeDialog(view);
        getNumSpinnerPicker();
        alertDialog.show();
    }

    /**
     * 初始化Dialog*/
    private void initPickerTimeDialog(View view) {
        alertDialog = new AlertDialog.Builder(context, R.style.DateTimeAlertDialog);
        alertDialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                datePickerDialogInterface.negativeListener(calendar);
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                datePickerDialogInterface.positiveListener(calendar);
                dialog.dismiss();
            }
        });
        alertDialog.setView(view);
    }

    /**
     * 反射的方式获取NumberPicker
     */
    private void getNumSpinnerPicker() {
        try {
            //month、day、year
            Resources systemResources = Resources.getSystem();
            int monthNumberPickerId = systemResources.getIdentifier("month", "id", "android");
            int dayNumberPickerId = systemResources.getIdentifier("day", "id", "android");
            int yearNumberPickerId = systemResources.getIdentifier("year", "id", "android");
            NumberPicker monthNumberPicker = datePicker.findViewById(monthNumberPickerId);
            NumberPicker dayNumberPicker = datePicker.findViewById(dayNumberPickerId);
            NumberPicker yearNumberPicker = datePicker.findViewById(yearNumberPickerId);
            setNumberPickerDivider(monthNumberPicker);
            setNumberPickerDivider(dayNumberPicker);
            setNumberPickerDivider(yearNumberPicker);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Divider 的颜色
     */
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

    public interface DatePickerDialogInterface {
        public void positiveListener(Calendar calendar);

        public void negativeListener(Calendar calendar);
    }
}
