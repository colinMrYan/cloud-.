package com.inspur.emmcloud.bean.work;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Room implements Serializable{

	private String room = "";
	private String roomid = "";
	private String name = "";
	private String equipments = "";
	private String galleryful = "";
	private String roomName = "";
	private List<String> equipmentList = new ArrayList<String>();
	private int maxAhead = 0,maxDuration = 0;

	public Room(String jsonObject) {
		try {
			JSONObject jsonObjectRoom = new JSONObject(jsonObject);
			if (jsonObjectRoom.has("id")) {
				this.roomid = jsonObjectRoom.getString("id");
			}
			if (jsonObjectRoom.has("name")) {
				this.name = jsonObjectRoom.getString("name");
			}
			if (jsonObjectRoom.has("equipments")) {
				this.equipments = jsonObjectRoom.getString("equipments");
				JSONArray equipmentArray = jsonObjectRoom.getJSONArray("equipments");
				for (int i = 0; i < equipmentArray.length(); i++) {
					String equipment = equipmentArray.getString(i);
					equipmentList.add(equipment);
				}

			}
			if (jsonObjectRoom.has("galleryful")) {
				this.galleryful = jsonObjectRoom.getString("galleryful");
			}
			if (jsonObjectRoom.has("maxAhead")) {
				this.maxAhead = Integer.parseInt(jsonObjectRoom.getString("maxAhead"));
			}
			if (jsonObjectRoom.has("maxDuration")) {
				this.maxDuration = Integer.parseInt(jsonObjectRoom.getString("maxDuration"));
			}
			if (jsonObjectRoom.has("building")) {
				JSONObject jsonObjectRoomname = new JSONObject(
						jsonObjectRoom.getString("building"));
				this.roomName = jsonObjectRoomname.getString("name");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public String getRoom() {
		return room;
	}

	public String getRoomId() {
		return roomid;
	}

	public String getName() {
		return name;
	}

	public String getEquipments() {
		return equipments;
	}

	public String getGalleryful() {
		return galleryful;
	}
	

	public int getMaxAhead() {
		return maxAhead;
	}

	public int getMaxDuration() {
		return maxDuration;
	}

	//	public String[] getEquipment() {
//		return equipment;
//	}
	public List<String> getEquipmentList(){
		return equipmentList;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomname(String roomname) {
		this.roomName = roomname;
	}

	public String getRoomid() {
		return roomid;
	}

	public void setRoomid(String roomid) {
		this.roomid = roomid;
	}

}
