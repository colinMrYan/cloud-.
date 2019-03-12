package com.inspur.emmcloud.ui.work;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.work.WorkSetting;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.cache.WorkSettingCacheUtils;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.dragsortlistview.DragSortController;
import com.inspur.emmcloud.widget.dragsortlistview.DragSortListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/7/25.
 */

public class WorkSettingActivity extends BaseActivity {
    private static final String TYPE_CALENDAR = "calendar";
    private static final String TYPE_APPROVAL = "approval";
    private static final String TYPE_MEETING = "meeting";
    private static final String TYPE_TASK = "task";
    private static final String TYPE_DATE = "date";
    private DragSortListView listView;
    private List<WorkSetting> workSettingList = new ArrayList<>();
    private boolean isChangeSetting = false;
    private BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return workSettingList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(WorkSettingActivity.this).inflate(R.layout.work_setting_item_view, null);
            WorkSetting workSetting = workSettingList.get(position);
            String id = workSetting.getId();
            if (id.equals(TYPE_CALENDAR)) {
                ((TextView) convertView.findViewById(R.id.text)).setText(R.string.work_calendar_text);
            } else if (id.equals(TYPE_MEETING)) {
                ((TextView) convertView.findViewById(R.id.text)).setText(R.string.meeting);
            } else {
                ((TextView) convertView.findViewById(R.id.text)).setText(R.string.work_task_text);
            }
            SwitchView switchView = (SwitchView) convertView.findViewById(R.id.open_switch);
            switchView.setOpened(workSetting.isOpen());
            switchView.setOnStateChangedListener(new StateChangedListener(workSetting));
            return convertView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_setting);
        boolean isShowDate = PreferencesByUserAndTanentUtils.getBoolean(getApplicationContext(), Constant.PREF_WORK_INFO_BAR_OPEN, true);
        SwitchView switchView = (SwitchView) findViewById(R.id.date_open_switch);
        switchView.setOpened(isShowDate);
        switchView.setOnStateChangedListener(new StateChangedListener(null));
        listView = (DragSortListView) findViewById(R.id.work_setting_list);
        workSettingList = WorkSettingCacheUtils.getAllWorkSettingList(this);
        listView.setAdapter(adapter);
        DragSortController controller = new DragSortController(listView);
        controller.setDragHandleId(R.id.handle_img);
        controller.setSortEnabled(true);
        controller.setDragInitMode(0);
        listView.setFloatViewManager(controller);
        listView.setOnTouchListener(controller);
        listView.setDragEnabled(true);
        listView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    isChangeSetting = true;
                    WorkSetting item = workSettingList.get(from);
                    workSettingList.remove(item);
                    workSettingList.add(to, item);
                    adapter.notifyDataSetChanged();
                    for (int i = 0; i < workSettingList.size(); i++) {
                        WorkSetting workSetting = workSettingList.get(i);
                        workSetting.setSort(i);
                    }
                    WorkSettingCacheUtils.saveWorkSettingList(getApplicationContext(), workSettingList);
                }
            }
        });
    }

    public void onClick(View v) {
        if (isChangeSetting) {
            setResult(RESULT_OK);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isChangeSetting) {
            setResult(RESULT_OK);
        }
        finish();
    }

    private class StateChangedListener implements SwitchView.OnStateChangedListener {
        private WorkSetting workSetting;

        public StateChangedListener(WorkSetting workSetting) {
            this.workSetting = workSetting;
        }

        @Override
        public void toggleToOn(View view) {
            // TODO Auto-generated method stub
            isChangeSetting = true;
            if (workSetting == null) {
                PreferencesByUserAndTanentUtils.putBoolean(getApplicationContext(), Constant.PREF_WORK_INFO_BAR_OPEN, true);
            } else {
                workSetting.setOpen(true);
                WorkSettingCacheUtils.saveWorkSetting(getApplicationContext(), workSetting);
            }
            ((SwitchView) view).setOpened(true);
        }

        @Override
        public void toggleToOff(View view) {
            // TODO Auto-generated method stub
            isChangeSetting = true;
            if (workSetting == null) {
                PreferencesByUserAndTanentUtils.putBoolean(getApplicationContext(), Constant.PREF_WORK_INFO_BAR_OPEN, false);
            } else {
                workSetting.setOpen(false);
                WorkSettingCacheUtils.saveWorkSetting(getApplicationContext(), workSetting);
            }
            ((SwitchView) view).setOpened(false);

        }
    }

}
