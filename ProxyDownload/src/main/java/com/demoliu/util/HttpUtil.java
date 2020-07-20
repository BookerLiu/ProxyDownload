package com.demoliu.util;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Demo-Liu
 * @create 2020-07-08 14:16
 * @description
 */
public class HttpUtil {


    /**
     * 设置请求头
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T setHeader(T t){
        Map<String, String> map = getHeaders();
        Set<String> headers = map.keySet();

        Class<?> c = t.getClass();

        if(c == HttpGet.class){
            for (String key : headers) {
                ((HttpGet) t).addHeader(key,map.get(key));
            }
        }else if(c == HttpPost.class){
            for (String key : headers) {
                ((HttpPost) t).addHeader(key,map.get(key));
            }
        }else if(c == HttpHead.class){
            for (String key : headers) {
                ((HttpHead) t).addHeader(key,map.get(key));
            }
        }
        return t;
    }

    /**
     * 默认请求头
     * @return
     */
    private static Map<String,String> getHeaders(){
        Map<String,String> map = new HashMap<>();
        map.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        map.put("Accept-Encoding","gzip, deflate, br");
        map.put("Accept-Language","zh-CN,zh;q=0.9");
        map.put("Connection","keep-alive");
//        map.put("Host","www.baidu.com");
        map.put("Sec-Fetch-Dest","document");
        map.put("Sec-Fetch-Mode","navigate");
        map.put("Sec-Fetch-Site","none");
        map.put("Sec-Fetch-User","?1");
        map.put("Upgrade-Insecure-Requests","1");
        map.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        return map;
    }
}
