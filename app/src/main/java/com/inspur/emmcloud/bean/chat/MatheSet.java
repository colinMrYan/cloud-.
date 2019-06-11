package com.inspur.emmcloud.bean.chat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


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

    public MatheSet(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    public MatheSet(Long start) {
        this.start = start;
        this.end = start;
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
        return one.isInMatheSet(other.start) || one.isInMatheSet(other.end);
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

    public void setStart(Long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
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
     * 判断目标是否在集合中
     *
     * @param target
     * @return
     */
    public boolean isInMatheSet(long target) {
        return (target >= start && target <= end);
    }

    public boolean isInMatheSet(String target) {
        long num = Long.valueOf(target);
        return isInMatheSet(num);
    }
}
