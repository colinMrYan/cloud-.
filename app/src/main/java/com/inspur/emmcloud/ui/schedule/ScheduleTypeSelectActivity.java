package com.inspur.emmcloud.ui.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/7/25.
 */

public class ScheduleTypeSelectActivity extends BaseActivity {


    @BindView(R.id.lv_schedule_types)
    ListView scheduleTypesListView;

    private List<ScheduleType> scheduleTypeList = new ArrayList<>();
    private ScheduleTypeAdapter scheduleTypeAdapter;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        scheduleTypeList.add(new ScheduleType("云+日程", false));
        scheduleTypeList.add(new ScheduleType("云+会议", false));
        scheduleTypeList.add(new ScheduleType("libaochao@inspur.com", false));
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
                bundle.putString("schedule_type", scheduleTypeList.get(i).getTypeName());
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
        String scheduleType = getIntent().getStringExtra("schedule_type");
        if (!StringUtils.isBlank(scheduleType)) {
            setStateByName(scheduleType);
        }
    }


    /**
     * 清除状态
     */
    private void cleanState() {
        for (int i = 0; i < scheduleTypeList.size(); i++) {
            scheduleTypeList.get(i).typeState = false;
        }
    }

    /**
     * 设置状态
     */
    private void setStateByIndex(int i) {
        scheduleTypeList.get(i).setTypeState(true);
    }

    /**
     * 设置状态
     */
    private void setStateByName(String name) {
        for (int i = 0; i < scheduleTypeList.size(); i++) {
            if (scheduleTypeList.get(i).getTypeName().equals(name)) {
                scheduleTypeList.get(i).setTypeState(true);
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
            ((TextView) (view.findViewById(R.id.tv_schedule_type_name))).setText(scheduleTypeList.get(i).getTypeName());
            (view.findViewById(R.id.iv_schedule_type_state)).setVisibility(scheduleTypeList.get(i).isTypeState() ? View.VISIBLE : View.GONE);
            return view;
        }
    }

    private class ScheduleType {

        private String typeName;
        private boolean typeState = false;


        public ScheduleType() {
        }

        public ScheduleType(String typeName, boolean state) {
            this.typeName = typeName;
            this.typeState = state;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public boolean isTypeState() {
            return typeState;
        }

        public void setTypeState(boolean typeState) {
            this.typeState = typeState;
        }
    }
}
