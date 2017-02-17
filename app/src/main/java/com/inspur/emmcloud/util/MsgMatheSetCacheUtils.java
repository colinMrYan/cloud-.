package com.inspur.emmcloud.util;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inspur.emmcloud.bean.MatheSet;
import com.inspur.emmcloud.bean.MsgMatheSet;


public class MsgMatheSetCacheUtils {

	public static void add(Context context, String channelID, MatheSet matheSet) {
		try {
			
			MsgMatheSet msgMatheSet = DbCacheUtils.getDb(context).findById(MsgMatheSet.class, channelID);
			Gson gson = new Gson();
			String matheSetStr = "";
			ArrayList<MatheSet> qeuqeu = null;
			if (msgMatheSet != null) {
				matheSetStr = msgMatheSet.getMatheSetStr();
				qeuqeu = (ArrayList<MatheSet>) gson.fromJson(matheSetStr,
						new TypeToken<ArrayList<MatheSet>>() {
						}.getType());
			}
			
			if (qeuqeu == null) {
				qeuqeu = new ArrayList<MatheSet>();
			}
			Iterator<MatheSet> it = qeuqeu.iterator();
			while (it.hasNext()) {
				MatheSet temp = it.next();
				if (MatheSet.isIntersection(temp, matheSet)) {
					matheSet.merge(temp);
					it.remove();
				}
			}
			qeuqeu.add(matheSet);
			matheSetStr = gson.toJson(qeuqeu);
			DbCacheUtils.getDb(context).saveOrUpdate(new MsgMatheSet(channelID, matheSetStr));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public static MatheSet getInMatheSet(Context context, String channelID,
			String msgID) {
		ArrayList<MatheSet> qeuqeu = null;
		try {
			
			MsgMatheSet msgMatheSet = DbCacheUtils.getDb(context).findById(MsgMatheSet.class, channelID);
			if (msgMatheSet != null) {
				String matheSetStr = msgMatheSet.getMatheSetStr();
				Gson gson = new Gson();
				qeuqeu = (ArrayList<MatheSet>) gson.fromJson(matheSetStr,
						new TypeToken<ArrayList<MatheSet>>() {
						}.getType());
			}
			if (qeuqeu == null) {
				qeuqeu = new ArrayList<MatheSet>();
			}
			Iterator<MatheSet> it = qeuqeu.iterator();
			while (it.hasNext()) {
				MatheSet temp = it.next();
				if (temp.isInMatheSet(msgID)) {
					return temp;
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;

	}
	


}
