package com.inspur.emmcloud.volume.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/11/25.
 */

public class GetReturnMoveOrCopyErrorResult {

    private List<String> operationFailFileSourceList = new ArrayList<>();

    private String errorDetail = "";

    public GetReturnMoveOrCopyErrorResult(String error) {
        errorDetail = error;
        try {
            JSONArray arrayData = JSONUtils.getJSONArray(error, new JSONArray());
            for (int i = 0; i < arrayData.length(); i++) {
                if (arrayData.isNull(i)) {
                    if (i == 0) {
                        operationFailFileSourceList = null;   //如果返回数据没有文件信息
                    }
                    break;
                }
                JSONObject jsonObject = arrayData.getJSONObject(i);
                boolean operationSuccess = jsonObject.getBoolean("success");
                String operationFailFile = jsonObject.getString("source");
                if (!operationSuccess && !StringUtils.isBlank(operationFailFile)) {
                    operationFailFileSourceList.add(operationFailFile);  //文件失败的加入列表
                }
            }
        } catch (Exception e) {
            operationFailFileSourceList = null;
            e.printStackTrace();
        }
    }

    /**
     * 返回复制或者移动失败的文件
     */
    public List<String> getOperationFailFileSourceList() {
        return operationFailFileSourceList;
    }

    /**
     * 返回失败详情
     **/
    public String getErrorDetail() {
        return errorDetail;
    }
}
