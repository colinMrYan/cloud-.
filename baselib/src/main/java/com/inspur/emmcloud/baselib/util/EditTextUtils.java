package com.inspur.emmcloud.baselib.util;

import android.text.Editable;
import android.text.Selection;
import android.widget.EditText;

public class EditTextUtils {
    public static void setText(EditText edit, String content) {
        try {
            content = StringUtils.isBlank(content) ? "" : content;
            edit.setText(content);
            Editable editable = edit.getText();
            int position = content.length();
            Selection.setSelection(editable, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
