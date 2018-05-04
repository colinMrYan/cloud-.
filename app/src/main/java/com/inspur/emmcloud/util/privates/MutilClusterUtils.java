package com.inspur.emmcloud.util.privates;

import android.net.Uri;

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
    private static final String ECM_CHAT = "com.inspur.ecm.chat";
    private static final String ECM_SCHEDULE = "com.inspur.ecm.schedule";
    private static final String ECM_DISTRIBUTION = "com.inspur.ecm.distribution";
    private static final String ECM_NEWS = "com.inspur.ecm.news";
    private static final String ECM_CLOUD_DRIVER = "com.inspur.ecm.cloud-drive";
    private static final String ECM_STORAGE_LEGACY = "com.inspur.ecm.storage.legacy";
    private static final String EMM_OLD = "com.inspur.emm";

    /**
     * 修改多云基地址，如果没有基地址则取默认
     *
     * @param enterprise
     */
    public static void setClusterBaseUrl(Enterprise enterprise) {
        List<ClusterBean> clusterBeanList = enterprise.getClusterBeanList();
        for (int i = 0; i < clusterBeanList.size(); i++) {
            String serviceName = clusterBeanList.get(i).getServiceName();
            String serviceUrl = getUrlByType(clusterBeanList.get(i), serviceName);
            switch (serviceName) {
                //旧版emm
                case EMM_OLD:
                    MyApplication.getInstance().setClusterEmm(clusterBeanList.get(i).getBaseUrl() + "/");
                    break;
                //聊天相关
                case ECM_CHAT:
                    MyApplication.getInstance().setClusterChat(serviceUrl);
                    break;
                //会议，日历，任务相关
                case ECM_SCHEDULE:
                    MyApplication.getInstance().setClusterSchedule(serviceUrl);
                    break;
                //tab，RN，闪屏分发相关
                case ECM_DISTRIBUTION:
                    MyApplication.getInstance().setClusterDistribution(serviceUrl);
                    break;
                //浪潮个性化相关
                case ECM_NEWS:
                    MyApplication.getInstance().setClusterNews(serviceUrl);
                    break;
                //云盘
                case ECM_CLOUD_DRIVER:
                    MyApplication.getInstance().setClusterCloudDrive(serviceUrl);
                    break;
                //文件服务相关
                case ECM_STORAGE_LEGACY:
                    MyApplication.getInstance().setClusterStorageLegacy(serviceUrl);
                    break;
            }
        }
    }

    /**
     * 返回合适的Url
     */
    private static String getUrlByType(ClusterBean clusterBeanNew, String serviceName) {
        String differentUrlByType = clusterBeanNew.getBaseUrl();
        ClusterBean clusterBean = clusterBeanNew;
        if (StringUtils.isBlank(differentUrlByType)) {
            Enterprise enterpriseOld = getOldEnterprise();
            List<ClusterBean> clusterBeanListOld = enterpriseOld.getClusterBeanList();
            ClusterBean clusterBeanIndex = new ClusterBean();
            clusterBeanIndex.setServiceName(serviceName);
            int clusterIndex = clusterBeanListOld.indexOf(clusterBeanIndex);
            if (clusterIndex != -1) {
                clusterBean = clusterBeanListOld.get(clusterIndex);
                differentUrlByType = clusterBean.getBaseUrl();
            }
        }
        if (serviceName.equals(ECM_CHAT)) {
            Uri clusterBeanUri = Uri.parse(differentUrlByType);
            MyApplication.getInstance().setClusterChat(differentUrlByType);
            String chatUrl = clusterBeanUri.getScheme() + "://" + clusterBeanUri.getAuthority();
            MyApplication.getInstance().setSocketPath(clusterBeanUri.getPath());
            MyApplication.getInstance().setClusterChatVersion(clusterBean.getServiceVersion());
            MyApplication.getInstance().setClusterChatSocket(chatUrl);
        }
        return differentUrlByType;
    }

    /**
     * 退化上一版的url
     *
     * @return
     */
    private static Enterprise getOldEnterprise() {
        String myInfo = PreferencesUtils.getString(MyApplication.getInstance().getApplicationContext(),
                Constant.PREF_MY_INFO_OLD, "");
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
}
