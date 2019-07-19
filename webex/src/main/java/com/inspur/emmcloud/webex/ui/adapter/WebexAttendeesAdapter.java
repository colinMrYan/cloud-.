package com.inspur.emmcloud.webex.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.login.R;
import com.inspur.emmcloud.webex.api.WebexAPIUri;
import com.inspur.emmcloud.webex.bean.WebexAttendees;

import java.util.List;

/**
 * Created by chenmch on 2018/10/30.
 */

public class WebexAttendeesAdapter extends BaseAdapter {
    private List<WebexAttendees> webexAttendeesList;
    private Context context;

    public WebexAttendeesAdapter(Context context, List<WebexAttendees> webexAttendeesList) {
        this.webexAttendeesList = webexAttendeesList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return webexAttendeesList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WebexAttendees webexAttendees = webexAttendeesList.get(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.item_view_webex_attendees, null);
        TextView emailText = (TextView) convertView.findViewById(R.id.tv_email);
        TextView personTypeText = (TextView) convertView.findViewById(R.id.tv_persion_type);
        TextView typeText = (TextView) convertView.findViewById(R.id.tv_type);
        emailText.setText(webexAttendees.getEmail());
        personTypeText.setText(webexAttendees.getPersonType() == 0 ? R.string.webex_external_attendees : R.string.webex_internal_attendees);
        typeText.setText(webexAttendees.getType());
        if (webexAttendees.getPersonType() == 1) {
            CircleTextImageView photoImg = (CircleTextImageView) convertView.findViewById(R.id.iv_photo);
            String photoUrl = WebexAPIUri.getWebexPhotoUrl(webexAttendees.getEmail());
            ImageDisplayUtils.getInstance().displayImage(photoImg, photoUrl, R.drawable.icon_person_default);
        }
        return convertView;
    }
}
