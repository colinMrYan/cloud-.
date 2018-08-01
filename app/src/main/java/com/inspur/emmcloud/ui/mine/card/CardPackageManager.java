package com.inspur.emmcloud.ui.mine.card;

import android.content.Context;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.mine.CardPackageBean;
import com.inspur.emmcloud.bean.mine.GetCardPackageResult;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.CardPackageCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/8/1.
 */

public class CardPackageManager {

    private Context context;
    private LoadingDialog loadingDialog;
    public CardPackageManager(Context context, LoadingDialog loadingDialog){
        this.context = context;
        this.loadingDialog = loadingDialog;
    }

    /**
     * 从网络获取package
     */
    public void getCardPackageListFromNet() {
        if(NetUtils.isNetworkConnected(context)){
            loadingDialog.show();
            MineAPIService mineAPIService = new MineAPIService(context);
            mineAPIService.setAPIInterface(new WebService());
            mineAPIService.getCardPackageList();
        }
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
            WebServiceMiddleUtils.hand(context,error,errorCode);
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
        List<CardPackageBean> cardPackageBeanListInCache = CardPackageCacheUtils.getCardPackageList(context);
        List<CardPackageBean> cardPackageBeanListSync = CardPackageCacheUtils.syncCardPackageStateList(cardPackageBeanListInCache,cardPackageBeanList);
        cardPackageBeanListInCache.removeAll(cardPackageBeanListSync);
        CardPackageCacheUtils.deleteCardPackageList(context,cardPackageBeanListInCache);
        CardPackageCacheUtils.saveCardPackageList(context,cardPackageBeanList);
        //发往CardPackageActivity
        EventBus.getDefault().post(cardPackageBeanList);
    }

}
