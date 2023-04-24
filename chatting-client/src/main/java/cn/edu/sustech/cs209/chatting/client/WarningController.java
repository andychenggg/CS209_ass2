package cn.edu.sustech.cs209.chatting.client;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class WarningController implements Initializable {
    @FXML
    private Label WarningTextLabel;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void setWarningTextLabel(String criticalMessage){
        WarningTextLabel.setText("Warning: "+criticalMessage);
    }
}
