package com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.util.mycamera.GetReatScaleResult;
import com.inspur.emmcloud.basemodule.util.mycamera.RectScale;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/12/2.
 */

public class CameraCropView extends RelativeLayout {
    // @BindView(R2.id.crop_view)
    CropView cropView;
    // @BindView(R2.id.recycler_view_radio)
    RecyclerView setRadioRecycleView;

    private String defaultRectScale;
    private List<RectScale> rectScaleList = new ArrayList<>();
    private int radioSelectPosition = 0;
    private Context context;

    public CameraCropView(Context context) {
        this(context, null);
    }

    public CameraCropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(final Context context) {
        // TODO Auto-generated method stub
        this.context = context;
        View view = View.inflate(context, R.layout.plugin_camera_crop_view, this);
        cropView = findViewById(R.id.crop_view);
        cropView.setTopMove(DensityUtil.dip2px(40));
        setRadioRecycleView = findViewById(R.id.recycler_view_radio);
//        View view = LayoutInflater.from(context).inflate(R.layout.plugin_camera_crop_view, this,true);
//        ButterKnife.bind(this, view);
    }

    public void setCropData(String json) {
        JSONObject optionsObj = JSONUtils.getJSONObject(json, "options", new JSONObject());
        defaultRectScale = JSONUtils.getString(optionsObj, "rectScale", null);
        String rectScaleListJson = JSONUtils.getString(optionsObj, "rectScaleList", "");
        rectScaleList = new GetReatScaleResult(rectScaleListJson).getRectScaleList();
        if (!StringUtils.isBlank(defaultRectScale) && rectScaleList.size() > 0) {
            boolean isSelectionRadio = false;
            for (int i = 0; i < rectScaleList.size(); i++) {
                String rectScale = rectScaleList.get(i).getRectScale();
                if (rectScale.equals(defaultRectScale)) {
                    radioSelectPosition = i;
                    isSelectionRadio = true;
                    break;
                }
            }
            if (!isSelectionRadio) {
                for (int i = 0; i < rectScaleList.size(); i++) {
                    String rectScale = rectScaleList.get(i).getRectScale();
                    if (rectScale.equals("custom")) {
                        radioSelectPosition = i;
                        break;
                    }
                }
            }
        }
        if (rectScaleList.size() > 0) {
            setVisibility(View.VISIBLE);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRadioRecycleView.setVisibility(View.VISIBLE);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    setRadioRecycleView.setLayoutManager(linearLayoutManager);
                    setRadioRecycleView.setAdapter(new Adapter());
                    cropView.setCustomRectScale(rectScaleList.get(radioSelectPosition).getRectScale());
                }
            }, 300);


        } else if (defaultRectScale != null) {
            setVisibility(View.VISIBLE);
            cropView.setCustomRectScale(defaultRectScale);

        } else {
            cropView.setCropEnabled(false);
            setVisibility(View.GONE);
        }
    }

    public RectScale getSelectRectScale(){
        if(rectScaleList == null || radioSelectPosition >= rectScaleList.size()){
            return null;
        }
        return rectScaleList.get(radioSelectPosition);
    }

    public void setCustomRectScale(String rectScale) {
        cropView.setCustomRectScale(rectScale);
    }

    public Bitmap getPicture(Bitmap originBitmap) {
        return cropView.getPicture(originBitmap);
    }

    public class Adapter extends
            RecyclerView.Adapter<Adapter.ViewHolder> {


        @Override
        public int getItemCount() {
            return rectScaleList.size();
        }

        /**
         * 创建ViewHolder
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
            TextView textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setPadding(DensityUtil.dip2px(context, 20), 0, DensityUtil.dip2px(context, 20), 0);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ViewHolder viewHolder = new ViewHolder(textView);
            viewHolder.textView = textView;
            return viewHolder;
        }

        /**
         * 设置值
         */
        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            viewHolder.textView.setText(rectScaleList.get(i).getName());
            viewHolder.textView.setTextColor((radioSelectPosition == i) ? Color.parseColor("#CB602D") : Color.parseColor("#FFFFFB"));
            viewHolder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    radioSelectPosition = i;
                    Adapter.this.notifyDataSetChanged();
                    RectScale rectScale = rectScaleList.get(i);
                    setCustomRectScale(rectScale.getRectScale());
                }
            });

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(View arg0) {
                super(arg0);
            }
        }

    }

}
