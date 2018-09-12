package com.inspur.emmcloud.ui.contact;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import java.util.Collections;
import java.util.List;

    /**
     * 系統組織功能顯示
     * **/
public class ContactOrgStructureActivity extends BaseActivity {
    private ContactUser contactUser;
    private OrgStrContactAdapter adapter;
    private ListView listView;
    private List<String> orgNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_org_structure);
        init();
        adapter = new OrgStrContactAdapter(this, orgNameList);
        listView = (ListView) findViewById(R.id.org_structure_lv);
        listView.setAdapter(adapter);
    }

    /**
     * 返回按鈕
     * **/
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
        String uid = null;
        uid = getIntent().getExtras().getString("uid");
        if (!StringUtils.isBlank(uid)) {
            contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
        }
        if (!StringUtils.isBlank(uid)) {
            orgNameList = new ArrayList<>();
            String root = "root";
            String orgNameOrID = contactUser.getParentId();
            while (!root.equals(orgNameOrID)) {
                ContactOrg contactOrgTest = ContactOrgCacheUtils.getContactOrg(orgNameOrID);
                orgNameOrID = contactOrgTest.getName();
                orgNameList.add(orgNameOrID);
                orgNameOrID = contactOrgTest.getParentId();
            }
            Collections.reverse(orgNameList);
        }
    }
}

/**
 * Adapter描述：
 * 每个组织信息
 **/
class OrgStrContactAdapter extends BaseAdapter {

    private Context adpContext;
    private List<String> groupOrgList = new ArrayList<>();

    public OrgStrContactAdapter(Context context, List<String> orgNameAdpList) {
        adpContext = context;
        if (orgNameAdpList != null) {
            groupOrgList = orgNameAdpList;
        }
    }

    @Override
    public int getCount() {
        return groupOrgList == null ? 0 : groupOrgList.size();
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
        LogUtils.LbcDebug("getView");
        String currentOrgName = groupOrgList.get(position).toString();
        LayoutInflater inflater = LayoutInflater.from(adpContext);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.contact_org_structure_item, null);
        }

        ImageView logImage = (ImageView) convertView.findViewById(R.id.org_item_log_iv);
        ImageView lineImage = (ImageView) convertView.findViewById(R.id.org_item_line_iv);
        TextView Name = (TextView) convertView.findViewById(R.id.org_item_name_tv);
        logImage.setImageResource((position == 0) ? R.drawable.ic_org_structure_head_log : R.drawable.ic_org_structure_mid_log);
        lineImage.setImageResource(R.drawable.ic_org_structure_liner);
        lineImage.setVisibility(position == (getCount() - 1) ? View.GONE : View.VISIBLE);
        Name.setText(currentOrgName);
        return convertView;
    }
}