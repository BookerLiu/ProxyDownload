package com.demoliu.ui;

import com.demoliu.common.Constants;
import com.demoliu.util.FileUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Demo-Liu
 * @create 2020-07-14 17:22
 * @description
 */
public class Window  extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(Constants.FXML_PATH)));
        Scene scene = new Scene(root);
        primaryStage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(Constants.LOGO_PATH))
        ));
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) throws IOException {
        FileUtil.fileIsExistsAndCreat(Constants.CONFIG_PRO);
        FileUtil.fileIsExistsAndCreat(Constants.PROXY_HOST_PRO);
        launch(args);
    }
}
