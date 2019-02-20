package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
	 * 
	 */

public class CalendarUtil {
	final private static long[] lunarInfo = new long[] { 0x04bd8, 0x04ae0,
			0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0,
			0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540,
			0x0d6a0, 0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5,
			0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
			0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3,
			0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0,
			0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0,
			0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8,
			0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570,
			0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5,
			0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0,
			0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50,
			0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0,
			0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
			0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7,
			0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50,
			0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954,
			0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260,
			0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0,
			0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0,
			0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20,
			0x0ada0 };
	final private static int[] year20 = new int[] { 1, 4, 1, 2, 1, 2, 1, 1, 2,
			1, 2, 1 };
	final private static int[] year19 = new int[] { 0, 3, 0, 1, 0, 1, 0, 0, 1,
			0, 1, 0 };
	final private static int[] year2000 = new int[] { 0, 3, 1, 2, 1, 2, 1, 1,
			2, 1, 2, 1 };
	public final static String[] nStr1 = new String[] { "", "正", "二", "三", "四",
			"五", "六", "七", "八", "九", "十", "冬", "腊" };
	private final static String[] Gan = new String[] { "甲", "乙", "丙", "丁", "戊",
			"己", "庚", "辛", "壬", "癸" };
	private final static String[] Zhi = new String[] { "子", "丑", "寅", "卯", "辰",
			"巳", "午", "未", "申", "酉", "戌", "亥" };
	private final static String[] Animals = new String[] { "鼠", "牛", "虎", "兔",
			"龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪" };

	private final static String[] solarTerm = new String[] { "小寒", "大寒", "立春",
			"雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑",
			"立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至" };
	private final static String[] sFtv = new String[] { "0101*元旦", "0214 情人节",
			"0308 妇女节", "0312 植树节", "0315 消费者权益日", "0401 愚人节", "0501 劳动节",
			"0504 青年节", "0512 护士节", "0601 儿童节", "0701 建党节", "0801 建军节",
			"0808 父亲节", "0909 mzd逝世纪念", "0910 教师节", "0928 孔子诞辰", "1001*国庆节",
			"1006 老人节", "1024 联合国日", "1112 孙中山诞辰", "1220 澳门回归", "1225 圣诞节",
			"1226 mzd诞辰" };
	private final static String[] lFtv = new String[] { "0101*农历春节",
			"0115 元宵节", "0505 端午节", "0707 七夕情人节", "0815 中秋节", "0909 重阳节",
			"1208 腊八节", "1224 小年", "0100*除夕" };
	
	private static ArrayList<ArrayList<String>> dayslist = new ArrayList<ArrayList<String>>();

	/**
	 * 传回农历 y年的总天数
	 * 
	 * @param y
	 * @return
	 */
	final private static int lYearDays(int y) {
		int i, sum = 348;
		for (i = 0x8000; i > 0x8; i >>= 1) {
			if ((lunarInfo[y - 1900] & i) != 0)
				sum += 1;
		}
		return (sum + leapDays(y));
	}

	/**
	 * 传回农历 y年闰月的天数
	 * 
	 * @param y
	 * @return
	 */
	final private static int leapDays(int y) {
		if (leapMonth(y) != 0) {
			if ((lunarInfo[y - 1900] & 0x10000) != 0)
				return 30;
			else
				return 29;
		} else
			return 0;
	}

	/**
	 * 传回农历 y年闰哪个月 1-12 , 没闰传回 0
	 * 
	 * @param y
	 * @return
	 */
	final private static int leapMonth(int y) {
		return (int) (lunarInfo[y - 1900] & 0xf);
	}

	/**
	 * 传回农历 y年m月的总天数
	 * 
	 * @param y
	 * @param m
	 * @return
	 */
	final private static int monthDays(int y, int m) {
		if ((lunarInfo[y - 1900] & (0x10000 >> m)) == 0)
			return 29;
		else
			return 30;
	}

	/**
	 * 传回农历 y年的生肖
	 * 
	 * @param y
	 * @return
	 */
	final public static String AnimalsYear(int y) {
		return Animals[(y - 4) % 12];
	}

	/**
	 * 传入 月日的offset 传回干支,0=甲子
	 * 
	 * @param num
	 * @return
	 */
	final private static String cyclicalm(int num) {
		return (Gan[num % 10] + Zhi[num % 12]);
	}

	/**
	 * 传入 offset 传回干支, 0=甲子
	 * 
	 * @param y
	 * @return
	 */
	final public static String cyclical(int y) {
		int num = y - 1900 + 36;
		return (cyclicalm(num));
	}

