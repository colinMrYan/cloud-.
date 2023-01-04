package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.widget.ImageViewRound;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.componentservice.communication.SearchModel;

/**
 * Created by libaochao on 2019/12/23.
 */

public class ConversationOrContactGetIconUtil {
    /**
     * 统一显示图片
     *
     * @param searchModel
     * @param photoImg
     */
    public static void displayImg(SearchModel searchModel, ImageViewRound photoImg) {
        Integer defaultIcon = null; // 默认显示图标
        String icon = null;
        String type = searchModel.getType();
        if (type.equals(SearchModel.TYPE_GROUP)) {
            defaultIcon = ResourceUtils.getResValueOfAttr(photoImg.getContext(), R.attr.design3_icon_group_default);
            icon = CommunicationUtils.getHeadUrl(searchModel);
        } else if (type.equals(SearchModel.TYPE_STRUCT)) {
            defaultIcon = R.drawable.design3_icon_contact_struct;
        } else if (type.equals(SearchModel.TYPE_TRANSFER)) {
            defaultIcon = R.drawable.design3_icon_transfer;
        } else if (type.equals(SearchModel.TYPE_DIRECT)) {
            defaultIcon = ResourceUtils.getResValueOfAttr(photoImg.getContext(), R.attr.design3_icon_person_default);
            icon = CommunicationUtils.getHeadUrl(searchModel);
        } else {
            defaultIcon = ResourceUtils.getResValueOfAttr(photoImg.getContext(), R.attr.design3_icon_person_default);
            if (!searchModel.getId().equals("null")) {
                icon = APIUri.getChannelImgUrl(MyApplication.getInstance(), searchModel.getId());
            }

        }
        ImageDisplayUtils.getInstance().displayImageByTag(
                photoImg, icon, defaultIcon);
    }
}
