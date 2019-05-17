package com.inspur.emmcloud.ui.work.task;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.schedule.task.TagColorBean;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.TaskTagColorUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.ArrayList;
import java.util.Iterator;

public class MessionTagActivity extends BaseActivity {

    private ListView setListView;
    private WorkAPIService apiService;
    private EditText tagNameEdit;
    private ArrayList<TagColorBean> tagColorBeans = new ArrayList<TagColorBean>();
    private TagAdapter tagAdapter;
    private int colorIndex = 0;
    private RelativeLayout deleteLayout;
    private String color = "", title = "";
    private LoadingDialog loadingDialog;
    private String deleteTagName = "";
    private ArrayList<String> messionTagList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mession_tag);
        initViews();
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        apiService = new WorkAPIService(MessionTagActivity.this);
        apiService.setAPIInterface(new WebService());
        loadingDialog = new LoadingDialog(MessionTagActivity.this);
        setListView = (ListView) findViewById(R.id.mession_tag_list);
        tagNameEdit = (EditText) findViewById(R.id.mession_tagname_edit);
        deleteLayout = (RelativeLayout) findViewById(R.id.mession_manage_layout);
        initData();
        tagAdapter = new TagAdapter();
        setListView.setAdapter(tagAdapter);
        if (getIntent().hasExtra("id")) {
            deleteLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MyQMUIDialog.MessageDialogBuilder(MessionTagActivity.this)
                            .setMessage(R.string.mession_delete_tag)
                            .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    deleteTags();
                                }
                            })
                            .show();
                }
            });
        } else {
            deleteLayout.setVisibility(View.GONE);
        }
        setListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                tagAdapter.notifyDataSetChanged();
                colorIndex = position;
            }
        });

    }

    /**
     * 删除标签
     */
    private void deleteTags() {
        if (NetUtils.isNetworkConnected(MessionTagActivity.this)) {
            loadingDialog.show();
            apiService.deleteTag(getIntent().getStringExtra("id"));
        }
    }

    /**
     * 初始化显示数据
     */
    private void initData() {
        deleteTagName = getIntent().getStringExtra("title");
        String userId = ((MyApplication) getApplicationContext()).getUid();
        messionTagList = JSONUtils.JSONArray2List(PreferencesUtils.getString(MessionTagActivity.this, MyApplication.getInstance().getTanent() + userId + "messionTags", ""), new ArrayList<String>());
        if (getIntent().hasExtra("color")) {
            color = getIntent().getStringExtra("color");
        }
        if (getIntent().hasExtra("title")) {
            title = getIntent().getStringExtra("title");
            tagNameEdit.setText(title);
        }

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
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.mession_settiing_txt:
                String color = "";
                if (NetUtils.isNetworkConnected(MessionTagActivity.this)) {
                    color = translateColor();
                    String userId = PreferencesUtils.getString(
                            MessionTagActivity.this, "userID");
                    String tagName = tagNameEdit.getText().toString();
                    if (getIntent().hasExtra("id") && (!TextUtils.isEmpty(tagName))) {
                        String tagId = getIntent().getStringExtra("id");
                        changeTag(tagId, tagName, color, userId);
                        setResult(RESULT_OK);
                        break;
                    }

                    if (!StringUtils.isEmpty(tagName)) {
                        for (int i = 0; i < messionTagList.size(); i++) {
                            if (messionTagList.get(i)
                                    .contains(tagName)) {
                                ToastUtils.show(MessionTagActivity.this,
                                        getString(R.string.mession_delete_exist));
                                return;
                            }
                        }
                        creatTag(tagName, color);
                    } else {
                        ToastUtils.show(MessionTagActivity.this,
                                getString(R.string.mession_delete_no_empty));
                    }

                }
                break;
            default:
                break;
        }
    }

    /**
     * 修改标签
     *
     * @param tagId
     * @param tagName
     * @param color
     * @param userId
     */
    private void changeTag(String tagId, String tagName, String color,
                           String userId) {
        if (NetUtils.isNetworkConnected(MessionTagActivity.this)) {
            loadingDialog.show();
            apiService.changeTag(tagId, tagName, color, userId);
        }
    }

    /**
     * 转化颜色
     *
     * @return
     */
    private String translateColor() {
        String tagColor = "";
        switch (colorIndex) {
            case 0:
                tagColor = "PINK";
                break;
            case 1:
                tagColor = "ORANGE";
                break;
            case 2:
                tagColor = "YELLOW";
                break;
            case 3:
                tagColor = "GREEN";
                break;
            case 4:
                tagColor = "BLUE";
                break;
            case 5:
                tagColor = "PURPLE";
                break;
            default:
                tagColor = "YELLOW";
                break;
        }
        return tagColor;
    }

    /**
     * 创建一个tag
     *
     * @param tagName
     * @param color
     */
    private void creatTag(String tagName, String color) {
        if (NetUtils.isNetworkConnected(MessionTagActivity.this)) {
            loadingDialog.show();
            apiService.createTag(tagName, color);
        }
    }

    /**
     * 保存标签,如果是个选中的标签要把它选中列表中清除
     */
    public void saveTagsAfterDelete() {
        String userId = ((MyApplication) getApplicationContext()).getUid();
        String choosenTags = PreferencesUtils.getString(MessionTagActivity.this,
                MyApplication.getInstance().getTanent() + userId + "chooseTags", "");
        ArrayList<String> afterDeleteList = JSONUtils.JSONArray2List(choosenTags, new ArrayList<String>());
        if (afterDeleteList.size() > 0) {
            Iterator<String> afterIteror = afterDeleteList.iterator();
            while (afterIteror.hasNext()) {
                String deleteName = afterIteror.next();
                if (deleteName.equals(deleteTagName)) {
                    afterIteror.remove();
                }
            }
            PreferencesUtils
                    .putString(MessionTagActivity.this, MyApplication.getInstance().getTanent()
                                    + userId + "chooseTags",
                            JSONUtils.toJSONString(afterDeleteList));
        } else {
            PreferencesUtils
                    .putString(MessionTagActivity.this, MyApplication.getInstance().getTanent()
                                    + userId + "chooseTags",
                            "");
        }
    }

    /**
     * 设置是否选择
     *
     * @param selectImg
     * @param positionChoose
     * @param position
     */
    public void handleSelectImg(ImageView selectImg, int positionChoose,
                                int position) {
        if ((positionChoose == -1) && (position == 0)
                && StringUtils.isBlank(color)) {
            selectImg.setVisibility(View.VISIBLE);
        }
        if ((positionChoose == -1) && (!(StringUtils.isBlank(color)))
                && (color.equals(tagColorBeans.get(position).getColor()))) {
            selectImg.setVisibility(View.VISIBLE);
        } else if (position == positionChoose) {
            selectImg.setVisibility(View.VISIBLE);
        } else {
            selectImg.setVisibility(View.GONE);
        }
    }

    // /**
    // * 显示颜色标签
    // * @param imageView
    // * @param position
    // */
    // public void handleColorImg(ImageView imageView, int position) {
    // String tagColor = tagColorBeans.get(position).getColor();
    // if (tagColor.equals("PINK")) {
    // imageView.setImageResource(R.drawable.icon_mession_red);
    // } else if (tagColor.equals("ORANGE")) {
    // imageView.setImageResource(R.drawable.icon_mession_orange);
    // } else if (tagColor.equals("YELLOW")) {
    // imageView.setImageResource(R.drawable.icon_mession_yellow);
    // } else if (tagColor.equals("GREEN")) {
    // imageView.setImageResource(R.drawable.icon_mession_green);
    // } else if (tagColor.equals("BLUE")) {
    // imageView.setImageResource(R.drawable.icon_mession_blue);
    // } else if (tagColor.equals("PURPLE")) {
    // imageView.setImageResource(R.drawable.icon_mession_purple);
    // }
    // }

    class TagAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return tagColorBeans.size();
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
            int positionChoose = setListView.getCheckedItemPosition();
            convertView = LayoutInflater.from(MessionTagActivity.this).inflate(
                    R.layout.meession_setlist_item, null);
            ((TextView) (convertView.findViewById(R.id.mession_set_text)))
                    .setText(tagColorBeans.get(position).getContent());
            ImageView imageView = (ImageView) convertView
                    .findViewById(R.id.mession_tagcolor_img);
            String tagColor = tagColorBeans.get(position).getColor();
            TaskTagColorUtils.setTagColorImg(imageView, tagColor);
            // handleColorImg(imageView,position);
            ImageView selectImg = (ImageView) convertView
                    .findViewById(R.id.mession_ring_img);
            handleSelectImg(selectImg, positionChoose, position);
            return convertView;
        }

    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnDeleteTagSuccess() {
            super.returnDeleteTagSuccess();
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            saveTagsAfterDelete();
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void returnDeleteTagFail(String error, int errorCode) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(MessionTagActivity.this, error, errorCode);
        }

        @Override
        public void returnCreateTagSuccess() {
            super.returnCreateTagSuccess();
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void returnCreateTagFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(MessionTagActivity.this, error, errorCode);
        }

    }

}
