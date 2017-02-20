package com.inspur.emmcloud.bean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.lidroid.xutils.db.annotation.Id;

/**
 * 数学集合的实现类
 */
public class MatheSet {
	long start = 0L; // 集合的边界
	long end = 0L; // 集合的边界

	public MatheSet() {

	}

	public MatheSet(String start, String end) {
		this.start = Long.valueOf(start);
		this.end = Long.valueOf(end);
	}

	@Override
	public String toString() {
		return "{" + start + ":" + end + "}";
	}

	public void setStart(String start) {
		this.start = Long.valueOf(start);
	}

	public void setEnd(String end) {
		this.end = Long.valueOf(end);
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	/**
	 * 集合合并
	 *
	 * @param other
	 */
	public void merge(MatheSet other) {
		List<Long> l = Arrays.asList(start, end, other.start, other.end);
		start = Collections.min(l);
		end = Collections.max(l);
		other.start = 0;
		other.end = 0;

	}

	/**
	 * 判断两个集合是否有交集
	 *
	 * @param one
	 * @param other
	 * @return
	 */
	public static boolean isIntersection(MatheSet one, MatheSet other) {
		if (one == null || other == null) {
			return false;
		}
		return one.inMatheSet(other.start) || one.inMatheSet(other.end);
	}

	/**
	 * 判断目标是否在集合中
	 *
	 * @param target
	 * @return
	 */
	public boolean inMatheSet(long target) {
		return (target >= start && target <= end);
	}

	public boolean isInMatheSet(String target) {
		long num = Long.valueOf(target);
		return inMatheSet(num);
	}

}
