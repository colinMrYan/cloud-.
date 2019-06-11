package com.inspur.emmcloud.bean.chat;

public class GetFileUploadResult {

    private String response = "";

    public GetFileUploadResult(String response) {
        this.response = response;
    }

    public String getFileMsgBody() {
        return response;
    }
}
