package com.inspur.emmcloud.ui.appcenter.volume;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by yufuchang on 2018/2/28.
 */
@ContentView(R.layout.activity_volume_permission)
public class VolumeFilePermissionManagerActivity extends BaseActivity{

    @ViewInject(R.id.volume_file_permission_recyclerview)
    protected RecyclerView groupRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
        }
    }

    private void getVolumeFileGroup(){

    }
}
