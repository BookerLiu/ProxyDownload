package com.demoliu.core;


import com.demoliu.util.HttpUtil;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

/**
 * @author Demo-Liu
 * @create 2020-07-08 17:20
 * @description 下载线程
 */
public class DownThread extends Thread{

    private HttpClient httpClient;
    private HttpHost httpHost;
    private String url;
    private File file;
    private Long position;
    private Long lastPosition;
    private CountDownLatch count;
    private Label log;
    private int index;

    public DownThread(HttpClient httpClient, HttpHost httpHost, String url, File file, Long position, Long lastPosition, CountDownLatch count, Label log, int index) {
        this.httpClient = httpClient;
        this.httpHost = httpHost;
        this.url = url;
        this.file = file;
        this.position = position;
        this.lastPosition = lastPosition;
        this.count = count;
        this.log = log;
        this.index = index;
    }

    @Override
    public void run() {
            HttpGet httpGet = new HttpGet(url);
            RequestConfig.Builder builder = RequestConfig
                    .custom()
                    .setConnectTimeout(10000)
                    .setSocketTimeout(10000)
                    .setSocketTimeout(10000);
            if(httpHost != null){
                builder.setProxy(httpHost);
            }
            httpGet.setConfig(builder.build());
            HttpUtil.setHeader(httpGet);

            String range = "bytes="+position+"-"+lastPosition;
            httpGet.setHeader("Range", range);
            //线程开始执行下载
            downStart(httpGet);
    }

    /**
     * 开始下载
     * @param httpGet
     */
    private void downStart(HttpGet httpGet) {
        InputStream inputStream = null;
        RandomAccessFile outputStream = null;
        try {
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            while (statusCode != 200 && statusCode != 206) {
                System.out.println("线程" + index + "失败(" + statusCode + "),重新发送get请求...");
                final int finalStatusCode = statusCode;
                Platform.runLater(() -> log.setText("线程" + index + "失败(" + finalStatusCode + "),重新发送get请求..."));
                httpClient.execute(httpGet);
                statusCode = response.getStatusLine().getStatusCode();
            }

            inputStream = response.getEntity().getContent();
            outputStream = new RandomAccessFile(file, "rw");
            outputStream.seek(position);


            int count = 0;
            byte[] buffer = new byte[1024];
            while ((count = inputStream.read(buffer, 0, buffer.length)) > 0) {
                outputStream.write(buffer, 0, count);
                DownLoadControler.PROGRESS += count;
            }

        } catch (Exception e) {
            Platform.runLater(() -> log.setText("线程"+index+"执行出错,重新发送get请求..."));
            downStart(httpGet);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //线程执行完毕 计数器-1
        count.countDown();
    }

}
