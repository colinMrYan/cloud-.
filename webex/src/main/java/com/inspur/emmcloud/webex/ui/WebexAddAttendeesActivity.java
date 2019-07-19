package com.inspur.emmcloud.webex.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.webex.R;
import com.inspur.emmcloud.webex.R2;
import com.inspur.emmcloud.webex.bean.WebexAttendees;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/10/11.
 */
public class WebexAddAttendeesActivity extends BaseActivity {
    public static final String EXTRA_ATTENDEES_LIST = "attendeesList";
    private static final int REQUEST_ADD_INTERNAL_ATTENDEES = 1;
    private static final int REQUEST_ADD_EXTERNAL_ATTENDEES = 2;
    @BindView(R2.id.lv_attendees)
    ScrollViewWithListView attendeesListView;
    @BindView(R2.id.sv_content)
    ScrollView contentScrollView;
    @BindView(R2.id.tv_num)
    TextView numText;
    private List<WebexAttendees> webexAttendeesList = new ArrayList<>();
    private Adapter adapter;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        webexAttendeesList = (List<WebexAttendees>) getIntent().getSerializableExtra(EXTRA_ATTENDEES_LIST);
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
        return R.layout.webex_activity_add_attendees;
    }

    @Nullable
    public void onClick(View v) {
        Intent intent = new Intent();
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.tv_complete) {
            intent.putExtra(EXTRA_ATTENDEES_LIST, (Serializable) webexAttendeesList);
            setResult(Activity.RESULT_OK, intent);
            finish();

        } else if (i == R.id.rl_add_external_attendees) {
            intent.setClass(WebexAddAttendeesActivity.this, WebexAddExternalAttendeesActivity.class);
            intent.putExtra(WebexAddAttendeesActivity.EXTRA_ATTENDEES_LIST, (Serializable) webexAttendeesList);
            startActivityForResult(intent, REQUEST_ADD_EXTERNAL_ATTENDEES);

        } else if (i == R.id.rl_add_internal_attendees) {
            intent.putExtra("title", 2);
            intent.putExtra("isMulti_select", true);
            intent.putExtra("isContainMe", true);
            intent.putExtra("hasSearchResult", getString(R.string.meeting_invating_members));
            intent.putExtra("select_limit", 20 - getExternalAttendeeList().size());
            intent.putExtra("hasSearchResult", (Serializable) getInternalAttendeesSearchModelLsit());
            ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_SEARCH).navigation(this, REQUEST_ADD_INTERNAL_ATTENDEES);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ADD_INTERNAL_ATTENDEES) {
                if (data.getExtras().containsKey("selectMemList")) {
                    List<SearchModel> selectMemList = (List<SearchModel>) data.getExtras()
                            .getSerializable("selectMemList");
                    for (SearchModel searchModel : selectMemList) {
                        if (StringUtils.isBlank(searchModel.getEmail())) {
                            Router router = Router.getInstance();
                            if (router.getService(ContactService.class) != null) {
                                ContactService service = router.getService(ContactService.class);
                                ContactUser contactUser = service.getContactUserByUid(searchModel.getId());
                                searchModel.setEmail(contactUser.getEmail());
                            }

                        }
                    }
                    List<WebexAttendees> externalWebexAttendeesList = getExternalAttendeeList();
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
            } else if (requestCode == REQUEST_ADD_EXTERNAL_ATTENDEES) {
                List<WebexAttendees> selectWebexAttendeesList = (List<WebexAttendees>) data.getExtras().getSerializable(EXTRA_ATTENDEES_LIST);
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

    private List<WebexAttendees> getExternalAttendeeList() {
        List<WebexAttendees> targetWebexAttendeesList = new ArrayList<>();
        for (WebexAttendees webexAttendees : webexAttendeesList) {
            if (webexAttendees.getSearchModel() == null) {
                Router router = Router.getInstance();
                if (router.getService(ContactService.class) != null) {
                    ContactService service = router.getService(ContactService.class);
                    ContactUser contactUser = service.getContactUserByMail(webexAttendees.getEmail());
                    if (contactUser == null) {
                        targetWebexAttendeesList.add(webexAttendees);
                    }
                }


            }
        }
        return targetWebexAttendeesList;
    }

    private List<SearchModel> getInternalAttendeesSearchModelLsit() {
        List<SearchModel> internalAttendeesSearchModelLsit = new ArrayList<>();
        for (WebexAttendees webexAttendees : webexAttendeesList) {
            if (webexAttendees.getSearchModel() != null) {
                internalAttendeesSearchModelLsit.add(webexAttendees.getSearchModel());
            } else {
                Router router = Router.getInstance();
                if (router.getService(ContactService.class) != null) {
                    ContactService service = router.getService(ContactService.class);
                    ContactUser contactUser = service.getContactUserByMail(webexAttendees.getEmail());
                    if (contactUser != null) {
                        internalAttendeesSearchModelLsit.add(new SearchModel(contactUser));
                    }
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
            convertView = LayoutInflater.from(WebexAddAttendeesActivity.this).inflate(R.layout.webex_item_view_add_attendees, null);
            TextView emailText = (TextView) convertView.findViewById(R.id.tv_attendees);
            ImageView deleteImg = (ImageView) convertView.findViewById(R.id.iv_delete);
            CircleTextImageView photoImg = (CircleTextImageView) convertView.findViewById(R.id.iv_photo);
            emailText.setText(webexAttendees.getEmail());
            if (webexAttendees.getSearchModel() != null) {
                ImageDisplayUtils.getInstance().displayImage(photoImg, BaseModuleApiUri.getUserPhoto(BaseApplication.getInstance(), webexAttendees.getSearchModel().getId()), R.drawable.icon_person_default);
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