	/**
	 * 传出农历.year0 .month1 .day2 .yearCyl3 .monCyl4 .dayCyl5 .isLeap6
	 * 
	 * @param y
	 * @param m
	 * @return
	 */
	final private long[] Lunar(int y, int m) {
		long[] nongDate = new long[7];
		int i = 0, temp = 0, leap = 0;
		Date baseDate = new GregorianCalendar(1900 + 1900, 1, 31).getTime();
		Date objDate = new GregorianCalendar(y + 1900, m, 1).getTime();
		long offset = (objDate.getTime() - baseDate.getTime()) / 86400000L;
		if (y < 2000)
			offset += year19[m - 1];
		if (y > 2000)
			offset += year20[m - 1];
		if (y == 2000)
			offset += year2000[m - 1];
		nongDate[5] = offset + 40;
		nongDate[4] = 14;
		for (i = 1900; i < 2050 && offset > 0; i++) {
			temp = lYearDays(i);
			offset -= temp;
			nongDate[4] += 12;
		}
		if (offset < 0) {
			offset += temp;
			i--;
			nongDate[4] -= 12;
		}
		nongDate[0] = i;
		nongDate[3] = i - 1864;
		leap = leapMonth(i); // 闰哪个月
		nongDate[6] = 0;
		for (i = 1; i < 13 && offset > 0; i++) {
			// 闰月
			if (leap > 0 && i == (leap + 1) && nongDate[6] == 0) {
				--i;
				nongDate[6] = 1;
				temp = leapDays((int) nongDate[0]);
			} else {
				temp = monthDays((int) nongDate[0], i);
			}
			// 解除闰月
			if (nongDate[6] == 1 && i == (leap + 1))
				nongDate[6] = 0;
			offset -= temp;
			if (nongDate[6] == 0)
				nongDate[4]++;
		}
		if (offset == 0 && leap > 0 && i == leap + 1) {
			if (nongDate[6] == 1) {
				nongDate[6] = 0;
			} else {
				nongDate[6] = 1;
				--i;
				--nongDate[4];
			}
		}
		if (offset < 0) {
			offset += temp;
			--i;
			--nongDate[4];
		}
		nongDate[1] = i;
		nongDate[2] = offset + 1;
		return nongDate;
	}

	/**
	 * 传出y年m月d日对应的农历.year0 .month1 .day2 .yearCyl3 .monCyl4 .dayCyl5 .isLeap6
	 * 
	 * @param y
	 * @param m
	 * @param d
	 * @return
	 */
	final public static long[] calElement(int y, int m, int d) {
		long[] nongDate = new long[7];
		int i = 0, temp = 0, leap = 0;
		Date baseDate = new GregorianCalendar(0 + 1900, 0, 31).getTime();
		Date objDate = new GregorianCalendar(y, m - 1, d).getTime();
		long offset = (objDate.getTime() - baseDate.getTime()) / 86400000L;
		nongDate[5] = offset + 40;
		nongDate[4] = 14;
		for (i = 1900; i < 2050 && offset > 0; i++) {
			temp = lYearDays(i);
			offset -= temp;
			nongDate[4] += 12;
		}
		if (offset < 0) {
			offset += temp;
			i--;
			nongDate[4] -= 12;
		}
		nongDate[0] = i;
		nongDate[3] = i - 1864;
		leap = leapMonth(i); // 闰哪个月
		nongDate[6] = 0;
		for (i = 1; i < 13 && offset > 0; i++) {
			// 闰月
			if (leap > 0 && i == (leap + 1) && nongDate[6] == 0) {
				--i;
				nongDate[6] = 1;
				temp = leapDays((int) nongDate[0]);
			} else {
				temp = monthDays((int) nongDate[0], i);
			}
			// 解除闰月
			if (nongDate[6] == 1 && i == (leap + 1))
				nongDate[6] = 0;
			offset -= temp;
			if (nongDate[6] == 0)
				nongDate[4]++;
		}
		if (offset == 0 && leap > 0 && i == leap + 1) {
			if (nongDate[6] == 1) {
				nongDate[6] = 0;
			} else {
				nongDate[6] = 1;
				--i;
				--nongDate[4];
			}
		}
		if (offset < 0) {
			offset += temp;
			--i;
			--nongDate[4];
		}
		nongDate[1] = i;
		nongDate[2] = offset + 1;
		return nongDate;
	}

