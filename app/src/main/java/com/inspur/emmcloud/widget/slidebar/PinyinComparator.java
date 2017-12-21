package com.inspur.emmcloud.widget.slidebar;


import com.inspur.emmcloud.bean.PersonDto;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;


public class PinyinComparator implements Comparator<PersonDto> {

	@Override
	public int compare(PersonDto o1, PersonDto o2) {
		if (o1.getSortLetters().equals("☆")) {
			return -1;
		} else if (o2.getSortLetters().equals("☆")) {
			return 1;
		} else if (o1.getSortLetters().equals("#")) {
			return -1;
		} else if (o2.getSortLetters().equals("#")) {
			return 1;
		} else {
			return Collator.getInstance(Locale.CHINA).compare(o1.getName(), o2.getName());
		}
	}

}
