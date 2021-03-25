package com.inspur.emmcloud.login.ui.adapter;


import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.Enterprise;
import com.inspur.emmcloud.login.R;

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
        int paddingLeft = DensityUtil.dip2px(context, 36);
        int paddingRight = DensityUtil.dip2px(context, 21);
        int paddingTop = DensityUtil.dip2px(context, 12);
        enterpriseText.setPadding(paddingLeft, paddingTop, paddingRight, paddingTop);
        enterpriseText.setTextColor(ResourceUtils.getResValueOfAttr(context, R.attr.text_color));
        Enterprise enterprise = enterpriseList.get(position);
        enterpriseText.setText(enterprise.getName());
        return enterpriseText;
    }
}
