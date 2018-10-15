package com.inspur.emmcloud.ui.appcenter.webex;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_add_attendees)
public class WebexAddAttendeesActivity extends BaseActivity {
    public static final String EXTRA_ATTENDEES_LIST = "attendeesList";
    private static final int REQUEST_ADD_ATTENDEES = 1;
    @ViewInject(R.id.lv_attendees)
    private ScrollViewWithListView attendeesListView;
    @ViewInject(R.id.et_add_attendees)
    private EditText addAttendeesEdit;
    @ViewInject(R.id.rl_add_attendees)
    private RelativeLayout addAttendeesLayout;
    @ViewInject(R.id.sv_content)
    private ScrollView contentScrollView;
    @ViewInject(R.id.tv_num)
    private TextView numText;
    private ArrayList<String> attendeesList = new ArrayList<>();
    private Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attendeesList = getIntent().getStringArrayListExtra(EXTRA_ATTENDEES_LIST);
        numText.setText(getString(R.string.webex_add_invitee_num,20-attendeesList.size(),20));
        adapter = new Adapter();
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                addAttendeesLayout.setVisibility(attendeesList.size()==20?View.GONE:View.VISIBLE);
                numText.setText(getString(R.string.webex_add_invitee_num,20-attendeesList.size(),20));
            }
        });
        attendeesListView.setAdapter(adapter);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_complete:
                Intent intent = new Intent();
                intent.putStringArrayListExtra(EXTRA_ATTENDEES_LIST,attendeesList);
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.bt_add_attendees:
                String email = addAttendeesEdit.getText().toString();
                if (addAttendees(email)){
                    addAttendeesEdit.setText("");
                }

                break;
            case R.id.rl_add_attendees_from_contact:
                Intent intentContact = new Intent();
                intentContact.putExtra("select_content", 2);
                intentContact.putExtra("isMulti_select", false);
                intentContact.putExtra("isContainMe", true);
                intentContact.putExtra("title", getString(R.string.select_invitees));
                intentContact.setClass(getApplicationContext(), ContactSearchActivity.class);
                startActivityForResult(intentContact, REQUEST_ADD_ATTENDEES);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_ADD_ATTENDEES){
            if (data.getExtras().containsKey("selectMemList")) {
               List<SearchModel> selectMemList = (List<SearchModel>) data.getExtras()
                        .getSerializable("selectMemList");
               String uid = selectMemList.get(0).getId();
                ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
                String email = contactUser.getEmail();
                if (!StringUtils.isBlank(email)){
                    addAttendees(email);
                }

            }
        }
    }

    private boolean addAttendees(String email){
//        if (attendeesList.size() == 20) {
//            ToastUtils.show(MyApplication.getInstance(), "最多只能邀请20人");
//            return false;
//        }

        if (StringUtils.isBlank(email)) {
            ToastUtils.show(MyApplication.getInstance(), R.string.input_invitee_emails);
            return false;
        }
        if (!FomatUtils.isValiadEmail(email)) {
            ToastUtils.show(MyApplication.getInstance(), R.string.input_correct_invitee_emails);
            return false;
        }
        if (attendeesList.contains(email)) {
            ToastUtils.show(MyApplication.getInstance(), R.string.not_add_repeated);
            return false;
        }
        attendeesList.add(email);
        adapter.notifyDataSetChanged();
        // 滚动到页面最后
        contentScrollView.post(new Runnable() {
            public void run() {
                contentScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        return true;
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
