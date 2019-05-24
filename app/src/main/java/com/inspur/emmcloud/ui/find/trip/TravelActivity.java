package com.inspur.emmcloud.ui.find.trip;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

public class TravelActivity extends BaseActivity {

    private ListView travelListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        travelListView = (ListView) findViewById(R.id.travel_analysis_list);
        travelListView.setAdapter(new TravelAdapter());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_travel;
    }

    public void onClick(View v) {
        finish();
    }

    class TravelAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 1;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            convertView = LayoutInflater.from(TravelActivity.this).inflate(
                    R.layout.travel_list_item, null);
            RelativeLayout travelStarsLayout = (RelativeLayout) convertView
                    .findViewById(R.id.travel_item_layout);

            travelStarsLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (position == 0) {
                        Intent intent = new Intent();
                        intent.setClass(TravelActivity.this, TravelStarsActivity.class);
                        startActivity(intent);
                    }

                }
            });
            return convertView;
        }

    }
}
