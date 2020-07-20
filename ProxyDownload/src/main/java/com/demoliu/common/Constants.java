package com.demoliu.common;


/**
 * @author Demo-Liu
 * @create 2020-07-07 10:39
 * @description 常量
 */
public class Constants {

    private static String PROJECT_LAST_DIR;

    static {
        String userDir = System.getProperty("user.dir");
        if(userDir.startsWith("C:")){
            //如果是C盘,可能没有写入权限,将持久化配置写入用户目录
            PROJECT_LAST_DIR = System.getProperty("user.home")+"/.pd";
        }else{
            PROJECT_LAST_DIR = userDir + "/runtime";
        }
    }

    //代理配置文件
    public static String PROXY_HOST_PRO = PROJECT_LAST_DIR + "/proxy.properties";

    //全局配置文件
    public static String CONFIG_PRO =  PROJECT_LAST_DIR + "/config.properties";

    //校验url正则
    public final static String URL = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%()?=~_|!:,.;]+[-A-Za-z0-9+&@#/%()=~_|]";

    //校验port正则
    public final static String NUMBER = "^[0-9]*$";

    //logo
    public final static String LOGO_PATH = "static/pd.png";

    //fxml
    public final static String FXML_PATH = "ui/downui.fxml";
}
