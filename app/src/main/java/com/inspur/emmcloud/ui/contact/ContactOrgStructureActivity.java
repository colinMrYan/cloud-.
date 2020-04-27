package com.inspur.emmcloud.ui.contact;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.bean.contact.ContactOrg;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactOrgCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 系統組織功能顯示
 **/
public class ContactOrgStructureActivity extends BaseActivity {
    private ContactUser contactUser;
    private OrgStrContactAdapter adapter;
    private ListView listView;
    private List<String> orgNameList = new ArrayList<>();

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
        String orgnaizeId = getIntent().getExtras().getString(UserInfoActivity.ORG_ID);
        orgNameList = new ArrayList<>();
            String root = "root";
            while (!root.equals(orgnaizeId)) {
                ContactOrg contactOrgTest = ContactOrgCacheUtils.getContactOrg(orgnaizeId);
                if (contactOrgTest == null) return;
                String orgName = contactOrgTest.getName();
                orgNameList.add(orgName);
                orgnaizeId = contactOrgTest.getParentId();
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
        convertView.setClickable(false);
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