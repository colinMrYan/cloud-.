package com.inspur.emmcloud.bean.work;

import com.inspur.emmcloud.util.privates.TimeUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetRoomAvailableResult {

    long starttime = TimeUtils.getStartTime();
    long middletime = TimeUtils.getMiddleTime();
    long endtime = TimeUtils.getEndTime();
    private JSONArray jsonArray;
    private ArrayList<ArrayList<AvailableTime>> roomAvailableDays = new ArrayList<ArrayList<AvailableTime>>();
    private ArrayList<AvailableTime> roomAvailableTimes = new ArrayList<AvailableTime>();

    public GetRoomAvailableResult(String response) {

        try {
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                if (i == 0) {
                    for (int j = 0; j < array.length(); j++) {

                        JSONObject obj = array.getJSONObject(j);
                        AvailableTime availableTime = new AvailableTime(obj);
                        String todayEndtime = availableTime.getEnd();
                        Long todayEndtimelong = Long.parseLong(todayEndtime);
                        String todayBeginTime = availableTime.getStart();
                        Long todayBegintimeLong = Long.parseLong(todayBeginTime);
                        Long nowtimelong = System.currentTimeMillis();
                        if (nowtimelong > todayEndtimelong) {
                        } else if (nowtimelong < todayEndtimelong) {
                            if ((todayEndtimelong - nowtimelong) < (30 * 60 * 1000)) {
                            } else {
                                if (todayBegintimeLong < starttime) {
                                    availableTime.setStart("" + starttime);
                                }
                                if (todayEndtimelong > endtime) {
                                    availableTime.setEnd("" + endtime);
                                }
                                if (todayBegintimeLong < nowtimelong) {
                                    availableTime.setStart(nowtimelong + "");
                                }

                                roomAvailableTimes.add(availableTime);

                            }
                        }

                    }
                    roomAvailableDays.add(roomAvailableTimes);
                } else {

                    ArrayList<AvailableTime> tommrrowRoomAvailableTimes = new ArrayList<AvailableTime>();
                    for (int j = 0; j < array.length(); j++) {
                        JSONObject obj = array.getJSONObject(j);
                        AvailableTime availableTime = new AvailableTime(obj);

//						String tomorrowEndtime = availableTime.getEnd();
//						Long tomorrowEndtimelong = Long.parseLong(tomorrowEndtime);
//						String tomorrowBeginTime = availableTime.getStart();
//						Long tomorrowBegintimeLong = Long.parseLong(tomorrowBeginTime);

//						if(tomorrowBegintimeLong<eight){
//							LogUtils.debug("yfcLog", "处理头部时间"+availableTime.getStart());
//							availableTime.setStart(""+eight);
//						}
//						if(tomorrowEndtimelong>five){
//							availableTime.setEnd(""+five);
//							LogUtils.debug("yfcLog", "处理尾部时间"+availableTime.getEnd());
//						}

                        tommrrowRoomAvailableTimes.add(availableTime);
                    }
                    roomAvailableDays.add(tommrrowRoomAvailableTimes);
                }
//				else if (i == 2) {
//					ArrayList<AvailableTime> tommrrowRoomAvailableTimes = new ArrayList<GetRoomAvailableResult.AvailableTime>();
//					for (int j = 0; j < array.length(); j++) {
//						JSONObject obj = array.getJSONObject(j);
//						AvailableTime availableTime = new AvailableTime(obj);
//						tommrrowRoomAvailableTimes.add(availableTime);
//					}
//					roomAvailableDays.add(tommrrowRoomAvailableTimes);
//				}else if (i == 3) {
//					ArrayList<AvailableTime> tommrrowRoomAvailableTimes = new ArrayList<GetRoomAvailableResult.AvailableTime>();
//					for (int j = 0; j < array.length(); j++) {
//						JSONObject obj = array.getJSONObject(j);
//						AvailableTime availableTime = new AvailableTime(obj);
//						tommrrowRoomAvailableTimes.add(availableTime);
//					}
//					roomAvailableDays.add(tommrrowRoomAvailableTimes);
//				}else if (i == 4) {
//					ArrayList<AvailableTime> tommrrowRoomAvailableTimes = new ArrayList<GetRoomAvailableResult.AvailableTime>();
//					for (int j = 0; j < array.length(); j++) {
//						JSONObject obj = array.getJSONObject(j);
//						AvailableTime availableTime = new AvailableTime(obj);
//						tommrrowRoomAvailableTimes.add(availableTime);
//					}
//					roomAvailableDays.add(tommrrowRoomAvailableTimes);
//				}else if (i == 5) {
//					ArrayList<AvailableTime> tommrrowRoomAvailableTimes = new ArrayList<GetRoomAvailableResult.AvailableTime>();
//					for (int j = 0; j < array.length(); j++) {
//						JSONObject obj = array.getJSONObject(j);
//						AvailableTime availableTime = new AvailableTime(obj);
//						tommrrowRoomAvailableTimes.add(availableTime);
//					}
//					roomAvailableDays.add(tommrrowRoomAvailableTimes);
//				}else if (i == 6) {
//					ArrayList<AvailableTime> tommrrowRoomAvailableTimes = new ArrayList<GetRoomAvailableResult.AvailableTime>();
//					for (int j = 0; j < array.length(); j++) {
//						JSONObject obj = array.getJSONObject(j);
//						AvailableTime availableTime = new AvailableTime(obj);
//						tommrrowRoomAvailableTimes.add(availableTime);
//					}
//					roomAvailableDays.add(tommrrowRoomAvailableTimes);
//				}


            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<ArrayList<AvailableTime>> getRoomAvailableDays() {
        return roomAvailableDays;
    }

    public class AvailableTime {
        private String duration = "";
        private String start = "";
        private String end = "";

        public AvailableTime(JSONObject obj) {
            try {
                if (obj.has("duration")) {
                    this.duration = obj.getString("duration");
                }
                if (obj.has("init")) {
                    this.start = obj.getString("init");
                }
                if (obj.has("end")) {
                    this.end = obj.getString("end");
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public String getDuration() {
            return duration;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }


    }

//	public ArrayList<AvailableTime> getRoomAvailableTimes() {
//		return roomAvailableTimes;
//	}

}
