package com.inspur.emmcloud.bean.appcenter.webex;

import com.inspur.emmcloud.bean.contact.SearchModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/10/16.
 */

public class WebexAttendees implements Serializable{
    private String email;
    private SearchModel searchModel;
    public WebexAttendees(){

    }
    public WebexAttendees(String email){
        this.email = email;
    }

    public WebexAttendees(SearchModel searchModel){
        this.searchModel = searchModel;
        this.email = searchModel.getEmail();
    }

    public static List<WebexAttendees> SearchModelList2WebexAttendeesList(List<SearchModel> searchModelList){
        List<WebexAttendees> webexAttendeesList = new ArrayList<>();
        for (SearchModel searchModel:searchModelList){
            webexAttendeesList.add(new WebexAttendees(searchModel));
        }
        return webexAttendeesList;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public SearchModel getSearchModel() {
        return searchModel;
    }

    public void setSearchModel(SearchModel searchModel) {
        this.searchModel = searchModel;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof WebexAttendees))
            return false;

        final WebexAttendees otherWebexAttendees = (WebexAttendees) other;
        return getEmail().equals(otherWebexAttendees.getEmail());
    }
}
