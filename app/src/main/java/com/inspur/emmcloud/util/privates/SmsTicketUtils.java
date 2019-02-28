package com.inspur.emmcloud.util.privates;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.inspur.emmcloud.bean.find.TicketMsg;
import com.inspur.emmcloud.bean.find.TicketMsg.SeatInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

///订购多张  改签一张
public class SmsTicketUtils {
    public static List<TicketMsg> getTicketMsg(Context Context, Date sendDate) {
        List<TicketMsg> buyTicketMsgList = new ArrayList<TicketMsg>();
        List<TicketMsg> alertTicketMsgList = new ArrayList<TicketMsg>();
        buyTicketMsgList = new ArrayList<TicketMsg>();
        List<JSONObject> smsBodyList = getSmsInPhone(Context, sendDate);
        for (int i = 0; i < smsBodyList.size(); i++) {
            String msgBody;
            try {
                msgBody = smsBodyList.get(i).getString("smsBody");
                if (msgBody.contains("您已购")) {
                    TicketMsg ticketMsg = new TicketMsg(smsBodyList.get(i), Context);
                    List<SeatInfo> seatInfoList = ticketMsg.getSeatInfoList();
                    for (int j = 0; j < seatInfoList.size(); j++) {
                        ticketMsg.setSeatInfo(seatInfoList.get(j).toString());
                        buyTicketMsgList.add(ticketMsg);
                    }

                }
//				if (msgBody.contains("您已签")) {
//					TicketMsg ticketMsg = new TicketMsg(smsBodyList.get(i), Context);
//					List<SeatInfo> seatInfoList = ticketMsg.getSeatInfoList();
//					for (int j = 0; j < seatInfoList.size(); j++) {
//						ticketMsg.setSeatInfo(seatInfoList.get(j).toString());
//						alertTicketMsgList.add(ticketMsg);
//					}
//				}
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

//		if (buyTicketMsgList.size()>0 &&alertTicketMsgList.size()>0) {
//			for (int i = 0; i < buyTicketMsgList.size(); i++) {
//				TicketMsg buyTicketMsg = buyTicketMsgList.get(i);
//				for (int j = 0; j < alertTicketMsgList.size(); j++) {
//					TicketMsg alertTicketMsg = alertTicketMsgList.get(j);
//					if (buyTicketMsg.getOrderNum().equals(alertTicketMsg.getOrderNum())) {
//						buyTicketMsgList.remove(i);
//						buyTicketMsgList.add(i, alertTicketMsg);
//					}
//				}
//			}
//		}
        return buyTicketMsgList;
    }

    // android获取短信所有内容

    private static List<JSONObject> getSmsInPhone(Context context, Date sendDate) {
        // final String SMS_URI_ALL = "content://sms/";
        final String SMS_URI_INBOX = "content://sms/inbox";
        // final String SMS_URI_SEND = "content://sms/sent";
        // final String SMS_URI_DRAFT = "content://sms/draft";
        List<JSONObject> smsBodyList = new ArrayList<JSONObject>();
        StringBuilder smsBuilder = new StringBuilder();

        try {
            ContentResolver cr = context.getContentResolver();
            String[] projection = new String[]{"_id", "address", "person",
                    "body", "date"};
            Uri uri = Uri.parse(SMS_URI_INBOX);
            //筛选最近一年的短信
            long lastUploadDateL = 0;
            if (sendDate != null) {
                lastUploadDateL = sendDate.getTime();
            }
            String where = " address in (10010,95105105) AND date >  "
                    + (System.currentTimeMillis() - 31536000000L) + " AND date > " + lastUploadDateL;
            Cursor cur = cr.query(uri, projection, where,
                    null, "date");
            if (cur.moveToFirst()) {
                // String name;
                // String phoneNumber;
                String smsbody;
                String date;

                int nameColumn = cur.getColumnIndex("person");
                int phoneNumberColumn = cur.getColumnIndex("address");
                int smsbodyColumn = cur.getColumnIndex("body");
                int dateColumn = cur.getColumnIndex("date");
                do {
                    // name = cur.getString(nameColumn);
                    // phoneNumber = cur.getString(phoneNumberColumn);
                    smsbody = cur.getString(smsbodyColumn);
                    String smsDate = cur.getString(dateColumn);
                    JSONObject smsObj = new JSONObject();
                    if (smsDate != null) {
                        smsObj.put("smsDate", smsDate);
                    }

                    if (smsbody != null) {
                        smsObj.put("smsBody", smsbody);
                    }
                    smsBodyList.add(smsObj);

                } while (cur.moveToNext());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return smsBodyList;
    }
}
