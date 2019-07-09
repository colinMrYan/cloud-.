package com.inspur.emmcloud.news.api;

import com.inspur.emmcloud.news.bean.GetGroupNewsDetailResult;
import com.inspur.emmcloud.news.bean.GetNewsInstructionResult;
import com.inspur.emmcloud.news.bean.GetNewsTitleResult;
import com.inspur.emmcloud.news.bean.GetSendMsgResult;

public class NewsAPIInsterfaceImpl implements NewsAPIInterface {
    @Override
    public void returnGroupNewsTitleSuccess(GetNewsTitleResult getNewsTitleResult) {

    }

    @Override
    public void returnGroupNewsTitleFail(String error, int errorCode) {

    }

    @Override
    public void returnGroupNewsDetailSuccess(GetGroupNewsDetailResult getGroupNewsDetailResult, int page) {

    }

    @Override
    public void returnGroupNewsDetailFail(String error, int errorCode, int page) {

    }

    @Override
    public void returnNewsInstructionSuccess(GetNewsInstructionResult getNewsInstructionResult) {

    }

    @Override
    public void returnNewsInstructionFail(String error, int errorCode) {

    }

    @Override
    public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult, String fakeMessageId) {

    }

    @Override
    public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {

    }
}
