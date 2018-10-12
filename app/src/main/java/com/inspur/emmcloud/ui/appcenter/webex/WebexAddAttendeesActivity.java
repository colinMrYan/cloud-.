package com.inspur.emmcloud.ui.appcenter.webex;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_add_attendees)
public class WebexAddAttendeesActivity extends BaseActivity {
    public static final String EXTRA_ATTENDEES_LIST = "attendeesList";
    @ViewInject(R.id.lv_attendees)
    private ScrollViewWithListView attendeesListView;
    @ViewInject(R.id.et_add_attendees)
    private EditText addAttendeesEdit;
    private ArrayList<String> attendeesList = new ArrayList<>();
    private Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attendeesList = getIntent().getStringArrayListExtra(EXTRA_ATTENDEES_LIST);
        adapter = new Adapter();
        attendeesListView.setAdapter(adapter);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_complete:
                if (attendeesList.size() == 0) {
                    ToastUtils.show(MyApplication.getInstance(), "请添加受邀者");
                    return;
                }
                Intent intent = new Intent();
                intent.putStringArrayListExtra(EXTRA_ATTENDEES_LIST,attendeesList);
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.iv_add_attendees:
                if (attendeesList.size() == 20) {
                    ToastUtils.show(MyApplication.getInstance(), "最多只能邀请20人");
                    return;
                }
                String email = addAttendeesEdit.getText().toString();
                if (StringUtils.isBlank(email)) {
                    ToastUtils.show(MyApplication.getInstance(), "请输入受邀者的电子邮箱地址");
                    return;
                }
                if (!FomatUtils.isValiadEmail(email)) {
                    ToastUtils.show(MyApplication.getInstance(), "请输入正确的的电子邮箱地址");
                    return;
                }
                if (attendeesList.contains(email)) {
                    ToastUtils.show(MyApplication.getInstance(), "不能重复添加");
                    return;
                }
                attendeesList.add(email);
                adapter.notifyDataSetChanged();
                addAttendeesEdit.setText("");
                break;
        }
    }

    public class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return attendeesList.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(WebexAddAttendeesActivity.this).inflate(R.layout.item_view_webex_attendees, null);
            TextView emailText = (TextView) convertView.findViewById(R.id.tv_attendees);
            ImageView deleteImg = (ImageView) convertView.findViewById(R.id.iv_delete);
            emailText.setText(attendeesList.get(position));
            deleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    attendeesList.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }
}
