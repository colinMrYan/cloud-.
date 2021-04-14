package com.inspur.emmcloud.baselib.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.baselib.R;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.TimePicker.TimePicker;
import com.inspur.emmcloud.baselib.widget.TimePicker.Utils.ConvertUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by libaochao on 2019/3/1.
 */

public class DateTimePickerDialog {
    private Context context;
    private AlertDialog.Builder alertDialog;
    private TimePickerDialogInterface timePickerDialogInterface;
    private DatePicker datePicker;
    private int mTag = 0;
    private RelativeLayout relativeLayout;
    private Calendar resultCalendar;
    private TextView timeTextView;

    public DateTimePickerDialog(Context context) {
        super();
        this.context = context;
        resultCalendar = Calendar.getInstance();
    }

    public void setDataTimePickerDialogListener(TimePickerDialogInterface timePickerDialogListener) {
        timePickerDialogInterface = timePickerDialogListener;
    }

    /**
     * 初始化DatePicker
     *
     * @return
     */
    private View initDatePicker(Boolean isAllDay) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.basewidget_dialog_date_time_picker, null);
        datePicker = inflate.findViewById(R.id.datePicker_no_head);
        resizePicker(datePicker);
        hideDatePickerHeader(datePicker);
        int year = resultCalendar.get(Calendar.YEAR);
        int monthOfYear = resultCalendar.get(Calendar.MONTH);
        int dayOfMonth = resultCalendar.get(Calendar.DAY_OF_MONTH);
        datePicker.init(year, monthOfYear, dayOfMonth, null);
        relativeLayout = inflate.findViewById(R.id.rl_select_time);
        timeTextView = inflate.findViewById(R.id.tv_time);
        String hourMinute = TimeUtils.calendar2FormatString(context, resultCalendar, TimeUtils.FORMAT_HOUR_MINUTE);
        timeTextView.setText(hourMinute);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePicker picker = new TimePicker((Activity) context, TimePicker.HOUR_24, true);
                int currentThemeNo = PreferencesUtils.getInt(context, "app_theme_num_v1", 0);
                picker.setBackgroundColor(Color.parseColor(currentThemeNo != 3 ? "#ECEEF2":"#292929"));
                picker.setIntervalMinutes(true);
                picker.setUseWeight(false);
                picker.setCycleDisable(false);
                picker.setRangeStart(0, 0);//00:00
                picker.setRangeEnd(23, 3);//23:59
                picker.setGravity(Gravity.CENTER);//弹框居中
                picker.setContentPadding(0, 5);
                picker.setSelectedItem(resultCalendar.get(Calendar.HOUR_OF_DAY), resultCalendar.get(Calendar.MINUTE));
                picker.setTopLineVisible(false);
                picker.setTopBackgroundColor(Color.parseColor(currentThemeNo != 3 ? "#ECEEF2":"#292929"));
                picker.setTextPadding(ConvertUtils.toPx((Activity) context, 15));
                picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
                    @Override
                    public void onTimePicked(String hour, String minute) {
                        int hourOfDay = Integer.parseInt(hour);
                        int minutes = Integer.parseInt(minute);
                        resultCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        resultCalendar.set(Calendar.MINUTE, minutes);
                        String sHour = hourOfDay < 10 ? "0" + hourOfDay : "" + hourOfDay;
                        String sMinute = minutes < 10 ? "0" + minutes : "" + minutes;
                        String time = sHour + ":" + sMinute;
                        timeTextView.setText(time);
                    }
                });
                picker.show();
            }
        });
        if (isAllDay) {
            relativeLayout.setVisibility(View.GONE);
        }
        return inflate;
    }

    /**
     * 创建dialog
     *
     * @param view
     */
    private void initDialog(View view) {
        alertDialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                getDatePickerValue();
                timePickerDialogInterface.negativeListener(resultCalendar);
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                getDatePickerValue();
                timePickerDialogInterface.positiveListener(resultCalendar);
                dialog.dismiss();
            }
        });
        alertDialog.setView(view);
    }

    /**
     * 显示日期选择器
     */
    public void showDatePickerDialog(Boolean isAllday, Calendar orgCalendar) {
        mTag = 1;
        resultCalendar = (Calendar) orgCalendar.clone();
        View view = initDatePicker(isAllday);
        alertDialog = new AlertDialog.Builder(context, R.style.DateTimeAlertDialog);
        initDialog(view);
        Dialog dialog = alertDialog.create();

        dialog.show();
    }

    private void showTimePickerDialog() {

    }

    /**
     * 调整numberpicker大小
     */
    private void resizeNumberPicker(NumberPicker np) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 0);
        np.setLayoutParams(params);
    }

    /**
     * 调整FrameLayout大小
     *
     * @param tp
     */
    private void resizePicker(FrameLayout tp) {
        List<NumberPicker> npList = findNumberPicker(tp);
        for (NumberPicker np : npList) {
            resizeNumberPicker(np);
        }
    }

    /**
     * 得到viewGroup里面的numberpicker组件
     *
     * @param viewGroup
     * @return
     */
    private List<NumberPicker> findNumberPicker(ViewGroup viewGroup) {
        List<NumberPicker> npList = new ArrayList<NumberPicker>();
        View child = null;
        if (null != viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                child = viewGroup.getChildAt(i);
                if (child instanceof NumberPicker) {
                    npList.add((NumberPicker) child);
                } else if (child instanceof LinearLayout) {
                    List<NumberPicker> result = findNumberPicker((ViewGroup) child);
                    if (result.size() > 0) {
                        return result;
                    }
                }
            }
        }
        return npList;
    }

    /**
     * 获取日期选择的值
     */
    private void getDatePickerValue() {
        resultCalendar.set(Calendar.YEAR, datePicker.getYear());
        resultCalendar.set(Calendar.MONTH, datePicker.getMonth());
        resultCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
    }

    /**
     * 隐藏DatePickerHeader
     */
    private void hideDatePickerHeader(DatePicker datePicker) {
        ViewGroup rootView = (ViewGroup) datePicker.getChildAt(0);
        if (rootView == null) {
            return;
        }
        View headerView = rootView.getChildAt(0);
        if (headerView == null) {
            return;
        }
        // 5.0+
        int headerId = context.getResources().getIdentifier("day_picker_selector_layout", "id", "android");
        if (headerId == headerView.getId()) {
            headerView.setVisibility(View.GONE);

            ViewGroup.LayoutParams layoutParamsRoot = rootView.getLayoutParams();
            layoutParamsRoot.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            rootView.setLayoutParams(layoutParamsRoot);

            ViewGroup animator = (ViewGroup) rootView.getChildAt(1);
            ViewGroup.LayoutParams layoutParamsAnimator = animator.getLayoutParams();
            layoutParamsAnimator.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            animator.setLayoutParams(layoutParamsAnimator);

            View child = animator.getChildAt(0);
            ViewGroup.LayoutParams layoutParamsChild = child.getLayoutParams();
            layoutParamsChild.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            child.setLayoutParams(layoutParamsChild);
            return;
        }
        // 6.0+
        headerId = context.getResources().getIdentifier("date_picker_header", "id", "android");
        if (headerId == headerView.getId()) {
            headerView.setVisibility(View.GONE);
        }
    }

    public interface TimePickerDialogInterface {
        public void positiveListener(Calendar calendar);

        public void negativeListener(Calendar calendar);
    }
}
