package com.inspur.emmcloud.ui.appcenter.webex;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.appcenter.webex.WebexAttendees;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.ScrollViewWithListView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_add_attendees)
public class WebexAddAttendeesActivity extends BaseActivity {
    public static final String EXTRA_ATTENDEES_LIST = "attendeesList";
    private static final int REQUEST_ADD_INTERNAL_ATTENDEES = 1;
    private static final int REQUEST_ADD_EXTERNAL_ATTENDEES = 2;
    @ViewInject(R.id.lv_attendees)
    private ScrollViewWithListView attendeesListView;
    @ViewInject(R.id.rl_add_attendees)
    private RelativeLayout addAttendeesLayout;
    @ViewInject(R.id.sv_content)
    private ScrollView contentScrollView;
    @ViewInject(R.id.tv_num)
    private TextView numText;
    private List<WebexAttendees> webexAttendeesList = new ArrayList<>();
    private Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webexAttendeesList = (List<WebexAttendees>) getIntent().getSerializableExtra(EXTRA_ATTENDEES_LIST);
        numText.setText(getString(R.string.webex_add_invitee_num, 20 - webexAttendeesList.size(), 20));
        adapter = new Adapter();
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                numText.setText(getString(R.string.webex_add_invitee_num, 20 - webexAttendeesList.size(), 20));
            }
        });
        attendeesListView.setAdapter(adapter);
    }


    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.tv_complete:
                intent.putExtra(EXTRA_ATTENDEES_LIST, (Serializable) webexAttendeesList);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.rl_add_external_attendees:
                intent.setClass(WebexAddAttendeesActivity.this,WebexAddExternalAttendeesActivity.class);
                intent.putExtra(WebexAddAttendeesActivity.EXTRA_ATTENDEES_LIST, (Serializable)webexAttendeesList);
                startActivityForResult(intent, REQUEST_ADD_EXTERNAL_ATTENDEES);
                break;
            case R.id.rl_add_internal_attendees:
                intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                intent.putExtra(ContactSearchFragment.EXTRA_CONTAIN_ME, true);
                intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.meeting_invating_members));
                intent.setClass(getApplicationContext(), ContactSearchActivity.class);
                intent.putExtra(ContactSearchFragment.EXTRA_LIMIT,20-getAttendeesList(true).size());
                intent.putExtra(ContactSearchFragment.EXTRA_HAS_SELECT, (Serializable) getInternalAttendeesSearchModelLsit());
                startActivityForResult(intent, REQUEST_ADD_INTERNAL_ATTENDEES);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_INTERNAL_ATTENDEES) {
                if (data.getExtras().containsKey("selectMemList")) {
                    List<SearchModel> selectMemList = (List<SearchModel>) data.getExtras()
                            .getSerializable("selectMemList");
                    for (SearchModel searchModel:selectMemList){
                        if (StringUtils.isBlank(searchModel.getEmail())){
                            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(searchModel.getId());
                            searchModel.setEmail(contactUser.getEmail());
                        }
                    }
                    List<WebexAttendees> externalWebexAttendeesList = getAttendeesList(true);
                    List<WebexAttendees> internalWebexAttendeesLsit = WebexAttendees.SearchModelList2WebexAttendeesList(selectMemList);
                    internalWebexAttendeesLsit.removeAll(externalWebexAttendeesList);
                    internalWebexAttendeesLsit.addAll(externalWebexAttendeesList);
                    webexAttendeesList.clear();
                    webexAttendeesList.addAll(internalWebexAttendeesLsit);
                    adapter.notifyDataSetChanged();
                    contentScrollView.post(new Runnable() {
                        public void run() {
                            contentScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
            }else if (requestCode == REQUEST_ADD_EXTERNAL_ATTENDEES){
                List<WebexAttendees> selectWebexAttendeesList = (List<WebexAttendees>)data.getExtras().getSerializable(EXTRA_ATTENDEES_LIST);
                webexAttendeesList.clear();
                webexAttendeesList.addAll(selectWebexAttendeesList);
                adapter.notifyDataSetChanged();
                contentScrollView.post(new Runnable() {
                    public void run() {
                        contentScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        }
    }

    private List<WebexAttendees> getAttendeesList(boolean isExternal) {
        List<WebexAttendees> targetWebexAttendeesList = new ArrayList<>();
        for (WebexAttendees webexAttendees : webexAttendeesList) {
            if ((webexAttendees.getSearchModel() == null) == isExternal) {
                targetWebexAttendeesList.add(webexAttendees);
            }
        }
        return targetWebexAttendeesList;
    }

    private List<SearchModel> getInternalAttendeesSearchModelLsit() {
        List<SearchModel> internalAttendeesSearchModelLsit = new ArrayList<>();
        for (WebexAttendees webexAttendees : webexAttendeesList) {
            if (webexAttendees.getSearchModel() != null) {
                internalAttendeesSearchModelLsit.add(webexAttendees.getSearchModel());
            }else {
                ContactUser contactUser = ContactUserCacheUtils.getContactUserByEmail(webexAttendees.getEmail());
                if (contactUser != null){
                    internalAttendeesSearchModelLsit.add(new SearchModel(contactUser));
                }
            }
        }
        return internalAttendeesSearchModelLsit;
    }

    public class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return webexAttendeesList.size();
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
            WebexAttendees webexAttendees = webexAttendeesList.get(position);
            convertView = LayoutInflater.from(WebexAddAttendeesActivity.this).inflate(R.layout.item_view_webex_attendees, null);
            TextView emailText = (TextView) convertView.findViewById(R.id.tv_attendees);
            ImageView deleteImg = (ImageView) convertView.findViewById(R.id.iv_delete);
            CircleTextImageView photoImg = (CircleTextImageView)convertView.findViewById(R.id.iv_photo);
            emailText.setText(webexAttendees.getEmail());
            if (webexAttendees.getSearchModel() != null){
                ImageDisplayUtils.getInstance().displayImage(photoImg, APIUri.getUserIconUrl(MyApplication.getInstance(),webexAttendees.getSearchModel().getId()),R.drawable.icon_person_default);
                emailText.setText(webexAttendees.getSearchModel().getName());
            }
            deleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webexAttendeesList.remove(position);
                    adapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }
}
