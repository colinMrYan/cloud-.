package com.inspur.emmcloud.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetMeetingsResult {

	private static final String TAG = "GetMeetingsResult";

	private ArrayList<Meeting> meetings = new ArrayList<Meeting>();

	public GetMeetingsResult(String response) {
		try {
			JSONArray jsonArray = new JSONArray(response);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				meetings.add(new Meeting(obj));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

//	public class Meetting {
//		private String meettingId = "";
//		private String topic = "";
//		private String from = "";
//		private String to = "";
//		private String participant = "";
//		private JSONArray participantArray;
//		private String alert;
//		private String notice;
//		private JSONObject jsonObject;
//		private String attendant = "";
//		private String organizer = "";
//		private ArrayList<Room> rooms = new ArrayList<GetMeetingsResult.Room>();
//
//		public Meetting(JSONObject obj) {
//			jsonObject = new JSONObject();
//			jsonObject = obj;
//			try {
//				if (jsonObject.has("id")) {
//
//					this.meettingId = jsonObject.getString("id");
//				}
//
//				if (jsonObject.has("organizer")) {
//
//					this.organizer = jsonObject.getString("organizer");
//				}
//				if (jsonObject.has("topic")) {
//					this.topic = jsonObject.getString("topic");
//				}
//				if (jsonObject.has("from")) {
//					this.from = jsonObject.getString("from");
//				}
//				if (jsonObject.has("to")) {
//					this.to = jsonObject.getString("to");
//				}
//				if (jsonObject.has("participant")) {
//					this.participant = jsonObject.getString("participant");
//					this.participantArray = jsonObject
//							.getJSONArray("participant");
//				}
//
//				if (jsonObject.has("attendant")) {
//					this.attendant = jsonObject.getString("attendant");
//				}
//
//				if (jsonObject.has("room")) {
//					String json = jsonObject.getString("room");
//					JSONObject jsonRoom = new JSONObject(json);
//					rooms.add(new Room(jsonRoom));
//				}
//				if (jsonObject.has("alert")) {
//					this.alert = jsonObject.getString("alert");
//				}
//				if (jsonObject.has("notice")) {
//					this.notice = jsonObject.getString("notice");
//				}
//			} catch (Exception e) {
//				// TODO: handle exception
//			}
//
//		}
//
//		public String getOrganizer() {
//			return organizer;
//		}
//
//		public void setOrganizer(String organizer) {
//			this.organizer = organizer;
//		}
//
//		public String getMeettingId() {
//			return meettingId;
//		}
//
//		public String getTopic() {
//			return topic;
//		}
//
//		public String getFrom() {
//			return from;
//		}
//
//		public String getTo() {
//			return to;
//		}
//
//		public String getParticipant() {
//			return participant;
//		}
//
//		public String getAlert() {
//			return alert;
//		}
//
//		public String getNotice() {
//			return notice;
//		}
//
//		public ArrayList<Room> getRooms() {
//			return rooms;
//		}
//
//		public JSONArray getParticipantArray() {
//			return participantArray;
//		}
//
//		public void setParticipantArray(JSONArray participantArray) {
//			this.participantArray = participantArray;
//		}
//
//		public String getAttendant() {
//			return attendant;
//		}
//
//		public void setAttendant(String attendant) {
//			this.attendant = attendant;
//		}
//
//	}
//
//	public class Room {
//
//		private String room = "";
//		private String roomid = "";
//		private String name = "";
//		private String equipments = "";
//		private String equipment[] = new String[2];
//		private String galleryful = "";
//		private String roomname = "";
//		private String shortname = "";
//
//		public Room(JSONObject jsonObject) {
//			JSONObject jsonObjectRoom = jsonObject;
//			try {
//				if (jsonObjectRoom.has("id")) {
//					this.roomid = jsonObjectRoom.getString("id");
//				}
//				if (jsonObjectRoom.has("name")) {
//					this.name = jsonObjectRoom.getString("name");
//				}
//				if (jsonObjectRoom.has("building")) {
//					JSONObject jsonObjectRoomname = new JSONObject(
//							jsonObjectRoom.getString("building"));
//					this.roomname = jsonObjectRoomname.getString("name");
//				}
//
//				if (jsonObjectRoom.has("shortname")) {
//					this.shortname = jsonObjectRoom.getString("shortname");
//				}
//				if (jsonObjectRoom.has("equipments")) {
//					this.equipments = jsonObjectRoom.getString("equipments");
//					String equip = jsonObjectRoom.getString("equipments");
//					String equips[] = equip.replace("[", "").replace("\"", "")
//							.replace("]", "").split(",");
//					for (int i = 0; i < equips.length; i++) {
//						equipment[i] = equips[i];
//					}
//
//				}
//				if (jsonObjectRoom.has("galleryful")) {
//					this.galleryful = jsonObjectRoom.getString("galleryful");
//				}
//
//			} catch (Exception e) {
//				// TODO: handle exception
//			}
//		}
//
//		public String getRoom() {
//			return room;
//		}
//
//		public String getRoomid() {
//			return roomid;
//		}
//
//		public String getName() {
//			return name;
//		}
//
//		public String getEquipments() {
//			return equipments;
//		}
//
//		public String getGalleryful() {
//			return galleryful;
//		}
//
//		public String[] getEquipment() {
//			return equipment;
//		}
//
//		public String getRoomname() {
//			return roomname;
//		}
//
//		public void setRoomname(String roomname) {
//			this.roomname = roomname;
//		}
//
//		public String getShortname() {
//			return shortname;
//		}
//
//		public void setShortname(String shortname) {
//			this.shortname = shortname;
//		}
//
//	}

	public ArrayList<Meeting> getMeetingsList() {
		return meetings;
	}

}
