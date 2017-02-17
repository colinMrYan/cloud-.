package com.inspur.emmcloud.util;

import android.text.Editable;
import android.text.Selection;
import android.widget.EditText;

public class EditTextUtils {
	public static void setText(EditText edit,String content) {
		edit.setText(content);
		Editable editable = edit.getText();
		int position = content.length();
		Selection.setSelection(editable, position);
	}
}
