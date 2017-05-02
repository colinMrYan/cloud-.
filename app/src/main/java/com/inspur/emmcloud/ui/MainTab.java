package com.inspur.emmcloud.ui;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.app.MyAppFragment;
import com.inspur.emmcloud.ui.chat.MessageFragment;
import com.inspur.emmcloud.ui.find.FindFragment;
import com.inspur.emmcloud.ui.mine.MoreFragment;
import com.inspur.emmcloud.ui.notsupport.NotSupportFragment;
import com.inspur.emmcloud.ui.work.WorkFragment;

/**
 * Tab页ui
 * @author Administrator
 *
 */
public enum MainTab {

	NEWS(0, R.string.communicate, R.drawable.selector_tab_message_btn,
			MessageFragment.class),

	WORK(1, R.string.work, R.drawable.selector_tab_work_btn,
			WorkFragment.class),

	FIND(2, R.string.find, R.drawable.selector_tab_find_btn,
			FindFragment.class),

	APPLICATION(3, R.string.application, R.drawable.selector_tab_app_btn,
			MyAppFragment.class),

	MINE(4, R.string.mine, R.drawable.selector_tab_more_btn,
			MoreFragment.class),

	NOTSUPPORT(5, R.string.unknown, R.drawable.selector_tab_unknown_btn,
		 NotSupportFragment.class);

	private int idx;
	private int resName;
	private int resIcon;
	private String  configureName = "";
	private String  configureIcon = "";
	private Class<?> clz;

	private MainTab(int idx, int resName, int resIcon, Class<?> clz) {
		this.idx = idx;
		this.resName = resName;
		this.resIcon = resIcon;
		this.clz = clz;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public int getResName() {
		return resName;
	}

	public void setResName(int resName) {
		this.resName = resName;
	}

	public int getResIcon() {
		return resIcon;
	}

	public void setResIcon(int resIcon) {
		this.resIcon = resIcon;
	}

	public Class<?> getClz() {
		return clz;
	}

	public void setClz(Class<?> clz) {
		this.clz = clz;
	}

	public String getConfigureName() {
		return configureName;
	}

	public void setConfigureName(String configureName) {
		this.configureName = configureName;
	}

	public String getConfigureIcon() {
		return configureIcon;
	}

	public void setConfigureIcon(String configureIcon) {
		this.configureIcon = configureIcon;
	}
}
