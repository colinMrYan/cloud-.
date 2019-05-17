package com.inspur.emmcloud.widget.keyboardview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.InputMethodUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmmSecurityKeyboard extends PopupWindow {

    private static final int KEYBOARD_RADIX = 10;
    private static final int KEYBOARD_NUMBER_RANDOM_TYPE = 1;
    private static final int KEYBOARD_LETTER_RANDOM_TYPE = 2;
    private static final int KEYBOARD_CHANGE_NUMBER = -7;
    private static final int KEYBOARD_CHANGE_LETTER = -8;
    private static final int KEYBOARD_CHANGE_SYMBOL = -9;
    private KeyboardView keyboardView;
    private Keyboard keyboardLetter;
    private Keyboard keyboardNumber;
    private Keyboard keyboardSymbol;
    private boolean isNumber = false;
    private boolean isUpper = false;
    private boolean isSafeLetter = false;
    private View mainView;
    private ArrayList<String> numberList = new ArrayList<>();
    private ArrayList<String> letterList = new ArrayList<>();
    private EmmSecurityConfigure configuration;
    private EditText curEditText;
    private Context contextLocal;

    private OnKeyboardActionListener listener = new OnKeyboardActionListener() {
        @Override
        public void swipeUp() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onPress(int primaryCode) {
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Editable editable = curEditText.getText();
            int start = curEditText.getSelectionStart();
            if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                // 完成按钮所做的动作
                dismiss();
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                // 删除按钮所做的动作
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                // 大小写切换
                changeLetterKey();
                keyboardView.setKeyboard(keyboardLetter);
            } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
                // 数字键盘切换,默认是英文键盘
                if (isNumber) {
                    isNumber = false;
                    keyboardView.setKeyboard(keyboardLetter);
                } else {
                    isNumber = true;
                    keyboardView.setKeyboard(keyboardNumber);
                }
            } else if (primaryCode == 57419) {
                //左移
                if (start > 0) {
                    curEditText.setSelection(start - 1);
                }
            } else if (primaryCode == 57421) {
                //右移
                if (start < curEditText.length()) {
                    curEditText.setSelection(start + 1);
                }
            } else if (primaryCode == KEYBOARD_CHANGE_LETTER) {
                //切换到字母键盘
                keyboardView.setKeyboard(keyboardLetter);
            } else if (primaryCode == KEYBOARD_CHANGE_NUMBER) {
                //切换到数字键盘
                keyboardView.setKeyboard(keyboardNumber);
            } else if (primaryCode == KEYBOARD_CHANGE_SYMBOL) {
                //切换到特殊符号键盘
                keyboardView.setKeyboard(keyboardSymbol);
            } else if(primaryCode == -10){
                isSafeLetter = isSafeLetter?false:true;
                setRandomLetter(isSafeLetter);
            } else {
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }
    };

    public EmmSecurityKeyboard(Context context) {
        this(context, null);
    }

    public EmmSecurityKeyboard(Context context, EmmSecurityConfigure securityConfigure) {
        super(context);
        if (securityConfigure == null) {
            configuration = new EmmSecurityConfigure();
        } else {
            configuration = securityConfigure;
        }
        this.contextLocal = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(R.layout.emm_keyboard, null);
        mainView.findViewById(R.id.iv_keyboard_view_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mainView.findViewById(R.id.iv_keyboard_view_sys).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                InputMethodUtils.showKeyboard(curEditText);
            }
        });
        ((TextView)mainView.findViewById(R.id.tv_keyboard_name)).setText(R.string.emm_secure_keyboard);
        this.setContentView(mainView);
        this.setWidth(EmmDisplayUtils.getScreenWidth(context));
        this.setHeight(LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(Color.parseColor("#212121"));
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        this.setFocusable(false);
        this.setOutsideTouchable(false);
        this.setPopupWindowTouchModal(this, false);
        this.setAnimationStyle(R.style.PopupKeybroad);
        if (EmmDisplayUtils.dp2px(context, 236) > (int) (EmmDisplayUtils
                .getScreenHeight(context) * 3.0f / 5.0f)) {
            keyboardLetter = new Keyboard(context,
                    R.xml.emm_keyboard_english_land);
            keyboardNumber = new Keyboard(context, R.xml.emm_keyboard_number_land);
            keyboardSymbol = new Keyboard(context, R.xml.emm_keyboard_symbols_shift_land);
            initKeys();
        } else {
            keyboardLetter = new Keyboard(context, R.xml.emm_keyboard_english);
            keyboardNumber = new Keyboard(context, R.xml.emm_keyboard_number);
            keyboardSymbol = new Keyboard(context, R.xml.emm_keyboard_symbols_shift);
            initKeys();
        }
        keyboardView = mainView.findViewById(R.id.keyboard_view);
        switch (configuration.getDefaultKeyboardType().getCode()) {
            case 0:
                keyboardView.setKeyboard(keyboardLetter);
                break;
            case 1:
                keyboardView.setKeyboard(keyboardNumber);
                break;
            case 2:
                keyboardView.setKeyboard(keyboardSymbol);
                break;
            default:
                keyboardView.setKeyboard(keyboardLetter);
                break;
        }
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(listener);
    }

    /**
     * 设置字母键盘是否乱序
     * @param isRandomLetter
     */
    private void setRandomLetter(boolean isRandomLetter){
        if(isRandomLetter){
            randomKeys(KEYBOARD_LETTER_RANDOM_TYPE);
            randomKeys(KEYBOARD_NUMBER_RANDOM_TYPE);
            keyboardView.setKeyboard(keyboardNumber);
            keyboardView.setKeyboard(keyboardLetter);
            for (Key key : keyboardLetter.getKeys()) {
                if (key.codes[0] == -10) {
                    key.icon = contextLocal.getResources().getDrawable(
                            R.drawable.ic_safe_keyboard_press);
                }
                correctKeyLabelAndCode(key);
            }
        }else{
            EmmCreateKeyList.initLetters(letterList);
            EmmCreateKeyList.initNumbers(numberList);
            keyboardLetter = new Keyboard(contextLocal, R.xml.emm_keyboard_english);
            keyboardNumber = new Keyboard(contextLocal, R.xml.emm_keyboard_number);
            keyboardView.setKeyboard(keyboardNumber);
            keyboardView.setKeyboard(keyboardLetter);
            for (Key key : keyboardLetter.getKeys()) {
                if (key.codes[0] == -10) {
                    key.icon = contextLocal.getResources().getDrawable(
                            R.drawable.ic_safe_keyboard_normal);
                }
                correctKeyLabelAndCode(key);
            }

        }
    }

    private void correctKeyLabelAndCode(Key key){
        if(isUpper){
            if(key.label != null && isLowerASCIILetter(key)){
                key.label = key.label.toString().toUpperCase();
            }
            if(isLowerASCIILetter(key)){
                key.codes[0] = key.codes[0] - 32;
            }
            if (key.codes[0] == -1) {
                key.icon = contextLocal.getResources().getDrawable(
                        R.drawable.icon_keyboard_shift_c);
            }
        }else{
            if(key.label != null && isUpperASCIILetter(key)){
                key.label = key.label.toString().toLowerCase();
            }
            if(isUpperASCIILetter(key)){
                key.codes[0] = key.codes[0] + 32;
            }
            if (key.codes[0] == -1) {
                key.icon = contextLocal.getResources().getDrawable(
                        R.drawable.icon_keyboard_shift);
            }
        }
    }

    private boolean isLowerASCIILetter(Key key){
        return (key.codes[0]>=97 && key.codes[0] <= 122);
    }

    private boolean isUpperASCIILetter(Key key){
        return (key.codes[0]>=65 && key.codes[0] <= 90);
    }

    private void editTextAddListener() {
        //方法1，需要把构造函数里的EditText换成ClearEditText
//        curEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if(!b){
//                    dismiss();
//                }
//                clearEditText.onFocusChange(view,b);
//            }
//        });
        //方法2
        curEditText.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (!curEditText.isFocused()) {
                    dismiss();
                }
            }
        });
    }

    /**
     * 自主调用安全键盘
     */
    public void showSecurityKeyBoard(EditText clearEditText) {
        this.curEditText = clearEditText;
        editTextAddListener();
        disableShowInput();
        curEditText.requestFocus();
        //curEditText.setInputType(InputType.TYPE_NULL);
        //将光标移到文本最后
        Editable editable = curEditText.getText();
        Selection.setSelection(editable, editable.length());
        hideSystemKeyboard(curEditText);
        showKeyboard(curEditText);
    }

    private void disableShowInput() {
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            curEditText.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(curEditText, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initKeys() {
        EmmCreateKeyList.initLetters(letterList);
//        randomKeys(KEYBOARD_LETTER_RANDOM_TYPE);
        EmmCreateKeyList.initNumbers(numberList);
//        randomKeys(KEYBOARD_NUMBER_RANDOM_TYPE);
    }

    /**
     * @param popupWindow popupWindow 的touch事件传递
     * @param touchModal  true代表拦截，事件不向下一层传递，false表示不拦截，事件向下一层传递
     */
    @SuppressLint("PrivateApi")
    private void setPopupWindowTouchModal(PopupWindow popupWindow,
                                          boolean touchModal) {
        Method method;
        try {
            method = PopupWindow.class.getDeclaredMethod("setTouchModal",
                    boolean.class);
            method.setAccessible(true);
            method.invoke(popupWindow, touchModal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideSystemKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void changeLetterKey() {
        List<Key> keyList = keyboardLetter.getKeys();
        if (isUpper) {
            isUpper = false;
            for (Key key : keyList) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
                if (key.codes[0] == -1) {
                    key.icon = contextLocal.getResources().getDrawable(
                            R.drawable.icon_keyboard_shift);
                }
            }
        } else {// 小写切换大写
            isUpper = true;
            for (Key key : keyList) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
                if (key.codes[0] == -1) {
                    key.icon = contextLocal.getResources().getDrawable(
                            R.drawable.icon_keyboard_shift_c);
                }
            }
        }
    }

    private void randomKeys(int keyType) {
        List<Key> keyList = null;
        ArrayList<String> temList = null;
        switch (keyType) {
            case KEYBOARD_NUMBER_RANDOM_TYPE:
                if (numberList.size() == 0) {
                    EmmCreateKeyList.initNumbers(numberList);
                }
                keyList = keyboardNumber.getKeys();
                temList = new ArrayList<>(numberList);
                break;
            case KEYBOARD_LETTER_RANDOM_TYPE:
                if (letterList.size() == 0) {
                    EmmCreateKeyList.initLetters(letterList);
                }
                keyList = keyboardLetter.getKeys();
                temList = new ArrayList<>(letterList);
                break;
            default:
                keyList = new ArrayList<>();
                temList = new ArrayList<>();
                break;
        }
        for (Key key : keyList) {
            //这一行代表两个意义1，首先确定是不是对字母乱序，2，确定按键上是不是一个字母或者数字
            if (key.label != null && (keyType == KEYBOARD_LETTER_RANDOM_TYPE ?
                    isLetter(key.label.toString()) : isNumber(key.label.toString()))) {
                int number = new Random().nextInt(temList.size());
                String[] textArray = temList.get(number).split("#");
                key.label = isUpper ? textArray[1].toUpperCase():textArray[1].toLowerCase();
                key.codes[0] = Integer.valueOf(textArray[0], KEYBOARD_RADIX);
                temList.remove(number);
            }

        }
    }

    /**
     * 弹出键盘
     *
     * @param view
     */
    private void showKeyboard(View view) {
        int realHeight = 0;
        int yOff;
        yOff = realHeight - EmmDisplayUtils.dp2px(contextLocal, 231);
        if (EmmDisplayUtils.dp2px(contextLocal, 236) > (int) (EmmDisplayUtils
                .getScreenHeight(contextLocal) * 3.0f / 5.0f)) {
            yOff = EmmDisplayUtils.getScreenHeight(contextLocal)
                    - EmmDisplayUtils.dp2px(contextLocal, 199);
        }
        Animation animation = AnimationUtils.loadAnimation(contextLocal, R.anim.push_bottom_in);
        showAtLocation(view, Gravity.BOTTOM | Gravity.LEFT, 0, yOff);
        getContentView().setAnimation(animation);
    }


    private boolean isLetter(String str) {
        String letterStr = contextLocal.getString(R.string.emm_keyboard_a2z);
        return letterStr.contains(str.toLowerCase());
    }

    private boolean isNumber(String str) {
        String numStr = contextLocal.getString(R.string.emm_keyboard_zero2nine);
        return numStr.contains(str.toLowerCase());
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

}
