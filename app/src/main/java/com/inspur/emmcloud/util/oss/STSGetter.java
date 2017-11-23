package com.inspur.emmcloud.util.oss;

import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileUploadSTSTokenResult;

/**
 * Created by Administrator on 2015/12/9 0009.
 * 重载OSSFederationCredentialProvider生成自己的获取STS的功能
 */
public class STSGetter extends OSSFederationCredentialProvider {
    private GetVolumeFileUploadSTSTokenResult getVolumeFileUploadSTSTokenResult;

    public STSGetter(GetVolumeFileUploadSTSTokenResult getVolumeFileUploadSTSTokenResult) {
        this.getVolumeFileUploadSTSTokenResult = getVolumeFileUploadSTSTokenResult;
    }


    public OSSFederationToken getFederationToken() {
        return new OSSFederationToken(getVolumeFileUploadSTSTokenResult.getAccessKeyId(), getVolumeFileUploadSTSTokenResult.getAccessKeySecret(),
                getVolumeFileUploadSTSTokenResult.getSecurityToken(), getVolumeFileUploadSTSTokenResult.getExpiration());
    }

}
