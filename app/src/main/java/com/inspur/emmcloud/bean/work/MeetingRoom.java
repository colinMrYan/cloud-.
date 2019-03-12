package com.inspur.emmcloud.bean.work;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MeetingRoom {

    private String meetingId;
    private String name;
    //	private String equipments;
//	private String equipment[];
    private ArrayList<String> equipmentList = new ArrayList<String>();
    private int galleryful;
    //	private int available[];
    private String admin;
    private String light;
    private ArrayList<String> busyDegreeList = new ArrayList<String>();
    private String shortname = "";
    private String maxAhead = "";
    private String maxDuration = "";

    public MeetingRoom(JSONObject jsonObject) {

        try {
            if (jsonObject.has("id")) {
                this.meetingId = jsonObject.getString("id");
            }
            if (jsonObject.has("name")) {
                this.name = jsonObject.getString("name");
            }
            if (jsonObject.has("admin")) {
                this.admin = jsonObject.getString("admin");
            }
            if (jsonObject.has("galleryful")) {
                this.galleryful = jsonObject.getInt("galleryful");
            }

            if (jsonObject.has("maxAhead")) {
                this.maxAhead = jsonObject.getString("maxAhead");
            }

            if (jsonObject.has("maxDuration")) {
                this.maxDuration = jsonObject.getString("maxDuration");
            }
//			if(jsonObject.has("available")){
//				String json = jsonObject.getString("available");
//				JSONObject jsonObjectAvailable = new JSONObject(json);
//				available = new int[2];
//				if(jsonObjectAvailable.has("today")){
//					available[0] = jsonObjectAvailable.getInt("today");
//				}
//				if(jsonObjectAvailable.has("tomorrow")){
//					available[1] = jsonObjectAvailable.getInt("tomorrow");
//				}
//				
//			}
            if (jsonObject.has("light")) {
                this.light = jsonObject.getString("light");
            }

            if (jsonObject.has("equipments")) {
//				this.equipments = jsonObject.getString("equipments");
                String equip = jsonObject.getString("equipments");
//				String equips[] = equip.replace("[", "").replace("\"", "").replace("]", "").split(",");
//				equipment = new String[equips.length];
//				for (int i = 0; i < equips.length; i++) {
//					equipment[i] = equips[i];
//				}
                JSONArray equipmentArray = new JSONArray(equip);
                for (int i = 0; i < equipmentArray.length(); i++) {
                    equipmentList.add(equipmentArray.getString(i));
                }
            }

            if (jsonObject.has("busyDegree")) {
                String busy = jsonObject.getString("busyDegree");
                JSONArray busyDegreeArray = new JSONArray(busy);
                for (int i = 0; i < busyDegreeArray.length(); i++) {
                    busyDegreeList.add(busyDegreeArray.getString(i));
                }
            }

            if (jsonObject.has("shortname")) {
                this.shortname = jsonObject.getString("shortname");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMeetingId() {
        return meetingId;
    }

    public String getName() {
        return name;
    }

    //	public String getEquipments() {
//		return equipments;
//	}
    public int getGalleryful() {
        return galleryful;
    }

    //	public int[] getAvailable() {
//		return available;
//	}
    public String getLight() {
        return light;
    }

    //	public String[] getEquipment() {
//		return equipment;
//	}
    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getMaxAhead() {
        return maxAhead;
    }

    public void setMaxAhead(String maxAhead) {
        this.maxAhead = maxAhead;
    }

    public String getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(String maxDuration) {
        this.maxDuration = maxDuration;
    }

    public ArrayList<String> getBusyDegreeList() {
        return busyDegreeList;
    }

    public ArrayList<String> getEquipmentList() {
        return equipmentList;
    }

}
