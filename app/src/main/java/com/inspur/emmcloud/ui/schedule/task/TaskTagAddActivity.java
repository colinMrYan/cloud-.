package com.inspur.emmcloud.ui.schedule.task;

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
import com.inspur.emmcloud.bean.work.TagColorBean;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/4/9.
 */
@ContentView(R.layout.activity_task_tags_add)
public class TaskTagAddActivity extends BaseActivity {
    @ViewInject(R.id.lv_tag_color)
    ListView tagColorList;
    @ViewInject(R.id.tv_delecte_tag)
    TextView delecteTagText;
    @ViewInject(R.id.et_tag_name)
    TextView tagNameEdit;

    private List<TagColorBean> tagColorBeans = new ArrayList<>();
    private int selectIndex = -1;
    private ColorTagAdapter colorTagAdapter = new ColorTagAdapter();
    private ArrayList<String> messionTagList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
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
        String deleteTagName = getIntent().getStringExtra("title");
        String userId = ((MyApplication) getApplicationContext()).getUid();
        messionTagList = JSONUtils.JSONArray2List(PreferencesUtils.getString(TaskTagAddActivity.this, MyApplication.getInstance().getTanent() + userId + "messionTags", ""), new ArrayList<String>());
        if (getIntent().hasExtra("color")) {
          String  color = getIntent().getStringExtra("color");
           selectIndex = getTagColorIndex(tagColorBeans,color);
        }
        if (getIntent().hasExtra("title")) {
          String title = getIntent().getStringExtra("title");
            tagNameEdit.setText(title);
        }
        tagColorList.setAdapter(colorTagAdapter);
    }

    private int getTagColorIndex( List<TagColorBean> tagColorBeans,String color){
        int tagIndex=-1;
        for(int i=0;i<tagColorBeans.size();i++){
          if(tagColorBeans.get(i).getColor().equals(color))
              return i;
        }
        return tagIndex;
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_save:
                break;
            case R.id.tv_delecte_tag:
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


}
