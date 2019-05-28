package com.inspur.emmcloud.ui.mine.card;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.CardPackageAdapter;
import com.inspur.emmcloud.bean.mine.CardPackageBean;
import com.inspur.emmcloud.interf.OnCardPackageClickListener;
import com.inspur.emmcloud.util.privates.cache.CardPackageCacheUtils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/8/1.
 */
public class CardPackageSetActivity extends BaseActivity {

    @BindView(R.id.recyclerview_card_package)
    RecyclerView cardPackageRecyclerView;
    private CardPackageAdapter cardPackageAdapter;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_card_package_set;
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
                CardPackageCacheUtils.saveCardPackage(CardPackageSetActivity.this, cardPackageBean);
            }
        });
        cardPackageAdapter.setAndRefreshCardPackageAdapter(CardPackageCacheUtils.getCardPackageList(this));
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
