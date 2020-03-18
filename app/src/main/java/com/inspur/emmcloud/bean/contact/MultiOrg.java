package com.inspur.emmcloud.bean.contact;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by: yufuchang
 * Date: 2020/3/17
 */
@Table(name = "MultiOrg")
public class MultiOrg {

    @Column(name = "id", isId = true)
    private String id = "";
    @Column(name = "inspurId")
    private String inspurId = "";
    @Column(name = "orgId")
    private String orgId = "";

    public MultiOrg(){}

    public MultiOrg(JSONObject jsonObject){

        this.inspurId = JSONUtils.getString(jsonObject,"inspur_id","");
        this.orgId = JSONUtils.getString(jsonObject,"org_id","");
        this.id = inspurId+"_"+orgId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInspurId() {
        return inspurId;
    }

    public void setInspurId(String inspurId) {
        this.inspurId = inspurId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
