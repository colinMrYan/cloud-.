package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.work.Task;
import com.inspur.emmcloud.util.privates.TimeUtils;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2019/4/3.
 */

public class TaskListAdapter extends BaseAdapter {
    private ArrayList<Task> taskList = new ArrayList<Task>();
    private Context context;
    public TaskListAdapter(Context context , ArrayList<Task> taskList){
        this.taskList = taskList;
        this.context = context;
    }
    @Override
    public int getCount() {
        if (taskList != null && taskList.size() > 0) {
            return taskList.size();
        } else {
            return 0;
        }
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
        convertView = LayoutInflater.from(context)
                .inflate(R.layout.task_list_item, null);
        Task task = taskList.get(position);
        ((TextView) convertView.findViewById(R.id.tv_task_name))
                .setText(task.getTitle());
        ((TextView) convertView.findViewById(R.id.tv_task_deadline))
                .setText(context.getString(R.string.work_task_end,TimeUtils.calendar2FormatString(context,
                        task.getCreationDate(),
                        TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE)));

        ((TextView) convertView.findViewById(R.id.tv_task_from))
                .setText("我创建的");
        ((ImageView)convertView.findViewById(R.id.iv_task_color)).setImageResource(R.drawable.tuesday);
        return convertView;
    }

    public void setAndChangeData(ArrayList<Task> taskList){
        this.taskList = taskList;
        notifyDataSetChanged();
    }

}
