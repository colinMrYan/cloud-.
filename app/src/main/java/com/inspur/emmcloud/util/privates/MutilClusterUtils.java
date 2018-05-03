package com.inspur.emmcloud.util.privates;

import android.net.Uri;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.login.ClusterBean;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;

import java.util.List;

/**
 * Created by yufuchang on 2018/2/6.
 */

public class MutilClusterUtils {
    private static final String ECM_CHAT = "com.inspur.ecm.chat";
    private static final String ECM_SCHEDULE = "com.inspur.ecm.schedule";
    private static final String ECM_DISTRIBUTION = "com.inspur.ecm.distribution";
    private static final String ECM_NEWS = "com.inspur.ecm.news";
    private static final String ECM_CLOUD_DRIVER = "com.inspur.ecm.cloud-drive";
    private static final String ECM_STORAGE_LEGACY = "com.inspur.ecm.storage.legacy";
    private static final String ECM_OLD = "com.inspur.ecm";
    private static final String EMM_OLD = "com.inspur.emm";

    /**
     * 修改多云基地址，如果没有基地址则取默认
     *
     * @param enterprise
     */
    public static void changeClusterBaseUrl(Enterprise enterprise) {
        List<ClusterBean> clusterBeanList = enterprise.getClusterBeanList();
        for (int i = 0; i < clusterBeanList.size(); i++) {
            String serviceName = clusterBeanList.get(i).getServiceName();
            LogUtils.YfcDebug("ServiceName:"+serviceName);
            switch (serviceName) {
                //旧版ecm
                case ECM_OLD:
                    MyApplication.getInstance().setClusterEcm(clusterBeanList.get(i).getBaseUrl() + "/");
                    break;
                //旧版emm
                case EMM_OLD:
                    MyApplication.getInstance().setClusterEmm(clusterBeanList.get(i).getBaseUrl() + "/");
                    break;
                //聊天相关
                case ECM_CHAT:
                    MyApplication.getInstance().setClusterChat(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),enterprise,serviceName));
                    break;
                //会议，日历，任务相关
                case ECM_SCHEDULE:
                    MyApplication.getInstance().setClusterSchedule(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),enterprise,serviceName));
                    break;
                //tab，RN，闪屏分发相关
                case ECM_DISTRIBUTION:
                    MyApplication.getInstance().setClusterDistribution(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),enterprise,serviceName));
                    break;
                //浪潮个性化相关
                case ECM_NEWS:
                    MyApplication.getInstance().setClusterNews(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),enterprise,serviceName));
                    break;
                //云盘
                case ECM_CLOUD_DRIVER:
                    MyApplication.getInstance().setClusterCloudDrive(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),enterprise,serviceName));
                    break;
                //文件服务相关
                case ECM_STORAGE_LEGACY:
                    MyApplication.getInstance().setClusterStorageLegacy(getSuitableUrl(clusterBeanList.get(i).getBaseUrl(),enterprise,serviceName));
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
        return new Enterprise();
    }

    /**
     * 返回合适的Url
     */
    private static String getSuitableUrl(String url,Enterprise enterpriseNew,String type){
        String suitableUrl = url;
        if(!StringUtils.isBlank(suitableUrl)){
            List<ClusterBean> clusterBeanListNew = enterpriseNew.getClusterBeanList();
            ClusterBean clusterBeanWithType = new ClusterBean();
            clusterBeanWithType.setServiceName(type);
            int suitableUrlIndex = clusterBeanListNew.indexOf(clusterBeanWithType);
            if(suitableUrlIndex != -1){
                ClusterBean clusterBean = clusterBeanListNew.get(suitableUrlIndex);
                suitableUrl = getDivisionUrlByType(clusterBean,type);
                MyApplication.getInstance().setClusterVersion(clusterBean.getServiceVersion());
            }
        }else{
            Enterprise enterpriseOld = getOldEnterprise();
            List<ClusterBean> clusterBeanListOld = enterpriseOld.getClusterBeanList();
            int suitableUrlIndex = clusterBeanListOld.indexOf(type);
            if(suitableUrlIndex != -1){
                ClusterBean clusterBean = clusterBeanListOld.get(suitableUrlIndex);
                suitableUrl = getDivisionUrlByType(clusterBean,type);
            }
        }
        return suitableUrl;
    }

    /**
     * 获取区分类型的url
     * @param clusterBean
     * @param type
     */
    private static String getDivisionUrlByType(ClusterBean clusterBean, String type) {
        String suitableUrl = "";
        if(type.equals(ECM_CHAT)){
            Uri clusterBeanUri = Uri.parse(clusterBean.getBaseUrl());
            suitableUrl = clusterBeanUri.getScheme() + "://" + clusterBeanUri.getHost();
            MyApplication.getInstance().setSocketPath(clusterBeanUri.getPath());
        }else{
            suitableUrl = clusterBean.getBaseUrl();
        }
        return suitableUrl;
    }
}
