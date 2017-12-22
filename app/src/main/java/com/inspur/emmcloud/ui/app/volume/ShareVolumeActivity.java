package com.inspur.emmcloud.ui.app.volume;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.Volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.Volume.Volume;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

/**
 * 云盘-共享网盘列表页面
 */

@ContentView(R.layout.activity_share_volume)
public class ShareVolumeActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener{

    private List<Volume> shareVolumeList;

    @ViewInject(R.id.share_volume_list)
    private ListView shareVolumeListView;

    @ViewInject(R.id.refresh_layout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shareVolumeList = (List<Volume>)getIntent().getExtras().getSerializable("shareVolumeList");
        adapter = new Adapter();
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.header_bg), ContextCompat.getColor(getApplicationContext(), R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(this);
        shareVolumeListView.setAdapter(adapter);
        shareVolumeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Volume volume = shareVolumeList.get(position);
            }
        });
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.back_layout:
                finish();
                break;
            case R.id.add_img:
                break;
            default:
                    break;
        }
    }


    private class Adapter extends BaseAdapter{
        @Override
        public int getCount() {
            return shareVolumeList.size();
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
            convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.app_volume_share_item_view,null);
            ((TextView)convertView.findViewById(R.id.name_text)).setText(shareVolumeList.get(position).getName());
            return convertView;
        }
    }

    @Override
    public void onRefresh() {
        getVolumeList();
    }

    /**
     * 获取云盘列表
     */
    private void getVolumeList() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            MyAppAPIService apiService = new MyAppAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.getVolumeList();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnVolumeListSuccess(GetVolumeListResult getVolumeListResult) {
            swipeRefreshLayout.setRefreshing(false);
            shareVolumeList = getVolumeListResult.getShareVolumeList();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnVolumeListFail(String error, int errorCode) {
            swipeRefreshLayout.setRefreshing(false);
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
