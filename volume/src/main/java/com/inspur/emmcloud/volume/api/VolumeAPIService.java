package com.inspur.emmcloud.volume.api;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.componentservice.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.componentservice.volume.Volume;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.componentservice.volume.VolumeFileUpload;
import com.inspur.emmcloud.volume.bean.GetReturnMoveOrCopyErrorResult;
import com.inspur.emmcloud.volume.bean.GetVolumeFileListResult;
import com.inspur.emmcloud.volume.bean.GetVolumeGroupPermissionResult;
import com.inspur.emmcloud.volume.bean.GetVolumeGroupResult;
import com.inspur.emmcloud.volume.bean.GetVolumeListResult;
import com.inspur.emmcloud.volume.bean.GetVolumeResultWithPermissionResult;
import com.inspur.emmcloud.volume.bean.VolumeDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.List;

public class VolumeAPIService {
    private Context context;
    private VolumeAPIInterface apiInterface;

    public VolumeAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(VolumeAPIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    private void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.refreshToken(oauthCallBack, requestTime);
        }
    }

    /**
     * 增加云盘成员
     *
     * @param volumeId
     * @param uidList
     */
    public void volumeMemAdd(final String volumeId, final List<String> uidList) {
        final String url = VolumeAPIUri.getVolumeMemUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("members", uidList);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeMemAddSuccess(uidList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeMemAddFail(error, responseCode);
            }


            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        volumeMemAdd(volumeId, uidList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 增加组成员
     *
     * @param groupId
     * @param uidList
     */
    public void groupMemAdd(final String groupId, final List<String> uidList) {
        final String url = VolumeAPIUri.getGroupMemBaseUrl(groupId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("members", uidList);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGroupMemAddSuccess(uidList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGroupMemAddFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        groupMemAdd(groupId, uidList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 删除组成员
     *
     * @param groupId
     * @param uidList
     */
    public void groupMemDel(final String groupId, final List<String> uidList) {
        final String url = VolumeAPIUri.getGroupMemBaseUrl(groupId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("members", uidList);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGroupMemDelSuccess(uidList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGroupMemDelFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        groupMemDel(groupId, uidList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 创建共享网盘
     *
     * @param myUid
     * @param volumeName
     */
    public void createShareVolume(final String myUid, final String volumeName) {
        final String url = VolumeAPIUri.getVolumeListUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        JSONObject object = new JSONObject();
        try {
            object.put("name", volumeName);
            JSONArray array = new JSONArray();
            array.put(myUid);
            object.put("members", array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(object.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnCreateShareVolumeSuccess(new Volume(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCreateShareVolumeFail("", -1);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createShareVolume(myUid, volumeName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });

    }

    /*****************************************************云盘**********************************************************/
    /**
     * 获取云盘列表
     */
    public void getVolumeList() {
        final String url = VolumeAPIUri.getVolumeListUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeListSuccess(new GetVolumeListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeListFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 获取云盘文件列表
     *
     * @param volumeId
     * @param currentDirAbsolutePath
     */
    public void getVolumeFileList(final String volumeId, final String currentDirAbsolutePath) {
        final String url = VolumeAPIUri.getVolumeFileOperationUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("path", currentDirAbsolutePath);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeFileListSuccess(new GetVolumeFileListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileListFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeFileList(volumeId, currentDirAbsolutePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 获取文件上传Token
     * @param fileName
     * @param volumeFileUpload
     * @param mockVolumeFile
     */
    public void getVolumeFileUploadToken(final String fileName, final VolumeFileUpload volumeFileUpload, final VolumeFile mockVolumeFile) {
        final String url = VolumeAPIUri.getVolumeFileUploadSTSTokenUrl(mockVolumeFile.getVolume());
        String volumeFilePath = volumeFileUpload.getVolumeFileParentPath();
        final String localFilePath = volumeFileUpload.getLocalFilePath();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        JSONObject bodyObj = new JSONObject();
        try {
            bodyObj.put("name", fileName);
            bodyObj.put("path", volumeFilePath + fileName);
            if (!StringUtils.isBlank(volumeFileUpload.getUploadId())) {
                JSONObject baseObj = new JSONObject();
                baseObj.put("path", volumeFileUpload.getUploadPath());
                baseObj.put("id", volumeFileUpload.getUploadId());
                params.addQueryStringParameter("strategy", "multipart");
                bodyObj.put("base", baseObj);
            }
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(bodyObj.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeFileUploadTokenSuccess(new GetVolumeFileUploadTokenResult(new String(arg0)), localFilePath, mockVolumeFile, volumeFileUpload.getTransferObserverId());
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileUploadTokenFail(mockVolumeFile, error, responseCode, localFilePath);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeFileUploadToken(fileName, volumeFileUpload, mockVolumeFile);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 移动云盘文件
     *
     * @param volumeId
     * @param currentDirAbsolutePath
     */
    public void moveVolumeFile(final String volumeId, final String currentDirAbsolutePath, final List<VolumeFile> moveVolumeFileList, final String toPath) {
        final String url = VolumeAPIUri.getMoveVolumeFileUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        JSONArray array = new JSONArray();
        try {
            for (int i = 0; i < moveVolumeFileList.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("from", currentDirAbsolutePath + moveVolumeFileList.get(i).getName());
                object.put("to", toPath);
                array.put(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(array.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnMoveFileSuccess(moveVolumeFileList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnMoveFileFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        moveVolumeFile(volumeId, currentDirAbsolutePath, moveVolumeFileList, toPath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 复制云盘文件
     *
     * @param volumeId
     * @param currentDirAbsolutePath
     */
    public void copyVolumeFile(final String volumeId, final String currentDirAbsolutePath, final List<VolumeFile> copyVolumeFileList, final String toPath) {
        final String url = VolumeAPIUri.getCopyVolumeFileUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        JSONArray array = new JSONArray();
        try {
            for (int i = 0; i < copyVolumeFileList.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("from", currentDirAbsolutePath + copyVolumeFileList.get(i).getName());
                object.put("to", toPath);
                array.put(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(array.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnCopyFileSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCopyFileFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        copyVolumeFile(volumeId, currentDirAbsolutePath, copyVolumeFileList, toPath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }


    /**
     * 夸网盘复制文件
     **/
    public void copyFileBetweenVolume(final VolumeFile copyVolumeFile, final String fromVolumeId, final String toVolumeId, final String srcVolumeFilePath, final String desVolumeFilePath) {
        final String url = VolumeAPIUri.getCopyFileBetweenVolumeUrl(fromVolumeId, toVolumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("path", srcVolumeFilePath);
        params.addQueryStringParameter("to", desVolumeFilePath);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnCopyFileBetweenVolumeSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCopyFileBetweenVolumeFail(error, responseCode, copyVolumeFile);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        copyFileBetweenVolume(copyVolumeFile, fromVolumeId, toVolumeId, srcVolumeFilePath, desVolumeFilePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 跨网盘复制或移动
     **/
    public void copyOrMoveFileBetweenVolume(final List<VolumeFile> operationVolumeFile, final String fromVolumeId, final String toVolumeId, final String operation, final String srcVolumeFilePath, final String desVolumeFilePath) {
        final String url = VolumeAPIUri.getCopyOrMoveFileBetweenVolumeUrl(fromVolumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        JSONArray array = new JSONArray();
        JSONObject rootObj = new JSONObject();
        try {
            for (int i = 0; i < operationVolumeFile.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("source", srcVolumeFilePath + operationVolumeFile.get(i).getName());
                object.put("destination", desVolumeFilePath);
                array.put(object);
            }
            rootObj.put("targetVolume", toVolumeId);
            rootObj.put("operation", operation);
            rootObj.put("requests", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.setBodyContent(rootObj.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnMoveOrCopyFileBetweenVolumeSuccess(operation);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnMoveOrCopyFileBetweenVolumeFail(new GetReturnMoveOrCopyErrorResult(error), responseCode, srcVolumeFilePath, operation, operationVolumeFile);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        copyOrMoveFileBetweenVolume(operationVolumeFile, fromVolumeId, toVolumeId, operation, srcVolumeFilePath, desVolumeFilePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }



    /**
     * 创建文件夹
     *
     * @param volumeId
     * @param forderName
     * @param currentDirAbsolutePath
     */
    public void createForder(final String volumeId, final String forderName, final String currentDirAbsolutePath) {
        final String url = VolumeAPIUri.getCreateForderUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("path", currentDirAbsolutePath + forderName);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnCreateForderSuccess(new VolumeFile(new String(new String(arg0))));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCreateForderFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createForder(volumeId, forderName, currentDirAbsolutePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }


    /**
     * 删除文件
     *
     * @param volumeId
     * @param deleteVolumeFileList
     * @param currentDirAbsolutePath
     */
    public void volumeFileDelete(final String volumeId, final List<VolumeFile> deleteVolumeFileList, final String currentDirAbsolutePath) {
        final String url = VolumeAPIUri.getVolumeFileOperationUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        JSONArray array = new JSONArray();
        try {
            for (int i = 0; i < deleteVolumeFileList.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("path", currentDirAbsolutePath + deleteVolumeFileList.get(i).getName());
                array.put(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(array.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeFileDeleteSuccess(deleteVolumeFileList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileDeleteFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        volumeFileDelete(volumeId, deleteVolumeFileList, currentDirAbsolutePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 文件重命名
     * @param volumeId
     * @param volumeFile
     * @param currentDirAbsolutePath
     * @param fileNewName
     */
    public void volumeFileRename(final String volumeId, final VolumeFile volumeFile, final String currentDirAbsolutePath, final String fileNewName) {
        final String url = VolumeAPIUri.getVolumeFileRenameUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("path", currentDirAbsolutePath + volumeFile.getName());
        params.addQueryStringParameter("name", fileNewName);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeFileRenameSuccess(volumeFile, fileNewName);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileRenameFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        volumeFileRename(volumeId, volumeFile, currentDirAbsolutePath, fileNewName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 修改网盘名称
     *
     * @param volume
     * @param name
     */
    public void updateShareVolumeName(final Volume volume, final String name) {
        final String url = VolumeAPIUri.getUpdateVolumeInfoUrl(volume.getId());
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("name", name);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUpdateShareVolumeNameSuccess(volume, name);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUpdateShareVolumeNameFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateShareVolumeName(volume, name);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });

    }


    /**
     * 修改网盘名称
     *
     * @param volume
     */
    public void removeShareVolumeName(final Volume volume) {
        final String url = VolumeAPIUri.getUpdateVolumeInfoUrl(volume.getId());
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("id", volume.getId());
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnRemoveShareVolumeSuccess(volume);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRemoveShareVolumeFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        removeShareVolumeName(volume);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });

    }

    /**
     * 获取共享网盘详细信息
     *
     * @param volumeId
     */
    public void getVolumeInfo(final String volumeId) {
        final String url = VolumeAPIUri.getUpdateVolumeInfoUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("id", volumeId);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeDetailSuccess(new VolumeDetail(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeDetailFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeInfo(volumeId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 删除云盘成员
     *
     * @param volumeId
     * @param uidList
     */
    public void volumeMemDel(final String volumeId, final List<String> uidList) {
        final String url = VolumeAPIUri.getVolumeMemUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("members", uidList);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeMemDelSuccess(uidList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeMemDelFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        volumeMemDel(volumeId, uidList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 更改组名称
     *
     * @param groupId
     * @param groupName
     */
    public void updateGroupName(final String groupId, final String groupName) {
        final String url = VolumeAPIUri.getGroupBaseUrl(groupId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("name", groupName);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUpdateGroupNameSuccess(groupName);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUpdateGroupNameFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateGroupName(groupId, groupName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 获取网盘下包含自己的组
     *
     * @param volumeId
     */
    public void getVolumeGroupContainMe(final String volumeId) {
        final String url = VolumeAPIUri.getVolumeGroupUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("isMember", true);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeGroupContainMeSuccess(new GetVolumeGroupResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeGroupContainMeFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeGroupContainMe(volumeId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });

    }

    /**
     * 根据volumeId获取文件夹的权限组
     *
     * @param volumeId
     */
    public void getVolumeFileGroup(final String volumeId, final String path) {
        String pathResult = StringUtils.encodeURIComponent(path);
        final String url = VolumeAPIUri.getVolumeFileGroupUrl(volumeId) + "?path=" + pathResult;
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeGroupSuccess(new GetVolumeResultWithPermissionResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeGroupFail(error, responseCode);
            }


            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeFileGroup(volumeId, path);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });

    }

    /**
     * 修改文件夹组权限
     *
     * @param volumeId
     * @param path
     * @param group
     * @param privilege
     * @param recurse
     */
    public void updateVolumeFileGroupPermission(final String volumeId, final String path, final String group, final int privilege, final boolean recurse) {
        final String url = VolumeAPIUri.getVolumeFileGroupUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("path", path);
        params.addQueryStringParameter("group", group);
        params.addQueryStringParameter("privilege", privilege + "");
        params.addQueryStringParameter("recurse", recurse + "");
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                GetVolumeGroupPermissionResult getVolumeGroupPermissionResult = new GetVolumeGroupPermissionResult("");
                getVolumeGroupPermissionResult.setPrivilege(privilege);
                apiInterface.returnUpdateVolumeGroupPermissionSuccess(getVolumeGroupPermissionResult);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUpdateVolumeGroupPermissionFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateVolumeFileGroupPermission(volumeId, path, group, privilege, recurse);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }
}
