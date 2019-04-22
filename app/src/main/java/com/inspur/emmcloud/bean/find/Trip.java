package com.inspur.emmcloud.bean.find;

import android.content.Context;

import com.alibaba.fastjson.annotation.JSONField;
import com.inspur.emmcloud.util.privates.TimeUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * [{"cost":184.5, "destination":"济南西", "from":"北京南", "level":"二等座",
 * "number":"G203", "orderID":"EB88381008", "seatnumber":"10车01F号",
 * "sendDate":"2016-04-14T07:53:54Z", "source":"email",
 * "sourceuid":"4d40846e-12cc-4664-9b12-c2c7a74a4175", "sourceuname":"部凡",
 * "startWebSocket":"2016-03-17T19:00:00Z", "tid":1,
 * "uid":"4d40846e-12cc-4664-9b12-c2c7a74a4175", "uname":"部凡", "way":"TRAIN"},
 * {"cost":184.5,"destination":"济南西","from":"北京南","level":"二等座","number":"G203",
 * "orderID"
 * :"EB88381008","seatnumber":"12车01A号","sendDate":"2016-04-14T07:53:54Z"
 * ,"source"
 * :"email","sourceuid":"4d40846e-12cc-4664-9b12-c2c7a74a4175","sourceuname"
 * :"李斌","startWebSocket":"2016-03-17T19:00:00Z","tid":2,"way":"TRAIN"}]
 *
 * @author Administrator
 */
public class Trip implements Serializable {
    private String tid = "";
    private float cost;
    private String destination = "";
    private String from = "";
    private String level = "";
    private String number = "";
    private String orderID = "";
    private String seatnumber = "";
    private Date sendDate;
    private String source = "";
    private String sourceuid = "";
    private String sourceuname = "";
    private Date start;
    private String uid = "";
    private String uname = "";
    private String way = "";
    private Date end;
    private String fromCity;
    private String destinationCity;

    public Trip() {

    }

    public Trip(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            if (obj.has("tid")) {
                this.tid = obj.getString("tid");
            }
            if (obj.has("cost")) {
                String costStr = obj.getString("cost");
                cost = Float.valueOf(costStr);
            }
            if (obj.has("destination")) {
                this.destination = obj.getString("destination");
            }
            if (obj.has("from")) {
                this.from = obj.getString("from");
            }

            if (obj.has("level")) {
                this.level = obj.getString("level");
            }
            if (obj.has("number")) {
                this.number = obj.getString("number");
            }
            if (obj.has("orderID")) {
                this.orderID = obj.getString("orderID");
            }
            if (obj.has("seatnumber")) {
                this.seatnumber = obj.getString("seatnumber");
            }
            if (obj.has("sendDate")) {
                String sendDateStr = obj.getString("sendDate");
                this.sendDate = TimeUtils.UTCString2LocalDate(sendDateStr);
            }
            if (obj.has("source")) {
                this.source = obj.getString("source");
            }

            if (obj.has("sourceuid")) {
                this.sourceuid = obj.getString("sourceuid");
            }
            if (obj.has("sourceuname")) {
                this.sourceuname = obj.getString("sourceuname");
            }
            if (obj.has("uid")) {
                this.uid = obj.getString("uid");
            }
            if (obj.has("uname")) {
                this.uname = obj.getString("uname");
            }
            if (obj.has("way")) {
                this.way = obj.getString("way");
            }
            if (obj.has("init")) {
                String startStr = obj.getString("init");
                this.start = TimeUtils.UTCString2LocalDate(startStr);
            }
            if (obj.has("end")) {
                String endStr = obj.getString("end");
                this.end = TimeUtils.UTCString2LocalDate(endStr);
            }
            if (obj.has("fromCity")) {
                this.fromCity = obj.getString("fromCity");
            }
            if (obj.has("destinationCity")) {
                this.destinationCity = obj.getString("destinationCity");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Trip(JSONObject obj) {
        try {

            if (obj.has("tid")) {
                this.tid = obj.getString("tid");
            }
            if (obj.has("cost")) {
                String costStr = obj.getString("cost");
                cost = Float.valueOf(costStr);
            }
            if (obj.has("destination")) {
                this.destination = obj.getString("destination");
            }
            if (obj.has("from")) {
                this.from = obj.getString("from");
            }

            if (obj.has("level")) {
                this.level = obj.getString("level");
            }
            if (obj.has("number")) {
                this.number = obj.getString("number");
            }
            if (obj.has("orderID")) {
                this.orderID = obj.getString("orderID");
            }
            if (obj.has("seatnumber")) {
                this.seatnumber = obj.getString("seatnumber");
            }
            if (obj.has("sendDate")) {
                String sendDateStr = obj.getString("sendDate");
                this.sendDate = TimeUtils.UTCString2LocalDate(sendDateStr);

            }
            if (obj.has("source")) {
                this.source = obj.getString("source");
            }

            if (obj.has("sourceuid")) {
                this.sourceuid = obj.getString("sourceuid");
            }
            if (obj.has("sourceuname")) {
                this.sourceuname = obj.getString("sourceuname");
            }
            if (obj.has("uid")) {
                this.uid = obj.getString("uid");
            }
            if (obj.has("uname")) {
                this.uname = obj.getString("uname");
            }
            if (obj.has("way")) {
                this.way = obj.getString("way");
            }
            if (obj.has("startWebSocket")) {
                String startStr = obj.getString("startWebSocket");
                this.start = TimeUtils.UTCString2LocalDate(startStr);
            }
            if (obj.has("end")) {
                String endStr = obj.getString("end");
                this.end = TimeUtils.UTCString2LocalDate(endStr);
            }
            if (obj.has("fromCity")) {
                this.fromCity = obj.getString("fromCity");
            }
            if (obj.has("destinationCity")) {
                this.destinationCity = obj.getString("destinationCity");
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getSourceuname() {
        return sourceuname;
    }

    public void setSourceuname(String sourceuname) {
        this.sourceuname = sourceuname;
    }

    public String getSourceuid() {
        return sourceuid;
    }

    public void setSourceuid(String sourceuid) {
        this.sourceuid = sourceuid;
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }


    public String getSeatnumber() {
        return seatnumber;
    }

    public void setSeatnumber(String seatnumber) {
        this.seatnumber = seatnumber;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }


    @JSONField(serialize = false)
    public String getStartDate(Context context) {
        return TimeUtils.calendar2FormatString(context, start, TimeUtils.FORMAT_YEAR_MONTH_DAY);
    }

    @JSONField(serialize = false)
    public String getStartTime(Context context) {
        return TimeUtils.calendar2FormatString(context, start, TimeUtils.FORMAT_HOUR_MINUTE);
    }

    @JSONField(serialize = false)
    public String getEndDate(Context context) {
        return TimeUtils.calendar2FormatString(context, end, TimeUtils.FORMAT_YEAR_MONTH_DAY);
    }

    @JSONField(serialize = false)
    public String getEndTime(Context context) {
        return TimeUtils.calendar2FormatString(context, end, TimeUtils.FORMAT_HOUR_MINUTE);
    }
}
