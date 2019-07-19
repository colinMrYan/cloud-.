package com.inspur.mvp_demo.meeting.presenter;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.mvp.BasePresenter;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.mvp_demo.meeting.contract.MeetingContract;
import com.inspur.mvp_demo.meeting.model.api.ApiServiceImpl;
import com.inspur.mvp_demo.meeting.model.api.ApiUrl;
import com.inspur.mvp_demo.meeting.model.bean.GetMeetingListResult;
import com.inspur.mvp_demo.meeting.model.bean.Meeting;

import java.util.ArrayList;
import java.util.List;

public class MeetingHistoryPresenter extends BasePresenter<MeetingContract.View> implements MeetingContract.Presenter {
    private int pageNum = 1;
    private int currentPageSize = 50;
    private boolean isPullUp = false;
    private List<Meeting> meetingList = new ArrayList<>();
    private List<Meeting> uiMeetingList = new ArrayList<>();

    @Override
    public void getMeetingList(int pageNum) {
        if (!BaseApplication.getInstance().isHaveLogin()) {
            ARouter.getInstance().build("/login/main").navigation();
            return;
        }
        String completeUrl = ApiUrl.getMeetingHistoryUrl(pageNum);

        ApiServiceImpl.getInstance().getMeetingList(new BaseModuleAPICallback(mView.getContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                GetMeetingListResult getMeetingListResult = new GetMeetingListResult(new String(arg0));
                meetingList = getMeetingListResult.getMeetingList();
                uiMeetingList.clear();
                uiMeetingList.addAll(meetingList);
                mView.showMeetingList(uiMeetingList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                mView.showError(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingList(1);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                ApiServiceImpl.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        }, pageNum);
    }
}
