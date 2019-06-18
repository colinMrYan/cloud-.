package com.inspur.emmcloud.web.plugin.datetime;

import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.widget.TimePicker;

import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 设置时间
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class TimePickerService extends ImpPlugin {

    private Calendar calendar;
    // js页面传递过来的默认时间
    private String defaultTime;
    // 设置时间的回调函数
    private String timeFunction;
    // 设置时间的格式
    private String timeFormat;
    private String time;
    private int hour;
    private int minute;
    // 时间对话框主题
    private int theme = android.R.style.Theme_Holo_Light_Dialog_NoActionBar;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("open".equals(action)) {
            openTimePicker(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    /**
     * 设置时间
     *
     * @param paramsObject
     * @return
     */
    @SuppressWarnings("deprecation")
    @TargetApi(11)
    public String openTimePicker(JSONObject paramsObject) {
        // 解析json串获取到传递过来的参数和回调函数
        try {
            if (!paramsObject.isNull("defaultTime"))
                defaultTime = paramsObject.getString("defaultTime");
            if (!paramsObject.isNull("callback"))
                timeFunction = paramsObject.getString("callback");
            if (!paramsObject.isNull("format"))
                timeFormat = paramsObject.getString("format");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        time = null;
        if (defaultTime == null || "null".equals(defaultTime)) {
            // 获取当前的时间
            calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            // 24小时模式
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        } else {
            String[] str;
            if (defaultTime.contains(":")) {
                str = defaultTime.split(":");
            } else {
                return null;
            }
            hour = Integer.parseInt(str[0]);
            minute = Integer.parseInt(str[1]);
        }

        // 显示设置时间对话框
        TimePickerDialog timeDlg = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minuteOfDay) {
                        hour = hourOfDay;
                        minute = minuteOfDay;
                        calendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
                        calendar.set(calendar.getTime().getYear(), calendar
                                .getTime().getMonth(), calendar.getTime()
                                .getDay(), hour, minute);
                        String time = sdf.format(calendar.getTime());
                        // 设置回调js页面函数
                        jsCallback(timeFunction, time);
                    }
                }, hour, minute, true);
        timeDlg.setTitle("时间设置");
        timeDlg.setButton2("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        timeDlg.show();
        return time;
    }

    @Override
    public void onDestroy() {

    }

}
