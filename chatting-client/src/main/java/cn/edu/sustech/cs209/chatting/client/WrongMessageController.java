package cn.edu.sustech.cs209.chatting.client;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class WrongMessageController implements Initializable {
  @FXML
  private Label WrongTextLabel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  public void setWrongTextLabel(String criticalMessage) {
    WrongTextLabel.setText("Error: " + criticalMessage);
  }
}