	public final static String getChinaDate(int day) {
		String a = "";
		if (day == 10)
			return "初十";
		if (day == 20)
			return "二十";
		if (day == 30)
			return "三十";
		int two = (day) / 10;
		if (two == 0)
			a = "初";
		if (two == 1)
			a = "十";
		if (two == 2)
			a = "廿";
		if (two == 3)
			a = "三";
		int one = day % 10;
		switch (one) {
		case 1:
			a += "一";
			break;
		case 2:
			a += "二";
			break;
		case 3:
			a += "三";
			break;
		case 4:
			a += "四";
			break;
		case 5:
			a += "五";
			break;
		case 6:
			a += "六";
			break;
		case 7:
			a += "七";
			break;
		case 8:
			a += "八";
			break;
		case 9:
			a += "九";
			break;
		}
		return a;
	}

	public static String today() {
		Calendar today = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE);
		int year = today.get(Calendar.YEAR);
		int month = today.get(Calendar.MONTH) + 1;
		int date = today.get(Calendar.DATE);
		long[] l = calElement(year, month, date);
		StringBuffer sToday = new StringBuffer();
		try {
			sToday.append(sdf.format(today.getTime()));
			sToday.append(" 农历");
			sToday.append(cyclical(year));
			sToday.append('(');
			sToday.append(AnimalsYear(year));
			sToday.append(")年");
			sToday.append(nStr1[(int) l[1]]);
			sToday.append("月");
			sToday.append(getChinaDate((int) (l[2])));
			return sToday.toString();
		} finally {
			sToday = null;
		}
	}

	public static String oneDay(int year, int month, int day) {
		Calendar today = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE);


		int y, m, d, h, mi, s;
		Calendar cal = Calendar.getInstance();
		y = cal.get(Calendar.YEAR);
		m = cal.get(Calendar.MONTH);
		m = m + 1;
		d = cal.get(Calendar.DATE);
		// h=cal.get(MyCalendar.HOUR_OF_DAY);
		// mi=cal.get(MyCalendar.MINUTE);
		// s=cal.get(MyCalendar.SECOND);
		// System.out.println("现在时刻是"+y+"年"+m+"月"+d+"日"+h+"时"+mi+"分"+s+"秒");
		// long[] l = calElement(year, month, day);

		long[] l = calElement(y, m, d);
		StringBuffer sToday = new StringBuffer();
		try {
			String weekday = getWeekDay(today);
			// sToday.append(sdf.format(today.getTime()));
			sToday.append(" 农历");
			// sToday.append(cyclical(year));
			// sToday.append('(');
			// sToday.append(AnimalsYear(year));
			// sToday.append(")年");
			sToday.append(nStr1[(int) l[1]]);
			sToday.append("月");
			sToday.append(getChinaDate((int) (l[2])));
			sToday.append("." + weekday);
			return sToday.toString();
		} finally {
			sToday = null;
		}
	}

	public static String getChineseToday() {
		Calendar today = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE);

		int y, m, d, h, mi, s;
		Calendar cal = Calendar.getInstance();
		y = cal.get(Calendar.YEAR);
		m = cal.get(Calendar.MONTH);
		m = m + 1;
		d = cal.get(Calendar.DATE);

		long[] l = calElement(y, m, d);
		StringBuffer sToday = new StringBuffer();
		try {
			String weekday = getWeekDay(today);
			sToday.append(" 农历");
			sToday.append(nStr1[(int) l[1]]);
			sToday.append("月");
			sToday.append(getChinaDate((int) (l[2])));
			sToday.append("  ");
//			sToday.append("·" + weekday);
			return sToday.toString();
		} finally {
			sToday = null;
		}
	}

	public static String getDate() {
		Calendar today = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE);

		int y, m, d, h, mi, s;
		Calendar cal = Calendar.getInstance();
		y = cal.get(Calendar.YEAR);
		m = cal.get(Calendar.MONTH);
		m = m + 1;
		d = cal.get(Calendar.DATE);

		long[] l = calElement(y, m, d);
		StringBuffer sToday = new StringBuffer();
		try {
			sToday.append(y);
			sToday.append("-");
			sToday.append(m);
			sToday.append("-");
			sToday.append(d);
			return sToday.toString();
		} finally {
			sToday = null;
		}
	}

	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy年M月d日 EEEEE");

	/**
	 * 农历日历工具使用演示
	 * 
	 * @param args
	 */
	// public static void main(String[] args) {
	// System.out.println(today());
	// System.out.println(oneDay(1989, 9, 10));
	// }

	public static String getWeekDay(Calendar c) {
		if (c == null) {
			return MyApplication.getInstance().getString(R.string.monday);
		}

		if (Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)) {
			return MyApplication.getInstance().getString(R.string.monday);
		}
		if (Calendar.TUESDAY == c.get(Calendar.DAY_OF_WEEK)) {
			return MyApplication.getInstance().getString(R.string.tuesday);
		}
		if (Calendar.WEDNESDAY == c.get(Calendar.DAY_OF_WEEK)) {
			return MyApplication.getInstance().getString(R.string.wednesday);
		}
		if (Calendar.THURSDAY == c.get(Calendar.DAY_OF_WEEK)) {
			return MyApplication.getInstance().getString(R.string.thursday);
		}
		if (Calendar.FRIDAY == c.get(Calendar.DAY_OF_WEEK)) {
			return MyApplication.getInstance().getString(R.string.friday);
		}
		if (Calendar.SATURDAY == c.get(Calendar.DAY_OF_WEEK)) {
			return MyApplication.getInstance().getString(R.string.saturday);
		}
		if (Calendar.SUNDAY == c.get(Calendar.DAY_OF_WEEK)) {
			return MyApplication.getInstance().getString(R.string.sunday);
		}

		return MyApplication.getInstance().getString(R.string.monday);
	}

