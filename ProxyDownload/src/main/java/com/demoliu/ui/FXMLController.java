package com.demoliu.ui;

import com.demoliu.common.Constants;
import com.demoliu.core.DownLoadControler;
import com.demoliu.util.DialogBuilder;
import com.demoliu.util.PropertiesUtil;
import com.demoliu.util.StringUtil;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * @author Demo-Liu
 * @create 2020-07-06 15:20
 * @description
 */
public class FXMLController {



    @FXML
    private Label path;
    @FXML
    private JFXButton choose;
    @FXML
    private JFXTextField url;
    @FXML
    private ProgressBar progress;
    @FXML
    private JFXButton start;
    @FXML
    private JFXButton save;
    @FXML
    private JFXCheckBox useProxy;
    @FXML
    private Label log;
    @FXML
    private JFXTextField host1;
    @FXML
    private JFXTextField port1;
    @FXML
    private JFXTextField host2;
    @FXML
    private JFXTextField port2;
    @FXML
    private JFXTextField host3;
    @FXML
    private JFXTextField port3;
    @FXML
    private JFXTextField host4;
    @FXML
    private JFXTextField port4;
    @FXML
    private JFXTextField host5;
    @FXML
    private JFXTextField port5;
    @FXML
    private JFXTextField host6;
    @FXML
    private JFXTextField port6;

    private volatile static Map<String,String> proxyMap = new HashMap<>();



    private final static String HOST = "host";
    private final static String PORT = "port";
    private final static String NOT_SET = "Not Set ...";
    private final static String SAVE_PATH = "savePath";

    private final static String[] startTexts = new String[]{"Start","正在下载..."};

    private final static Color DEFAULT_RED = Color.web("#DA0F0F");
    private final static Color THEME_COLOR1 = Color.web("#BABABA");


    public FXMLController(){}

