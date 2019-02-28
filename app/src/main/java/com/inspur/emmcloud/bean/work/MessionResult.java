package com.inspur.emmcloud.bean.work;

public class MessionResult {

    private String messionContent = "";
    private String messionRimind = "";
    private String messionLevel = "";
    private String messionColor = "";

    public void MessionResult(String reponse) {

    }

    public String getMessionContent() {
        return messionContent;
    }

    public void setMessionContent(String messionContent) {
        this.messionContent = messionContent;
    }

    public String getMessionRimind() {
        return messionRimind;
    }

    public void setMessionRimind(String messionRimind) {
        this.messionRimind = messionRimind;
    }

    public String getMessionLevel() {
        return messionLevel;
    }

    public void setMessionLevel(String messionLevel) {
        this.messionLevel = messionLevel;
    }

    public String getMessionColor() {
        return messionColor;
    }

    public void setMessionColor(String messionColor) {
        this.messionColor = messionColor;
    }


}
