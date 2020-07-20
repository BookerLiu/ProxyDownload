package com.demoliu.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @Author Demo-Liu
 * @Date 2019/4/3 10:04
 * @description 配置文件工具类
 */
public class PropertiesUtil {

    private Properties properties;

    public PropertiesUtil(String filePath){
        InputStream inputStream = null;
        properties = new Properties();
        try {
            inputStream = new FileInputStream(new File(filePath));
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(inputStream!=null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据属性名 获取指定配置
     * @param property
     * @return
     */
    public String getProperty(String property){
        return properties.getProperty(property);
    }


    /**
     * 获取所有配置
     * @return
     */
    public Set<Map.Entry<Object, Object>> getPropertys(){
        return properties.entrySet();
    }


    /**
     * 持久化至文件
     * @param properties
     * @param filePath
     */
    public static void outToFile(Properties properties, String filePath){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            properties.store(fos,"update time: "+ sdf.format(new Date()));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