    @FXML
    private void initialize()
    {
        /**
         * 开始下载点击事件
         */
        start.setOnAction(event -> {
            String urlText = url.getText().trim();
            if(!StringUtil.isEmpty(urlText)) {
                if (!path.getText().equals(NOT_SET)) {
                    if (Pattern.matches(Constants.URL, urlText)) {
                        //立即设置状态为不可点击  视觉上看起来更快
                        start.setDisable(true);
//                        start.setText(startTexts[1]);
                        progress.setProgress(0.0);
                        new Thread(()
                                -> DownLoadControler.start(proxyMap, urlText, path.getText(), useProxy.isSelected(), progress, start, log))
                                .start();
                    } else {
                        new DialogBuilder(start).setTitle("提示").setMessage("请输入正确的url").setNegativeBtn("确定").create();
                    }
                } else {
                    new DialogBuilder(start).setTitle("提示").setMessage("未输入url").setNegativeBtn("确定").create();
                }
            }else{
                new DialogBuilder(start).setTitle("提示").setMessage("未选择下载路径").setNegativeBtn("确定").create();
            }
        });

        /**
         * 将代理保存至文件
         */
        save.setOnAction(event -> {
            try {
                String methodName;
                JFXTextField jfxTextField;
                Method method;
                Class fxmlC = FXMLController.this.getClass();
                String host;
                String port;
                Properties properties = new Properties();
                for(int i=1;i<=6;i++){
                    //通过反射获取组件(属性)
                    methodName = "getHost"+i;
                    method = fxmlC.getMethod(methodName);
                    jfxTextField  = (JFXTextField)method.invoke(FXMLController.this);
                    host = jfxTextField.getText();


                    methodName = "getPort"+i;
                    method = fxmlC.getMethod(methodName);
                    jfxTextField  = (JFXTextField)method.invoke(FXMLController.this);
                    port = jfxTextField.getText();


                    //要不两个都为空 要不两个都不为空
                    if(!StringUtil.isEmpty(host,port)){
                        //host 和 ip都不为空 配置有效,进行基础校验
                        if(Pattern.matches(Constants.NUMBER, port)){
                            properties.setProperty(HOST+i, host);
                            properties.setProperty(PORT+i, port);
                            proxyMap.put(HOST+i, host);
                            proxyMap.put(PORT+i, port);
                            //校验通过 持久化至配置文件
                            PropertiesUtil.outToFile(properties,fxmlC.getClassLoader().getResource(Constants.PROXY_HOST_PRO).getPath());
                        }else{
                            //端口不符合规则
                            new DialogBuilder(start).setTitle("提示").setMessage("Port 只能为数字").setNegativeBtn("确定").create();
                            return;
                        }
                    }else if(StringUtil.isEmpty(host) && StringUtil.isEmpty(port)){
                        //都为空
                        properties.setProperty(HOST+i, "");
                        properties.setProperty(PORT+i, "");
                        proxyMap.remove(HOST+i);
                        proxyMap.remove(PORT+i);
                        //持久化至配置文件
                        PropertiesUtil.outToFile(properties,fxmlC.getClassLoader().getResource(Constants.PROXY_HOST_PRO).getPath());
                    } else {
                        if(StringUtil.isEmpty(host)){
                            new DialogBuilder(start).setTitle("提示").setMessage("请配置Host").setNegativeBtn("确定").create();
                        }else{
                            new DialogBuilder(start).setTitle("提示").setMessage("请配置Port").setNegativeBtn("确定").create();
                        }
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });

        /**
         * 设置文件下载路径
         */
        choose.setOnAction(event -> {
            DirectoryChooser directoryChooser=new DirectoryChooser();
            File file = directoryChooser.showDialog(new Stage());
            if(file == null){
                path.setText(NOT_SET);
                path.setTextFill(DEFAULT_RED);
            }else{
                String savePath = file.getPath(); //选择的文件夹路径
                path.setText(savePath);
                path.setTextFill(THEME_COLOR1);
                //持久化到文件
                Properties properties = new Properties();
                properties.setProperty(SAVE_PATH,savePath);
                PropertiesUtil.outToFile(properties,FXMLController.class.getClassLoader().getResource(Constants.CONFIG_PRO).getPath());
            }
        });


        try {
            //加载已配置代理
            setProxy(new PropertiesUtil(Constants.PROXY_HOST_PRO).getPropertys());
            //加载历史 savePath
            String savePath = new PropertiesUtil(Constants.CONFIG_PRO).getProperty(SAVE_PATH);
            if(!StringUtil.isEmpty(savePath)){
                path.setText(savePath);
                path.setTextFill(THEME_COLOR1);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 初始化配置文件代理
     * @param sets
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     */
    private void setProxy(Set<Map.Entry<Object, Object>> sets) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        String methodName;
        String key;
        String value;
        JFXTextField jfxTextField;
        //通过反射 初始化已配置代理
        for (Map.Entry<Object, Object> map : sets) {
            key = map.getKey().toString();
            if(!key.contains(HOST) && !key.contains(PORT)) continue;
            methodName = "get" + String.valueOf(key.charAt(0)).toUpperCase() + key.substring(1);
            Method method = this.getClass().getMethod(methodName);
            jfxTextField  = (JFXTextField)method.invoke(this);
            value = map.getValue().toString();
            jfxTextField.setText(value);

            //将配置加载至内存
            proxyMap.put(key, value);
        }
    }

    public JFXTextField getHost1() {
        return host1;
    }

    public JFXTextField getPort1() {
        return port1;
    }

    public JFXTextField getHost2() {
        return host2;
    }

    public JFXTextField getPort2() {
        return port2;
    }

    public JFXTextField getHost3() {
        return host3;
    }

    public JFXTextField getPort3() {
        return port3;
    }

    public JFXTextField getHost4() {
        return host4;
    }

    public JFXTextField getPort4() {
        return port4;
    }

    public JFXTextField getHost5() {
        return host5;
    }

    public JFXTextField getPort5() {
        return port5;
    }

    public JFXTextField getHost6() {
        return host6;
    }

    public JFXTextField getPort6() {
        return port6;
    }

}
