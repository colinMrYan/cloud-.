package com.inspur.emmcloud.util.privates;

/**
 * Created by yufuchang on 2018/2/6.
 */

public class MutilClusterUtils {
/*    public static final String ECM_CHAT = "com.inspur.ecm.chat";
    private static final String ECM_SCHEDULE = "com.inspur.ecm.schedule";
    private static final String ECM_DISTRIBUTION = "com.inspur.ecm.distribution";
    private static final String ECM_NEWS = "com.inspur.ecm.news";
    private static final String ECM_CLOUD_DRIVER = "com.inspur.ecm.cloud-drive";
    private static final String ECM_STORAGE_LEGACY = "com.inspur.ecm.storage.legacy";
    private static final String EMM_OLD = "com.inspur.emm";
    private static final String ECM_CLIENT_REGISTRY = "com.inspur.ecm.client-registry";
    private static final String ECM_BOT = "com.inspur.ecm.bot";
    private static final String ECM_OLD = "com.inspur.ecm";

    *//**
     * 修改多云基地址，如果没有基地址则取默认
     *
     * @param enterprise
     *//*
    public static void setClusterBaseUrl(Enterprise enterprise) {
        List<ClusterBean> clusterBeanList = enterprise.getClusterBeanList();
        initClusters();
        for (int i = 0; i < clusterBeanList.size(); i++) {
            String serviceName = clusterBeanList.get(i).getServiceName();
            String serviceUrl = getUrlByType(clusterBeanList.get(i), serviceName);
            switch (serviceName) {
                //旧版ecm
                case ECM_OLD:
                    BaseApplication.getInstance().setClusterEcm(serviceUrl);
                    break;
                //旧版emm
                case EMM_OLD:
                    BaseApplication.getInstance().setClusterEmm(clusterBeanList.get(i).getBaseUrl() + "/");
                    break;
                //聊天相关
                case ECM_CHAT:
                    BaseApplication.getInstance().setClusterChat(serviceUrl);
                    break;
                //会议，日历，任务相关
                case ECM_SCHEDULE:
                    BaseApplication.getInstance().setClusterSchedule(serviceUrl);
                    break;
                //tab，RN，闪屏分发相关
                case ECM_DISTRIBUTION:
                    BaseApplication.getInstance().setClusterDistribution(serviceUrl);
                    break;
                //浪潮个性化相关
                case ECM_NEWS:
                    BaseApplication.getInstance().setClusterNews(serviceUrl);
                    break;
                //云盘
                case ECM_CLOUD_DRIVER:
                    BaseApplication.getInstance().setClusterCloudDrive(serviceUrl);
                    break;
                //文件服务相关
                case ECM_STORAGE_LEGACY:
                    BaseApplication.getInstance().setClusterStorageLegacy(serviceUrl);
                    break;
                //消息服务的client注册
                case ECM_CLIENT_REGISTRY:
                    BaseApplication.getInstance().setClusterClientRegistry(serviceUrl);
                    break;
                //机器人
                case ECM_BOT:
                    BaseApplication.getInstance().setClusterBot(serviceUrl);
                    break;
            }
        }
    }

    *//**
     * 重新初始化路由
     *//*
    private static void initClusters() {
        //切企业时重置路由
        BaseApplication.getInstance().setClusterEmm("");
        BaseApplication.getInstance().setClusterChat("");
        BaseApplication.getInstance().setClusterSchedule("");
        BaseApplication.getInstance().setClusterDistribution("");
        BaseApplication.getInstance().setClusterNews("");
        BaseApplication.getInstance().setClusterCloudDrive("");
        BaseApplication.getInstance().setClusterStorageLegacy("");
        BaseApplication.getInstance().setClusterClientRegistry("");
        BaseApplication.getInstance().setClusterBot("");
        BaseApplication.getInstance().setClusterEcm("");

        //切企业时重置Chat和Schedule版本
        BaseApplication.getInstance().setClusterChatVersion("");
        BaseApplication.getInstance().setClusterScheduleVersion("");
    }


    *//**
     * 返回合适的Url
     *//*
    private static String getUrlByType(ClusterBean clusterBeanNew, String serviceName) {
        String differentUrlByType = clusterBeanNew.getBaseUrl();
        ClusterBean clusterBean = clusterBeanNew;
        if (StringUtils.isBlank(differentUrlByType)) {
            Enterprise enterpriseOld = getOldEnterprise();
            if (enterpriseOld != null) {
                List<ClusterBean> clusterBeanListOld = enterpriseOld.getClusterBeanList();
                ClusterBean clusterBeanIndex = new ClusterBean();
                clusterBeanIndex.setServiceName(serviceName);
                int clusterIndex = clusterBeanListOld.indexOf(clusterBeanIndex);
                if (clusterIndex != -1) {
                    clusterBean = clusterBeanListOld.get(clusterIndex);
                    differentUrlByType = clusterBean.getBaseUrl();
                }
            }
        }
        if (serviceName.equals(ECM_CHAT)) {
            Uri clusterBeanUri = Uri.parse(differentUrlByType);
            BaseApplication.getInstance().setClusterChat(differentUrlByType);
            String chatUrl = clusterBeanUri.getScheme() + "://" + clusterBeanUri.getAuthority();
            BaseApplication.getInstance().setSocketPath(clusterBeanUri.getPath());
            BaseApplication.getInstance().setClusterChatVersion(clusterBean.getServiceVersion());
            BaseApplication.getInstance().setClusterChatSocket(chatUrl);
        }
        if (serviceName.equals(ECM_SCHEDULE)) {
            BaseApplication.getInstance().setClusterScheduleVersion(clusterBean.getServiceVersion());
        }
        return differentUrlByType;
    }

    *//**
     * 退化上一版的url
     *
     * @return
     *//*
    private static Enterprise getOldEnterprise() {
        String myInfo = PreferencesUtils.getString(BaseApplication.getInstance().getApplicationContext(),
                Constant.PREF_MY_INFO_OLD, "");
        if (!StringUtils.isBlank(myInfo)) {
            GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
            String currentEnterpriseId = PreferencesByUsersUtils.getString(BaseApplication.getInstance().getApplicationContext(), "current_enterprise_id");
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

    *//**
     * 获取ClusterBean
     *
     * @param serviceName
     * @return
     *//*
    public static ClusterBean getClusterBean(String serviceName) {
        ArrayList<ClusterBean> clusterBeanArrayListOld = MyApplication.getInstance().getCurrentEnterprise().getClusterBeanList();
        int index = clusterBeanArrayListOld.indexOf(new ClusterBean(serviceName));
        ClusterBean clusterBean = null;
        if (index != -1) {
            clusterBean = clusterBeanArrayListOld.get(index);
        }
        return clusterBean;
    }*/

}
