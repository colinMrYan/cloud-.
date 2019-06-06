package com.inspur.emmcloud.ui.schedule.task;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.schedule.task.TagColorBean;
import com.inspur.emmcloud.bean.schedule.task.TaskColorTag;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/4/9.
 */
public class TaskTagAddActivity extends BaseActivity  {
    @BindView(R.id.lv_tag_color)
    ListView tagColorList;
    @BindView(R.id.tv_delecte_tag)
    TextView deleteTagText;
    @BindView(R.id.et_tag_name)
    EditText tagNameEdit;

    private List<TagColorBean> tagColorBeans = new ArrayList<>();
    private int selectIndex = 0;
    private ColorTagAdapter colorTagAdapter = new ColorTagAdapter();
    private ArrayList<String> messionTagList=new ArrayList<>();
    private LoadingDialog loadingDialog;
    private ScheduleApiService scheduleAPIService;
    private TaskColorTag taskColorTag;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initData();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_task_tags_add;
    }

    private void initData() {
        TagColorBean tagColorPink = new TagColorBean("PINK", getString(R.string.mession_delete_red));
        TagColorBean tagColorOrange = new TagColorBean("ORANGE", getString(R.string.mession_delete_orange));
        TagColorBean tagColorYellow = new TagColorBean("YELLOW", getString(R.string.mession_delete_yellow));
        TagColorBean tagColorGreen = new TagColorBean("GREEN", getString(R.string.mession_delete_green));
        TagColorBean tagColorBlue = new TagColorBean("BLUE", getString(R.string.mession_delete_blue));
        TagColorBean tagColorPurple = new TagColorBean("PURPLE", getString(R.string.mession_delete_purple));

        tagColorBeans.add(tagColorPink);
        tagColorBeans.add(tagColorOrange);
        tagColorBeans.add(tagColorYellow);
        tagColorBeans.add(tagColorGreen);
        tagColorBeans.add(tagColorBlue);
        tagColorBeans.add(tagColorPurple);
        tagColorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectIndex = i;
                colorTagAdapter.notifyDataSetChanged();
            }
        });
        String userId = ((MyApplication) getApplicationContext()).getUid();
        messionTagList = JSONUtils.JSONArray2List(PreferencesUtils.getString(TaskTagAddActivity.this, MyApplication.getInstance().getTanent() + userId + "messionTags", ""), new ArrayList<String>());
        if (getIntent().hasExtra(TaskTagsManageActivity.EXTRA_DELETE_TAGS)) {
            taskColorTag = (TaskColorTag) getIntent().getSerializableExtra(TaskTagsManageActivity.EXTRA_DELETE_TAGS);
            selectIndex = getTagColorIndex(tagColorBeans, taskColorTag.getColor());
            tagNameEdit.setText(taskColorTag.getTitle());
            deleteTagText.setVisibility(View.VISIBLE);
        }
        tagNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = charSequence.toString();
                if (str.length()>64){
                    tagNameEdit.setText(str.substring(0,64)); //截取前x位
                    tagNameEdit.requestFocus();
                    tagNameEdit.setSelection(tagNameEdit.getText().length()); //光标移动到最后
                    ToastUtils.show(getBaseContext(),R.string.schedule_task_tag_name_length);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        tagColorList.setAdapter(colorTagAdapter);
        loadingDialog = new LoadingDialog(TaskTagAddActivity.this);
        scheduleAPIService = new ScheduleApiService(this);
        scheduleAPIService.setAPIInterface(new WebService());
    }

    private int getTagColorIndex(List<TagColorBean> tagColorBeans, String color) {
        int tagIndex = -1;
        for (int i = 0; i < tagColorBeans.size(); i++) {
            if (tagColorBeans.get(i).getColor().equals(color))
                return i;
        }
        return tagIndex;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_save:
                //存储条件，网络及名称不能为空
                String title = tagNameEdit.getText().toString();
                if (!NetUtils.isNetworkConnected(this)) {
                    ToastUtils.show(this, getString(R.string.net_connected_error));
                    return;
                }
                if (StringUtils.isBlank(title)) {
                    ToastUtils.show(this, getString(R.string.schedule_task_tag_name_null_hint));
                    return;
                }
                loadingDialog.show();
                //一种是新建；第二种是更新
                if (getIntent().hasExtra(TaskTagsManageActivity.EXTRA_DELETE_TAGS)) {
                    String userId = PreferencesUtils.getString(
                            TaskTagAddActivity.this, "userID");
                    scheduleAPIService.changeTag(taskColorTag.getId(), title, tagColorBeans.get(selectIndex).getColor(), userId);
                } else {
                    scheduleAPIService.createTag(title, tagColorBeans.get(selectIndex).getColor());
                }
                break;
            case R.id.tv_delecte_tag:
                if (!NetUtils.isNetworkConnected(this)) {
                    ToastUtils.show(this, getString(R.string.net_connected_error));
                    return;
                }
                if (getIntent().hasExtra(TaskTagsManageActivity.EXTRA_DELETE_TAGS))
                    scheduleAPIService.deleteTag(taskColorTag.getId());
                break;
            case R.id.ibt_back:
                finish();
                break;
            default:
                break;
        }
    }

    class ColorTagHolder {
        public TextView colorNameText;
        public ImageView colorFlagImage;
        public ImageView colorSelectImage;
    }

    private class ColorTagAdapter extends BaseAdapter {
        public ColorTagAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return tagColorBeans.size();
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
            ColorTagHolder colorTagHolder = new ColorTagHolder();
            if (view == null) {
                view = View.inflate(TaskTagAddActivity.this, R.layout.task_manage_item, null);
                colorTagHolder.colorFlagImage = view.findViewById(R.id.iv_task_tag_flag);
                colorTagHolder.colorSelectImage = view.findViewById(R.id.iv_task_tag_select);
                colorTagHolder.colorNameText = view.findViewById(R.id.tv_task_tag_name);
                view.setTag(colorTagHolder);
            } else {
                colorTagHolder = (ColorTagHolder) view.getTag();
            }
            colorTagHolder.colorNameText.setText(tagColorBeans.get(i).getContent());
            colorTagHolder.colorFlagImage.setImageResource(CalendarColorUtils.getColorCircleImage(tagColorBeans.get(i).getColor()));
            colorTagHolder.colorSelectImage.setVisibility(i == selectIndex ? View.VISIBLE : View.GONE);
            return view;
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnDeleteTagSuccess() {
            super.returnDeleteTagSuccess();
            LoadingDialog.dimissDlg(loadingDialog);

            // saveTagsAfterDelete();
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void returnDeleteTagFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(TaskTagAddActivity.this, error, errorCode);
        }

        @Override
        public void returnCreateTagSuccess() {
            super.returnCreateTagSuccess();
            LoadingDialog.dimissDlg(loadingDialog);
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void returnCreateTagFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(TaskTagAddActivity.this, error, errorCode);
        }

    }

}
