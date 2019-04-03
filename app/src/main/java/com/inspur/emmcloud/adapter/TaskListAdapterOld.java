package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.work.TaskResult;
import com.inspur.emmcloud.util.privates.MessionTagColorUtils;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2019/4/1.
 */

public class TaskListAdapterOld extends BaseAdapter {
    private ArrayList<TaskResult> taskList = new ArrayList<TaskResult>();
    private Context context;
    public TaskListAdapterOld(Context context , ArrayList<TaskResult> taskList){
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
                .inflate(R.layout.meession_list_item, null);
        ((TextView) convertView.findViewById(R.id.mession_text))
                .setText(taskList.get(position).getTitle());
        if (taskList.get(position).getTags().size() > 0) {
            MessionTagColorUtils.setTagColorImg((ImageView) convertView
                            .findViewById(R.id.mession_color),
                    taskList.get(position).getTags().get(0).getColor());
        } else {
            // 如果没有tag，显示默认tag
            MessionTagColorUtils.setTagColorImg((ImageView) convertView
                    .findViewById(R.id.mession_color), "YELLOW");
        }
        if (taskList.get(position).getPriority() == 1) {
            // 当重要程度为1时可能后续需要做处理
            // ((ImageView) convertView
            // .findViewById(R.id.mession_state_img))
            // .setVisibility(View.VISIBLE);
        } else if (taskList.get(position).getPriority() == 2) {
            convertView.findViewById(R.id.mession_state_img)
                    .setVisibility(View.VISIBLE);
            // 当重要程度为2时后续可能需要添加两个叹号
            // ((ImageView) convertView
            // .findViewById(R.id.mession_state_img2))
            // .setVisibility(View.VISIBLE);
        }
        return convertView;
    }

}
