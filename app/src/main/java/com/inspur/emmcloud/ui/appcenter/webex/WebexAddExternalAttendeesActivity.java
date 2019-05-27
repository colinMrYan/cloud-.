package com.inspur.emmcloud.ui.appcenter.webex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.appcenter.webex.WebexAttendees;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;

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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/10/11.
 */
public class WebexAddExternalAttendeesActivity extends BaseActivity {
    public static final String EXTRA_ATTENDEES_LIST = "attendeesList";
    private static final int REQUEST_ADD_ATTENDEES = 1;
    @BindView(R.id.lv_attendees)
    ScrollViewWithListView attendeesListView;
    @BindView(R.id.et_add_attendees)
    EditText addAttendeesEdit;
    @BindView(R.id.rl_add_attendees)
    RelativeLayout addAttendeesLayout;
    @BindView(R.id.sv_content)
    ScrollView contentScrollView;
    @BindView(R.id.tv_num)
    TextView numText;
    private List<WebexAttendees> webexAttendeesList = new ArrayList<>();
    private List<WebexAttendees> externalWebexAttendeesList = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        webexAttendeesList = (List<WebexAttendees>) getIntent().getSerializableExtra(EXTRA_ATTENDEES_LIST);
        externalWebexAttendeesList = getExternalAttendeesList();
        numText.setText(getString(R.string.webex_add_invitee_num, webexAttendeesList.size(), 20 - webexAttendeesList.size()));
        adapter = new Adapter();
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                numText.setText(getString(R.string.webex_add_invitee_num, webexAttendeesList.size(), 20 - webexAttendeesList.size()));
            }
        });
        attendeesListView.setAdapter(adapter);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_webex_add_external_attendees;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_complete:
                Intent intent = new Intent();
                intent.putExtra(EXTRA_ATTENDEES_LIST, (Serializable) webexAttendeesList);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.bt_add_attendees:
                String email = addAttendeesEdit.getText().toString();
                if (addAttendees(email)) {
                    addAttendeesEdit.setText("");
                }

                break;

        }
    }

    private List<WebexAttendees> getExternalAttendeesList() {
        List<WebexAttendees> targetWebexAttendeesList = new ArrayList<>();
        for (WebexAttendees webexAttendees : webexAttendeesList) {
            if (webexAttendees.getSearchModel() == null) {
                targetWebexAttendeesList.add(webexAttendees);
            }
        }
        return targetWebexAttendeesList;
    }


    private boolean addAttendees(String email) {
        if (StringUtils.isBlank(email)) {
            ToastUtils.show(this, R.string.webex_input_invitee_emails);
            return false;
        }
        if (webexAttendeesList.size() == 20) {
            ToastUtils.show(this, R.string.contact_select_limit_warning);
            return false;
        }
        if (!FomatUtils.isValiadEmail(email)) {
            ToastUtils.show(this, R.string.webex_input_correct_invitee_emails);
            return false;
        }

        if (webexAttendeesList.contains(new WebexAttendees(email))) {
            ToastUtils.show(this, R.string.webex_not_add_repeated);
            return false;
        }

        WebexAttendees webexAttendees = new WebexAttendees(email);
        webexAttendeesList.add(webexAttendees);
        externalWebexAttendeesList.add(webexAttendees);
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
            return externalWebexAttendeesList.size();
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
            WebexAttendees webexAttendees = externalWebexAttendeesList.get(position);
            convertView = LayoutInflater.from(WebexAddExternalAttendeesActivity.this).inflate(R.layout.item_view_webex_add_attendees, null);
            TextView emailText = (TextView) convertView.findViewById(R.id.tv_attendees);
            ImageView deleteImg = (ImageView) convertView.findViewById(R.id.iv_delete);
            emailText.setText(webexAttendees.getEmail());
            deleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webexAttendeesList.remove(externalWebexAttendeesList.get(position));
                    externalWebexAttendeesList.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }
}
