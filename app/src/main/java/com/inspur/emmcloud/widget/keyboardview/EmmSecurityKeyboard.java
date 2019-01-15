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
import android.text.Selection;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.ClearEditText;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 安全键盘主类
 *
 * @author yidong (onlyloveyd@gmaol.com)
 * @date 2018/6/22 07:45
 */
public class EmmSecurityKeyboard extends PopupWindow {

    private static final int KEYBOARD_RADIX = 10;
    private static final int KEYBOARD_NUMBER_RADOM_TYPE = 1;
    private static final int KEYBOARD_LETTER_RADOM_TYPE = 2;
    private KeyboardView keyboardView;
    private Keyboard keyboardLetter;
    private Keyboard keyboardNumber;
    private Keyboard keyboardSymbol;
    private boolean isNumber = false;
    private boolean isUpper = false;
    private TextView symbolText, letterText, numberText;
    private View mainView;
    private ArrayList<String> numberList = new ArrayList<>();
    private ArrayList<String> letterList = new ArrayList<>();
    private ClearEditText curEditText;

    private RelativeLayout keyboardViewLy;
    private EmmSecurityConfigure configuration;

    private ViewGroup keyboardParentLayout;
    private Context context;

    @SuppressLint("ClickableViewAccessibility")
    public EmmSecurityKeyboard(final ViewGroup parentLayout, EmmSecurityConfigure securityConfigure) {
        super(parentLayout.getContext());
        if (securityConfigure == null) {
            configuration = new EmmSecurityConfigure();
        } else {
            configuration = securityConfigure;
        }
        keyboardParentLayout = parentLayout;
        context = parentLayout.getContext();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(R.layout.emm_keyboard, null);
        this.setContentView(mainView);
        this.setWidth(EmmDisplayUtils.getScreenWidth(context));
        this.setHeight(LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(Color.parseColor("#00000000"));
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setPopupWindowTouchModal(this, false);
        this.setAnimationStyle(R.style.PopupKeybroad);
        if (EmmDisplayUtils.dp2px(context, 236) > (int) (EmmDisplayUtils
                .getScreenHeight(context) * 3.0f / 5.0f)) {
            keyboardLetter = new Keyboard(context,
                    R.xml.emm_keyboard_english_land);
            keyboardNumber = new Keyboard(context, R.xml.emm_keyboard_number_land);
            keyboardSymbol = new Keyboard(context, R.xml.emm_keyboard_symbols_shift_land);
        } else {
            keyboardLetter = new Keyboard(context, R.xml.emm_keyboard_english);
            keyboardNumber = new Keyboard(context, R.xml.emm_keyboard_number);
            keyboardSymbol = new Keyboard(context, R.xml.emm_keyboard_symbols_shift);
        }
        keyboardView = mainView.findViewById(R.id.keyboard_view);
        keyboardViewLy = mainView.findViewById(R.id.keyboard_view_ly);
        symbolText = mainView.findViewById(R.id.tv_symbol);
        letterText = mainView.findViewById(R.id.tv_letter);
        numberText = mainView.findViewById(R.id.tv_number);
        if (!configuration.isLetterEnabled()) {
            letterText.setVisibility(View.GONE);
        }
        if (!configuration.isNumberEnabled()) {
            numberText.setVisibility(View.GONE);
        }
        if (!configuration.isSymbolEnabled()) {
            symbolText.setVisibility(View.GONE);
        }
        switchKeyboardType(configuration.getDefaultKeyboardType(),
                configuration.getSelectedColor(), configuration.getUnselectedColor());

        switch (configuration.getDefaultKeyboardType().getCode()) {
            case 0:
                keyboardView.setKeyboard(keyboardLetter);
                break;
            case 1:
                EmmCreateKeyList.initNumbers(numberList);
//                randomNumbers();
                randomKeys(KEYBOARD_NUMBER_RADOM_TYPE);
                keyboardView.setKeyboard(keyboardNumber);
                break;
            case 2:
                EmmCreateKeyList.initLetters(letterList);
//                randomLetters();
                randomKeys(KEYBOARD_LETTER_RADOM_TYPE);
                keyboardView.setKeyboard(keyboardSymbol);
                break;
            default:
                keyboardView.setKeyboard(keyboardLetter);
                break;
        }
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(listener);
        numberText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                switchKeyboardType(EmmKeyboardType.NUMBER,
                        configuration.getSelectedColor(),
                        configuration.getUnselectedColor());
                //点击需要改变时，解开这里
//                randomNumbers();
                randomKeys(KEYBOARD_NUMBER_RADOM_TYPE);
                keyboardView.setKeyboard(keyboardNumber);
            }
        });
        letterText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                switchKeyboardType(EmmKeyboardType.LETTER,
                        configuration.getSelectedColor(),
                        configuration.getUnselectedColor());
                //点击需要改变时，解开这里
