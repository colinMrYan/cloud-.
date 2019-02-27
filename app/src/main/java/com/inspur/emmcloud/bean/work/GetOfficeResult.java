package com.inspur.emmcloud.bean.work;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetOfficeResult {

    private JSONArray jsonArray;
    private ArrayList<Office> officeList = new ArrayList<Office>();

    public GetOfficeResult(String response) {
        try {
            jsonArray = new JSONArray(response);
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    officeList.add(new Office(jsonArray.getJSONObject(i)));
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public ArrayList<Office> getOfficeList() {
        return officeList;
    }


    public class Office {
        private String officeId = "";
        private String locationId = "";
        private String buildingId = "";
        private String floor = "";
        private String officeName = "";
        private String uid = "";
        private String buidingName = "";
        private String buildings = "";
        private Location location;

        public Office(JSONObject jsonObject) {
            try {
                if (jsonObject.has("floor")) {
                    this.floor = jsonObject.getString("floor");
                }

                if (jsonObject.has("id")) {
                    this.officeId = jsonObject.getString("id");
                }

                if (jsonObject.has("name")) {
                    this.officeName = jsonObject.getString("name");
                }

                if (jsonObject.has("uid")) {
                    this.uid = jsonObject.getString("uid");
                }

                if (jsonObject.has("building")) {
                    JSONObject jsonBuilding = jsonObject.getJSONObject("building");

                    if (jsonBuilding.has("id")) {
                        this.buildingId = jsonBuilding.getString("id");
                    }

                    if (jsonBuilding.has("name")) {
                        this.buidingName = jsonBuilding.getString("name");
                    }

                    if (jsonBuilding.has("location")) {
                        JSONObject jsonLoc = jsonBuilding.getJSONObject("location");
                        location = new Location(jsonLoc);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public String getOfficeId() {
            return officeId;
        }

        public void setOfficeId(String officeid) {
            this.officeId = officeid;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public String getBuildingId() {
            return buildingId;
        }

        public void setBuildingId(String buildingid) {
            this.buildingId = buildingid;
        }

        public String getFloor() {
            return floor;
        }

        public void setFloor(String floor) {
            this.floor = floor;
        }

        public String getOfficeName() {
            return officeName;
        }

        public void setOfficeName(String officename) {
            this.officeName = officename;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getBuidingName() {
            return buidingName;
        }

        public void setBuidingName(String buidingname) {
            this.buidingName = buidingname;
        }


        public String getBuildings() {
            return buildings;
        }

        public void setBuildings(String buildings) {
            this.buildings = buildings;
        }


    }

}
