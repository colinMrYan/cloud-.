package com.inspur.emmcloud.util; 

import android.content.Context;

import com.inspur.emmcloud.bean.FestivalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * classes : com.inspur.emmcloud.util.FestivalUtils
 * Create at 2017年1月6日 下午2:40:18
 */
public class FestivalCacheUtils {

	/**
	 * 根据festivalKey获取提示
	 * @param context
	 * @param festivalKey
	 * @return
	 */
	public static String getFestivalTips(Context context,String festivalKey){
		if(StringUtils.isBlank(festivalKey)){
			return "";
		}
		//stringID：string.xml内配置的ID  
		//errorKey: string.xml内配置的名字 
		int stringID = context.getResources().getIdentifier(festivalKey,"string", "com.inspur.emmcloud");  
		//string.xml内配置的具体内容  
		//festivalDate:日期提示
		String festivalDate = context.getResources().getString(stringID);
		if(StringUtils.isBlank(festivalDate)){
			festivalDate = "";
		}
		return festivalDate;
	}
	
	/**
	 * 初始化节日
	 * @param context
	 */
	public static void saveFestivalList(Context context){
		List<FestivalDate> festivalDateList = new ArrayList<FestivalDate>();
		
		Calendar cal = Calendar.getInstance();
		cal.set(2018, 0, 1,0,0,0);
		FestivalDate newYearDay = new FestivalDate("work_new_year_day", cal.getTimeInMillis());
		festivalDateList.add(newYearDay);
		cal.set(2018, 1, 15,0,0,0);
		FestivalDate springFestival = new FestivalDate("work_spring_festival", cal.getTimeInMillis());
		festivalDateList.add(springFestival);
		cal.set(2018, 3, 5, 0, 0, 0);
		FestivalDate qingMingFestival = new FestivalDate("work_qingming_festival", cal.getTimeInMillis());
		festivalDateList.add(qingMingFestival);
		cal.set(2018, 3, 29, 0, 0, 0);
		FestivalDate labourFestival = new FestivalDate("work_international_labour_day", cal.getTimeInMillis());
		festivalDateList.add(labourFestival);
		cal.set(2018, 5, 16, 0, 0, 0);
		FestivalDate dragonBoatFestival = new FestivalDate("work_dragon_boat_festival", cal.getTimeInMillis());
		festivalDateList.add(dragonBoatFestival);
		cal.set(2018, 9, 1, 0, 0, 0);
		FestivalDate nationalFestival = new FestivalDate("work_national_day", cal.getTimeInMillis());
		festivalDateList.add(nationalFestival);
		cal.set(2018, 8, 22, 0, 0, 0);
		FestivalDate moonFestival = new FestivalDate("work_moon_festival", cal.getTimeInMillis());
		festivalDateList.add(moonFestival);
		try {
			DbCacheUtils.getDb(context).saveOrUpdate(festivalDateList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * 通过时间获取节日
	 * @param context
	 * @return
	 */
	public static FestivalDate getFestival(Context context){
		FestivalDate festivalDate = null;
		try {
			Calendar cal = Calendar.getInstance();
			festivalDate = DbCacheUtils.getDb(context).selector(FestivalDate.class).where("festivalTime", ">", cal.getTimeInMillis()).orderBy("festivalTime", false).findFirst();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(festivalDate == null){
			festivalDate = new FestivalDate();
		}
		return festivalDate;
	}

	/**
	 * 判断节日表时间是否需要更新
	 * @param context
	 * @return
	 */
	public static boolean isNeedUpdateFestivalTable(Context context){
		FestivalDate festivalDate = null;
		try {
			festivalDate = DbCacheUtils.getDb(context).selector(FestivalDate.class).where("festivalKey", "=", "work_spring_festival").findFirst();
			if(festivalDate != null && festivalDate.getFestivalTime() < 1485619200000l){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	  
}
 