//	public static String getDateOfToday() {
//		int y, m, d, h, mi, s;
//		Calendar cal = Calendar.getInstance();
//		y = cal.get(Calendar.YEAR);
//		m = cal.get(Calendar.MONTH);
//
//		d = cal.get(Calendar.DATE);
//
//		return (m + 1) + "月" + d + "日";
//	}
	
	

//	/**
//	 * 计算两个日期之间相差的天数
//	 * 
//	 * @param smdate
//	 *            较小的时间
//	 * @param bdate
//	 *            较大的时间
//	 * @return 相差天数
//	 * @throws ParseException
//	 */
//	public static int daysBetween(Date smdate, Date bdate)
//			throws ParseException {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		smdate = sdf.parse(sdf.format(smdate));
//		bdate = sdf.parse(sdf.format(bdate));
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(smdate);
//		long time1 = cal.getTimeInMillis();
//		cal.setTime(bdate);
//		long time2 = cal.getTimeInMillis();
//		long between_days = (time2 - time1) / (1000 * 3600 * 24);
//
//		return Integer.parseInt(String.valueOf(between_days));
//	}

//	public static int daysBetween(String smdate, String bdate)
//			throws ParseException {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(sdf.parse(smdate));
//		long time1 = cal.getTimeInMillis();
//		cal.setTime(sdf.parse(bdate));
//		long time2 = cal.getTimeInMillis();
//		long between_days = (time2 - time1) / (1000 * 3600 * 24);
//
//		return Integer.parseInt(String.valueOf(between_days));
//	}

//	public static int dayForWeek(String pTime) {
//
//		String[] date = pTime.split("-"); 
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//
//		Calendar cal = new GregorianCalendar();
//		try {
//			cal.setTime(format.parse(pTime));
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	
////		cal.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
//		
//		return cal.get(Calendar.DAY_OF_WEEK);
//
//	}

//	public static String parseDateFromtime(Long longtime) {
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String d = "";
//		d = format.format(longtime);
//		TimeZone timeZone = TimeZone.getDefault();
//		format.setTimeZone(timeZone);
//		Date date = null;
//		int offset = timeZone.getRawOffset();
//		try {
//			date = new Date(format.parse(d).getTime());
//			date = new Date(date.getTime() + offset);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return d;
//
//	}
	
//	public static Calendar parseDateFromLongtime(String longtime) {
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String d = "";
//		d = format.format(longtime);
//		TimeZone timeZone = TimeZone.getDefault();
//		format.setTimeZone(timeZone);
//		Date date = null;
//		int offset = timeZone.getRawOffset();
//		try {
//			date = new Date(format.parse(d).getTime());
//			date = new Date(date.getTime() + offset);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		long l = Long.parseLong(longtime);
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTimeInMillis(l);
//		
//		return calendar;
//
//	}

//	public static Long parseTimeFromDate(String datetime) {
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		
//		TimeZone timeZone = TimeZone.getDefault();
//		format.setTimeZone(timeZone);
//		String time = "1970-01-06 11:45:55";
//		Date date = null;
//		try {
//			date = format.parse(datetime);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
////		System.out.print("Format To times:" + date.getTime());
//		if(date == null){
//			return (long) 0;
//		}
//		return date.getTime();
//
//	}
	
