package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.work.Task;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.MessionTagColorUtils;
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
        String deadLine = TimeUtils.calendar2FormatString(context,
                task.getDueDate(),TimeUtils.FORMAT_MONTH_DAY_HOUR_MINUTE);
        TextView deadLineText = convertView.findViewById(R.id.tv_task_deadline);
        if(!StringUtils.isBlank(deadLine)){
            deadLineText.setVisibility(View.VISIBLE);
            deadLineText.setText(context.getString(R.string.work_task_end,deadLine));
        }else{
            RelativeLayout relativeLayout = convertView.findViewById(R.id.ll_category_title);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayout.setLayoutParams(params);
            deadLineText.setVisibility(View.GONE);
        }
        if (taskList.get(position).getTags().size() > 0) {
            MessionTagColorUtils.setTagColorImg((ImageView)convertView.findViewById(R.id.iv_task_color),
                    taskList.get(position).getTags().get(0).getColor());
        } else {
            // 如果没有tag，显示默认tag
            MessionTagColorUtils.setTagColorImg((ImageView)convertView.findViewById(R.id.iv_task_color), "YELLOW");
        }
//        ((ImageView)convertView.findViewById(R.id.iv_task_color)).setImageResource(R.drawable.tuesday);
        return convertView;
    }

    public void setAndChangeData(ArrayList<Task> taskList){
        this.taskList = taskList;
        notifyDataSetChanged();
    }

}
