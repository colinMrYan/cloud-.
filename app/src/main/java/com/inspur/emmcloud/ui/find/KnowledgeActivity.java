package com.inspur.emmcloud.ui.find;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.FindAPIService;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.find.GetKnowledgeInfo;
import com.inspur.emmcloud.bean.find.KnowledgeInfo;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;

import java.util.ArrayList;
import java.util.List;


public class KnowledgeActivity extends BaseActivity {

    private ListView knowledgeListView;
    private List<KnowledgeInfo> knowledgeList = new ArrayList<KnowledgeInfo>();

    private FindAPIService apiService;
    private Adapter adapter;

    @Override
    public void onCreate() {
        ((TextView) findViewById(R.id.header_text)).setText(getString(R.string.knowledge));
        apiService = new FindAPIService(KnowledgeActivity.this);
        apiService.setAPIInterface(new WebService());
        setKnowlegeList();
        adapter = new Adapter();
        knowledgeListView = (ListView) findViewById(R.id.lv_file);
        knowledgeListView.setAdapter(adapter);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_file;
    }

    private void setKnowlegeList() {
        apiService.getKnowledgeList();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            default:
                break;
        }

    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return knowledgeList.size();
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
            LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.knowledge_item_view, null);
            TextView nameText = (TextView) convertView
                    .findViewById(R.id.tv_name);
            ImageView knowledgeImageView = (ImageView) convertView.findViewById(R.id.knowledge_img);
            nameText.setText(knowledgeList.get(position).getName());
            ImageDisplayUtils.getInstance().displayImage(knowledgeImageView, APIUri.getPreviewUrl(knowledgeList.get(position).getIcon()), R.drawable.icon_photo_default);
            return convertView;
        }

    }

    class WebService extends APIInterfaceInstance {

        @Override
        public void returnKnowledgeListSuccess(GetKnowledgeInfo getKnowledgeInfo) {
            // TODO Auto-generated method stub
            super.returnKnowledgeListSuccess(getKnowledgeInfo);
            knowledgeList.addAll(getKnowledgeInfo.getKnowLedgeLists());
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnKnowledgeListFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            WebServiceMiddleUtils.hand(KnowledgeActivity.this, error, errorCode);
        }

    }

}
