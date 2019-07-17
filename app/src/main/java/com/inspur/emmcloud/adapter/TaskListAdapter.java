package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.bean.schedule.task.Task;
import com.inspur.emmcloud.util.privates.CalendarUtils;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2019/4/3.
 */

public class TaskListAdapter extends BaseAdapter {
    private ArrayList<Task> taskList = new ArrayList<Task>();
    private Context context;

    public TaskListAdapter(Context context, ArrayList<Task> taskList) {
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
        String deadLine = TimeUtils.calendar2FormatString(context,
                task.getDueDate(), TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE);
        TextView deadLineText = convertView.findViewById(R.id.tv_task_deadline);
        if (!StringUtils.isBlank(deadLine)) {
            deadLineText.setVisibility(View.VISIBLE);
            deadLineText.setText(context.getString(R.string.work_task_end, deadLine));
        } else {
            deadLineText.setVisibility(View.GONE);
        }
        if (task.getTags().size() > 0) {
            ((ImageView) convertView.findViewById(R.id.iv_task_color)).setImageResource(CalendarUtils.getCalendarTypeResId(task.getTags().get(0).getColor()));
        } else {
            // 如果没有tag，显示默认tag
            ((ImageView) convertView.findViewById(R.id.iv_task_color)).setImageResource(CalendarUtils.getCalendarTypeResId("BLUE"));
        }
        convertView.findViewById(R.id.iv_task_level).setVisibility(task.getPriority() == 2 ? View.VISIBLE : View.GONE);
        return convertView;
    }

    public void setAndChangeData(ArrayList<Task> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();
    }

}
