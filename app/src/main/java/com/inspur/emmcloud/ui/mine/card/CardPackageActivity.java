package com.inspur.emmcloud.ui.mine.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.CardStackAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.mine.CardPackageBean;
import com.inspur.emmcloud.bean.mine.GetCardPackageResult;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.CardPackageCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.cardstack.RxAdapterUpDownStackAnimator;
import com.inspur.emmcloud.widget.cardstack.RxCardStackView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/7/27.
 */
@ContentView(R.layout.activity_card_package)
public class CardPackageActivity extends BaseActivity  implements RxCardStackView.ItemExpendListener{
    private static final int CARD_PACKAGE_SET_REQUEST = 1;
    @ViewInject(R.id.stackview_card_package)
    private RxCardStackView cardStackView;
    private CardStackAdapter cardStackAdapter;
    private LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarUtils.changeStateBarColor(this,R.color.content_bg);
        initViews();
        getCardPackageListFromNet();
    }

    /**
     * 初始化views
     */
    private void initViews() {
        loadingDialog = new LoadingDialog(this);
        cardStackView.setItemExpendListener(this);
        cardStackAdapter = new CardStackAdapter(this);
        cardStackView.setAdapter(cardStackAdapter);
        cardStackView.setRxAdapterAnimator(new RxAdapterUpDownStackAnimator(cardStackView));
        cardStackAdapter.updateData(CardPackageCacheUtils.getSelectedCardPackageList(CardPackageActivity.this));
    }

    /**
     * 刷新
     */
    private void reFreshCardPackage() {
        List<CardPackageBean> cardPackageBeanList = CardPackageCacheUtils.getSelectedCardPackageList(this);
        if(cardPackageBeanList.size() == 0){
            cardPackageBeanList = CardPackageCacheUtils.getCardPackageList(this);
            for (int i = 0; i < cardPackageBeanList.size(); i++) {
                cardPackageBeanList.get(i).setState(1);
            }
            CardPackageCacheUtils.saveCardPackageList(this,cardPackageBeanList);
        }
        cardStackAdapter.updateData(cardPackageBeanList);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
            case R.id.txt_card_package_set:
                Intent intent = new Intent();
                intent.setClass(CardPackageActivity.this,CardPackageSetActivity.class);
                startActivityForResult(intent,CARD_PACKAGE_SET_REQUEST);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == CARD_PACKAGE_SET_REQUEST){
            reFreshCardPackage();
        }
    }

    @Override
    public void onItemExpend(boolean expend) {
    }

    /**
     * 从网络获取package
     */
    public void getCardPackageListFromNet() {
        if(NetUtils.isNetworkConnected(this)){
            loadingDialog.show();
            MineAPIService mineAPIService = new MineAPIService(this);
            mineAPIService.setAPIInterface(new WebService());
            mineAPIService.getCardPackageList();
        }
    }

    /**
     * 处理网络获取的数据
     * 先同步缓存里和网络数据里的选中状态
     * 如果网络数据有删除，则剔除掉缓存中仍然存在的Card
     * 保存同步过的状态的Card数据
     * @param cardPackageBeanList
     */
    private void handleCardPackageData(ArrayList<CardPackageBean> cardPackageBeanList) {
        List<CardPackageBean> cardPackageBeanListInCache = CardPackageCacheUtils.getCardPackageList(this);
        List<CardPackageBean> cardPackageBeanListSync = CardPackageCacheUtils.syncCardPackageStateList(cardPackageBeanListInCache,cardPackageBeanList);
        cardPackageBeanListInCache.removeAll(cardPackageBeanListSync);
        CardPackageCacheUtils.deleteCardPackageList(this);
        CardPackageCacheUtils.saveCardPackageList(this,cardPackageBeanListSync);
        reFreshCardPackage();
    }


    class WebService extends APIInterfaceInstance {
        @Override
        public void returnCardPackageListSuccess(GetCardPackageResult getCardPackageResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            handleCardPackageData(getCardPackageResult.getCardPackageBeanList());
        }

        @Override
        public void returnCardPackageListFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(CardPackageActivity.this,error,errorCode);
        }
    }

}