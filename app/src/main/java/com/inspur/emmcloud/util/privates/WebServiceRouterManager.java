package com.inspur.emmcloud.util.privates;

import android.net.Uri;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.login.ClusterBean;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/2/6.
 */

public class WebServiceRouterManager {
    public static final String ECM_CHAT = "com.inspur.ecm.chat";
    private static final String ECM_SCHEDULE = "com.inspur.ecm.schedule";
    private static final String ECM_DISTRIBUTION = "com.inspur.ecm.distribution";
    private static final String ECM_NEWS = "com.inspur.ecm.news";
    private static final String ECM_CLOUD_DRIVER = "com.inspur.ecm.cloud-drive";
    private static final String ECM_STORAGE_LEGACY = "com.inspur.ecm.storage.legacy";
    private static final String EMM_OLD = "com.inspur.emm";
    private static final String ECM_CLIENT_REGISTRY = "com.inspur.ecm.client-registry";
    private static final String ECM_BOT = "com.inspur.ecm.bot";
    private static final String ECM_OLD = "com.inspur.ecm";
    private static WebServiceRouterManager mInstance;
    private String clusterEcm = "";//多云ecm服务
    private String clusterChat = "";
    private String clusterSchedule = "";
    private String clusterDistribution = "";
    private String clusterNews = "";
    private String clusterCloudDrive = "";
    private String clusterStorageLegacy = "";
    private String socketPath = "";
    private String clusterChatVersion = "";//仅标识chat的version
    private String clusterChatSocket = "";
    private String clusterEmm = Constant.DEFAULT_CLUSTER_EMM;//多云emm服务
    private String clusterClientRegistry = "";
    private String clusterScheduleVersion = "";//仅标识Schedule
    private String clusterBot = "";

    public static WebServiceRouterManager getInstance() {
        if (mInstance == null) {
            synchronized (WebServiceRouterManager.class) {
                if (mInstance == null) {
                    mInstance = new WebServiceRouterManager();
                }
            }
        }
        return mInstance;
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
        return null;
    }

    /**
     * 修改多云基地址，如果没有基地址则取默认
     *
     * @param enterprise
     */
    public void setWebServiceRouter(Enterprise enterprise) {
        List<ClusterBean> clusterBeanList = enterprise.getClusterBeanList();
        initClusters();
        for (int i = 0; i < clusterBeanList.size(); i++) {
            String serviceName = clusterBeanList.get(i).getServiceName();
            String serviceUrl = getUrlByType(clusterBeanList.get(i), serviceName);
            switch (serviceName) {
                //旧版ecm
                case ECM_OLD:
                    setClusterEcm(serviceUrl);
                    break;
                //旧版emm
                case EMM_OLD:
                    setClusterEmm(clusterBeanList.get(i).getBaseUrl() + "/");
                    break;
                //聊天相关
                case ECM_CHAT:
                    setClusterChat(serviceUrl);
                    break;
                //会议，日历，任务相关
                case ECM_SCHEDULE:
                    setClusterSchedule(serviceUrl);
                    break;
                //tab，RN，闪屏分发相关
                case ECM_DISTRIBUTION:
                    setClusterDistribution(serviceUrl);
                    break;
                //浪潮个性化相关
                case ECM_NEWS:
                    setClusterNews(serviceUrl);
                    break;
                //云盘
                case ECM_CLOUD_DRIVER:
                    setClusterCloudDrive(serviceUrl);
                    break;
                //文件服务相关
                case ECM_STORAGE_LEGACY:
                    setClusterStorageLegacy(serviceUrl);
                    break;
                //消息服务的client注册
                case ECM_CLIENT_REGISTRY:
                    setClusterClientRegistry(serviceUrl);
                    break;
                //机器人
                case ECM_BOT:
                    setClusterBot(serviceUrl);
                    break;
            }
        }
    }

    /**
     * 重新初始化路由
     */
    private void initClusters() {
        //切企业时重置路由
        setClusterEmm("");
        setClusterChat("");
        setClusterSchedule("");
        setClusterDistribution("");
        setClusterNews("");
        setClusterCloudDrive("");
        setClusterStorageLegacy("");
        setClusterClientRegistry("");
        setClusterBot("");
        setClusterEcm("");

        //切企业时重置Chat和Schedule版本
        setClusterChatVersion("");
        setClusterScheduleVersion("");
    }

