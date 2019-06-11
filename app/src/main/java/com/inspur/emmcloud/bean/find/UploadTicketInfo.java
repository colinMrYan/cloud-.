package com.inspur.emmcloud.bean.find;

import android.content.Context;

import java.util.Date;

public class UploadTicketInfo {
    private String sourceuname;
    private Date start;
    private String source = "SMS";
    private String from;
    private float cost = -1;
    private String number;
    private String way = "TRAIN";
    private Date sendDate;
    private String orderID;
    private String seatnumber;
    private Date end;

    public UploadTicketInfo(Context context, TicketMsg ticketMsg, String userName) {
        sourceuname = userName;
        start = ticketMsg.getStartDate(context);
        from = ticketMsg.getStartingPlace();
        number = ticketMsg.getTrainNum();
        sendDate = ticketMsg.getSendDate();
        orderID = ticketMsg.getOrderNum();
        seatnumber = ticketMsg.getSeatInfo();
    }

    public UploadTicketInfo() {

    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceuname() {
        return sourceuname;
    }

    public void setSourceuname(String sourceuname) {
        this.sourceuname = sourceuname;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String sonumberurce) {
        this.number = number;
    }

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getSeatnumber() {
        return seatnumber;
    }

    public void setSeatnumber(String seatnumber) {
        this.seatnumber = seatnumber;
    }

}
