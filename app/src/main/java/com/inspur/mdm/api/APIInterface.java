package com.inspur.mdm.api;

import com.inspur.mdm.bean.GetDeviceCheckResult;

public interface APIInterface {

	public void returnDeviceCheckSuccess(
			GetDeviceCheckResult getDeviceCheckResult);

	public void returnDeviceCheckFail(String error);

	public void returnDeviceRegisterSuccess(GetDeviceCheckResult getDeviceCheckResult);
	public void returnDeviceRegisterFail(String error);

}