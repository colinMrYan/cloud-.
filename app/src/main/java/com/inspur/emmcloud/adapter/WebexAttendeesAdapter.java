package com.inspur.emmcloud.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.appcenter.webex.WebexAttendees;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;

import java.util.List;

/**
 * Created by chenmch on 2018/10/30.
 */

public class WebexAttendeesAdapter extends BaseAdapter {
    private List<WebexAttendees> webexAttendeesList;
    private Context context;
    public WebexAttendeesAdapter(Context context,List<WebexAttendees> webexAttendeesList) {
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
        convertView = LayoutInflater.from(context).inflate(R.layout.item_view_webex_attendees,null);
        TextView emailText = (TextView)convertView.findViewById(R.id.tv_email);
        TextView personTypeText = (TextView)convertView.findViewById(R.id.tv_persion_type);
        TextView typeText = (TextView)convertView.findViewById(R.id.tv_type);
        emailText.setText(webexAttendees.getEmail());
        personTypeText.setText(webexAttendees.getPersonType() == 0?R.string.external_attendees:R.string.internal_attendees);
        typeText.setText(webexAttendees.getType());
        if (webexAttendees.getPersonType() == 1){
            CircleTextImageView photoImg = (CircleTextImageView)convertView.findViewById(R.id.iv_photo);
            String photoUrl = APIUri.getWebexPhotoUrl(webexAttendees.getEmail());
            ImageDisplayUtils.getInstance().displayImage(photoImg, photoUrl, R.drawable.icon_person_default);
        }
        return convertView;
    }
}
