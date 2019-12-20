package com.inspur.emmcloud.schedule.ui.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.R2;
import com.inspur.emmcloud.schedule.bean.task.MessionSetModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2019/4/9.
 */
public class TaskSetActivity extends BaseActivity {

    public static final String TASK_ORDER_BY = "order_by";
    public static final String TASK_ORDER_TYPE = "order_type";
    public static final String TASK_ORDER_TYPE_ASC = "ASC";
    public static final String TASK_ORDER_TYPE_DESC = "DESC";
    public static final String TASK_SET_ORDER = "setorder";
    public static final String TASK_ORDER = "order";
    public static final String TASK_ORDER_PRIORITY = "PRIORITY";
    public static final String TASK_ORDER_DUE_DATE = "DUE_DATE";
    @BindView(R2.id.lv_task_list)
    ListView setListView;
    private ArrayList<MessionSetModel> taskSetModel = new ArrayList<MessionSetModel>();

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_task_set;
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
                            TASK_ORDER_BY, TASK_ORDER_PRIORITY);
                } else if (position == 1) {
                    PreferencesUtils.putString(TaskSetActivity.this,
                            TASK_ORDER_BY, TASK_ORDER_DUE_DATE);
                }
                PreferencesUtils.putInt(TaskSetActivity.this, TASK_SET_ORDER,
                        position);
                setAdapter.notifyDataSetChanged();
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TASK_ORDER_CHANGE));
            }
        });
        MessionSetModel levelModel = new MessionSetModel("");
        levelModel.setContent(getString(R.string.schedule_mession_set_level));
        levelModel.setShow("1");
        taskSetModel.add(levelModel);

        MessionSetModel timeModel = new MessionSetModel("");
        timeModel.setContent(getString(R.string.schedule_mession_set_time));
        taskSetModel.add(timeModel);

    }

    /**
     * 第一次进入时没有order配置
     */
    private void initFirstOrder() {
        if (PreferencesUtils.getInt(TaskSetActivity.this, TASK_SET_ORDER, -1) == -1) {
            PreferencesUtils.putInt(TaskSetActivity.this, TASK_SET_ORDER, 0);
        }
        if (PreferencesUtils.getInt(TaskSetActivity.this, TASK_ORDER, -1) == -1) {
            PreferencesUtils.putInt(TaskSetActivity.this, TASK_ORDER, 0);
        }
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            setResult(RESULT_OK);
            finish();
        } else if (i == R.id.rl_task_manager) {
            IntentUtils.startActivity(this, TaskTagsManageActivity.class);
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
            int positionChoose = PreferencesUtils.getInt(TaskSetActivity.this, TASK_SET_ORDER, -1);
            convertView = LayoutInflater.from(TaskSetActivity.this).inflate(R.layout.task_set_item, null);
            convertView.findViewById(R.id.v_head_line).setVisibility(position == 0 ? View.GONE : View.VISIBLE);
            ((TextView) (convertView.findViewById(R.id.tv_task_set))).setText(taskSetModel.get(position).getContent());
            convertView.findViewById(R.id.iv_selected).setVisibility(position == positionChoose ? View.VISIBLE : View.GONE);
            return convertView;
        }
    }
}
