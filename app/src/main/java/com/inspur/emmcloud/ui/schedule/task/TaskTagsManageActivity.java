package com.inspur.emmcloud.ui.schedule.task;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.work.TaskColorTag;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/4/8.
 */
@ContentView(R.layout.activity_task_manage)
public class TaskTagsManageActivity extends BaseActivity {
    @ViewInject(R.id.lv_task_manage_tags)
    private ListView taskManageTagsList;

    private List<TaskColorTag> taskColorTags;
    private TaskTagsAdapter taskTagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    /***/
    private void initData() {
        taskColorTags = new ArrayList<>();
        taskTagsAdapter = new TaskTagsAdapter();
        taskManageTagsList.setAdapter(taskTagsAdapter);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                break;
            case R.id.ibt_add:
                break;
            default:
                break;
        }
    }

    /***/
    private class ManageTagsHolder {
        public TextView tagNameText;
        public ImageView tagFlagImage;
    }

    /***/
    public class TaskTagsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 0;
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
            ManageTagsHolder manageTagsHolder;
            if (null == view) {
                view = View.inflate(TaskTagsManageActivity.this, R.layout.task_manage_item, null);
                manageTagsHolder = new ManageTagsHolder();
                manageTagsHolder.tagNameText = view.findViewById(R.id.tv_task_tag_name);
                manageTagsHolder.tagFlagImage = view.findViewById(R.id.iv_task_tag_flag);
                view.setTag(manageTagsHolder);
            } else {
                manageTagsHolder = (ManageTagsHolder) view.getTag();
            }
            int colorId = CalendarColorUtils.getColorCircleImage(taskColorTags.get(i).getColor());
            manageTagsHolder.tagNameText.setText(taskColorTags.get(i).getTitle());
            manageTagsHolder.tagFlagImage.setImageResource(colorId);
            return view;
        }
    }

}
