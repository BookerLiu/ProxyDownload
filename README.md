# ProxyDownload
  ![](https://github.com/Demo-Liu/MyPicture/blob/master/PDico/%E6%9C%AA%E6%A0%87%E9%A2%98-4.png)  
  **PD-ProxyDownload** , 一个多线程下载软件,可通过配置多代理的方式破解网络限速(局域网ip限速)
  

# 开发工具
- **JDK 11+**
- **JavaFX SDK14.0.1**
  
  
# 程序界面

- **主界面**  
![](https://github.com/Demo-Liu/MyPicture/raw/master/ProxyDownload/%E4%B8%8B%E8%BD%BD%E7%95%8C%E9%9D%A2.png)  

- **代理配置**  
![](https://github.com/Demo-Liu/MyPicture/raw/master/ProxyDownload/%E4%BB%A3%E7%90%86%E9%85%8D%E7%BD%AE%E7%95%8C%E9%9D%A2.png)  

- **开始下载**  
![](https://github.com/Demo-Liu/MyPicture/raw/master/ProxyDownload/%E4%B8%8B%E8%BD%BD%E4%B8%AD%E7%95%8C%E9%9D%A2.png)  

# 软件下载
[ProxyDownload V0.0.1](https://github.com/Demo-Liu/ProxyDownload/releases/tag/0.0.1)

# 如何构建较小jvm
  **PD** 运行环境使用 jlink 构建(Java 9+ 中包含)  
  执行以下命令
  ```
  jlink --no-header-files --no-man-pages --module-path $JAVAFX_DIR --add-modules java.base,java.xml,java.scripting,javafx.base,javafx.fxml,javafx.graphics,javafx.controls,java.naming --output runtime
  ```  
  上面代码块中 **$JAVAFX_DIR** 为JavaFX SDK的lib目录,如 **D:\Java\javafx-sdk-14.0.1\lib**
# 如何通过 VMware 搭建多个代理
  由于篇幅原因,这里不再赘述,请移步公众号 **"抓几个娃"** 回复 **"PD"** 获取教程 欢迎关注  
  ![](https://github.com/Demo-Liu/MyPicture/raw/master/%E6%8A%93%E5%87%A0%E4%B8%AA%E5%A8%83.jpg)  
