package com.inspur.emmcloud.bean.find;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TicketMsg {
    private String date = "";  //出发日期
    private String startTime = ""; //出发时间
    private String trainNum = "";  //车次
    private String startingPlace = ""; //始发站
    private String orderNum = ""; //订单号
    private int ticketNum = 0; //每条短信订阅车票的张数
    private String seatInfo;
    private Date sendDate;
    private List<SeatInfo> seatInfoList = new ArrayList<SeatInfo>();//座次列表

    /**
     * .
     * 订单号E404337706，陈先生您已购08月14日K1575次03车04号上铺,聊城15:34开。请尽快换取纸质车票。【铁路客服】
     */

    public TicketMsg(JSONObject ticketMsgObj, Context context) {
        try {
            String ticketMsg = ticketMsgObj.getString("smsBody");
            String orderNumRegEx = "[a-z0-9A-Z]{10}";
            Pattern orderNumP = Pattern.compile(orderNumRegEx);
            Matcher orderNumM = orderNumP.matcher(ticketMsg);
            if (orderNumM.find()) {
                orderNum = orderNumM.group();
            }

            String seatNumRegEx = "(\\d{1,2}车[0-9][a-zA-Z0-9]{0,}号)|([0-9][a-zA-Z0-9]{0,}号)";
            Pattern seatNumP = Pattern.compile(seatNumRegEx);
            Matcher seatNumM = seatNumP.matcher(ticketMsg);
            while (seatNumM.find()) {
                ticketNum++;
                SeatInfo seatInfo = new SeatInfo(seatNumM.group());
                if (StringUtils.isBlank(seatInfo.getCarriageNum()) && seatInfoList.size() > 0) {
                    seatInfo.setCarriageNum(seatInfoList.get(seatInfoList.size() - 1).getCarriageNum());
                }
                seatInfoList.add(seatInfo);
            }
            String dateRegEx = "\\d{1,2}月\\d{1,2}日";
            Pattern dateP = Pattern.compile(dateRegEx);
            Matcher dateM = dateP.matcher(ticketMsg);
            if (dateM.find()) {
                date = dateM.group();
            }

            trainNum = ticketMsg.substring(ticketMsg.indexOf(date) + date.length(),
                    ticketMsg.indexOf("次"));
            String timeRegEx = "([0-9]|([0-1][0-9])|2[0-3]):([0-5][0-9]|[0-9])";
            Pattern p = Pattern.compile(timeRegEx);
            Matcher m = p.matcher(ticketMsg);
            if (m.find()) {
                startTime = m.group();
            }
            String smsDateStr = ticketMsgObj.getString("smsDate");
            long smsMillis = Long.parseLong(smsDateStr);
            //为了确定火车票的出发年数
            sendDate = new Date(smsMillis);
            Calendar sendCalendar = Calendar.getInstance();
            sendCalendar.setTime(sendDate);
            int smsYears = sendCalendar.get(Calendar.YEAR);
            int smsMonths = sendCalendar.get(Calendar.MONTH) + 1;
            int ticketMonths = Integer.parseInt(date.split("月")[0]);
            int ticketDays = Integer.parseInt(date.substring(date.indexOf("月") + 1, date.indexOf("日")));
            if (smsMonths > ticketMonths) {
                smsYears = smsYears + 1;
            }
            date = smsYears + "-" + ticketMonths + "-" + ticketDays;
            String result = ticketMsg.substring(ticketMsg.indexOf(trainNum) + trainNum.length(),
                    ticketMsg.indexOf(startTime));
            if (result.contains(",")) {
                startingPlace = result.split(",")[1];
            } else {
                String[] stringArray = result.split("号");
                startingPlace = result.split("号")[stringArray.length - 1];
            }
//			seatInfo = result.split("号")[0];
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public String getDate() {
        return date;
    }

    public Date getStartDate(Context context) {
        String dateStr = date + " " + startTime;
        SimpleDateFormat fomat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = TimeUtils.timeString2Date(dateStr, fomat);
        return date;

    }

    public String getEndTime() {
        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int startMin = Integer.parseInt(startTime.split(":")[1]);
        int endMins = startHour * 60 + startMin + 100;
        int endHour = endMins / 60;
        int endMin = endMins % 60;
        String endMinStr = endMin + "";
        if (endMinStr.length() == 1) {
            endMinStr = endMinStr + "0";
        }
        return endHour + ":" + endMinStr;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getTrainNum() {
        return trainNum;
    }

    public String getStartingPlace() {
        return startingPlace;
    }


    public List<SeatInfo> getSeatInfoList() {
        return seatInfoList;
    }

    public String getSeatInfo() {
        return seatInfo;
    }

    public void setSeatInfo(String seatInfo) {
        this.seatInfo = seatInfo;
    }

    public Date getSendDate() {
        return sendDate;
    }

//	public String getSeatInfo(){
//		return seatInfo;
//	}

    public String getOrderNum() {
        return orderNum;
    }

    public class SeatInfo {
        private String carriageNum = ""; //车厢号
        private String seatNum = ""; //座位号
        //private String seatType="";  //座位类型


        public SeatInfo(String seatInfoStr) {//\\d{1,2}车[0-9][a-zA-Z0-9]{0,}号
            String carriageNumRegEx = "\\d{1,2}车";
            Pattern carriageNumP = Pattern.compile(carriageNumRegEx);
            Matcher carriageNumM = carriageNumP.matcher(seatInfoStr);
            if (carriageNumM.find()) {
                carriageNum = carriageNumM.group();
            }

            String seatNumRegEx = "[0-9][a-zA-Z0-9]{0,}号";
            Pattern seatNumP = Pattern.compile(seatNumRegEx);
            Matcher seatNumM = seatNumP.matcher(seatInfoStr);
            if (seatNumM.find()) {
                seatNum = seatNumM.group();
            }
        }

        public String getCarriageNum() {
            return carriageNum;
        }

        public void setCarriageNum(String carriageNum) {
            this.carriageNum = carriageNum;
        }

        public String getSeatNum() {
            return seatNum;
        }

        public void setSeatNum(String seatNum) {
            this.seatNum = seatNum;
        }

//		public String getSeatType(){
//			return seatType;
//		}
//		public void setSeatType(String seatType){
//			this.seatType = seatType;
//		}

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return carriageNum + seatNum;
        }

    }

}
