package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;

/**
 * Created by chenmch on 2018/12/20.
 */

public class MailLeftMenuFragment extends Fragment {
    private final int[] forderIcons = {R.drawable.ic_mail_inbox,R.drawable.ic_mail_outbox,R.drawable.ic_mail_outbox};
    private final String[]  forderNames= {"收件箱","已发送邮件","导入证书"};
    private ListView mailForderListView;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.mail_left_menu,null);
        initView(view);
        return view;
    }

    private void initView(View view){
        mailForderListView = (ListView)view.findViewById(R.id.lv_mail_forder);
        mailForderListView.setAdapter(adapter);
    }

    private BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return forderIcons.length;
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mail_forder_item_view,null);
            ImageView imageView = (ImageView)convertView.findViewById(R.id.iv_forder_icon);
            TextView textView = (TextView)convertView.findViewById(R.id.tv_forder_name);
            imageView.setImageResource(forderIcons[position]);
            textView.setText(forderNames[position]);
            return convertView;
        }
    };
}
