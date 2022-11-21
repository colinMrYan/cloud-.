package com.inspur.emmcloud.bean.chat;

public class PersonDto {
    private String name;// 姓名
    private String head;// 头像
    private String utype;// 用户类型
    private String sortLetters; // 显示数据拼音的首字母
    private String suoxie;// 姓名缩写
    private String uid; //userid
    private String pinyinFull; //全拼音
    private String nickname; //群聊时，成员昵称


    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getHead() {
        return head;
    }

    public final void setHead(String head) {
        this.head = head;
    }

    public final String getUtype() {
        return utype;
    }

    public final void setUtype(String utype) {
        this.utype = utype;
    }

    public final String getSortLetters() {
        return sortLetters;
    }

    public final void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public final String getSuoxie() {
        return suoxie;
    }

    public final void setSuoxie(String suoxie) {
        this.suoxie = suoxie;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPinyinFull() {
        return pinyinFull;
    }

    public void setPinyinFull(String pinyinFull) {
        this.pinyinFull = pinyinFull;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 复写equals方法
     *
     * @param other
     * @return
     */
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (!(other instanceof PersonDto))
            return false;
        final PersonDto personDto = (PersonDto) other;
        return getUid().equals(personDto.getUid());
    }
}