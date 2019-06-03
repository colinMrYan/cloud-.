package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.util.List;

/**
 * Created by chenmch on 2018/4/11.
 */

public class ChannelInfoUtils {
    private GetChannelInfoCallBack getChannelInfoCallBack;
    private LoadingDialog loadingDlg;
    private String cid;

    public void getChannelInfo(Context context, String cid, LoadingDialog loadingDlg, GetChannelInfoCallBack getChannelInfoCallBack) {
        this.getChannelInfoCallBack = getChannelInfoCallBack;
        Channel channel = ChannelCacheUtils.getChannel(context, cid);
        if (channel != null) {
            callbackSuccess(channel);
        } else if (NetUtils.isNetworkConnected(context)) {
            loadingDlg.show();
            this.loadingDlg = loadingDlg;
            this.cid = cid;
            ChatAPIService apiService = new ChatAPIService(context);
            apiService.setAPIInterface(new WebService());
            String[] cidArray = {cid};
            apiService.getChannelGroupList(cidArray);
        } else {
            callbackFail("", -1);
        }
    }

    private void callbackSuccess(Channel channel) {
        LoadingDialog.dimissDlg(loadingDlg);
        if (getChannelInfoCallBack != null) {
            getChannelInfoCallBack.getChannelInfoSuccess(channel);
        }
    }

    private void callbackFail(String error, int errorCode) {
        LoadingDialog.dimissDlg(loadingDlg);
        if (getChannelInfoCallBack != null) {
            getChannelInfoCallBack.getChannelInfoFail(error, errorCode);
        }
    }

    public interface GetChannelInfoCallBack {
        void getChannelInfoSuccess(Channel channel);

        void getChannelInfoFail(String error, int errorCode);
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnSearchChannelGroupSuccess(
                GetSearchChannelGroupResult getSearchChannelGroupResult) {
            List<ChannelGroup> channelGroupList = getSearchChannelGroupResult.getSearchChannelGroupList();
            if (channelGroupList.size() != 0) {
                Channel channel = new Channel(channelGroupList.get(0));
                callbackSuccess(channel);
            } else {
                callbackFail("", -1);
            }

        }

        @Override
        public void returnSearchChannelGroupFail(String error, int errorCode) {
            Channel channel = ChannelCacheUtils.getChannel(MyApplication.getInstance(), cid);
            if (channel == null) {
                callbackFail(error, errorCode);
            } else {
                callbackSuccess(channel);
            }
        }
    }
}
