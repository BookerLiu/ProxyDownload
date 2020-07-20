package com.demoliu.util;

import java.io.File;
import java.io.IOException;

/**
 * @author LiuFei
 * @create 2020-07-17 17:10
 * @description 文件工具类
 */
public class FileUtil {




    /**
     * 判断文件是否存在 不存在就新建
     * @param filePath 文件路径
     * @throws IOException
     */
    public static void fileIsExistsAndCreat(String filePath) throws IOException {
        File file = new File(filePath);
        if(!file.exists()){
            dirIsExistsAndCreat(filePath.substring(0,filePath.lastIndexOf("/")));
            file.createNewFile();
        }
    }

    /**
     * 判断文件夹是否存在 不存在就新建
     * @param dirPath
     */
    public static void dirIsExistsAndCreat(String dirPath){
        File dir = new File(dirPath);
        if(!dir.exists()) dir.mkdirs();
    }
}
