package com.inspur.emmcloud.util; 

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;

import com.inspur.emmcloud.bean.FestivalDate;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

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
		cal.set(2017, 0, 28,0,0,0);
		FestivalDate springFestival = new FestivalDate("work_spring_festival", cal.getTimeInMillis());
		festivalDateList.add(springFestival);
		cal.set(2017, 3, 4, 0, 0, 0);
		FestivalDate qingMingFestival = new FestivalDate("work_qingming_festival", cal.getTimeInMillis());
		festivalDateList.add(qingMingFestival);
		cal.set(2017, 4, 1, 0, 0, 0);
		FestivalDate labourFestival = new FestivalDate("work_international_labour_day", cal.getTimeInMillis());
		festivalDateList.add(labourFestival);
		cal.set(2017, 4, 30, 0, 0, 0);
		FestivalDate dragonBoatFestival = new FestivalDate("work_dragon_boat_festival", cal.getTimeInMillis());
		festivalDateList.add(dragonBoatFestival);
		cal.set(2017, 9, 1, 0, 0, 0);
		FestivalDate nationalFestival = new FestivalDate("work_national_day", cal.getTimeInMillis());
		festivalDateList.add(nationalFestival);
		cal.set(2017, 9, 4, 0, 0, 0);
		FestivalDate moonFestival = new FestivalDate("work_moon_festival", cal.getTimeInMillis());
		festivalDateList.add(moonFestival);
		try {
			DbCacheUtils.getDb(context).saveOrUpdateAll(festivalDateList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 存储节日列表
	 * @param context
	 * @param festivalDateList
	 */
	public void saveFestivalList(Context context,List<FestivalDate> festivalDateList){
		try {
			DbCacheUtils.getDb(context).saveOrUpdateAll(festivalDateList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 通过时间获取节日
	 * @param context
	 * @param nowTime
	 * @return
	 */
	public static FestivalDate getFestival(Context context){
		FestivalDate festivalDate = null;
		try {
			Calendar cal = Calendar.getInstance();
			festivalDate = DbCacheUtils.getDb(context).findFirst(Selector.from(FestivalDate.class).where("festivalTime", ">", cal.getTimeInMillis()).orderBy("festivalTime", false));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(festivalDate == null){
			festivalDate = new FestivalDate();
		}
		return festivalDate;
	}
	  
}
 