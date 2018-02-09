package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.login.ClusterBean;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/2/6.
 */

public class MutilClusterUtils {
    /**
     * 修改多云基地址，如果没有基地址则取默认
     *
     * @param enterprise
     */
    public static void changeClusterBaseUrl(Enterprise enterprise) {
        List<String> clusterStringList = new ArrayList<>();
        clusterStringList.add("com.inspur.ecm");
        clusterStringList.add("com.inspur.emm");
        List<ClusterBean> clusterBeanList = enterprise.getClusterBeanList();
        initClusterBaseUrl();
        for (int i = 0; i < clusterBeanList.size(); i++) {
            switch (clusterBeanList.get(i).getServiceName()) {
                case "com.inspur.ecm":
                    MyApplication.getInstance().setClusterEcm(clusterBeanList.get(i).getBaseUrl() + "/");
                    break;
                case "com.inspur.emm":
                    MyApplication.getInstance().setClusterEmm(clusterBeanList.get(i).getBaseUrl() + "/");
                    break;
            }
        }
        LogUtils.YfcDebug("替换完成EMMURL："+MyApplication.getInstance().getClusterEmm());
        LogUtils.YfcDebug("替换完成ECMURL："+MyApplication.getInstance().getClusterEcm());
    }

    /**
     * 把baseurl初始化为默认值，防止切企业时ecm和emm有一个没有改变影响切完企业后的地址
     */
    private static void initClusterBaseUrl() {
        MyApplication.getInstance().setClusterEcm(Constant.DEFAULT_CLUSTER_ECM);
        MyApplication.getInstance().setClusterEmm(Constant.DEFAULT_CLUSTER_EMM);
    }
}
