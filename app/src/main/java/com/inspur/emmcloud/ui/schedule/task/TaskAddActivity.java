package com.inspur.emmcloud.ui.schedule.task;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.SegmentControl;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by libaochao on 2019/3/28.
 */
@ContentView(R.layout.activity_add_task)
public class TaskAddActivity extends BaseActivity {
    @ViewInject(R.id.et_input_content)
    private EditText contentInputEdit;
    @ViewInject(R.id.segment_control)
    private SegmentControl segmentControl;
    @ViewInject(R.id.iv_task_type_tap)
    private ImageView taskTypeTapImage;
    @ViewInject(R.id.tv_task_type_name)
    private TextView taskTypeNameText;
    @ViewInject(R.id.tv_deadline_time)
    private TextView deadlineTimeText;
    @ViewInject(R.id.tv_deadline_time)
    private TextView stateText;
    @ViewInject(R.id.iv_more)
    private ImageView moreImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_save:
                break;
            case R.id.tv_cancel:
                break;
            case R.id.rl_task_type:
                break;
            case R.id.rl_task_manager:
                break;
            case R.id.rl_task_parter:
                break;
            case R.id.rl_deadline:
                break;
            case R.id.rl_state:
                break;
            case R.id.rl_more:
                break;
        }
    }
}
