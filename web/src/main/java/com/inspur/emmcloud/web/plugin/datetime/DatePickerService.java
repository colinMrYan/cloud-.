package com.inspur.emmcloud.web.plugin.datetime;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * 设置日期和时间
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class DatePickerService extends ImpPlugin {

    private Calendar calendar;
    private int year;
    private int month;
    private int day;
    private String date1;
    // js页面传递过来的默认日期
    private String defaultDate;
    // 设置日期的回调函数
    private String functName;
    // js页面传递过来的日期格式
    private String dateFormat;

    // 日期对话框
    private int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
    private int theme = android.R.style.Theme_Holo_Light_Dialog_NoActionBar;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        // 打开日期组件
        if ("open".equals(action)) {
            open(paramsObject);
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
     * @param
     */
    @SuppressWarnings("deprecation")
    private void open(JSONObject paramsObject) {
        // 解析json串获取到传递过来的参数和回调函数
        try {
            if (!paramsObject.isNull("defaultDate"))
                defaultDate = paramsObject.getString("defaultDate");
            if (!paramsObject.isNull("callback"))
                functName = paramsObject.getString("callback");
            if (!paramsObject.isNull("format"))
                dateFormat = paramsObject.getString("format");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (defaultDate == null || "null".equals(defaultDate)) {
            // 获取当前的年月日
            calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            day = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            // 解析默认的日期
            String[] str;
            if (defaultDate.contains("-")) {
                str = defaultDate.split("-");
            } else if (defaultDate.contains("/")) {
                str = defaultDate.split("/");
            } else {
                return;
            }
            year = Integer.parseInt(str[0]);
            month = Integer.parseInt(str[1]);
            day = Integer.parseInt(str[2]);
        }

        if (sdkVersion < 12) {
            theme = 0;
        }

        // 显示设置日期对话框
        DatePickerDialog dateDlg = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int yearNum,
                                          int monthOfYear, int dayOfMonth) {
                        month = monthOfYear;
                        year = yearNum;
                        day = dayOfMonth;
                        calendar = Calendar.getInstance();
                        calendar.set(year, month, day);
                        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                        date1 = sdf.format(calendar.getTime());
                        jsCallback(functName, date1);
                    }
                }, year, month - 1, day);
        dateDlg.setTitle("日期设置");
        dateDlg.setButton2("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dateDlg.show();
        // 根据日期的设置格式，设置日期对话框
        DatePicker dp = findDatePicker((ViewGroup) dateDlg.getWindow()
                .getDecorView());
        if (dp != null && !dateFormat.contains("dd")) {
            String model = Build.MODEL;
            // 魅族手机设置
            if (model.contains("MEIZU") || model.startsWith("m")) {
                if (dp.getChildAt(0) != null
                        && ((ViewGroup) dp.getChildAt(0)).getChildAt(1) != null
                        && ((ViewGroup) ((ViewGroup) dp.getChildAt(0))
                        .getChildAt(1)).getChildAt(4) != null) {
                    ((ViewGroup) ((ViewGroup) dp.getChildAt(0)).getChildAt(1))
                            .getChildAt(4).setVisibility(View.GONE);
                }
            }
            // htc手机
            else if (model.contains("htc")) {
                // 获取最前一位的宽度
                int width1 = ((ViewGroup) dp.getChildAt(0)).getChildAt(0)
                        .getLayoutParams().width;
                // 获取最后一位的宽度
                int width2 = ((ViewGroup) dp.getChildAt(0)).getChildAt(2)
                        .getLayoutParams().width;
                // 判断是否是年-月-日的显示方式
                if (width1 > width2) {
                    ((ViewGroup) dp.getChildAt(0)).getChildAt(2).setVisibility(
                            View.GONE);
                } else {
                    ((ViewGroup) dp.getChildAt(0)).getChildAt(0).setVisibility(
                            View.GONE);
                }
            }
            // 其他品牌手机，版本大于3.0
            else {
                ((ViewGroup) ((ViewGroup) dp.getChildAt(0)).getChildAt(0))
                        .getChildAt(2).setVisibility(View.GONE);
            }
        }
    }

    /**
     * 从当前Dialog中查找DatePicker子控件
     *
     * @param group
     * @return
     */
    private DatePicker findDatePicker(ViewGroup group) {
        if (group != null) {
            for (int i = 0, j = group.getChildCount(); i < j; i++) {
                View child = group.getChildAt(i);
                if (child instanceof DatePicker) {
                    return (DatePicker) child;
                } else if (child instanceof ViewGroup) {
                    DatePicker result = findDatePicker((ViewGroup) child);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {

    }

}
