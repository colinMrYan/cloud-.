package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.bean.mine.Enterprise;

import java.util.List;

/**
 * Created by chenmch on 2017/12/12.
 */

public class LoginSelectEnterpriseAdapter extends BaseAdapter {

    private Context context;
    private List<Enterprise> enterpriseList;

    public LoginSelectEnterpriseAdapter(Context context, List<Enterprise> enterpriseList) {
        this.context = context;
        this.enterpriseList = enterpriseList;
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
        TextView enterpriseText = new TextView(context);
        enterpriseText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        enterpriseText.setSingleLine();
        int paddingLeft = DensityUtil.dip2px(MyApplication.getInstance(), 36);
        int paddingRight = DensityUtil.dip2px(MyApplication.getInstance(), 21);
        int paddingTop = DensityUtil.dip2px(MyApplication.getInstance(), 12);
        enterpriseText.setPadding(paddingLeft, paddingTop, paddingRight, paddingTop);
        enterpriseText.setTextColor(Color.parseColor("#333333"));
        Enterprise enterprise = enterpriseList.get(position);
        enterpriseText.setText(enterprise.getName());
        return enterpriseText;
    }
}
