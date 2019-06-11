package com.inspur.emmcloud.bean.schedule.task;

import com.alibaba.fastjson.annotation.JSONField;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.widget.calendardayview.Event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Task implements Serializable {

    private Calendar creationDate;
    private String lastUpdate = "";
    private String state = "";
    private String id = "";
    private String master = "";
    private String owner = "";
    private String title = "";
    private int priority = 1;
    private Calendar dueDate;
    private List<TaskColorTag> tags = new ArrayList<TaskColorTag>();
    private List<Attachment> attachments = new ArrayList<Attachment>();
    private TaskSubject taskSubject;
    private String subjectstr = "";
    private TaskSubject subject;

    public Task() {
    }

    public Task(String task) {
        this(JSONUtils.getJSONObject(task));
    }

    public Task(JSONObject jsonObject) {

        try {

            if (jsonObject.has("attachments")) {
                JSONArray jsonArray = jsonObject.getJSONArray("attachments");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonAttach = jsonArray.getJSONObject(i);
                    Attachment attachment = new Attachment(jsonAttach);
                    attachments.add(attachment);
                }
            }

            if (jsonObject.has("dueDate")) {

                if (isNumeric(jsonObject.getString("dueDate"))) {
                    Long dueDateLong = jsonObject.getLong("dueDate");

                    if (dueDateLong != null) {
                        dueDate = TimeUtils.timeLong2Calendar(dueDateLong);
                    }
                }


            }
            if (jsonObject.has("creationDate")) {
                long creationDateLong = jsonObject.getLong("creationDate");
                creationDate = TimeUtils.timeLong2Calendar(creationDateLong);
            }
            if (jsonObject.has("lastUpdate")) {
                this.lastUpdate = jsonObject.getString("lastUpdate");
            }
            if (jsonObject.has("state")) {
                this.state = jsonObject.getString("state");
            }
            if (jsonObject.has("id")) {
                this.id = jsonObject.getString("id");
            }
            if (jsonObject.has("master")) {
                this.master = jsonObject.getString("master");
            }
            if (jsonObject.has("owner")) {
                this.owner = jsonObject.getString("owner");
            }
            if (jsonObject.has("title")) {
                this.title = jsonObject.getString("title");
            }
            if (jsonObject.has("priority")) {
                this.priority = jsonObject.getInt("priority");
            }


            if (jsonObject.has("tags")) {
                JSONArray array = jsonObject.getJSONArray("tags");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    tags.add(new TaskColorTag(obj));
                }
            }


            if (jsonObject.has("list")) {
                String listStr = jsonObject.getString("list");
                if (listStr != null && !listStr.equals("null")) {
                    this.taskSubject = new TaskSubject(jsonObject.getString("list"));
                }

            }
            if (jsonObject.has("subject")) {
                String subjectStr = jsonObject.getString("subject");
                if (subjectStr != null && !subjectStr.equals("null")) {
                    this.subject = new TaskSubject(jsonObject.getString("subject"));
                }

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static List<Event> taskList2EventList(List<Task> taskList, Calendar selectCalendar) {
        List<Event> eventList = new ArrayList<>();
        for (Task task : taskList) {
            if (TimeUtils.isSameDay(selectCalendar, task.getCreationDate()) || TimeUtils.isSameDay(selectCalendar, task.getDueDate())) {
                Event event = new Event(task.getId(), Schedule.TYPE_TASK, task.getTitle(), "", task.getCreationDate(), task.getDueDate(), task);
                eventList.add(event);
            }

        }
        return eventList;
    }

    public List<TaskColorTag> getTags() {
        return tags;
    }

    public void setTags(List<TaskColorTag> tags) {
        this.tags = tags;
    }

    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Calendar getDueDate() {
        return dueDate;
    }

    public void setDueDate(Calendar dueDate) {
        this.dueDate = dueDate;
    }

    @JSONField(serialize = false)
    public Calendar getLocalDueDate() {
        return TimeUtils.UTCCalendar2LocalCalendar(dueDate);
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public TaskSubject getList() {
        return taskSubject;
    }

    public void setList(TaskSubject list) {
        this.taskSubject = list;
    }

    public TaskSubject getSubject() {
        return subject;
    }

    public void setSubject(TaskSubject taskList) {
        this.subject = taskList;
    }

    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof Task)) {
            return false;
        }
        Task taskResult = (Task) other;
        //此处从==判断是否相等  改为equals
        return getId().equals(taskResult.getId());
    }
}
