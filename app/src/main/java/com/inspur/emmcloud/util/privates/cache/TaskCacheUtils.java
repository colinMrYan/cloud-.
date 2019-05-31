package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.schedule.task.Task;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/11.
 */

public class TaskCacheUtils {

    public static void saveTaskList(final Context context, final List<Task> taskList) {
        try {

            DbCacheUtils.getDb(context).saveOrUpdate(taskList); // 存储消息
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static List<Task> getTaskList(final Context context, Calendar startTime, Calendar endTime) {
        List<Task> taskList = null;
        try {

            taskList = DbCacheUtils.getDb(context).selector(Task.class).where(WhereBuilder.b("startTime", ">", startTime)
                    .and("endTime", "<", endTime)).or(WhereBuilder.b("startTime", "<=", startTime)
                    .and("endTime", ">", endTime)).or(WhereBuilder.b("startTime", "<=", endTime)
                    .and("endTime", ">=", endTime)).orderBy("lastTime", true).findAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (taskList == null) {
            taskList = new ArrayList<>();
        }
        return taskList;
    }


}
