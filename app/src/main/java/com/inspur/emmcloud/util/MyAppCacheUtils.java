package com.inspur.emmcloud.util;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inspur.emmcloud.bean.AppGroupBean;
import com.inspur.emmcloud.config.MyAppConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by yufuchang on 2017/8/15.
 */

public class MyAppCacheUtils {

    /**
     * 保存常用应用数据
     * @param context
     * @param appGroupList
     */
    public static void saveMyAppList(Context context, List<AppGroupBean> appGroupList){
        Gson gson=new Gson();
        String gsonString = gson.toJson(appGroupList);
//        String appList = JSON.toJSONString(appGroupList);
        LogUtils.YfcDebug("存储的应用文字信息："+appGroupList.get(2).getAppItemList().get(0).toString());
        writeData2File(gsonString,"before.txt");
        if(!gsonString.equals("null") && !StringUtils.isBlank(gsonString)){
            PreferencesByUserAndTanentUtils.putString(context,"my_app_list",gsonString);
        }



    }

    /**
     * 获取常用应用数据，字符串形式
     * @param context
     */
    public static String getMyAppsData(Context context){
        return PreferencesByUserAndTanentUtils.getString(context,"my_app_list","");
    }

    /**
     * 获取常用应用列表
     * @param context
     * @return
     */
    public static List<AppGroupBean> getMyApps(Context context){
        String appsString = PreferencesByUserAndTanentUtils.getString(context,"my_app_list","");
        Gson gson = new Gson();
//        gson.fromJson(appsString,AppGroupBean.class);
        List<AppGroupBean> appGroupBeenList = gson.fromJson(appsString, new TypeToken<List<AppGroupBean>>(){}.getType());
        writeData2File(appsString,"after.txt");
        return JSON.parseArray(appsString,AppGroupBean.class);
    }

    /**
     * 保存是否含有常用应用标志
     * @param hasCommonlyApp
     */
    public static void saveHasCommonlyApp(Context context,boolean hasCommonlyApp){
        PreferencesByUserAndTanentUtils.putBoolean(context,"is_has_commonly_app",hasCommonlyApp);
    }

    /**
     * 获取是否含有常用应用标志
     * @param context
     * @return
     */
    public  static boolean getHasCommonlyApp(Context context){
        return PreferencesByUserAndTanentUtils.getBoolean(context,"is_has_commonly_app",false);
    }


    private static void writeData2File(String content,String filename) {
        String filePath = MyAppConfig.LOCAL_CACHE_PATH;
        String fileName = filename;

        writeTxtToFile(content, filePath, fileName);
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath+fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
//            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
//            raf.seek(file.length());
//            raf.write(strContent.getBytes());
//            raf.close();


            FileOutputStream out = null;
            out = new FileOutputStream(file);
            out.write(strContent.getBytes());
            out.flush();
        } catch (Exception e) {
            LogUtils.YfcDebug("Error on write File:" + e);
        }
    }

    // 生成文件
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            LogUtils.YfcDebug( e+"");
        }
    }
}
