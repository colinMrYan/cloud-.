package com.inspur.emmcloud.news.api;

import com.inspur.emmcloud.news.bean.GetGroupNewsDetailResult;
import com.inspur.emmcloud.news.bean.GetNewsInstructionResult;
import com.inspur.emmcloud.news.bean.GetNewsTitleResult;

public interface NewsAPIInterface {
    void returnGroupNewsTitleSuccess(GetNewsTitleResult getNewsTitleResult);

    void returnGroupNewsTitleFail(String error, int errorCode);

    void returnGroupNewsDetailSuccess(GetGroupNewsDetailResult getGroupNewsDetailResult, int page);

    void returnGroupNewsDetailFail(String error, int errorCode, int page);


    void returnNewsInstructionSuccess(GetNewsInstructionResult getNewsInstructionResult);

    void returnNewsInstructionFail(String error, int errorCode);

}
