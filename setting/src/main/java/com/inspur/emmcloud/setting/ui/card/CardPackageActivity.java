package com.inspur.emmcloud.setting.ui.card;

import android.content.Intent;
import android.view.View;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.ui.NotSupportLand;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;
import com.inspur.emmcloud.setting.adapter.CardStackAdapter;
import com.inspur.emmcloud.setting.api.SettingAPIInterfaceImpl;
import com.inspur.emmcloud.setting.api.SettingAPIService;
import com.inspur.emmcloud.setting.bean.CardPackageBean;
import com.inspur.emmcloud.setting.bean.GetCardPackageResult;
import com.inspur.emmcloud.setting.util.CardPackageCacheUtils;
import com.inspur.emmcloud.setting.widget.cardstack.RxAdapterAllMoveDownAnimator;
import com.inspur.emmcloud.setting.widget.cardstack.RxCardStackView;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/7/27.
 */
public class CardPackageActivity extends BaseActivity implements RxCardStackView.ItemExpendListener, NotSupportLand {
    private static final int CARD_PACKAGE_SET_REQUEST = 1;
    @BindView(R2.id.stackview_card_package)
    RxCardStackView cardStackView;
    private CardStackAdapter cardStackAdapter;
    private LoadingDialog loadingDialog;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
        getCardPackageListFromNet();
        UMConfigure.init(this, "59aa1f8f76661373290010d3"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
//        QueuedWork.isUseThreadPool = false;
//        UMShareAPI.get(this);
        PlatformConfig.setWeixin(Constant.WECHAT_APPID, "56a0426315f1d0985a1cc1e75e96130d");
        PlatformConfig.setQQZone("1105561850", "1kaw4r1c37SUupFL");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_card_package_activity;
    }

    @Override
    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }

    /**
     * 初始化views
     */
    private void initViews() {
        loadingDialog = new LoadingDialog(this);
        cardStackView.setItemExpendListener(this);
        cardStackAdapter = new CardStackAdapter(this);
        cardStackView.setAdapter(cardStackAdapter);
        cardStackView.setRxAdapterAnimator(new RxAdapterAllMoveDownAnimator(cardStackView));
        cardStackAdapter.updateData(CardPackageCacheUtils.getSelectedCardPackageList(CardPackageActivity.this));
    }

    /**
     * 刷新
     */
    private void reFreshCardPackage() {
        List<CardPackageBean> cardPackageBeanList = CardPackageCacheUtils.getSelectedCardPackageList(this);
        if (cardPackageBeanList.size() == 0) {
            cardPackageBeanList = CardPackageCacheUtils.getCardPackageList(this);
            for (int i = 0; i < cardPackageBeanList.size(); i++) {
                cardPackageBeanList.get(i).setState(1);
            }
            CardPackageCacheUtils.saveCardPackageList(this, cardPackageBeanList);
        }
        cardStackAdapter.updateData(cardPackageBeanList);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.tv_set) {
            Intent intent = new Intent();
            intent.setClass(CardPackageActivity.this, CardPackageSetActivity.class);
            startActivityForResult(intent, CARD_PACKAGE_SET_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CARD_PACKAGE_SET_REQUEST) {
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
        if (NetUtils.isNetworkConnected(this)) {
            if (CardPackageCacheUtils.getCardPackageList(this).size() == 0) {
                loadingDialog.show();
            }
            SettingAPIService mineAPIService = new SettingAPIService(this);
            mineAPIService.setAPIInterface(new WebService());
            mineAPIService.getCardPackageList();
        }
    }

    /**
     * 处理网络获取的数据
     * 先同步缓存里和网络数据里的选中状态
     * 如果网络数据有删除，则剔除掉缓存中仍然存在的Card
     * 保存同步过的状态的Card数据
     *
     * @param cardPackageBeanList
     */
    private void handleCardPackageData(ArrayList<CardPackageBean> cardPackageBeanList) {
        List<CardPackageBean> cardPackageBeanListInCache = CardPackageCacheUtils.getCardPackageList(this);
        List<CardPackageBean> cardPackageBeanListSync = CardPackageCacheUtils.syncCardPackageStateList(cardPackageBeanListInCache, cardPackageBeanList);
        cardPackageBeanListInCache.removeAll(cardPackageBeanListSync);
        CardPackageCacheUtils.deleteCardPackageList(this);
        CardPackageCacheUtils.saveCardPackageList(this, cardPackageBeanListSync);
        reFreshCardPackage();
    }


    class WebService extends SettingAPIInterfaceImpl {
        @Override
        public void returnCardPackageListSuccess(GetCardPackageResult getCardPackageResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            handleCardPackageData(getCardPackageResult.getCardPackageBeanList());
        }

        @Override
        public void returnCardPackageListFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDialog);
            WebServiceMiddleUtils.hand(CardPackageActivity.this, error, errorCode);
        }
    }

}