package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.login.ClusterBean;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;

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
        List<ClusterBean> clusterBeanList = enterprise.getClusterBeanList();
//        initClusterBaseUrl();
        for (int i = 0; i < clusterBeanList.size(); i++) {
            switch (clusterBeanList.get(i).getServiceName()) {
                case "com.inspur.ecm":
                    MyApplication.getInstance().setClusterEcm(clusterBeanList.get(i).getBaseUrl() + "/");
                    break;
                case "com.inspur.emm":
                    MyApplication.getInstance().setClusterEmm(clusterBeanList.get(i).getBaseUrl() + "/");
                    break;
                //聊天相关
                case "com.inspur.ecm.chat":
                    MyApplication.getInstance().setClusterChat(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),"chat"));
                    break;
                //会议，日历，任务相关
                case "com.inspur.ecm.schedule":
                    MyApplication.getInstance().setClusterSchedule(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),"schedule"));
                    break;
                //tab，RN，闪屏分发相关
                case "com.inspur.ecm.distribution":
                    MyApplication.getInstance().setClusterDistribution(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),"distribution"));
                    break;
                //浪潮个性化相关
                case "com.inspur.ecm.news":
                    MyApplication.getInstance().setClusterNews(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),"news"));
                    break;
                //云盘
                case "com.inspur.ecm.cloud-drive":
                    MyApplication.getInstance().setClusterCloudDrive(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),"cloud_drive"));
                    break;
                //文件服务相关
                case "com.inspur.ecm.storage.legacy":
                    MyApplication.getInstance().setClusterStorageLegacy(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),"storage_legacy"));
                    break;
            }
        }
    }


    /**
     * 把baseurl初始化为默认值，防止切企业时ecm和emm有一个没有改变影响切完企业后的地址
     */
    private static void initClusterBaseUrl() {
        MyApplication.getInstance().setClusterEcm(Constant.DEFAULT_CLUSTER_ECM);
        MyApplication.getInstance().setClusterEmm(Constant.DEFAULT_CLUSTER_EMM);
    }

    /**
     * 查找上一版的
     * @return
     */
    private static Enterprise getOldEnterprise() {
        String myInfo = PreferencesUtils.getString(MyApplication.getInstance().getApplicationContext(),
                Constant.MY_INFO_OLD,"");
        if (!StringUtils.isBlank(myInfo)) {
            GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
            String currentEnterpriseId = PreferencesByUsersUtils.getString(MyApplication.getInstance().getApplicationContext(), "current_enterprise_id");
            if (!StringUtils.isBlank(currentEnterpriseId)) {
                List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
                for (int i = 0; i < enterpriseList.size(); i++) {
                    Enterprise enterprise = enterpriseList.get(i);
                    if (enterprise.getId().equals(currentEnterpriseId)) {
                        return enterprise;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 返回合适的Url
     */
    private static String getSuitableUrl(String url,String type){
        String suitableUrl = url;
        if(StringUtils.isBlank(suitableUrl)){
            Enterprise enterprise = getOldEnterprise();
            List<ClusterBean> clusterBeanList = enterprise.getClusterBeanList();
            switch (type){
                case "chat":
                    suitableUrl = clusterBeanList.get(1).getBaseUrl();
                    break;
                case "schedule":

                    break;
                case "distribution":

                    break;
                case "news":

                    break;
                case "cloud_drive":

                    break;
                case "storage_legacy":

                    break;
            }
        }
        return suitableUrl;
    }

}
