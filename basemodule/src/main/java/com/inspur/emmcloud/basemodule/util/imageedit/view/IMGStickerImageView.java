package com.inspur.emmcloud.basemodule.util.imageedit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.basemodule.R;


/**
 * Created by felix on 2017/12/21 下午10:58.
 */

public class IMGStickerImageView extends IMGStickerView {

    private ImageView mImageView;

    public IMGStickerImageView(Context context) {
        super(context);
    }

    public IMGStickerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IMGStickerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public View onCreateContentView(Context context) {
        mImageView = new ImageView(context);
        mImageView.setImageResource(R.drawable.ic_launcher);
        return mImageView;
    }
}
