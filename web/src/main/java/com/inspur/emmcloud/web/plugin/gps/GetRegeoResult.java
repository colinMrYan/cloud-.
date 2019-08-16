package com.inspur.emmcloud.web.plugin.gps;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/8/15.
 */

public class GetRegeoResult {
    private int status;
    private String info;
    private String addr;
    private String country;
    private String province;
    private String city;
    private String district;
    private String street;
    private String streetNum;

    public GetRegeoResult(String response) {
        JSONObject obj = JSONUtils.getJSONObject(response);
        this.status = JSONUtils.getInt(obj, "status", 1);
        this.info = JSONUtils.getString(obj, "info", "");
        JSONObject regeocodeObj = JSONUtils.getJSONObject(obj, "regeocode", new JSONObject());
        this.addr = JSONUtils.getString(regeocodeObj, "formatted_address", "");
        JSONObject addressComponentObj = JSONUtils.getJSONObject(regeocodeObj, "addressComponent", new JSONObject());
        this.country = JSONUtils.getString(addressComponentObj, "country", "");
        this.province = JSONUtils.getString(addressComponentObj, "province", "");
        this.city = JSONUtils.getString(addressComponentObj, "city", "");
        this.district = JSONUtils.getString(addressComponentObj, "district", "");
        JSONObject streetNumberOb = JSONUtils.getJSONObject(addressComponentObj, "streetNumber", new JSONObject());
        this.street = JSONUtils.getString(streetNumberOb, "street", "");
        this.streetNum = JSONUtils.getString(streetNumberOb, "number", "");
    }

    public int getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }

    public String getAddr() {
        return addr;
    }

    public String getCountry() {
        return country;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getStreet() {
        return street;
    }

    public String getStreetNum() {
        return streetNum;
    }
}
