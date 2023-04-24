package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Client;
import cn.edu.sustech.cs209.chatting.common.HugeMessage;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageRecord;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {
    Lock lk = new ReentrantLock();
    boolean isModified = false;
    InputStream inputStream;
    OutputStream outputStream;
    String s = "111";
    ObservableList<Client> onlineClients = FXCollections.observableArrayList();

    @FXML private ListView<Client> userContentList;
    @FXML private Label currentOnlineCnt;
    @FXML private HBox privateHBox;
    @FXML private HBox groupHBox;
    @FXML private Button privateAdd;
    @FXML private Button groupAdd;
    @FXML private Label avatar;
    @FXML private Label username;
    private CopyOnWriteArraySet<Client> selectedUsers = new CopyOnWriteArraySet<>();
    private CopyOnWriteArraySet<String> selectedUsername = new CopyOnWriteArraySet<>();
    private CopyOnWriteArrayList<CheckBox> currentCheckBoxes = new CopyOnWriteArrayList<>();
    // every set in currentGroups contain the current user
    private ConcurrentHashMap<Set<String>, Stage> currentGroups = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Set<String>, ChatController> currentControllers = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<String> mostRecentMap = new CopyOnWriteArrayList<>();
    private MessageRecord messageRecord;

    Client user;

    public ChatController getChatCTR(Set<String> cls){
        return currentControllers.get(cls);
    }

    synchronized public void updateOnlineClient(Collection<Client> clients){
        clients.removeIf(c -> c.getUserName().equals(user.getUserName()));
        Platform.runLater(() -> currentOnlineCnt.setText("Online: "+clients.size()));
        Platform.runLater(() -> {
            onlineClients.setAll(clients);
            onlineClients.sort((e1, e2) -> {
                if(!mostRecentMap.contains(e1.getUserName()) && !mostRecentMap.contains(e2.getUserName())){
                    return 0;
                }
                else if(!mostRecentMap.contains(e1.getUserName())){
                    return +1;
                }
                else if(!mostRecentMap.contains(e2.getUserName())){
                    return -1;
                }
                else {
                    return mostRecentMap.indexOf(e1.getUserName()) - mostRecentMap.indexOf(e2.getUserName());
                }
            });
        });
    }

    synchronized public void removeOnlineClient(Client removable){
        Platform.runLater(() -> onlineClients.remove(removable));
    }
    synchronized public void putClientAhead(Collection<String> recent){
        Platform.runLater(() -> {
            CopyOnWriteArraySet<Client> cl = new CopyOnWriteArraySet<>();
            onlineClients.removeIf(c -> {
                if(c != null && recent.contains(c.getUserName())){
                    cl.add(c);
                    return true;
                }
                return false;
            });
            cl.forEach(c -> onlineClients.add(0, c));
        });
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userContentList.setCellFactory(new UserCellFactory());
        userContentList.setItems(onlineClients);
    }
    public void initializeClient(Client client){
        user = client;
        messageRecord = new MessageRecord(user.getUserName());
        username.setText(user.getUserName());
        ImageView iv = new ImageView(user.getAvatar());
        iv.setFitHeight(60);
        iv.setFitWidth(60);
        avatar.setGraphic(iv);
    }
    public void showWrongMessageInController(String s){
        FXMLLoader wrongLoader = new FXMLLoader(getClass().getResource("WrongInfoPopup.fxml"));
        Stage wrongStage = new Stage();
        try {
            wrongStage.setScene(new Scene(wrongLoader.load()));
            WrongMessageController sc = wrongLoader.getController();
            sc.setWrongTextLabel(s);
        }catch (IOException e1){
            return;
        }
        wrongStage.initModality(Modality.APPLICATION_MODAL);
        wrongStage.show();
    }
    public void showWarningInController(String s){
        Platform.runLater(() -> {
            FXMLLoader wrongLoader = new FXMLLoader(getClass().getResource("Warning.fxml"));
            Stage wrongStage = new Stage();
            try {
                wrongStage.setScene(new Scene(wrongLoader.load()));
                WarningController sc = wrongLoader.getController();
                sc.setWarningTextLabel(s);
            }catch (IOException e1){
                return;
            }
            wrongStage.initModality(Modality.APPLICATION_MODAL);
            wrongStage.show();
        });

    }
    public Set<Set<String>> getCurrentGroups(){
        Set<Set<String>> sets = new HashSet<>(currentGroups.keySet());
        System.out.println(sets);
        return sets;
    }
    public void closeStages(){
        Platform.runLater(() -> {
            currentGroups.values().forEach(Stage::close);
        });
    }

    public void saveMessage(){
        File directory = new File("cn.edu.sustech.cs209.chatting.client/"+user.getUserName());
        File f = new File("cn.edu.sustech.cs209.chatting.client/"+user.getUserName()+"/messageMap");
        if (!directory.exists()) {
            directory.mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(messageRecord);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void readMessage(){
        try (
            FileInputStream fis = new FileInputStream("cn.edu.sustech.cs209.chatting.client/"+user.getUserName()+"/messageMap");
            ObjectInputStream is = new ObjectInputStream(fis)) {
            messageRecord = (MessageRecord) is.readObject();
        } catch (IOException |ClassNotFoundException e) {
            System.out.println("readMessage"+e.getMessage());
        }
    }

    public void addRecent(String sendBy){
        AtomicReference<String> s = new AtomicReference<>(sendBy);
        boolean success = mostRecentMap.removeIf(c -> c.equals(s.get()));
        mostRecentMap.add(0, s.get());
    }

    public void saveRecentOrder(){
        File directory = new File("cn.edu.sustech.cs209.chatting.client/"+user.getUserName());
        File f = new File("cn.edu.sustech.cs209.chatting.client/"+user.getUserName()+"/recentOrder");
        if (!directory.exists()) {
            directory.mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(mostRecentMap);
            os.flush();
        } catch (IOException e) {
            System.out.println("saveRecentOrder"+e.getMessage());
        }
    }
    public void readRecentOrder(){
        try (
            FileInputStream fis = new FileInputStream("cn.edu.sustech.cs209.chatting.client/"+user.getUserName()+"/recentOrder");
            ObjectInputStream is = new ObjectInputStream(fis)) {
            mostRecentMap = (CopyOnWriteArrayList<String>)  is.readObject();
        } catch (IOException |ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    public void privateMouseEnter(MouseEvent event){
        privateHBox.setStyle("-fx-background-color: #F57130;");
        privateHBox.applyCss();
        privateHBox.layout();
    }

    @FXML
    public void privateMouseExit(MouseEvent event){
        privateHBox.setStyle("-fx-background-color: transparent;");
        privateHBox.applyCss();
        privateHBox.layout();
    }

    @FXML
    public void groupMouseEnter(MouseEvent event){
        groupHBox.setStyle("-fx-background-color: #F57130;");
        groupHBox.applyCss();
        groupHBox.layout();
    }

    @FXML
    public void groupMouseExit(MouseEvent event){
        groupHBox.setStyle("-fx-background-color: transparent;");
        groupHBox.applyCss();
        groupHBox.layout();
    }

    @FXML
    public void createPrivateChat(MouseEvent event) {
        userContentList.applyCss();
        userContentList.layout();

        System.out.println("selectedUsers: "+selectedUsers);
        if(selectedUsers.size() != 1){
            showWrongMessageInController("Private Group Only allow 2 persons in total!");
            selectedUsers = new CopyOnWriteArraySet<>();
            currentCheckBoxes.forEach(e -> e.setSelected(false));
        }
        else{
            System.out.println("selectedUsers"+selectedUsers);
            Set<Client> checkUsers = new CopyOnWriteArraySet<>(selectedUsers);
            checkUsers.add(user);
            Set<String> checkUsernames = checkUsers.stream().map(Client::getUserName).collect(
                Collectors.toSet());
            if(currentGroups.containsKey(checkUsernames)){
                if(!currentGroups.get(checkUsernames).isShowing())
                    currentGroups.get(checkUsernames).show();
                currentGroups.get(checkUsernames).toFront();
            }
            else{
                try {
                    ClientApplication.createGroup(new HashSet<>(selectedUsers), user);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showChatStage(checkUsers);
            }
            currentCheckBoxes.forEach(e -> e.setSelected(false));
        }
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat(MouseEvent event) {
        userContentList.applyCss();
        userContentList.layout();

        System.out.println("selectedUsers: "+selectedUsers);
        if(selectedUsers.size() <= 1){
            showWrongMessageInController("Group Chats Only allow 3 persons or more in total!");
            selectedUsers = new CopyOnWriteArraySet<>();
            currentCheckBoxes.forEach(e -> e.setSelected(false));
        }
        else{
            System.out.println("selectedUsers"+selectedUsers);
            Set<Client> checkUsers = new CopyOnWriteArraySet<>(selectedUsers);
            checkUsers.add(user);
            Set<String> checkUsernames = checkUsers.stream().map(Client::getUserName).collect(
                Collectors.toSet());
            if(currentGroups.containsKey(checkUsernames)){
                if(!currentGroups.get(checkUsernames).isShowing())
                    currentGroups.get(checkUsernames).show();
                currentGroups.get(checkUsernames).toFront();
            }
            else{
                try {
                    ClientApplication.createGroup(new HashSet<>(selectedUsers), user);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showChatStage(checkUsers);
            }
            currentCheckBoxes.forEach(e -> e.setSelected(false));
        }
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */

    public void remindMessage(Set<String> cls){
        Platform.runLater(() -> {
            FXMLLoader remindLoader = new FXMLLoader(getClass().getResource("MessageReminder.fxml"));
            Parent remindRoot = null;
            try {
                remindRoot = remindLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 创建一个Scene
            Scene remindScene = new Scene(remindRoot);
            // 创建一个Stage并设置Scene
            Stage remindStage = new Stage();
            ReminderController r = remindLoader.getController();
            r.initializeStage(currentGroups.get(cls), remindStage);
            remindStage.setScene(remindScene);
            remindStage.initModality(Modality.APPLICATION_MODAL);
            remindStage.show();
            remindStage.toFront();
        });

    }
    public void receiveMessage(HugeMessage h) {
        if(h instanceof Message){
            // show the message
            Message m = (Message) h;
            Set<String> sendTo = m.getSendTo();
            sendTo.add(m.getSentBy());
            System.out.println("receiveMessage: "+sendTo);
            ChatController ctr = currentControllers.get(sendTo);
            ctr.addMessage(m);
            remindMessage(sendTo);
        }
    }

    public void showChatStage(Set<Client> groups){
        Platform.runLater(() -> {

            groups.add(user);
            Set<String> usernames = groups.stream().map(Client::getUserName).collect(Collectors.toSet());
            if(currentGroups.containsKey(groups)){
                if(!currentGroups.get(usernames).isShowing())
                    currentGroups.get(usernames).show();
                currentGroups.get(usernames).toFront();
            }
            else{
                FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("Chat.fxml"));
                Parent chatRoot = null;
                try {
                    chatRoot = chatLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // set ChatController
                ChatController ctr = chatLoader.getController();
                groups.add(user);
                currentControllers.put(groups.stream().map(Client::getUserName).collect(Collectors.toSet()), ctr);
                // 创建一个Scene
                Scene chatScene = new Scene(chatRoot);

                // 创建一个Stage并设置Scene
                Stage chatStage = new Stage();
                currentGroups.put(groups.stream().map(Client::getUserName).collect(Collectors.toSet()), chatStage);
                chatStage.setScene(chatScene);
                ctr.initializeClients(user, groups);
                if(messageRecord.getRecord().containsKey(groups.stream().map(Client::getUserName).collect(Collectors.toSet()))){
                    System.out.println("showChatStage: find records");
                    ctr.addAllMessage(messageRecord.getRecord().get(groups.stream().map(Client::getUserName).collect(Collectors.toSet())));
                }
                chatStage.setOnCloseRequest(e -> {
                    e.consume();
                    // store the message into the map
                    Set<String> cls = groups.stream().map(Client::getUserName).collect(Collectors.toSet());
                    System.out.println("close"+ctr.getAllMessages().size());
                    if(messageRecord.getRecord().containsKey(cls)){
                        messageRecord.getRecord().replace(cls, ctr.getAllMessages());
                    }
                    else {
                        messageRecord.getRecord().put(cls, ctr.getAllMessages());
                    }
                    saveMessage();
                    chatStage.hide();
                });
                chatStage.show();
                chatStage.toFront();
            }

        });
    }


    private class UserCellFactory implements Callback<ListView<Client>, ListCell<Client>> {
        @Override
        public ListCell<Client> call(ListView<Client> param) {
            return new ListCell<Client>() {
                @Override
                public void updateItem(Client client, boolean empty) {

                super.updateItem(client, empty);
                if (empty || Objects.isNull(client) ) {
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    return;
                }
                else if(user.getUserName().equals(client.getUserName())){
                    System.out.println(client.getLogIn());
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    return;
                }

                HBox hBox = new HBox();
                hBox.setPrefSize(300, 80);
                StackPane imagePane = new StackPane();
                imagePane.setPrefSize(80, 80);
                AnchorPane anchorPane = new AnchorPane();
                anchorPane.setPrefSize(220, 80);
                hBox.getChildren().add(imagePane);
                hBox.getChildren().add(anchorPane);

                Label avatarLabel = new Label();
                avatarLabel.setPrefSize(60, 60);
                ImageView iv = new ImageView(client.getAvatar());
                iv.setFitHeight(50);
                iv.setFitWidth(50);
                avatarLabel.setGraphic(iv);
                imagePane.getChildren().add(avatarLabel);

                Label usernameLabel = new Label();
                usernameLabel.setText(client.getUserName());
                usernameLabel.setStyle("-fx-font-size: 18px; -fx-font-family: \"Comic Sans MS\";");
                anchorPane.getChildren().add(usernameLabel);
                AnchorPane.setTopAnchor(usernameLabel, 10.0);

                CheckBox checkBox = new CheckBox();
                checkBox.setPrefSize(10, 10);
                AnchorPane.setRightAnchor(checkBox, 10.0);
                anchorPane.getChildren().add(checkBox);
                currentCheckBoxes.add(checkBox);
                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    // 处理状态变化
                    if(oldValue && !newValue){
                        selectedUsers.remove(client);
                        selectedUsername.remove(client.getUserName());
                    }
                    else {
                        selectedUsers.add(client);
                        selectedUsername.add(client.getUserName());
                    }
                });

                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(hBox);
                }
            };
        }
    }

}
