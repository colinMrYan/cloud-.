package com.inspur.emmcloud.ui.mine.card;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.CardStackAdapter;
import com.inspur.emmcloud.bean.mine.CardPackageBean;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.cache.CardPackageCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.cardstack.RxAdapterUpDownStackAnimator;
import com.inspur.emmcloud.widget.cardstack.RxCardStackView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

/**
 * Created by yufuchang on 2018/7/27.
 */
@ContentView(R.layout.activity_card_package)
public class CardPackageActivity extends BaseActivity  implements RxCardStackView.ItemExpendListener{
    @ViewInject(R.id.stackview_card_package)
    private RxCardStackView cardStackView;
    private CardStackAdapter cardStackAdapter;
    private LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        new CardPackageManager(this,loadingDialog).getCardPackageListFromNet();
        EventBus.getDefault().register(this);
    }

    private void initViews() {
        loadingDialog = new LoadingDialog(this);
        cardStackView.setItemExpendListener(this);
        cardStackAdapter = new CardStackAdapter(this);
        cardStackView.setAdapter(cardStackAdapter);
        cardStackView.setRxAdapterAnimator(new RxAdapterUpDownStackAnimator(cardStackView));
        cardStackAdapter.updateData(CardPackageCacheUtils.getSelectedCardPackageList(CardPackageActivity.this));
    }

    /**
     * 来自CardPackageManager
     * @param cardPackageBeanList
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateCardPackageUI(List<CardPackageBean> cardPackageBeanList) {
        reFreshCardPackage();
    }

    /**
     * 来自CardPackageSetActivity，当增减Card时会调用
     * @param cardPackageBean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateCardPackageUI(CardPackageBean cardPackageBean) {
        reFreshCardPackage();
    }

    /**
     * 刷新
     */
    private void reFreshCardPackage() {
        List<CardPackageBean> cardPackageBeanList = CardPackageCacheUtils.getSelectedCardPackageList(this);
        cardStackAdapter.updateData(cardPackageBeanList);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
            case R.id.txt_card_package_set:
                IntentUtils.startActivity(CardPackageActivity.this,CardPackageSetActivity.class);
                break;
        }
    }

    @Override
    public void onItemExpend(boolean expend) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}