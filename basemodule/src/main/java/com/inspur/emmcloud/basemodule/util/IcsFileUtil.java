package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class IcsFileUtil {
    /**
     * 解析ICS文件
     */
    public static void parseIcsFile(Context context, Uri uri) {
        try {
            String path = getFilePathFromContentUri(context, uri);
            FileInputStream input = new FileInputStream(path);
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(input);
            for (Iterator i = calendar.getComponents(Component.VEVENT).iterator(); i.hasNext(); ) {
                VEvent event = (VEvent) i.next();
                if (event != null) {
                    String startDate = "开始时间：" + event.getStartDate().getValue();
                    String endDate = "结束时间" + event.getEndDate().getValue();
                    String summary = "主题：" + event.getSummary().getValue();
                    if (event.getLocation() != null) {
                        String location = "地点" + event.getLocation().getValue();
                    }
                    if (event.getDescription() != null) {
                        String desc = "描述：" + event.getDescription().getValue();
                    }
                    if (event.getCreated() != null) {
                        String create = "创建时间" + event.getCreated().getValue();
                    }

                    if (event.getLastModified() != null) {
                        String lastModify = "最后修改时间" + event.getLastModified().getValue();
                    }

                    //参考https://www.cnblogs.com/parryyang/p/5948436.html

                    //TODO 将事件添加到日历 逻辑
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * uri 转化绝对路径
     */
    public static String getFilePathFromContentUri(Context context, Uri uri) {
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = context.getContentResolver().query(uri,
                filePathColumn, null, null, null);

        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }
}
