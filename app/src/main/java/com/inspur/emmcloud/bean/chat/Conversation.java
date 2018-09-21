package com.inspur.emmcloud.bean.chat;

import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Created by chenmch on 2018/9/20.
 */

@Table(name = "Conversation")
public class Conversation implements Serializable{
    public static final String CONVERSATION_TYPE_DIRECT = "DIRECT";
    public static final String CONVERSATION_TYPE_GROUP = "GROUP";
    public static final String CONVERSATION_TYPE_CAST = "CAST";
    private String id;
    private String enterprise;
    private String name;
    private String owner;
    private String avatar;
    private String type;
    private String state;
}
