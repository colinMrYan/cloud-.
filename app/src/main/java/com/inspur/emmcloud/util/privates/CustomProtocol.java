package com.inspur.emmcloud.util.privates;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义协议实体类
 * 包含协议，host，多个参数，参数以&连接，以=作为key，value分隔
 * 示例：
 * ecc-cloudplus-cmd:\/\/voice_channel?cmd=invite&channelid=143271038136877057&roomid=257db7ddc478429cab2d2a1ec4ed8626&uid=99999
 * Created by yufuchang on 2018/8/20.
 */

public class CustomProtocol {
    private String protocol;
    private String host;
    private Map<String, String> paramMap = new HashMap<>();

    public CustomProtocol(String response) {
        try {
            if (response.contains("\\")) {
                response = response.replaceAll("\\\\", "");
            }
            protocol = response.split("://")[0];
            String arg = response.split("://")[1];
            String[] hostAndParams = arg.split("\\?");
            host = hostAndParams[0];
            String[] paramArray = hostAndParams[1].split("&");
            if (paramArray != null) {
                for (int i = 0; i < paramArray.length; i++) {
                    String[] param = paramArray[i].split("=");
                    paramMap.put(param[0], param[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }
}