//	public static String getTodayDate(){
//		
//		int y,m,d,h,mi,s;    
//		String todaydate;
//		Calendar cal=Calendar.getInstance();  
//		y=cal.get(Calendar.YEAR);    
//		m=cal.get(Calendar.MONTH);    
//		d=cal.get(Calendar.DATE);    
//		h=cal.get(Calendar.HOUR_OF_DAY);    
//		mi=cal.get(Calendar.MINUTE);    
//		s=cal.get(Calendar.SECOND);    
//		todaydate = y+"-"+(m+1)+"-"+d+"  "+h+":"+mi;
//		return todaydate;
//	}
//	public static String getTomorrowDate(int offset){
//		
//		int y,m,d,h,mi,s;    
//		String tomorrowdate;
//		Calendar cal=Calendar.getInstance(); 
//		cal.add(Calendar.DATE, offset+1);
//		y=cal.get(Calendar.YEAR);    
//		m=cal.get(Calendar.MONTH);    
//		d=cal.get(Calendar.DATE);    
//		h=cal.get(Calendar.HOUR_OF_DAY);    
//		mi=cal.get(Calendar.MINUTE);    
//		s=cal.get(Calendar.SECOND);    
//		tomorrowdate = y+"-"+(m+1)+"-"+d;
//		return tomorrowdate;
//	}
	

	
//	public static Long getLongTimeFromDate(String time){
//		return parseTimeFromDate(time);
//	}

	//划分时间的逻辑
