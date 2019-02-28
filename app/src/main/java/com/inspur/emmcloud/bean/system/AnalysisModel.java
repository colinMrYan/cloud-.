package com.inspur.emmcloud.bean.system;

public class AnalysisModel {

    private String analysisName = "";

    public AnalysisModel(String response) {
        this.analysisName = response;
    }

    public String getAnalysisName() {
        return analysisName;
    }

    public void setAnalysisName(String analysisName) {
        this.analysisName = analysisName;
    }


}
