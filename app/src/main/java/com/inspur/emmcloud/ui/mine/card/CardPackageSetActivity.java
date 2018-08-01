package com.inspur.emmcloud.ui.mine.card;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.CardPackageAdapter;
import com.inspur.emmcloud.bean.mine.CardPackageBean;
import com.inspur.emmcloud.interf.OnCardPackageClickListener;
import com.inspur.emmcloud.util.privates.cache.CardPackageCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by yufuchang on 2018/8/1.
 */
@ContentView(R.layout.activity_card_package_set)
public class CardPackageSetActivity extends BaseActivity {

    @ViewInject(R.id.recyclerview_card_package)
    private RecyclerView cardPackageRecyclerView;
    private CardPackageAdapter cardPackageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cardPackageRecyclerView.setLayoutManager(layoutManager);
        cardPackageAdapter = new CardPackageAdapter(this);
        cardPackageRecyclerView.setAdapter(cardPackageAdapter);
        cardPackageAdapter.setOnCardPackageClickListener(new OnCardPackageClickListener() {
            @Override
            public void onCardPackageClick(CardPackageBean cardPackageBean) {
                CardPackageCacheUtils.saveCardPackage(CardPackageSetActivity.this,cardPackageBean);
                //发往CardPackageActivity
                EventBus.getDefault().post(cardPackageBean);
            }
        });
        cardPackageAdapter.setAndRefreshCardPackageAdapter(CardPackageCacheUtils.getCardPackageList(this));
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
