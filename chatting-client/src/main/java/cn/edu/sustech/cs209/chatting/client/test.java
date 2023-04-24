package cn.edu.sustech.cs209.chatting.client;

import java.awt.Desktop;
import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class test extends Application {
    private Stage primaryStage;
    private AnchorPane rootLayout;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("FileChooser Demo");

        // 创建菜单栏和布局
        // ...

        // 加载 FXML 文件
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择 FXML 文件");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MD Files", "*.md"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
            File initialDirectory = new File("C:\\Users\\程嘉朗\\IdeaProjects\\CS209_ass2\\chatting-client\\src\\main\\java\\cn\\edu\\sustech\\cs209\\chatting\\client");
            fileChooser.setInitialDirectory(initialDirectory);
            File file = fileChooser.showOpenDialog(primaryStage);
            System.out.println(file);
            if (file != null) {
                Desktop.getDesktop().open(new File(file.getAbsolutePath()));
            } else {
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Scene scene = new Scene(rootLayout);
//        primaryStage.setScene(scene);
//        primaryStage.show();
    }
}
