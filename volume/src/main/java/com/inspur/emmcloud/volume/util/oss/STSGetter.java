package com.inspur.emmcloud.volume.util.oss;

import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.inspur.emmcloud.componentservice.volume.GetVolumeFileUploadTokenResult;

/**
 * Created by Administrator on 2015/12/9 0009.
 * 重载OSSFederationCredentialProvider生成自己的获取STS的功能
 */
public class STSGetter extends OSSFederationCredentialProvider {
    private GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult;

    public STSGetter(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult) {
        this.getVolumeFileUploadTokenResult = getVolumeFileUploadTokenResult;
    }


    public OSSFederationToken getFederationToken() {
        return new OSSFederationToken(getVolumeFileUploadTokenResult.getAccessKeyId(), getVolumeFileUploadTokenResult.getAccessKeySecret(),
                getVolumeFileUploadTokenResult.getSecurityToken(), getVolumeFileUploadTokenResult.getExpiration());
    }

}
