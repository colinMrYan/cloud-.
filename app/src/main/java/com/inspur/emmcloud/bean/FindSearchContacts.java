/**
 * 
 * FindSearchNews.java
 * classes : com.inspur.emmcloud.bean.FindSearchNews
 * V 1.0.0
 * Create at 2016年10月18日 下午6:07:58
 */
package com.inspur.emmcloud.bean;

import org.json.JSONObject;

/**
 * com.inspur.emmcloud.bean.FindSearchNews create at 2016年10月18日 下午6:07:58
 */
public class FindSearchContacts {
	private String id = ""; // solr索引id
	private String oid = ""; // 数据原id
	private String state = "";// 状态
	private String name = "";
	private String deptName = ""; // 所属部门
	private String deptFacet = "";// 所属部门分面参数
	private String pinyin = "";
	private String inspurId = "";
	private String mobile = "";
	private String office = ""; // 序列
	private String officeFacet = ""; // 序列的分面参数
	private String gender = "";
	private String email = "";
	private String code = ""; // 编号
	private String url = ""; // 该联系人的具体信息链接
	private String dataType = "";
	private String version_ = "";

	public FindSearchContacts(JSONObject obj) {
		try {
			if (obj.has("id")) {
				id = obj.getString("id");
			}
			if (obj.has("oid")) {
				oid = obj.getString("oid");
			}
			if (obj.has("state")) {
				state = obj.getString("state");
			}
			if (obj.has("name")) {
				name = obj.getString("name");
			}
			if (obj.has("dept_name")) {
				deptName = obj.getString("dept_name");
			}
			if (obj.has("dept_facet")) {
				deptFacet = obj.getString("dept_facet");
			}
			if (obj.has("pinyin")) {
				pinyin = obj.getString("pinyin");
			}
			if (obj.has("inspur_id")) {
				inspurId = obj.getString("inspur_id");
			}
			if (obj.has("mobile")) {
				mobile = obj.getString("mobile");
			}
			if (obj.has("office")) {
				office = obj.getString("office");
			}
			if (obj.has("office_facet")) {
				officeFacet = obj.getString("office_facet");
			}

			if (obj.has("gender")) {
				gender = obj.getString("gender");
			}
			if (obj.has("email")) {
				email = obj.getString("email");
			}
			if (obj.has("code")) {
				code = obj.getString("code");
			}

			if (obj.has("url")) {
				url = obj.getString("url");
			}
			if (obj.has("datatype")) {
				dataType = obj.getString("datatype");
			}
			if (obj.has("version_")) {
				version_ = obj.getString("version_");
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public String getId() {
		return id;
	};

	public String getOid() {
		return oid;
	};

	public String getState() {
		return state;
	};

	public String getName() {
		return name;
	};

	public String getDepartName() {
		return deptName;
	};

	public String getDeptFacet() {
		return deptFacet;
	};

	public String getPinyin() {
		return pinyin;
	};

	public String getInspurId() {
		return inspurId;
	};

	public String getMobile() {
		return mobile;
	};

	public String getOffice() {
		return office;
	};

	public String getOfficeFacet() {
		return officeFacet;
	};

	public String getUrl() {
		return url;
	};

	public String getDataType() {
		return dataType;
	};

	public String getGender() {
		return gender;
	};

	public String getVersion() {
		return version_;
	};

	public String getEmail() {
		return email;
	};

	public String getCode() {
		return code;
	};
}
