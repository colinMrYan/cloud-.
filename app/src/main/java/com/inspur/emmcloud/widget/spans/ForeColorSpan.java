package com.inspur.emmcloud.widget.spans;

import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

public class ForeColorSpan extends ForegroundColorSpan {
	protected String mKeyWords;

	private int indexnum = -1;
	/**
	 * 
	 * @param color 指定颜色
	 * @param keyWords  指定key值
	 * @param num 指定编号
	 */
	public ForeColorSpan(int color, String keyWords,int num) {
		super(color);
		mKeyWords = keyWords;
		indexnum = num;
	}

	/**
	 * 
	 * @return
	 */
	protected String keyWords() {
		return mKeyWords;
	}

	/**
	 * keyword
	 * @return
	 */
	protected String toCode() {
		return mKeyWords;
	}

	public int getIndexnum() {
		return indexnum;
	}

	public void setIndexnum(int indexnum) {
		this.indexnum = indexnum;
	}
	
}
