package com.inspur.emmcloud.schedule.ui.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/7/5.
 */

public class CalendarAccountSelectActivity extends BaseActivity {
    private static final int REQUEST_EXCHANGE_LOGIN = 1;
    @BindView(R2.id.lv_calendar_account)
    ListView calendarAccountListView;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        calendarAccountListView.setAdapter(new Adapter());
        calendarAccountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString("from", "schedule_exchange_login");
                ARouter.getInstance().build(Constant.AROUTER_CLASS_MAIL_LOGIN).with(bundle).navigation(CalendarAccountSelectActivity.this, REQUEST_EXCHANGE_LOGIN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXCHANGE_LOGIN && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.schedule_activity_calendar_account_select;
    }

    private class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 1;
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
            convertView = LayoutInflater.from(CalendarAccountSelectActivity.this).inflate(R.layout.schedule_calendar_account_item_view, null);
            ImageView iconImg = convertView.findViewById(R.id.iv_icon);
            TextView accountText = convertView.findViewById(R.id.tv_calendar);
            accountText.setText("Exchange");
            return convertView;
        }
    }
}
