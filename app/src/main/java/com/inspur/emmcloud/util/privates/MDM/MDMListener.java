package com.inspur.emmcloud.util.privates.MDM;

import java.io.Serializable;

public interface MDMListener extends Serializable {
	//设备审核通过
	void MDMStatusPass();
	//设备审核未通过
	void MDMStatusNoPass();
	void dimissExternalLoadingDlg();
}
