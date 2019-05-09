package com.inspur.emmcloud.ui.schedule.task;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.schedule.task.TaskColorTag;
import com.inspur.emmcloud.bean.work.GetTagResult;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;

/**
 * Created by libaochao on 2019/4/8.
 */
@ContentView(R.layout.activity_task_manage)
public class TaskTagsManageActivity extends BaseActivity {
    public static String EXTRA_TAGS = "tags";
    public static String EXTRA_DELETE_TAGS = "tags";
    @ViewInject(R.id.lv_task_manage_tags)
    private ListView taskManageTagsList;
    private TaskTagsAdapter taskTagsAdapter;
    private LoadingDialog loadingDialog;
    private ArrayList<TaskColorTag> allTags = new ArrayList<TaskColorTag>();
    private ArrayList<TaskColorTag> selectTags = new ArrayList<TaskColorTag>();
    private WorkAPIService workAPIService;
    private boolean isHaveExtra = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    /**
     * 初始化 传入数据和不传入数据
     */
    private void initData() {
        isHaveExtra = getIntent().hasExtra(EXTRA_TAGS);    //是否传入数据
        taskTagsAdapter = new TaskTagsAdapter();
        taskManageTagsList.setAdapter(taskTagsAdapter);
        taskManageTagsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (isHaveExtra) {
                    //判定一下selectList里有没有，有的话删除，没有添加
                    int index = getSameTagIndex(allTags.get(i));
                    if (index > -1) {
                        selectTags.remove(index);
                    } else {
                        selectTags.add(allTags.get(i));
                    }
                    taskTagsAdapter.notifyDataSetChanged();
                } else {
                    Intent intent = new Intent();
                    intent.setClass(TaskTagsManageActivity.this, TaskTagAddActivity.class);
                    intent.putExtra(EXTRA_DELETE_TAGS, allTags.get(i));
                    startActivityForResult(intent, 111);
                }
            }
        });
        loadingDialog = new LoadingDialog(this);
        workAPIService = new WorkAPIService(this);
        workAPIService.setAPIInterface(new WebService());
        getTags();
        if (getIntent().hasExtra(EXTRA_TAGS)) {
            if (getIntent().getSerializableExtra(EXTRA_TAGS) != null) {
                selectTags.clear();
                selectTags = (ArrayList<TaskColorTag>) getIntent()
                        .getSerializableExtra(EXTRA_TAGS);
            }
        }
    }

    private int getSameTagIndex(TaskColorTag taskColorTag) {
        for (int n = 0; n < selectTags.size(); n++) {
            if (selectTags.get(n).getId().equals(taskColorTag.getId())) {
                return n;
            }
        }
        return -1;
    }

    /***/
    private void getTags() {
        if (NetUtils.isNetworkConnected(TaskTagsManageActivity.this)) {
            loadingDialog.show();
            workAPIService.getTags();
        }
    }

    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.ibt_add:
                intent.setClass(this, TaskTagAddActivity.class);
                startActivityForResult(intent, 11);
                break;
            case R.id.bt_add_tags:
                intent.putExtra(EXTRA_TAGS, selectTags);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            getTags();
        }
    }

    /***/
    private class ManageTagsHolder {
        public TextView tagNameText;
        public ImageView tagFlagImage;
        public ImageView tagSelectImage;
    }

    /***/
    public class TaskTagsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return allTags.size();
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
                manageTagsHolder.tagSelectImage = view.findViewById(R.id.iv_task_tag_select);
                view.setTag(manageTagsHolder);
            } else {
                manageTagsHolder = (ManageTagsHolder) view.getTag();
            }
            int colorId = CalendarColorUtils.getColorCircleImage(allTags.get(i).getColor());
            manageTagsHolder.tagNameText.setText(allTags.get(i).getTitle());
            manageTagsHolder.tagFlagImage.setImageResource(colorId);
            manageTagsHolder.tagSelectImage.setVisibility(View.GONE);
            for (int m = 0; m < selectTags.size(); m++) {
                if (allTags.get(i).equals(selectTags.get(m)))
                    manageTagsHolder.tagSelectImage.setVisibility(View.VISIBLE);
            }
            return view;
        }
    }


    class WebService extends APIInterfaceInstance {
        @Override
        public void returnGetTagResultSuccess(GetTagResult getTagResult) {
            super.returnGetTagResultSuccess(getTagResult);
            LoadingDialog.dimissDlg(loadingDialog);
            allTags = getTagResult.getArrayList();
            allTags.size();
            taskTagsAdapter.notifyDataSetChanged();
            String userId = ((MyApplication) getApplicationContext()).getUid();
            PreferencesUtils.putString(TaskTagsManageActivity.this,
                    MyApplication.getInstance().getTanent() + userId + "messionTags",
                    JSONUtils.toJSONString(allTags));
        }

        @Override
        public void returnGetTagResultFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(TaskTagsManageActivity.this, error, errorCode);
        }

    }


}
