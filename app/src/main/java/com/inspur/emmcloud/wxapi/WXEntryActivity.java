package com.inspur.emmcloud.wxapi;


import com.umeng.socialize.weixin.view.WXCallbackActivity;

public class WXEntryActivity extends WXCallbackActivity {


//    private boolean isTranslucentOrFloating(){
//        boolean isTranslucentOrFloating = false;
//        try {
//            int [] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
//            final TypedArray ta = obtainStyledAttributes(styleableRes);
//            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
//            m.setAccessible(true);
//            isTranslucentOrFloating = (boolean)m.invoke(null, ta);
//            m.setAccessible(false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return isTranslucentOrFloating;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
//            boolean result = fixOrientation();
//        }
//        super.onCreate(savedInstanceState);
//    }
//
//    private boolean fixOrientation(){
//        try {
//            Field field = Activity.class.getDeclaredField("mActivityInfo");
//            field.setAccessible(true);
//            ActivityInfo o = (ActivityInfo)field.get(this);
//            o.screenOrientation = -1;
//            field.setAccessible(false);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    @Override
//    public void setRequestedOrientation(int requestedOrientation) {
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
//            return;
//        }
//        super.setRequestedOrientation(requestedOrientation);
//    }

}
