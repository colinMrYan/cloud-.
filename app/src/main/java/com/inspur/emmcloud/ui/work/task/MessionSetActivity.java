package com.inspur.emmcloud.ui.work.task;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.work.GetTagResult;
import com.inspur.emmcloud.bean.work.MessionSetModel;
import com.inspur.emmcloud.bean.schedule.task.TaskColorTag;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.tag.Tag;
import com.inspur.emmcloud.widget.tag.TagListView;
import com.inspur.emmcloud.widget.tag.TagListView.OnTagClickListener;
import com.inspur.emmcloud.widget.tag.TagView;

import java.util.ArrayList;
import java.util.List;

public class MessionSetActivity extends BaseActivity {

    private static final int TAG_MANAGE = 0;
    private final List<Tag> allTags = new ArrayList<Tag>();
    private ListView setListView, setOrderListView;
    private TagListView tagListView;
    private ArrayList<MessionSetModel> messionSetModel = new ArrayList<MessionSetModel>();
    private ArrayList<MessionSetModel> messionSetOrderModel = new ArrayList<MessionSetModel>();
    private WorkAPIService apiService;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mession_set);
        initViews();
        getTags();
    }

    /**
     * 获取tags
     */
    private void getTags() {
        if (NetUtils.isNetworkConnected(MessionSetActivity.this)) {
            loadingDialog.show();
            apiService.getTags();
        }
    }

    /**
     * 初始化views
     */
    private void initViews() {
        apiService = new WorkAPIService(MessionSetActivity.this);
        apiService.setAPIInterface(new WebService());
        loadingDialog = new LoadingDialog(MessionSetActivity.this);
        final SetAdapter setAdapter = new SetAdapter();
        final SetOrderAdapter setOrderAdapter = new SetOrderAdapter();
        setListView = (ListView) findViewById(R.id.mession_set_list);
        setOrderListView = (ListView) findViewById(R.id.mession_setorder_list);
        setListView.setAdapter(setAdapter);
        setOrderListView.setAdapter(setOrderAdapter);
        initFirstOrder();
        setListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {
                    PreferencesUtils.putString(MessionSetActivity.this,
                            "order_by", "PRIORITY");
                } else if (position == 1) {
                    PreferencesUtils.putString(MessionSetActivity.this,
                            "order_by", "DUE_DATE");
                }
                PreferencesUtils.putInt(MessionSetActivity.this, "setorder",
                        position);
                setAdapter.notifyDataSetChanged();
            }
        });

        setOrderListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {
                    PreferencesUtils.putString(MessionSetActivity.this,
                            "order_type", "DESC");
                } else if (position == 1) {
                    PreferencesUtils.putString(MessionSetActivity.this,
                            "order_type", "ASC");
                }
                PreferencesUtils.putInt(MessionSetActivity.this, "order",
                        position);
                setOrderAdapter.notifyDataSetChanged();
            }
        });

        MessionSetModel levelModel = new MessionSetModel("");
        levelModel.setContent(getString(R.string.mession_set_level));
        levelModel.setShow("1");
        messionSetModel.add(levelModel);

        MessionSetModel timeModel = new MessionSetModel("");
        timeModel.setContent(getString(R.string.mession_set_time));
        messionSetModel.add(timeModel);

        MessionSetModel ascModel = new MessionSetModel("");
        ascModel.setContent(getString(R.string.mession_set_asc));
        ascModel.setShow("1");
        messionSetOrderModel.add(ascModel);

        MessionSetModel descModel = new MessionSetModel("");
        descModel.setContent(getString(R.string.mession_set_desc));
        messionSetOrderModel.add(descModel);
        RelativeLayout manageLayout = (RelativeLayout) findViewById(R.id.mession_manage_layout);
        manageLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MessionSetActivity.this,
                        MessionTagsManageActivity.class);
                startActivityForResult(intent, TAG_MANAGE);
            }
        });
        tagListView = (TagListView) findViewById(R.id.mession_tagview);
        tagListView.setOnTagClickListener(new OnTagClickListener() {
            @Override
            public void onTagClick(TagView tagView, Tag tag) {
                // String tagStr = "";
                tagView.setCheckEnable(true);
                if (allTags.get(tag.getId()).isChecked()) {
                    tagView.setChecked(true);
                    allTags.get(tag.getId()).setChecked(true);
                    ArrayList<String> tagList = new ArrayList<String>();
                    for (int i = 0; i < allTags.size(); i++) {
                        if (tagListView.getViewByTag(allTags.get(i))
                                .isChecked()) {
                            // tagStr = tagStr + allTags.get(i).getTitle() +
                            // ":";
                            tagList.add(allTags.get(i).getTitle());
                        }
                    }
                    saveChoosedTagList(tagList);
                } else {
                    tagView.setChecked(false);
                    allTags.get(tag.getId()).setChecked(false);
                    ArrayList<String> tagList = new ArrayList<String>();
                    // tagStr = "";
                    for (int i = 0; i < allTags.size(); i++) {
                        if (tagListView.getViewByTag(allTags.get(i))
                                .isChecked()) {
                            // tagStr = tagStr + allTags.get(i).getTitle() +
                            // ":";
                            tagList.add(allTags.get(i).getTitle());
                        }
                    }
                    saveChoosedTagList(tagList);
                }

            }
        });
    }

    /**
     * 保存TagList
     *
     * @param tagList
     */
    protected void saveChoosedTagList(ArrayList<String> tagList) {
        String userId = ((MyApplication) getApplicationContext()).getUid();
        if (tagList.size() > 0) {
            PreferencesUtils.putString(MessionSetActivity.this, MyApplication.getInstance().getTanent()
                    + userId + "chooseTags", JSONUtils.toJSONString(tagList));
        } else {
            PreferencesUtils.putString(MessionSetActivity.this, MyApplication.getInstance().getTanent()
                    + userId + "chooseTags", "");
        }
    }

    /**
     * 第一次进入时没有order配置
     */
    private void initFirstOrder() {
        if (PreferencesUtils.getInt(MessionSetActivity.this, "setorder", -1) == -1) {
            PreferencesUtils.putInt(MessionSetActivity.this, "setorder", 0);
        }
        if (PreferencesUtils.getInt(MessionSetActivity.this, "order", -1) == -1) {
            PreferencesUtils.putInt(MessionSetActivity.this, "order", 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // refreshTagListView();
            getTags();
        }
    }

    /**
     * 初始化标签
     */
    private void refreshTagListView() {
        // setUpData();
        tagListView.setTags(allTags);
        String userId = ((MyApplication) getApplicationContext()).getUid();
        String choosenTags = PreferencesUtils.getString(
                MessionSetActivity.this, MyApplication.getInstance().getTanent() + userId
                        + "chooseTags", "");
        ArrayList<String> chooseTagList = JSONUtils.JSONArray2List(choosenTags, new ArrayList<String>());
        if (chooseTagList.size() > 0) {
            for (int i = 0; i < chooseTagList.size(); i++) {
                for (int j = 0; j < allTags.size(); j++) {
                    if (chooseTagList.get(i).equals(allTags.get(j).getTitle())) {
                        try {
                            tagListView.getViewByTag(allTags.get(j))
                                    .setCheckEnable(true);
                            tagListView.getViewByTag(allTags.get(j))
                                    .setChecked(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            PreferencesUtils
                                    .putString(MessionSetActivity.this,
                                            MyApplication.getInstance().getTanent() + userId
                                                    + "chooseTags", "");
                        }
                    }
                }
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.mession_settiing_txt:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.mession_finish_layout:
                Intent intent = new Intent();
                intent.setClass(MessionSetActivity.this,
                        MessionFinishListActivity.class);
                startActivity(intent);
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

    /**
     * 排序设置
     *
     * @param imageView
     * @param position
     * @param positionChoose
     */
    public void handleImage(ImageView imageView, int position,
                            int positionChoose) {
        if (position == positionChoose) {
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    /**
     * 处理设置标签
     */
    private void handleSetTags(ArrayList<TaskColorTag> tags) {
        allTags.clear();
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = new Tag();
            TaskColorTag taskColorTag = tags.get(i);
            tag.setId(i);
            tag.setChecked(true);
            tag.setTagId(taskColorTag.getId());
            tag.setTitle(taskColorTag.getTitle());
            allTags.add(tag);
        }
        refreshTagListView();
    }

    class SetAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return messionSetModel.size();
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
                    MessionSetActivity.this, "setorder", -1);
            convertView = LayoutInflater.from(MessionSetActivity.this).inflate(
                    R.layout.task_set_item1, null);
            ((TextView) (convertView.findViewById(R.id.mession_set_text)))
                    .setText(messionSetModel.get(position).getContent());
            if (position == positionChoose) {
                convertView.findViewById(R.id.mession_ring_img)
                        .setVisibility(View.VISIBLE);
            } else {
                convertView.findViewById(R.id.mession_ring_img)
                        .setVisibility(View.GONE);
            }
            return convertView;
        }

    }

    class SetOrderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return messionSetOrderModel.size();
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
                    MessionSetActivity.this, "order");
            convertView = LayoutInflater.from(MessionSetActivity.this).inflate(
                    R.layout.task_set_item1, null);
            ((TextView) (convertView.findViewById(R.id.mession_set_text)))
                    .setText(messionSetOrderModel.get(position).getContent());
            ImageView imageView = (ImageView) convertView
                    .findViewById(R.id.mession_ring_img);
            handleImage(imageView, position, positionChoose);
            return convertView;
        }

    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnGetTagResultSuccess(GetTagResult getTagResult) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            ArrayList<TaskColorTag> tags = getTagResult.getArrayList();
            handleSetTags(tags);
        }

        @Override
        public void returnGetTagResultFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionSetActivity.this, error, errorCode);
        }
    }
}
