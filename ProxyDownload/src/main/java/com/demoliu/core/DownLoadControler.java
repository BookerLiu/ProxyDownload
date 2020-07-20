package com.demoliu.core;

import com.demoliu.util.DialogBuilder;
import com.demoliu.util.HttpUtil;
import com.demoliu.util.StringUtil;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Font;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Demo-Liu
 * @create 2020-07-08 10:50
 * @description 下载控制器
 */
public class DownLoadControler {

    //所有线程总进度 已下载文件大小
    volatile static long PROGRESS = 0;


    //最大线程数
    private final static Integer MAX_THREAD_NUM = 18;
    private final static String HOST = "host";
    private final static String PORT = "port";
    private static ThreadPoolExecutor pool = null;
    private static int index = 1;
    private final static String BAIDU_URI = "https://www.baidu.com";

    public static void start(Map<String, String> proxyMap, String url, String filePath, boolean useProxy, ProgressBar progress, JFXButton start, Label log) {
        try {
            long begin = System.currentTimeMillis();

            Map<String, String> proxyMapBak = new HashMap<>(proxyMap);


            HttpClient httpClient = HttpClients.createDefault();


            //获取文件大小和文件名
            HttpHead httpHead = new HttpHead(url);
            HttpUtil.setHeader(httpHead);
            HttpResponse response = httpClient.execute(httpHead);
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != 200){
                Platform.runLater(() -> {
                    new DialogBuilder(progress).setTitle("提示").setMessage("文件不存在(" + statusCode + ")").setNegativeBtn("确定").create();
                    start.setText("Start");
                    start.setDisable(false);
                });
                return;
            }
            //创建线程池
            if(pool == null){
                pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(MAX_THREAD_NUM);
            }
            long length = getFileLength(response);
            if(length == 0){
                Platform.runLater(() -> {
                    new DialogBuilder(progress).setTitle("提示").setMessage("文件不存在,检查url是否正确").setNegativeBtn("确定").create();
                    start.setText("Start");
                    start.setDisable(false);
                });
                return;
            }
            String fileName = getFileName(url, response);
            File file = new File(filePath + File.separator + fileName);


            CountDownLatch count = new CountDownLatch(MAX_THREAD_NUM);
            if (useProxy) {
                //代理连通性检测
                Platform.runLater(() -> log.setText("检测代理联通性..."));
                String msg = checkProxy(httpClient, proxyMapBak);
                int size = proxyMapBak.size() / 2;
                if(size == 0 && !StringUtil.isEmpty(msg)){
                    Platform.runLater(() -> new DialogBuilder(progress).setTitle("提示").setMessage("所配代理不可用,将不使用代理开始下载").setNegativeBtn("确定").create());
                    createDownThread(httpClient, url, file, length,count,log);
                }else if(size == 0 && StringUtil.isEmpty(msg)){
                    Platform.runLater(() -> new DialogBuilder(progress).setTitle("提示").setMessage("没有配置代理,将不使用代理开始下载").setNegativeBtn("确定").create());
                    createDownThread(httpClient, url, file, length,count,log);
                }else{
                    if(!StringUtil.isEmpty(msg)){
                        Platform.runLater(() -> new DialogBuilder(progress).setTitle("提示").setMessage("部分代理不可用:"+msg).setNegativeBtn("确定").create());
                    }
                    Set<String> keys = proxyMapBak.keySet();
                    Iterator<String> iterator = keys.iterator();
                    //使用代理  计算每个代理需要分配文件大小
                    long subLen = length % size == 0L ? length / size : (length / size + length % size);
                    //计算每个代理分配的线程
                    int[] nums = getProxyThreadNum(size);

                    long position = 0;
                    long end = subLen;
                    String hostKey;
                    int threadSize;
                    for (int i = 0; i < size; i++, position = end + 1, end = (i + 1) * subLen) {
                        hostKey = getNextHostKey(iterator);
                        if (end > length) end = length;
                        threadSize = i + 1 == size ? nums[1] : nums[0];
                        createDownThread(httpClient, url, file, position, end, threadSize, proxyMapBak.get(hostKey), Integer.parseInt(proxyMapBak.get(hostKey.replace(HOST, PORT))),count,log);
                    }
                }
            } else {
                //不使用代理 默认使用线程数
                createDownThread(httpClient, url, file, length,count,log);
            }

            long allLength = 0L; //下载块总量
            int noUpdate = 1;//判断下载块未更新次数
            long thisTime;
            Platform.runLater(() ->{
                start.setText("");
                start.setFont(Font.font(20));
            });
            while (PROGRESS < length && count.getCount()>0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(allLength != PROGRESS){
                    //计算实时网速
                    thisTime = PROGRESS - allLength;
                    long finalThisTime = thisTime;
                    int finalNoUpdate = noUpdate;
                    Platform.runLater(() -> {
                        start.setText(String.format("%.2f",(double) finalThisTime  / 1024 / (double) finalNoUpdate)+"kb/s");
                        progress.setProgress((double)PROGRESS/(double)length);
                    });
                    allLength = PROGRESS;
                }else{
                    noUpdate ++;
                }
            }
            Platform.runLater(() -> start.setText("合并下载块..."));
            count.await();
            Platform.runLater(() -> start.setText(""));
            start.setFont(Font.font(25));
            PROGRESS = 0;
            index = 1;
            long end = System.currentTimeMillis();
            //计算总用时
            long allSTime = (end - begin)/1000;
            String  allMSTime = allSTime/60+"分 "+(allSTime%60)+"秒";
            //计算平均网速
            String avgIS = String.format("%.2f", (double) length / 1024 / (double) allSTime) + "kb/s";

            Platform.runLater(() -> log.setText(""));
            Platform.runLater(() -> {
                new DialogBuilder(progress).setTitle("提示").setMessage("下载完成,总用时:"+allMSTime+",平均网速:"+avgIS).setNegativeBtn("确定").create();
                start.setText("Start");
                start.setDisable(false);
            });
        } catch (Exception e){
            e.printStackTrace();
            Platform.runLater(() -> {
                new DialogBuilder(progress).setTitle("提示").setMessage("下载过程中发生错误,这可能是由于连接被远程主机强行关闭或无法连接到此站点导致").setNegativeBtn("确定").create();
                start.setText("Start");
                start.setDisable(false);
            });
        }

    }

    /**
     * 获取文件名
     * @param url 文件下载地址
     * @param response
     * @return
     */
    private static String getFileName(String url, HttpResponse response) throws UnsupportedEncodingException {
        String fileName = null;
        Header firstHeader = response.getFirstHeader("Content-Disposition");
        if(firstHeader != null){
            HeaderElement[] elements = firstHeader.getElements();
            if(elements.length == 1){
                NameValuePair param = elements[0].getParameterByName("filename");
                if(param != null){
                    fileName = param.getValue();
                    fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
                }
            }
        }
        if(fileName == null){
            //url 中获取
            fileName = url.substring(url.lastIndexOf("/")+1);
            if(fileName.contains("?")){
                fileName = fileName.substring(0,fileName.indexOf("?"));
            }
        }
        return fileName;
    }

    /**
     * 获取文件大小
     * @param response
     * @return
     */
    private static Long getFileLength(HttpResponse response){
        Header[] headers = response.getHeaders("Content-Length");
        return Long.parseLong(headers[0].getValue());
    }

    /**
     * 计算每个代理和最后一个代理分配的线程数
     * @param size 代理数量
     * @return
     */
    private static int[] getProxyThreadNum(int size){
        int[] nums = new int[2];
        int i = MAX_THREAD_NUM % size;
        if(i == 0){
            nums[0] = MAX_THREAD_NUM / size;
            nums[1] = nums[0];
        }else{
            nums[0] = MAX_THREAD_NUM / size + 1;
            nums[1] = MAX_THREAD_NUM- (size-1)*nums[0];
        }
        return nums;
    }

    /**
     * 获取下一个 Host key
     * @param iterator
     * @return
     */
    private static String getNextHostKey(Iterator<String> iterator){
        String next = iterator.next();
        if(next.contains(HOST)){
            return next;
        }else{
            return getNextHostKey(iterator);
        }
    }

    /**
     * 创建启动下载线程, 根据 host 和 port是否为null来判断是否启用代理
     * @param url 下载地址
     * @param file 下载文件路径
     * @param position 线程分配文件起点
     * @param lastPosition 线程分配文件终点
     * @param size 每个代理分配线程数
     * @param host 代理ip
     * @param port 代理端口
     */
    private static void createDownThread(HttpClient httpClient, String url, File file, Long position, Long lastPosition, int size, String host, Integer port, CountDownLatch count, Label log){
        Platform.runLater(() -> log.setText("下载开始..."));
        Long length = lastPosition - position;
        //创建代理
        HttpHost httpHost = null;
        if(host!=null && port!=null){
            httpHost = new HttpHost(host, port);
        }
        //计算每个线程需要分配的大小
        long subLen = length%size == 0L ? length/size : (length/size + length%size);
        //第一次终点为 起点加平分长度
        long end = subLen+position;
        //启动线程
        for(int i=0;i<size;i++,position=end+1,end += subLen){
            if(end > lastPosition) end = lastPosition;
            pool.execute(new DownThread(httpClient, httpHost, url,file, position, end, count,log,index));
            index ++;
        }
    }

    private static void createDownThread(HttpClient httpClient, String url, File file, Long length, CountDownLatch count, Label log){
        createDownThread(httpClient, url, file, 0L, length, MAX_THREAD_NUM, null, null,count,log);
    }

    /**
     * 检测代理的连通性
     * @param httpClient
     * @param proxyMap
     */
    private static String checkProxy(HttpClient httpClient, Map<String,String> proxyMap){
        HttpGet httpGet = new HttpGet(BAIDU_URI);
        Set<String> keys = proxyMap.keySet();
        HttpHost httpHost;
        int port;
        HttpResponse response;
        StringBuilder sb = new StringBuilder();
        //记录要删除的key
        List<String> list = new ArrayList<>();
        for (String key : keys) {
            if(key.contains(HOST)){
                port = Integer.parseInt(proxyMap.get(key.replace(HOST,PORT)));
                httpHost = new HttpHost(proxyMap.get(key),port);
                httpGet.setConfig(
                        RequestConfig
                        .custom()
                        .setProxy(httpHost)
                        .setConnectTimeout(2000)
                        .setSocketTimeout(2000)
                        .setConnectionRequestTimeout(2000)
                        .build());
                try{
                    response = httpClient.execute(httpGet);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if(statusCode != 200){
                        list.add(key);
                        list.add(key.replace(HOST,PORT));
                        sb.append(proxyMap.get(key))
                                .append(":")
                                .append(port)
                                .append(", ");
                    }
                } catch (Exception e) {
                    list.add(key);
                    list.add(key.replace(HOST,PORT));
                    sb.append(proxyMap.get(key))
                            .append(":")
                            .append(port)
                            .append(", ");
                }
            }
        }
        for (String key : list) {
            proxyMap.remove(key);
        }
        return sb.toString();
    }

}