//                randomLetters();
                randomKeys(KEYBOARD_LETTER_RADOM_TYPE);
                keyboardView.setKeyboard(keyboardLetter);
            }
        });
        symbolText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                switchKeyboardType(EmmKeyboardType.SYMBOL,
                        configuration.getSelectedColor(),
                        configuration.getUnselectedColor());
                keyboardView.setKeyboard(keyboardSymbol);
            }
        });
        List<View> children = getAllChildren(parentLayout);
        for (int i = 0; i < children.size(); i++) {
            View view = children.get(i);
            if (view instanceof ClearEditText) {
                ClearEditText securityEditText = (ClearEditText) view;
                securityEditText.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            curEditText = (ClearEditText) v;
                            curEditText.requestFocus();
                            //curEditText.setInputType(InputType.TYPE_NULL);
                            //将光标移到文本最后
                            Editable editable = curEditText.getText();
                            Selection.setSelection(editable, editable.length());
                            hideSystemKeyboard(v);
                            showKeyboard(keyboardParentLayout);
                        }
                        return true;
                    }
                });
            }
        }
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
                hideKeyboard();
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                // 删除按钮所做的动作
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                // 大小写切换
                changeKey();
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
            } else {
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }
    };

    /**
     * 键盘大小写切换
     */
    private void changeKey() {
        List<Key> keylist = keyboardLetter.getKeys();
        if (isUpper) {
            isUpper = false;
            for (Key key : keylist) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
                if (key.codes[0] == -1) {
                    key.icon = context.getResources().getDrawable(
                            R.drawable.icon_keyboard_shift);
                }
            }
        } else {// 小写切换大写
            isUpper = true;
            for (Key key : keylist) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
                if (key.codes[0] == -1) {
                    key.icon = context.getResources().getDrawable(
                            R.drawable.icon_keyboard_shift_c);
                }
            }
        }
    }

    private void randomKeys(int keyType){
        List<Key> keyList = null;
        ArrayList<String> temNumList = null;
        switch (keyType){
            case KEYBOARD_NUMBER_RADOM_TYPE:
                if(numberList.size() == 0){
                    EmmCreateKeyList.initNumbers(numberList);
                }
                keyList = keyboardNumber.getKeys();
                temNumList = new ArrayList<>(numberList);
                break;
            case KEYBOARD_LETTER_RADOM_TYPE:
                if(letterList.size() == 0){
                    EmmCreateKeyList.initLetters(letterList);
                }
                keyList = keyboardLetter.getKeys();
                temNumList = new ArrayList<>(letterList);
                break;
            default:
                keyList = new ArrayList<>();
                temNumList = new ArrayList<>();
                break;
        }
        for (Key key : keyList) {
            if (key.label != null && (keyType == KEYBOARD_LETTER_RADOM_TYPE?
                    isLetter(key.label.toString()):isNumber(key.label.toString()))) {
                int number = new Random().nextInt(temNumList.size());
                String[] textArray = temNumList.get(number).split("#");
                key.label = textArray[1];
                key.codes[0] = Integer.valueOf(textArray[0], 10);
                temNumList.remove(number);
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
        yOff = realHeight - EmmDisplayUtils.dp2px(context, 231);
        if (EmmDisplayUtils.dp2px(context, 236) > (int) (EmmDisplayUtils
                .getScreenHeight(context) * 3.0f / 5.0f)) {
            yOff = EmmDisplayUtils.getScreenHeight(context)
                    - EmmDisplayUtils.dp2px(context, 199);
        }
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.push_bottom_in);
        showAtLocation(view, Gravity.BOTTOM | Gravity.LEFT, 0, yOff);
        getContentView().setAnimation(animation);
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        this.dismiss();
    }

    private boolean isLetter(String str) {
        String letterStr = context.getString(R.string.aToz);
        return letterStr.contains(str.toLowerCase());
    }

    private boolean isNumber(String str) {
        String numStr = context.getString(R.string.zeroTonine);
        return numStr.contains(str.toLowerCase());
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    /**
     * 键盘类型切换后文本颜色变化
     *
     * @param keyboardType
     * @param selectedColor
     * @param unSelectedColor
     */
    private void switchKeyboardType(EmmKeyboardType keyboardType, int selectedColor, int unSelectedColor) {
        switch (keyboardType.getCode()) {
            case 0:
                letterText.setTextColor(selectedColor);
                symbolText.setTextColor(unSelectedColor);
                numberText.setTextColor(unSelectedColor);
                break;
            case 1:
                numberText.setTextColor(selectedColor);
                symbolText.setTextColor(unSelectedColor);
                letterText.setTextColor(unSelectedColor);
                break;
            case 2:
                symbolText.setTextColor(selectedColor);
                letterText.setTextColor(unSelectedColor);
                numberText.setTextColor(unSelectedColor);
                break;
            default:
                throw new IllegalArgumentException("不支持的键盘类型");
        }
    }

    /**
     * 获取所有子元素
     *
     * @param parent
     * @return
     */
    private List<View> getAllChildren(View parent) {
        List<View> allChildren = new ArrayList<>();
        if (parent instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) parent;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View child = vp.getChildAt(i);
                allChildren.add(child);
                //再次 调用本身（递归）
                allChildren.addAll(getAllChildren(child));
            }
        }
        return allChildren;
    }
}
