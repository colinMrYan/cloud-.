package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.mine.Enterprise;

import java.util.List;

/**
 * Created by chenmch on 2017/12/12.
 */

public class LoginSelectEnterpriseAdapter extends BaseAdapter {

    private Context context;
    private List<Enterprise> enterpriseList;
    private Enterprise defaultEnterprise;

    public LoginSelectEnterpriseAdapter(Context context,List<Enterprise> enterpriseList,Enterprise defaultEnterprise){
        this.context =context;
        this.enterpriseList = enterpriseList;
        this.defaultEnterprise = defaultEnterprise;
    }

    @Override
    public int getCount() {
        return enterpriseList.size();
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
        Enterprise enterprise = enterpriseList.get(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.mine_setting_enterprise_item_view, null);
        TextView enterpriseText = (TextView)convertView.findViewById(R.id.enterprise_text);
        enterpriseText.setText(enterprise.getName());
        convertView.findViewById(R.id.img).setVisibility(View.GONE);
        return convertView;
    }
}
