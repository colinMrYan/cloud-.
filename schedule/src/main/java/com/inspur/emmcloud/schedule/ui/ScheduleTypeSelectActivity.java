package com.inspur.emmcloud.schedule.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.R2;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;
import com.inspur.emmcloud.schedule.util.CalendarUtils;
import com.inspur.emmcloud.schedule.util.ScheduleCalendarCacheUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/7/25.
 */

public class ScheduleTypeSelectActivity extends BaseActivity {

    public static final String SCHEDULE_AC_TYPE = "schedule_ac_type";

    @BindView(R2.id.lv_schedule_types)
    ListView scheduleTypesListView;

    private List<ScheduleCalendar> scheduleTypeList = new ArrayList<>();
    private ScheduleTypeAdapter scheduleTypeAdapter;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        boolean isEnableExchange = PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, false);
        scheduleTypeList = ScheduleCalendarCacheUtils.getScheduleCalendarList(BaseApplication.getInstance());
        cleanState();
        scheduleTypeAdapter = new ScheduleTypeAdapter();
        scheduleTypesListView.setAdapter(scheduleTypeAdapter);
        scheduleTypesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cleanState();
                setStateByIndex(i);
                scheduleTypeAdapter.notifyDataSetChanged();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable(SCHEDULE_AC_TYPE, scheduleTypeList.get(i));
                setResult(RESULT_OK, intent.putExtras(bundle));
                finish();
            }
        });

        findViewById(R.id.ibt_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        String scheduleTypeId = getIntent().getStringExtra(SCHEDULE_AC_TYPE);
        if (!StringUtils.isBlank(scheduleTypeId)) {
            setStateByName(scheduleTypeId);
        }
    }


    /**
     * 清除状态
     */
    private void cleanState() {
        for (int i = 0; i < scheduleTypeList.size(); i++) {
            scheduleTypeList.get(i).setOpen(false);
        }
    }

    /**
     * 设置状态
     */
    private void setStateByIndex(int i) {
        scheduleTypeList.get(i).setOpen(true);
    }

    /**
     * 设置状态
     */
    private void setStateByName(String name) {
        for (int i = 0; i < scheduleTypeList.size(); i++) {
            if (scheduleTypeList.get(i).getId().equals(name)) {
                scheduleTypeList.get(i).setOpen(true);
                break;
            }
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.schedule_activity_type_select;
    }

    private class ScheduleTypeAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return scheduleTypeList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(ScheduleTypeSelectActivity.this).inflate(R.layout.schedule_item_schedule_type, null);
            ((TextView) (view.findViewById(R.id.tv_schedule_type_name))).setText(CalendarUtils.getScheduleCalendarShowName(scheduleTypeList.get(i)));
            (view.findViewById(R.id.iv_schedule_type_state)).setVisibility(scheduleTypeList.get(i).isOpen() ? View.VISIBLE : View.GONE);
            return view;
        }
    }

}
