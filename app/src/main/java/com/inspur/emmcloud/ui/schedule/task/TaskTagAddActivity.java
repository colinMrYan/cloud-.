package com.inspur.emmcloud.ui.schedule.task;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onClick(View v){
        switch (v.getId()){
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

    class colorTagHolder{
       public TextView colorNameText;
       public ImageView colorFlagImage;
    }

    private class colorTagAdapter extends BaseAdapter{
        public colorTagAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return 0;
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
            return null;
        }
    }



}
