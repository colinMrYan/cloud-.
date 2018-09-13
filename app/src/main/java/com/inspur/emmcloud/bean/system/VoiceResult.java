package com.inspur.emmcloud.bean.system;

/**
 * 语音转义结果类
 * Created by yufuchang on 2018/9/13.
 */

public class VoiceResult {
    private String results = "";
    private float seconds = 0;
    private String filePath = "";

    public VoiceResult(String results,float seconds,String filePath){
        this.results = results;
        this.seconds = seconds;
        this.filePath = filePath;
    }

    public VoiceResult(String results,float seconds){
        this(results,seconds,"");
    }

    public VoiceResult(String results){
        this(results,0,"");
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public float getSeconds() {
        return seconds;
    }

    public void setSeconds(float seconds) {
        this.seconds = seconds;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