    /**
     * 返回合适的Url
     */
    private String getUrlByType(ClusterBean clusterBeanNew, String serviceName) {
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
            setClusterChat(differentUrlByType);
            String chatUrl = clusterBeanUri.getScheme() + "://" + clusterBeanUri.getAuthority();
            setSocketPath(clusterBeanUri.getPath());
            setClusterChatVersion(clusterBean.getServiceVersion());
            setClusterChatSocket(chatUrl);
        }
        if (serviceName.equals(ECM_SCHEDULE)) {
            setClusterScheduleVersion(clusterBean.getServiceVersion());
        }
        return differentUrlByType;
    }

    /**
     * 获取ClusterBean
     *
     * @param serviceName
     * @return
     */
    public ClusterBean getClusterBean(String serviceName) {
        ArrayList<ClusterBean> clusterBeanArrayListOld = MyApplication.getInstance().getCurrentEnterprise().getClusterBeanList();
        int index = clusterBeanArrayListOld.indexOf(new ClusterBean(serviceName));
        ClusterBean clusterBean = null;
        if (index != -1) {
            clusterBean = clusterBeanArrayListOld.get(index);
        }
        return clusterBean;
    }


    /**
     * 获取ecm云
     *
     * @return
     */
    public String getClusterEcm() {
        return clusterEcm;
    }

    /**
     * 设置ecm云
     *
     * @param clusterEcm
     */
    public void setClusterEcm(String clusterEcm) {
        this.clusterEcm = clusterEcm;
    }

    /**
     * 获取emm云
     *
     * @return
     */
    public String getClusterEmm() {
        return clusterEmm;
    }

    /**
     * 设置emm云
     *
     * @return
     */
    public void setClusterEmm(String clusterEmm) {
        this.clusterEmm = clusterEmm;
    }

    /**
     * 沟通相关
     *
     * @return
     */
    public String getClusterChat() {
        return clusterChat;
    }

    public void setClusterChat(String clusterChat) {
        this.clusterChat = clusterChat;
    }

    public String getClusterSchedule() {
        return clusterSchedule;
    }

    public void setClusterSchedule(String clusterSchedule) {
        this.clusterSchedule = clusterSchedule;
    }

    public String getClusterDistribution() {
        return clusterDistribution;
    }

    public void setClusterDistribution(String clusterDistribution) {
        this.clusterDistribution = clusterDistribution;
    }

    public String getClusterNews() {
        return clusterNews;
    }

    public void setClusterNews(String clusterNews) {
        this.clusterNews = clusterNews;
    }

    public String getClusterCloudDrive() {
        return clusterCloudDrive;
    }

    public void setClusterCloudDrive(String clusterCloudDrive) {
        this.clusterCloudDrive = clusterCloudDrive;
    }

    public String getClusterStorageLegacy() {
        return clusterStorageLegacy;
    }

    public void setClusterStorageLegacy(String clusterStorageLegacy) {
        this.clusterStorageLegacy = clusterStorageLegacy;
    }


    public String getClusterChatVersion() {
        return clusterChatVersion;
    }

    public void setClusterChatVersion(String clusterChatVersion) {
        this.clusterChatVersion = clusterChatVersion;
    }

    public String getClusterChatSocket() {
        return clusterChatSocket;
    }

    public void setClusterChatSocket(String clusterChatSocket) {
        this.clusterChatSocket = clusterChatSocket;
    }

    public String getClusterClientRegistry() {
        return clusterClientRegistry;
    }

    public void setClusterClientRegistry(String clusterClientRegistry) {
        this.clusterClientRegistry = clusterClientRegistry;
    }

    public String getClusterScheduleVersion() {
        return clusterScheduleVersion;
    }

    public void setClusterScheduleVersion(String clusterScheduleVersion) {
        this.clusterScheduleVersion = clusterScheduleVersion;
    }

    public String getClusterBot() {
        return clusterBot;
    }

    public void setClusterBot(String clusterBot) {
        this.clusterBot = clusterBot;
    }

    public boolean isV0VersionChat() {
        return getClusterChatVersion().toLowerCase().startsWith(Constant.SERVICE_VERSION_CHAT_V0);
    }


    public String getSocketPath() {
        return socketPath;
    }

    public void setSocketPath(String socketPath) {
        this.socketPath = socketPath;
    }

    /**
     * namespace
     * v1版及v1.x版返回/api/v1
     * v0版返回/
     *
     * @return
     */
    public String getChatSocketNameSpace() {
        if (getClusterChatVersion().toLowerCase().startsWith("v0")) {
            return "/";
        } else if (getClusterChatVersion().toLowerCase().startsWith("v1")) {
            return "/api/v1";
        }
        return "";
    }

    /**
     * 判断是v1.x版本
     *
     * @return
     */
    public boolean isV1xVersionChat() {
        return getClusterChatVersion().toLowerCase().startsWith(Constant.SERVICE_VERSION_CHAT_V1);
    }

    public String getIDMUrl() {
        String clusterId = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_CLOUD_IDM, Constant.DEFAULT_CLUSTER_ID);
        return StringUtils.isBlank(clusterId) ? Constant.DEFAULT_CLUSTER_ID : clusterId;
    }
}