//	public static ArrayList<ArrayList<String>> handleNineTime(GetRoomAvailableResult getRoomAvailableResult){
//		for (int i = 0; i < getRoomAvailableResult.getRoomAvailableDays().size(); i++) {
//			ArrayList<String> templist = new ArrayList<String>();
//			if(getRoomAvailableResult.getRoomAvailableDays().get(i) != null){
//				for (int j = 0; j < getRoomAvailableResult.getRoomAvailableDays().get(i).size(); j++) {
//
//					
//					String startWebSocket = getRoomAvailableResult
//							.getRoomAvailableDays().get(i).get(j)
//							.getStart();
//					long starttime = Long.parseLong(startWebSocket);
//					String end = getRoomAvailableResult
//							.getRoomAvailableDays().get(i).get(j).getEnd();
//					long endtime = Long.parseLong(end);
//					String startdate = CalendarUtil.parseDateFromtime(starttime).substring(0, 10);
//
//					
//					long eight = CalendarUtil.getLongTimeFromDate(startdate +" "+"00:00:00");
//					long twelve = CalendarUtil.getLongTimeFromDate(startdate +" "+"12:00:00");
//					long five = CalendarUtil.getLongTimeFromDate(startdate +" "+"23:59:59");
//					if(starttime<=eight){
//						if(endtime<eight){
//							LogUtils.debug("yfcLog", "超出显示范围，不作处理");
//						}else if ((endtime>=eight)&&(endtime<twelve)) {
//							
//							if ((endtime - eight) > (30 * 60 * 1000)) {
//								String tempendtime = CalendarUtil
//										.parseDateFromtime(endtime);
//								String temp = "00:00"
//										+ "-"
//										+ tempendtime.substring(
//												tempendtime.length() - 8,
//												tempendtime.length() - 3);
//								templist.add(temp);
//							
//							}
////							String bigtemp = keepBigTime(endtime, starttime);
////							templist.add(bigtemp);
//						}else if ((endtime>=twelve)&&(endtime<five)) {
//							templist.add("00:00-12:00");
//							if ((endtime - twelve) > (30 * 60 * 1000)) {
//								String tempendtime = CalendarUtil
//										.parseDateFromtime(endtime);
//								String temp = "12:00"
//										+ "-"
//										+ tempendtime.substring(
//												tempendtime.length() - 8,
//												tempendtime.length() - 3);
//								templist.add(temp);
//							}
//							String bigtemp = keepBigTime(endtime, starttime);
//							templist.add(bigtemp);
//						}else if (endtime>=five) {
//							templist.add("00:00-23:59");
//							templist.add("00:00-12:00");
//							templist.add("12:00-23:59");
//						}
//					}else if ((starttime>=eight)&&(starttime<twelve)) {
//						if((endtime>=eight)&&(endtime<twelve)){
//							if ((endtime - starttime) > (30 * 60 * 1000)) {
//								String tempendtime = CalendarUtil
//										.parseDateFromtime(endtime);
//								String tempstarttime = CalendarUtil
//										.parseDateFromtime(starttime);
//								String temp = tempstarttime.substring(
//										tempstarttime.length() - 8,
//										tempstarttime.length() - 3)
//										+ "-"
//										+ tempendtime.substring(
//												tempendtime.length() - 8,
//												tempendtime.length() - 3);
//								templist.add(temp);
//							}
//							//因为起止时间都在八点半到十二点之间不存在跨12点的情况，所以不需要保持大段时间的
////							String bigtemp = keepBigTime(endtime, starttime);
////							templist.add(bigtemp);
//						}else if ((endtime>=twelve)&&(endtime<five)) {
//							if ((twelve - starttime) > (30 * 60 * 1000)) {
//								String tempendtime = CalendarUtil
//										.parseDateFromtime(starttime);
//								String temp = tempendtime.substring(
//										tempendtime.length() - 8,
//										tempendtime.length() - 3)
//										+ "-" + "12:00";
//								templist.add(temp);
//							}
//							if ((endtime - twelve) > (30 * 60 * 1000)) {
//								String tempendtime = CalendarUtil
//										.parseDateFromtime(endtime);
//								String temp = "12:00"
//										+ "-"
//										+ tempendtime.substring(
//												tempendtime.length() - 8,
//												tempendtime.length() - 3);
//								templist.add(temp);
//							}
//							String bigtemp = keepBigTime(endtime, starttime);
//							templist.add(bigtemp);
//						}else if (endtime>=five) {
//							//&&(System.currentTimeMillis()<starttime)   保留
//							if ((twelve - starttime) > (30 * 60 * 1000)) {
//								String tempendtime = CalendarUtil
//										.parseDateFromtime(starttime);
//
//								String temp = tempendtime.substring(
//										tempendtime.length() - 8,
//										tempendtime.length() - 3)
//										+ "-" + "12:00";
//								templist.add(temp);
//							}
//								templist.add("12:00-23:59");
//								String bigtemp = keepBigTime(endtime, starttime);
//								templist.add(bigtemp);
//						}
//					}else if ((starttime>=twelve)&&(starttime<five)) {
//						if((endtime>=twelve)&&(endtime<five)){
//							if ((endtime - starttime) > (30 * 60 * 1000)) {
//								String tempendtime = CalendarUtil
//										.parseDateFromtime(endtime);
//								String tempstarttime = CalendarUtil
//										.parseDateFromtime(starttime);
//								String temp = tempstarttime.substring(
//										tempstarttime.length() - 8,
//										tempstarttime.length() - 3)
//										+ "-"
//										+ tempendtime.substring(
//												tempendtime.length() - 8,
//												tempendtime.length() - 3);
//								templist.add(temp);
//							}
//						}else if (endtime>=five) {
//							//&& System.currentTimeMillis()<starttime
//							if ((five - starttime) > (30 * 60 * 1000)) {
//								String bigtemp = keepBigTime(endtime, starttime);
//								templist.add(bigtemp);
//							}
//						}
//					}else if(starttime>=five){
//					}
//
//				}
//				
//			}else if (getRoomAvailableResult.getRoomAvailableDays().get(i) == null) {
//				LogUtils.debug("yfcLog", "数据为空");
//			}
//			dayslist.add(templist);
//			
//		}
//		return dayslist;
//	}
//	
//	public static String keepBigTime(long endtime, long starttime) {
//		String temp = "";
//		if ((endtime - starttime) > (30 * 60 * 1000)) {
////			String tempstarttime = null;
//			
//			String tempstarttime = CalendarUtil.parseDateFromtime(starttime);
//			String tempendtime = CalendarUtil.parseDateFromtime(endtime);
//
//			String startdate = CalendarUtil.parseDateFromtime(starttime).substring(0, 10);
//			long eight = CalendarUtil.getLongTimeFromDate(startdate +" "+"00:00:00");
//			long five = CalendarUtil.getLongTimeFromDate(startdate +" "+"23:59:59");
//			if (endtime >= five) {
//				tempendtime = CalendarUtil.parseDateFromtime(five);
//			}
//			if (starttime <= eight) {
//				tempstarttime = CalendarUtil.parseDateFromtime(eight);
//			}
//			temp = tempstarttime.substring(tempstarttime.length() - 8,
//					tempstarttime.length() - 3)
//					+ "-"
//					+ tempendtime.substring(tempendtime.length() - 8,
//							tempendtime.length() - 3);
////				todaytimelist.add(temp);
//
//			
//		}
//		return temp;
//	}
}
