package com.inspur.emmcloud.ui.find.trip;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TravelStarsActivity extends BaseActivity {


    @Override
    public void onCreate() {

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_travel_stars;
    }

    public void onClick(View v) {
        finish();
    }


    class TravelStarsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            convertView = LayoutInflater.from(TravelStarsActivity.this).inflate(R.layout.travel_list_item, null);
            return convertView;
        }

    }
}
