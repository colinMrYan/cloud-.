package com.inspur.emmcloud.bean;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Transient;

import java.util.HashMap;

import lombok.Data;

/**
 * Created by yufuchang on 2017/5/18.
 */

@Data
public class SplashPageBean {

    private String versionCode = "";
    private String command = "";
    private String url = "";
//    private String name = "";

    @Id
    private String version;

//    @Column(nullable = false, length = 128, unique = true)
    private String name;

    private String res1x;

    private String res2x;

    private String res3x;

    private String mdpi;

    private String hdpi;

    private String xhdpi;

    private String xxhdpi;

    private String xxxhdpi;

    @Transient
    private HashMap<String, HashMap<String, String>> resource = new HashMap<>();
}
