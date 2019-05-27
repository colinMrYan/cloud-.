package com.inspur.emmcloud.ui.contact;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 系統組織功能顯示
 **/
public class ContactOrgStructureActivity extends BaseActivity {
    private ContactUser contactUser;
    private OrgStrContactAdapter adapter;
    private ListView listView;
    private List<String> orgNameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        init();
        adapter = new OrgStrContactAdapter(this, orgNameList);
        listView = (ListView) findViewById(R.id.lv_org_structure);
        listView.setAdapter(adapter);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_contact_org_structure;
    }

    /**
     * 返回按鈕
     **/
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
        }
    }

    /***
     * 初始化Adapter数据
     * **/
    private void init() {
        String uid = null;
        uid = getIntent().getExtras().getString("uid");
        contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
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

/**
 * Adapter描述：
 * 每个组织信息
 **/
class OrgStrContactAdapter extends BaseAdapter {

    private Context adpContext;
    private List<String> groupOrgList;

    public OrgStrContactAdapter(Context context, List<String> orgNameAdpList) {
        adpContext = context;
        groupOrgList = orgNameAdpList;
    }

    @Override
    public int getCount() {
        return groupOrgList.size();
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
        convertView = LayoutInflater.from(adpContext).inflate(R.layout.contact_org_structure_item, null);
        ImageView logImage = (ImageView) convertView.findViewById(R.id.iv_org_item_flag);
        ImageView lineImage = (ImageView) convertView.findViewById(R.id.iv_org_item_line);
        TextView Name = (TextView) convertView.findViewById(R.id.tv_org_item_name);
        logImage.setImageResource((position == 0) ? R.drawable.ic_org_structure_head_log : R.drawable.ic_org_structure_mid_log);
        lineImage.setImageResource(R.drawable.ic_org_structure_liner);
        lineImage.setVisibility(position == (getCount() - 1) ? View.GONE : View.VISIBLE);
        Name.setText(groupOrgList.get(position).toString());
        return convertView;
    }
}