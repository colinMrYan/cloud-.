package com.inspur.emmcloud.baselib.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class ListUtils {
    public static <T> List<T> deepCopyList(List<T> src) {
        List<T> dest = null;
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            dest = (List<T>) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dest;
    }

    /**
     * 注！！！！！！→ 集合中的元素必须重写equals方法自行判断元素是否相同
     * 哈希地址相同 返回true
     * 如果两个参数都为空，则返回true
     * 如果有一项为空，则返回false
     * 如果数据长度不相同，则返回false
     * 集合1包含集合2中的所有元素，并且集合2也包含集合1中的所有元素 则返回true
     * 注！！！！！！→ 集合中的元素必须重写equals方法自行判断元素是否相同
     *
     * @param l0
     * @param l1
     * @return
     */
    public static boolean isListEqual(List l0, List l1) {
        if (l0 == l1)
            return true;
        if (l0 == null && l1 == null)
            return true;
        if (l0 == null || l1 == null)
            return false;
        if (l0.size() != l1.size())
            return false;
        if (l0.containsAll(l1) && l1.containsAll(l0)) {
            return true;
        }
        return false;
    }

    public static boolean isListContentEqual(List l0, List l1) {
        if (l0 == l1)
            return true;
        if (l0 == null || l1 == null)
            return false;
        if (l0.size() != l1.size())
            return false;
        for(int i = 0 ; i < l0.size(); i++){
            if(l0.get(0) != l1.get(i)){
                return false;
            }
        }
        return true;
    }
}
