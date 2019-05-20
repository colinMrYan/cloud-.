package com.inspur.emmcloud.bean.work;

import com.inspur.emmcloud.util.privates.TimeUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

public class Meeting implements Serializable, Comparator {
    private String meetingId = "";
    private String topic = "";
    private String from = "";
    private String to = "";
    private String participant = "";
    //	private JSONArray participantArray;
    private String alert;
    private String notice;
    private String organizer = "";
    private String attendant = "";
    private String location = "";
    private ArrayList<Room> rooms = new ArrayList<Room>();
    private ArrayList<String> participants = new ArrayList<String>();
    private String bookDate = "";

    public Meeting() {

    }

    public Meeting(JSONObject obj) {
        try {
            if (obj.has("id")) {
                this.meetingId = obj.getString("id");
            }
            if (obj.has("topic")) {
                this.topic = obj.getString("topic");
            }
            if (obj.has("from")) {
                this.from = obj.getString("from");
            }
            if (obj.has("to")) {
                this.to = obj.getString("to");
            }
            if (obj.has("participant")) {
                this.participant = obj.getString("participant");
//				this.participantArray = obj.getJSONArray("participant");
                JSONArray jsonArray = new JSONArray(participant);
                for (int i = 0; i < jsonArray.length(); i++) {
                    participants.add(jsonArray.getString(i));
                }
            }


            if (obj.has("room")) {
                String json = obj.getString("room");
                rooms.add(new Room(json));

                String roomstr = obj.getString("room");
                JSONObject jsonRoom = new JSONObject(roomstr);
//				if(jsonRoom.has("id")){
//					this.id = jsonRoom.getString("id");
//				}
                if (jsonRoom.has("building")) {
                    JSONObject jsonLoc = new JSONObject();
                    jsonLoc = jsonRoom.getJSONObject("building");
                    this.location = jsonLoc.getString("name");
                }
//				if(jsonRoom.has("name")){
//					this.name = jsonRoom.getString("name");
//				}
            }
            if (obj.has("alert")) {
                this.alert = obj.getString("alert");
            }
            if (obj.has("notice")) {
                this.notice = obj.getString("notice");
            }

            if (obj.has("organizer")) {
                this.organizer = obj.getString("organizer");
            }

            if (obj.has("bookDate")) {
                this.bookDate = obj.getString("bookDate");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

//	public JSONArray getParticipantArray() {
//		return participantArray;
//	}

//	public void setParticipantArray(JSONArray participantArray) {
//		this.participantArray = participantArray;
//	}

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public String getAlert() {
        return alert;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getBookDate() {
        return bookDate;
    }

    public void setBookDate(String bookDate) {
        this.bookDate = bookDate;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }

    public String getAttendant() {
        return attendant;
    }

    //
    public void setAttendant(String attendant) {
        this.attendant = attendant;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Calendar getFromCalendar() {
        return TimeUtils.timeString2Calendar(from);
    }

    public Calendar getToCalendar() {
        return TimeUtils.timeString2Calendar(to);
    }


    @Override
    public int compare(Object lhs, Object rhs) {
        Meeting meetingA = (Meeting) lhs;
        Meeting meetingB = (Meeting) rhs;
        Long fromA = Long.parseLong(meetingA.getFrom());
        Long fromB = Long.parseLong(meetingB.getFrom());
        if (fromA < fromB) {
            return -1;
        } else if (fromA > fromB) {
            return 1;
        } else {
            return 0;
        }
    }

    /*
     * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
     */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Meeting))
            return false;

        final Meeting otherMeeting = (Meeting) other;
        return getMeetingId().equals(otherMeeting.getMeetingId());
    }

}
