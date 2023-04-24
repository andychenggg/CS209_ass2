package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Client;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.Protocols;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ReminderController implements Initializable {
  @FXML
  private Button viewButton;
  private Stage chatStage;
  private Stage currentStage;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  public void initializeStage(Stage s, Stage t) {
    chatStage = s;
    currentStage = t;
  }

  @FXML
  public void handleViewClick(MouseEvent event) {
    // 处理按钮点击事件
    viewButton.setStyle(
        "-fx-background-color: transparent;-fx-text-fill: #0A7BFD;");
    viewButton.applyCss();
    viewButton.layout();
    chatStage.toFront();
    currentStage.close();
  }

  @FXML
  public void handleViewEnter(MouseEvent event) {
    // 处理鼠标进入事件
    viewButton.setStyle(
        "-fx-background-color: #0A7BFD;" +
            "-fx-text-fill: white;");
  }

  @FXML
  public void handleViewExit(MouseEvent event) {
    // 处理鼠标离开事件
    viewButton.setStyle(
        "-fx-background-color: transparent; -fx-text-fill: #0A7BFD;");
  }

}
