package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.login.ClusterBean;
import com.inspur.emmcloud.bean.mine.Enterprise;

import java.util.List;

/**
 * Created by yufuchang on 2018/2/6.
 */

public class MutilClusterUtils {
    /**
     * 修改多云基地址，如果没有基地址则取默认
     * @param enterprise
     */
    public static void changeClouldBaseUrl(Enterprise enterprise){
        List<ClusterBean> clusterBeanList = enterprise.getClusterBeanList();
        if(clusterBeanList.size()>0){
            for(int i = 0; i < clusterBeanList.size(); i++){
                switch (clusterBeanList.get(i).getServiceName()){
                    case "com.inspur.ecm":
                        APIUri.URL_BASE_ECM = clusterBeanList.get(i).getBaseUrl()+"/";
                        break;
                    case "com.inspur.emm":
                        APIUri.URL_BASE_EMM = clusterBeanList.get(i).getBaseUrl()+"/";
                        break;
                }
            }
        }else{
            APIUri.URL_BASE_EMM = "https://ecm.inspur.com/";
            APIUri.URL_BASE_ECM = "https://emm.inspur.com/";
        }
    }
}
