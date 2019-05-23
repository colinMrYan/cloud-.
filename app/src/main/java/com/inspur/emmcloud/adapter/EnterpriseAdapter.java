package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.mine.Enterprise;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/1/25.
 */

public class EnterpriseAdapter extends BaseAdapter {
    private Context context;
    private List<Enterprise> enterpriseList;
    private Holder holder;

    public EnterpriseAdapter(Context context, List<Enterprise> enterpriseList) {
        this.context = context;
        this.enterpriseList = enterpriseList;
    }

    @Override
    public int getCount() {
        return enterpriseList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mine_setting_enterprise_list_item, null);
            holder = new Holder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        Enterprise enterprise = enterpriseList.get(position);
        holder.enterpriseName.setText(enterprise.getName());
        if (enterprise.getId().equals(MyApplication.getInstance().getCurrentEnterprise().getId())) {
            holder.selectImg.setVisibility(View.VISIBLE);
        } else {
            holder.selectImg.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    public class Holder {
        @BindView(R.id.iv_select)
        ImageView selectImg;
        @BindView(R.id.tv_name)
        TextView enterpriseName;

        public Holder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
