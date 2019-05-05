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
import android.widget.Button;
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
import com.inspur.emmcloud.bean.schedule.task.TaskColorTag;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.ArrayList;

public class MessionTagsManageActivity extends BaseActivity {

    private static final int MESSION_TAG = 1;
    private ListView setListView;
    private WorkAPIService apiService;
    private ManageAdapter adapter;
    private LoadingDialog loadingDialog;
    private ArrayList<TaskColorTag> allTags = new ArrayList<TaskColorTag>();
    private ArrayList<TaskColorTag> selectTags = new ArrayList<TaskColorTag>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mession_manage);
        initViews();
    }

    /**
     * 初始化views
     */
    private void initViews() {
        setListView = (ListView) findViewById(R.id.mession_manage_list);
        Button saveBtn = (Button) findViewById(R.id.save_btn);
        loadingDialog = new LoadingDialog(MessionTagsManageActivity.this);
        apiService = new WorkAPIService(this);
        apiService.setAPIInterface(new WebService());
        getTags();
        if (getIntent().hasExtra("tag")) {
            if (getIntent().getSerializableExtra("tag") != null) {
                selectTags.clear();
                selectTags = (ArrayList<TaskColorTag>) getIntent()
                        .getSerializableExtra("tag");
            }
        }
        if (getIntent().hasExtra("from")) {
            // setListView.setOnItemClickListener(new OnItemClickListener() {
            //
            // @Override
            // public void onItemClick(AdapterView<?> parent, View view,
            // int position, long id) {
            // // TODO Auto-generated method stub
            // Intent intent = new Intent();
            // intent.setClass(MessionTagsManageActivity.this,
            // MessionTagActivity.class);
            // intent.putExtra("tag", tags.get(position));
            // setResult(RESULT_OK, intent);
            // finish();
            // }
            // });
        } else {
            saveBtn.setVisibility(View.GONE);
            setListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Intent intent = new Intent();
                    intent.setClass(MessionTagsManageActivity.this,
                            MessionTagActivity.class);
                    intent.putExtra("id", allTags.get(position).getId());
                    intent.putExtra("title", allTags.get(position).getTitle());
                    intent.putExtra("color", allTags.get(position).getColor());
                    intent.putExtra("tagid", position + "");
                    startActivityForResult(intent, MESSION_TAG);
                }
            });
        }
    }

    /**
     * 获取tags
     */
    private void getTags() {
        if (NetUtils.isNetworkConnected(MessionTagsManageActivity.this)) {
            loadingDialog.show();
            apiService.getTags();
        }
    }

    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ibt_back:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.mession_settiing_txt:
                intent.setClass(MessionTagsManageActivity.this,
                        MessionTagActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.save_btn:
                intent.setClass(MessionTagsManageActivity.this,
                        MessionTagActivity.class);
                intent.putExtra("tag", selectTags);
                LogUtils.debug("yfcLog", "回传的tags：" + selectTags.size());
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getTags();
    }

    /**
     * 标签颜色处理
     *
     * @param imageView
     * @param position
     */
    public void handleTagColor(ImageView imageView, int position) {
        if (allTags.get(position).getColor().equals("ORANGE")) {
            imageView.setImageResource(R.drawable.icon_mession_orange);
        } else if (allTags.get(position).getColor().equals("BLUE")) {
            imageView.setImageResource(R.drawable.icon_mession_blue);
        } else if (allTags.get(position).getColor().equals("GREEN")) {
            imageView.setImageResource(R.drawable.icon_mession_green);
        } else if (allTags.get(position).getColor().equals("PINK")) {
            imageView.setImageResource(R.drawable.icon_mession_red);
        } else if (allTags.get(position).getColor().equals("YELLOW")) {
            imageView.setImageResource(R.drawable.icon_mession_yellow);
        } else if (allTags.get(position).getColor().equals("PURPLE")) {
            imageView.setImageResource(R.drawable.icon_mession_purple);
        }
    }

    /**
     * 处理点击后选中
     *
     * @param layout
     * @param addImageView
     * @param position
     */
    public void handleClickSelect(RelativeLayout layout,
                                  final ImageView addImageView, final int position) {
        if (getIntent().hasExtra("from")) {
            if (getIntent().getStringExtra("from").contains("mession")) {
                layout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (addImageView.getVisibility() == View.VISIBLE) {
                            addImageView.setVisibility(View.GONE);
                            selectTags.remove(allTags.get(position));
                        } else if (addImageView.getVisibility() == View.GONE) {
                            addImageView.setVisibility(View.VISIBLE);
                            selectTags.add(allTags.get(position));
                        }

                    }
                });
            }
        }
    }

    /**
     * 处理是否选择
     *
     * @param addImageView
     * @param position
     */
    public void handleSelectImg(ImageView addImageView, int position) {
        String tagId = allTags.get(position).getId();
        for (int i = 0; i < selectTags.size(); i++) {
            if (tagId.equals(selectTags.get(i).getId())) {
                addImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    class ManageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return allTags.size();
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
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            convertView = LayoutInflater.from(MessionTagsManageActivity.this)
                    .inflate(R.layout.meession_managelist_item, null);
            ((TextView) (convertView.findViewById(R.id.mession_set_text)))
                    .setText(allTags.get(position).getTitle());
            final ImageView addImageView = (ImageView) convertView
                    .findViewById(R.id.mession_add_img);
            RelativeLayout layout = (RelativeLayout) convertView
                    .findViewById(R.id.work_expand_layout);
            handleSelectImg(addImageView, position);
            handleClickSelect(layout, addImageView, position);
            ImageView imageView = (ImageView) convertView
                    .findViewById(R.id.mession_ring_img);
            handleTagColor(imageView, position);
            return convertView;
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnGetTagResultSuccess(GetTagResult getTagResult) {
            super.returnGetTagResultSuccess(getTagResult);
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            allTags = getTagResult.getArrayList();
            adapter = new ManageAdapter();
            setListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            // String tags = JSON.toJSONString(allTags);
            // LogUtils.debug("yfcLog", "本用户所有的tag："+tags);
            // LogUtils.debug("yfcLog", "tag转化后list长度："+(JSON.parseArray(tags,
            // TaskColorTag.class)).size());
//			PreferencesUtils.putString(getApplicationContext(), "messiontags",
//					getTagResult.getResponse());

            String userId = ((MyApplication) getApplicationContext()).getUid();
            PreferencesUtils.putString(MessionTagsManageActivity.this,
                    MyApplication.getInstance().getTanent() + userId + "messionTags",
                    JSONUtils.toJSONString(allTags));
        }

        @Override
        public void returnGetTagResultFail(String error, int errorCode) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionTagsManageActivity.this, error, errorCode);
        }

    }
}
