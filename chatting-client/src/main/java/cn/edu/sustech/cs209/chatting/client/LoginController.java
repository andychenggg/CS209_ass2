package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Client;
import SelfDefineExceptions.IllegalFormatException;
import SelfDefineExceptions.WrongInfoException;
import cn.edu.sustech.cs209.chatting.common.Protocols;
import cn.edu.sustech.cs209.chatting.common.States;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginController implements Initializable {
  @FXML
  private TextField usernameTextField;
  @FXML
  private TextField passwordTextField;
  @FXML
  private TextField hostnameTextField;
  @FXML
  private TextField portTextField;
  @FXML
  private Button loginButton;
  @FXML
  private Button signButton;
  private Lock loginLock = new ReentrantLock();
  private Client client;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("Starting login.");
  }

  public void showWrongMessageInLogicController(String s) {
    FXMLLoader wrongLoader = new FXMLLoader(getClass().getResource("WrongInfoPopup.fxml"));
    Stage wrongStage = new Stage();
    try {
      wrongStage.setScene(new Scene(wrongLoader.load()));
      WrongMessageController sc = wrongLoader.getController();
      sc.setWrongTextLabel(s);
    } catch (IOException e1) {
      return;
    }
    wrongStage.initModality(Modality.APPLICATION_MODAL);
    wrongStage.show();
  }

  private boolean ConnectServer(String Server, String port) {
    try {
      ClientApplication.connectServer(Server, Integer.parseInt(port));
    } catch (ConnectException e) {
      // show a popup and return
      showWrongMessageInLogicController("Connection failed! ");
      return false;
    } catch (IOException e) {
      // show a popup and return
      showWrongMessageInLogicController("Input and output don't seem to work!");
      return false;
    } catch (NumberFormatException e) {
      // show a popup and return
      showWrongMessageInLogicController("Illegal input about host or port! ");
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public void handleLoginAction(ActionEvent event) {
    // Connect to the server
    boolean Connected = ConnectServer(hostnameTextField.getText(), portTextField.getText());
      if (!Connected) {
          return;
      }

    // After connect to the Server, deal with login
    try {
      ClientApplication.sentLoginSignup(Protocols.LOGIN, usernameTextField.getText(),
          passwordTextField.getText());
      client = ClientApplication.checkLoginSignup();
      if (client == null) {
        throw new WrongInfoException("Wrong username or password! ");
      }
    } catch (WrongInfoException e) {
      // show a popup and return
      showWrongMessageInLogicController(e.getMessage());
      return;
    } catch (IOException e) {
      // show a popup and return
      showWrongMessageInLogicController("Fail to convey the LOGIN request! ");
      return;
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    // Successfully login.
    Button loginButton = (Button) event.getSource();
    // 获取按钮所在的窗口
    Stage loginStage = (Stage) loginButton.getScene().getWindow();
    // 关闭登录窗口
    loginStage.close();

    // 打开聊天窗口
    showChatWindow();
  }


  public void handleSignupAction(ActionEvent event) {
    boolean Connected = ConnectServer(hostnameTextField.getText(), portTextField.getText());
      if (!Connected) {
          return;
      }

    // After connect to the Server, deal with signup
    try {
      ClientApplication.sentLoginSignup(Protocols.SIGNUP, usernameTextField.getText(),
          passwordTextField.getText());
      client = ClientApplication.checkLoginSignup();
      if (client == null) {
        throw new IllegalFormatException("Username has already exist!");
      }

    } catch (IllegalFormatException e) {
      // show a popup and return
      showWrongMessageInLogicController(e.getMessage());
      return;
    } catch (IOException e) {
      // show a popup and return
      showWrongMessageInLogicController("Fail to convey the SIGNUP request! ");
      return;
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    // 获取事件源，即登录按钮
    Button loginButton = (Button) event.getSource();
    // 获取按钮所在的窗口
    Stage loginStage = (Stage) loginButton.getScene().getWindow();
    // 关闭登录窗口
    loginStage.close();

    // 打开聊天窗口
    showChatWindow();
  }


  private void showChatWindow() {
//        ClientApplication.controllerLock.lock();
    // 创建一个FXMLLoader
    @SuppressWarnings("all")
    FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("main.fxml"));
    Parent chatRoot = null;
    try {
      chatRoot = chatLoader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Controller c = chatLoader.getController();
    ClientApplication.setController(c);
//        ClientApplication.controllerLock.unlock();
    c.initializeClient(client);
    c.readMessage();
    c.readRecentOrder();
    // 创建一个Scene
    Scene chatScene = new Scene(chatRoot);

    // 创建一个Stage并设置Scene
    Stage chatStage = new Stage();
    chatStage.setScene(chatScene);
    // set close request
    chatStage.setOnCloseRequest(e -> {
      Platform.runLater(() -> {
        try {
          ClientApplication.sentOffline(Protocols.OFFLINE, client.getUserName());
        } catch (IOException ex) {
          ex.printStackTrace();
        }
        c.saveMessage();
        c.saveRecentOrder();
        ClientApplication.getController().closeStages();
      });


      ClientApplication.Over = true;
      Platform.exit();
    });
    // 显示聊天窗口
    chatStage.show();
  }

  public void lockLoginLock() {
    loginLock.lock();
  }

  public void unlockLoginLock() {
    loginLock.unlock();
  }
}
