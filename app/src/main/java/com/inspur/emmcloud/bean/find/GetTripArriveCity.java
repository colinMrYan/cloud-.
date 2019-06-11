package com.inspur.emmcloud.bean.find;

import org.json.JSONObject;

public class GetTripArriveCity {

    private String city = "";
    private String cityShortName = "";
    private String id = "";
    private String station = "";

    public GetTripArriveCity(String response) {

        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(response);

            if (jsonObject.has("city")) {
                this.city = jsonObject.getString("city");
            }
            if (jsonObject.has("cityShortName")) {
                this.cityShortName = jsonObject.getString("cityShortName");
            }
            if (jsonObject.has("id")) {
                this.id = jsonObject.getString("id");
            }
            if (jsonObject.has("station")) {
                this.station = jsonObject.getString("station");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityShortName() {
        return cityShortName;
    }

    public void setCityShortName(String cityShortName) {
        this.cityShortName = cityShortName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

}
