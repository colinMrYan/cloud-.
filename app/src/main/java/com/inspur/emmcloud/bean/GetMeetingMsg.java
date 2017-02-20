package com.inspur.emmcloud.bean;


public class GetMeetingMsg {

//	private static final String TAG = "GetMeettingMsg";
//	
//	private String topic = "";
//	private String to = "";
//	private String alert = "";
//	private String organizer = "";
//	private String[] participant;
//	private String from;
//	private String notice = "";
//	private String id = "";
//	private String name = "";
//	private String location = "";
//	private String meettingid = "";
//	private JSONObject jsonObject,jsonObjectRe = null;
//	
//	public GetMeettingMsg(String msg){
//		
//		
//		try {
//			jsonObject = new JSONObject(msg);
//			if(jsonObject.has("reservation")){
//				String jsonre = jsonObject.getString("reservation");
//				jsonObjectRe = new JSONObject(jsonre);
//			}else {
//			}
//			
//			if(jsonObjectRe.has("topic")){
//				this.topic = jsonObjectRe.getString("topic");
//			}
//			
//			if(jsonObjectRe.has("to")){
//				this.to = jsonObjectRe.getString("to");
//			}
//			
//			if(jsonObjectRe.has("alert")){
//				this.alert = jsonObjectRe.getString("alert");
//			}
//			
//			if(jsonObjectRe.has("id")){
//				this.meettingid = jsonObjectRe.getString("id");
//			}
//			
//			if(jsonObjectRe.has("participant")){
//				this.participant = jsonObjectRe.getString("participant")
//						.replace("[", "").replace("]", "").replace("\"", "").split(",");
//			}
//			
//			if(jsonObjectRe.has("organizer")){
//				this.organizer = jsonObjectRe.getString("organizer");
//			}
//			
//			if(jsonObjectRe.has("from")){
//				this.from = jsonObjectRe.getString("from");
//			}
//			
//			if(jsonObjectRe.has("notice")){
//				this.notice = jsonObjectRe.getString("notice");
//			}
//			
//			if(jsonObjectRe.has("room")){
//				String roomstr = jsonObjectRe.getString("room");
//				JSONObject jsonRoom = new JSONObject(roomstr);
//				if(jsonRoom.has("id")){
//					this.id = jsonRoom.getString("id");
//				}
//				if(jsonRoom.has("building")){
//					JSONObject jsonLoc = new JSONObject();
//					jsonLoc = jsonRoom.getJSONObject("building");
//					this.location = jsonLoc.getString("name");
//				}
//				if(jsonRoom.has("name")){
//					this.name = jsonRoom.getString("name");
//				}
//			}
//			
//			
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//	}
//
//	public String getTopic() {
//		return topic;
//	}
//
//	public void setTopic(String topic) {
//		this.topic = topic;
//	}
//
//	public String getTo() {
//		return to;
//	}
//
//	public void setTo(String to) {
//		this.to = to;
//	}
//
//	public String getAlert() {
//		return alert;
//	}
//
//	public void setAlert(String alert) {
//		this.alert = alert;
//	}
//
//	public String getOrganizer() {
//		return organizer;
//	}
//
//	public void setOrganizer(String organizer) {
//		this.organizer = organizer;
//	}
//
//	public String[] getParticipant() {
//		return participant;
//	}
//
//	public void setParticipant(String[] participant) {
//		this.participant = participant;
//	}
//
//	public String getFrom() {
//		return from;
//	}
//
//	public void setFrom(String from) {
//		this.from = from;
//	}
//
//	public String getNotice() {
//		return notice;
//	}
//
//	public void setNotice(String notice) {
//		this.notice = notice;
//	}
//
//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public String getLocation() {
//		return location;
//	}
//
//	public void setLocation(String location) {
//		this.location = location;
//	}
//
//	public String getMeettingid() {
//		return meettingid;
//	}
//
//	public void setMeettingid(String meettingid) {
//		this.meettingid = meettingid;
//	}
//
//	public JSONObject getJsonObjectRe() {
//		return jsonObjectRe;
//	}
//
//	public void setJsonObjectRe(JSONObject jsonObjectRe) {
//		this.jsonObjectRe = jsonObjectRe;
//	}
	
	
}
