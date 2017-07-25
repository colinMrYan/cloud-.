package com.inspur.emmcloud.ui.work;

import android.os.Bundle;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.dragsortlistview.DragSortListView;

/**
 * Created by chenmch on 2017/7/25.
 */

public class WorkSettingActivity extends BaseActivity {
    private DragSortListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_setting);
        listView = (DragSortListView)findViewById(R.id.work_setting_list);
    }
}
