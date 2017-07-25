package com.inspur.emmcloud.ui.work;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.WorkSetting;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.WorkSettingCacheUtils;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.dragsortlistview.DragSortListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/7/25.
 */

public class WorkSettingActivity extends BaseActivity {
    private DragSortListView listView;
    private List<WorkSetting> workSettingList =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_setting);
        listView = (DragSortListView)findViewById(R.id.work_setting_list);
        workSettingList = WorkSettingCacheUtils.getAllWorkSettingList(this);
        listView.setAdapter(adapter);
    }

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
            convertView = LayoutInflater.from(WorkSettingActivity.this).inflate(R.layout.work_setting_item_view,null);
            WorkSetting workSetting = workSettingList.get(position);
            ((TextView)convertView.findViewById(R.id.text)).setText(workSetting.getName());
            ((SwitchView)convertView.findViewById(R.id.open_switch)).setOpened(workSetting.isOpen());
            return convertView;
        }
    };

    public void onClick(View v){
        finish();
    }
}
