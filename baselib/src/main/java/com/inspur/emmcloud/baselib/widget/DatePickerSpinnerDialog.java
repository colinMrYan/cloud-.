package com.inspur.emmcloud.baselib.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.inspur.baselib.R;

/**
 * Created by libaochao on 2019/7/16.
 */

public class DatePickerSpinnerDialog {
    private Context context;
    private android.app.TimePickerDialog.OnTimeSetListener onTimeSetListener;

    private AlertDialog.Builder alertDialog;
    private DatePicker datePicker;

    public DatePickerSpinnerDialog(Context context) {
        this.context = context;
    }

    /**
     * 初始化Dialog
     */
    private View initPicker() {
        View inflate = LayoutInflater.from(context).inflate(R.layout.basewidget_dialog_date_picker, null);
        datePicker = inflate.findViewById(R.id.date_picker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                }
            });
        }
        return inflate;
    }

    public void showTimePickerDialog() {
        View view = initPicker();
        initPickerTimeDialog(view);
        alertDialog.show();
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
                dialog.dismiss();
            }
        });
        alertDialog.setView(view);
    }


}
