package com.demoliu.util;

/**
 * @author Demo-Liu
 * @create 2020-07-07 9:03
 * @description String 工具类
 */
public class StringUtil {



    /**
     * 判断 strs 数组是否为null
     * @param strs
     * @return
     */
    public static boolean isEmpty(String...strs){
        if(strs==null || strs.length==0) return true;
        for (String str : strs) {
            if(str==null || str.trim().length()==0 || str.trim().equals("null")) return true;
        }
        return false;
    }
}
