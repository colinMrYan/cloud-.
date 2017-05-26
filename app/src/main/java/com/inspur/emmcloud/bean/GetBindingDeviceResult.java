package com.inspur.emmcloud.bean;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/15.
 */

public class GetBindingDeviceResult {
	private List<BindingDevice> bindingDeviceList = new ArrayList<>();
	public  GetBindingDeviceResult(String response){
		try {
			JSONArray array = new JSONArray(response);
			for (int i=0;i<array.length();i++){
				bindingDeviceList.add(new BindingDevice(array.getJSONObject(i)));
			}
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public List<BindingDevice> getBindingDeviceList(){
		return  bindingDeviceList;
	}
}
