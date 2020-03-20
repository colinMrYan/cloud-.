package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.contact.ContactOrg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: yufuchang
 * Date: 2020/3/18
 */
public class DepartMentAdpater extends BaseAdapter {
    private List<ContactOrg> contactOrgList = new ArrayList<>();
    private Context context;
    private DepartMentDetailListener listener;

    public DepartMentAdpater(Context context, List<ContactOrg> contactOrgList){
        this.context = context;
        this.contactOrgList = contactOrgList;
    }

    @Override
    public int getCount() {
        return contactOrgList != null?contactOrgList.size():0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_department, null);
        ((TextView)convertView.findViewById(R.id.tv_user_department)).setText(contactOrgList.get(position).getName());
        convertView.findViewById(R.id.iv_user_depart_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onDepartMentDetailClickListener(position);
                }
            }
        });
        return convertView;
    }

    public void setDepartMentDetailListener(DepartMentDetailListener listener){
        this.listener = listener;
    }

    public interface DepartMentDetailListener{
        void onDepartMentDetailClickListener(int postion);
    }
}
