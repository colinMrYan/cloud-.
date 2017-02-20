package com.inspur.emmcloud.bean; 

import com.lidroid.xutils.db.annotation.Id;

/**
 * classes : com.inspur.emmcloud.bean.FestivalDate
 * Create at 2017年1月6日 下午2:18:02
 */
public class FestivalDate {

	
//	private String id = "";
	private long festivalTime = 0;
	@Id
	private String festivalKey = "";
	
	public FestivalDate(){}
	public FestivalDate(String festivalKey,long festivalTime){
		this.festivalKey = festivalKey;
		this.festivalTime = festivalTime;
	}
	public String getFestivalKey() {
		return festivalKey;
	}
	public void setFestivalKey(String festivalKey) {
		this.festivalKey = festivalKey;
	}
	public long getFestivalTime() {
		return festivalTime;
	}
	public void setFestivalTime(long festivalTime) {
		this.festivalTime = festivalTime;
	}
//	public String getId() {
//		return id;
//	}
//	public void setId(String id) {
//		this.id = id;
//	}
	
}
 