package com.inspur.emmcloud.ui.contact;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.List;


public class ContactOrgStructureActivity extends BaseActivity {
    private ContactUser contactUser;
    private OrgStrContactAdapter adapter;
    private ListView listView;
    private List<OrgItem> orgItemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LogUtils.LbcDebug("lbc setsetContentView11");
        setContentView(R.layout.activity_contact_org_structure);
        LogUtils.LbcDebug("lbc setsetContentView");
        init();
        LogUtils.LbcDebug("ini finished");
        adapter = new OrgStrContactAdapter(this, R.layout.contact_org_structure_item, orgItemsList);
        listView = (ListView) findViewById(R.id.org_structure_lv);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        init();
    }

    //点击返回
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.org_back_layout:
                finish();
        }
    }

    /***
     * 初始化Adapter数据
     * **/
    private void init() {
        Boolean fristInit = true;
        String uid = null;
        String scheme = getIntent().getScheme();
        if (scheme != null) {
            String uri = getIntent().getDataString();
            uid = uri.split("//")[1];
        } else if (getIntent().hasExtra("uid")) {
            uid = getIntent().getExtras().getString("uid");
        }
        LogUtils.LbcDebug( "lbc uid  :"+ uid);
        if (!StringUtils.isBlank(uid)) {
            contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
        }

            if (!StringUtils.isBlank(uid)) {
                orgItemsList = new ArrayList<>();
                List<String> strList = new ArrayList<>();
                String root = "root";
                String org3Str = contactUser.getParentId();
                while (!root.equals(org3Str)) {
                    ContactOrg contactOrgTest = ContactOrgCacheUtils.getContactOrg(org3Str);
                    org3Str = contactOrgTest.getName();
                    strList.add(org3Str);
                    org3Str = contactOrgTest.getParentId();
                   // LogUtils.LbcDebug(org3Str);
                }
                for (int i = (strList.size() - 1); i >= 0; i--) {
                    if (fristInit == true) {
                        OrgItem singleOrgItem = new OrgItem(strList.get(i), R.drawable.ic_org_structure_head_log, R.drawable.ic_org_structure_liner);
                        orgItemsList.add(singleOrgItem);
                        fristInit = false;
                    } else {
                        OrgItem singleOrgItem = new OrgItem(strList.get(i), R.drawable.ic_org_structure_mid_log, R.drawable.ic_org_structure_liner);
                        orgItemsList.add(singleOrgItem);
                    }
                }
            }
    }

}

/**
 * 类描述：
 * 每个组织信息
 **/
   class OrgItem {
    private String orgName;
    private int logImageId;
    private int lineImageId;

    public OrgItem(String name, int logId, int lineId) {
        this.orgName = name;
        this.logImageId = logId;
        this.lineImageId = lineId;
    }

    public String getName() {
        return orgName;
    }

    public int getLogImageId() {
        return logImageId;
    }

    public int getLineImageId() {
        return lineImageId;
    }
}

/**
 * Adapter描述：
 * 每个组织信息
 **/
class OrgStrContactAdapter extends ArrayAdapter {
    private final int resourceId;

    public OrgStrContactAdapter(Context context, int textViewResourceId, List<OrgItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OrgItem orgItem = (OrgItem) getItem(position); // 获取当前项的实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        ImageView logImage = (ImageView) view.findViewById(R.id.org_item_log_iv);
        ImageView lineImage = (ImageView) view.findViewById(R.id.org_item_line_iv);
        TextView Name = (TextView) view.findViewById(R.id.org_item_name_tv);
        if(position==(getCount()-1)){
            logImage.setImageResource(orgItem.getLogImageId());
            Name.setText(orgItem.getName());
        }else {
            logImage.setImageResource(orgItem.getLogImageId());
            lineImage.setImageResource(orgItem.getLineImageId());
            Name.setText(orgItem.getName());
        }
        return view;
    }
}