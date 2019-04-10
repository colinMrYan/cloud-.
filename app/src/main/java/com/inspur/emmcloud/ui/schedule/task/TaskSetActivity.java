package com.inspur.emmcloud.ui.schedule.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.work.MessionSetModel;
import com.inspur.emmcloud.ui.work.task.MessionTagsManageActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2019/4/9.
 */
@ContentView(R.layout.activity_task_set)
public class TaskSetActivity extends BaseActivity{

    @ViewInject(R.id.lv_task_list)
    private ListView setListView;
    private ArrayList<MessionSetModel> taskSetModel = new ArrayList<MessionSetModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    /**
     * 初始化views
     */
    private void initViews() {
        final SetAdapter setAdapter = new SetAdapter();
        setListView.setAdapter(setAdapter);
        initFirstOrder();
        setListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {
                    PreferencesUtils.putString(TaskSetActivity.this,
                            "order_by", "PRIORITY");
                } else if (position == 1) {
                    PreferencesUtils.putString(TaskSetActivity.this,
                            "order_by", "DUE_DATE");
                }
                PreferencesUtils.putInt(TaskSetActivity.this, "setorder",
                        position);
                setAdapter.notifyDataSetChanged();
            }
        });


        MessionSetModel levelModel = new MessionSetModel("");
        levelModel.setContent(getString(R.string.mession_set_level));
        levelModel.setShow("1");
        taskSetModel.add(levelModel);

        MessionSetModel timeModel = new MessionSetModel("");
        timeModel.setContent(getString(R.string.mession_set_time));
        taskSetModel.add(timeModel);

    }

    /**
     * 第一次进入时没有order配置
     */
    private void initFirstOrder() {
        if (PreferencesUtils.getInt(TaskSetActivity.this, "setorder", -1) == -1) {
            PreferencesUtils.putInt(TaskSetActivity.this, "setorder", 0);
        }
        if (PreferencesUtils.getInt(TaskSetActivity.this, "order", -1) == -1) {
            PreferencesUtils.putInt(TaskSetActivity.this, "order", 0);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.tv_task_save:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.rl_task_manager:
                IntentUtils.startActivity(this, MessionTagsManageActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    class SetAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return taskSetModel.size();
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
            int positionChoose = PreferencesUtils.getInt(
                    TaskSetActivity.this, "setorder", -1);
            convertView = LayoutInflater.from(TaskSetActivity.this).inflate(
                    R.layout.task_set_item, null);
            if(position == 0){
                convertView.findViewById(R.id.v_head_line).setVisibility(View.GONE);
            }
            ((TextView) (convertView.findViewById(R.id.tv_task_set)))
                    .setText(taskSetModel.get(position).getContent());
            if (position == positionChoose) {
                convertView.findViewById(R.id.iv_selected)
                        .setVisibility(View.VISIBLE);
            } else {
                convertView.findViewById(R.id.iv_selected)
                        .setVisibility(View.GONE);
            }
            return convertView;
        }
    }
}
