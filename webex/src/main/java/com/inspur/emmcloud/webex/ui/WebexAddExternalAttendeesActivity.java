package com.inspur.emmcloud.webex.ui;

import android.app.Activity;
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

import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.ImageViewRound;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.webex.R;
import com.inspur.emmcloud.webex.R2;
import com.inspur.emmcloud.webex.bean.WebexAttendees;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by chenmch on 2018/10/11.
 */
public class WebexAddExternalAttendeesActivity extends BaseActivity {
    public static final String EXTRA_ATTENDEES_LIST = "attendeesList";
    private static final int REQUEST_ADD_ATTENDEES = 1;
    @BindView(R2.id.lv_attendees)
    ScrollViewWithListView attendeesListView;
    @BindView(R2.id.et_add_attendees)
    EditText addAttendeesEdit;
    @BindView(R2.id.rl_add_attendees)
    RelativeLayout addAttendeesLayout;
    @BindView(R2.id.sv_content)
    ScrollView contentScrollView;
    @BindView(R2.id.tv_num)
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
        return R.layout.webex_activity_add_external_attendees;
    }

    @OnClick(R2.id.bt_add_attendees)
    public void onAddAttendeesClick(View v) {
        String email = addAttendeesEdit.getText().toString();
        if (addAttendees(email)) {
            addAttendeesEdit.setText("");
        }
    }


    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.tv_complete) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_ATTENDEES_LIST, (Serializable) webexAttendeesList);
            setResult(Activity.RESULT_OK, intent);
            finish();

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
            ToastUtils.show(this, R.string.input_correct_emails);
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
            convertView = LayoutInflater.from(WebexAddExternalAttendeesActivity.this).inflate(R.layout.webex_item_view_add_attendees, null);
            TextView emailText = (TextView) convertView.findViewById(R.id.tv_attendees);
            ImageView deleteImg = (ImageView) convertView.findViewById(R.id.iv_delete);
            ImageViewRound photoImg = (ImageViewRound) convertView.findViewById(R.id.iv_photo);
            photoImg.setType(ImageViewRound.TYPE_ROUND);
            photoImg.setRoundRadius(photoImg.dpTodx(6));
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
