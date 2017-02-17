package com.inspur.emmcloud.bean;

import java.io.Serializable;

/**
 * 通讯录搜索中第一个group title中的list数据
 * @author Administrator
 *
 */
public class FirstGroupTextModel implements Serializable{
	private String name;
	private String id;
	private String fullPath;

	public FirstGroupTextModel(String name, String id,String fullPath) {
		this.name = name;
		this.id = id;
		this.fullPath = fullPath;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getFullPath(){
		return fullPath;
	}
}
