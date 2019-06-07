package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.bean.chat.MessageMatheSet;

import java.util.ArrayList;
import java.util.Iterator;


public class MessageMatheSetCacheUtils {

    public static void add(Context context, String cid, MatheSet matheSet) {
        try {

            MessageMatheSet messageMatheSet = DbCacheUtils.getDb(context).findById(MessageMatheSet.class, cid);
            Gson gson = new Gson();
            String matheSetStr = "";
            ArrayList<MatheSet> qeuqeu = null;
            if (messageMatheSet != null) {
                matheSetStr = messageMatheSet.getMatheSetStr();
                qeuqeu = gson.fromJson(matheSetStr,
                        new TypeToken<ArrayList<MatheSet>>() {
                        }.getType());
            }

            if (qeuqeu == null) {
                qeuqeu = new ArrayList<>();
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
            DbCacheUtils.getDb(context).saveOrUpdate(new MessageMatheSet(cid, matheSetStr));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public static MatheSet getInMatheSet(Context context, String cid,
                                         long creationDate) {
        ArrayList<MatheSet> qeuqeu = null;
        try {

            MessageMatheSet messageMatheSet = DbCacheUtils.getDb(context).findById(MessageMatheSet.class, cid);
            if (messageMatheSet != null) {
                String matheSetStr = messageMatheSet.getMatheSetStr();
                Gson gson = new Gson();
                qeuqeu = gson.fromJson(matheSetStr,
                        new TypeToken<ArrayList<MatheSet>>() {
                        }.getType());
            }
            if (qeuqeu == null) {
                qeuqeu = new ArrayList<>();
            }
            Iterator<MatheSet> it = qeuqeu.iterator();
            while (it.hasNext()) {
                MatheSet temp = it.next();
                if (temp.isInMatheSet(creationDate)) {
                    return temp;
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;

    }

    public static void clearMessageMatheSet(Context context) {
        try {
            DbCacheUtils.getDb(context).delete(MessageMatheSet.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
