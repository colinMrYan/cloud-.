package com.inspur.emmcloud.ui.find;

import java.util.ArrayList;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.system.AnalysisModel;
import com.inspur.emmcloud.ui.find.trip.TravelStarsActivity;
import com.inspur.emmcloud.util.common.IntentUtils;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 分析页面 com.inspur.emmcloud.ui.AnalysisActivity create at 2016年8月31日 下午2:37:52
 */
public class AnalysisActivity extends BaseActivity {

    private ListView analysisListView;
    private ArrayList<AnalysisModel> analysisList = new ArrayList<AnalysisModel>();
    private AnalysisAdapter analysisAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        analysisList.add(new AnalysisModel(getString(R.string.travel_analysis)));
        analysisListView = (ListView) findViewById(R.id.analysis_list);
        analysisAdapter = new AnalysisAdapter();
        analysisListView.setAdapter(analysisAdapter);
        analysisListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        analysisListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {
                    IntentUtils.startActivity(AnalysisActivity.this,
                            TravelStarsActivity.class);
                }
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_analysis;
    }

    public void onClick(View v) {
        finish();
    }

    class AnalysisAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return analysisList.size();
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
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            // TODO Auto-generated method stub
            convertView = LayoutInflater.from(AnalysisActivity.this).inflate(
                    R.layout.analysis_list_item, null);

            TextView analysisText = (TextView) convertView
                    .findViewById(R.id.analysis_item_text);
            analysisText.setText(analysisList.get(position).getAnalysisName());
            return convertView;
        }

    }
}
