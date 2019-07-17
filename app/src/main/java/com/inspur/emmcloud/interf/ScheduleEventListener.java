package com.inspur.emmcloud.interf;

import com.inspur.emmcloud.widget.calendardayview.Event;

/**
 * Created by chenmch on 2019/7/16.
 */

public interface ScheduleEventListener {
    void onShowEventDetail(Event event);

    void onEventTimeUpdate(Event event, int top, int height);

    boolean onRemoveEventAddDragScaleView();

    void onEventDelete(Event event);

    void onEventShare(Event event);

    void onGroupChat(Event event);

    void dismissAllDayEventDlg();
}
