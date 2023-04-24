package cn.edu.sustech.cs209.chatting.client;


import cn.edu.sustech.cs209.chatting.common.Client;
import cn.edu.sustech.cs209.chatting.common.FileMessage;
import cn.edu.sustech.cs209.chatting.common.HugeMessage;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.Protocols;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ChatController implements Initializable {
    @FXML
    private Tab textTab;
    @FXML
    private Tab fileTab;
    @FXML
    private Button sendButton;
    @FXML
    private Button browseButton;
    @FXML
    private TextArea Text;
    @FXML
    private HBox headBox;

    @FXML
    private ListView<HugeMessage> messageList;
    private final CopyOnWriteArraySet<Client> members = new CopyOnWriteArraySet<>();
    private final ObservableList<HugeMessage> messages = FXCollections.observableArrayList();
    private Client user;
    private final ConcurrentHashMap<String, AnchorPane> avatars = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<HBox, Integer> clickCounts = new ConcurrentHashMap<>();


    public void removeAvatar(String username){
        System.out.println("In removeAvatar: "+avatars.get(username));
        Platform.runLater(() -> {
            headBox.getChildren().remove(avatars.get(username));
        });
    }

    public void addMessage(HugeMessage m) {
        Platform.runLater(() -> messages.add(m));
    }
    public void addAllMessage(List<HugeMessage> hml){
        Platform.runLater(() -> messages.addAll(hml));
    }
    public List<HugeMessage> getAllMessages(){
        return new ArrayList<>(messages);
    }

    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Starting chats.");

        messageList.setCellFactory(new MessageCellFactory());
        messageList.setItems(messages);
    }

    public void initializeClients(Client user, Set<Client> group) {
        //test
//        members.add(new Client("333"));
//        members.add(new Client("333"));
        members.addAll(group);
        members.add(user);

        this.user = user;
        headBox.getChildren().clear();
        StackPane imagePane = new StackPane();
        imagePane.setPrefSize(80, 80);
        imagePane.setPadding(new Insets(0, 20, 0, 0));
        Label chatLabel = new Label();
        chatLabel.setPrefSize(80, 80);
        ImageView iv = new ImageView(String.valueOf(getClass().getResource("image/ChatRoom.png")));
        iv.setFitHeight(80);
        iv.setFitWidth(80);
        chatLabel.setGraphic(iv);
        imagePane.getChildren().add(chatLabel);
        headBox.getChildren().add(imagePane);
        for (Client c : members) {
            AnchorPane avatarPane = new AnchorPane();
            avatarPane.setPrefSize(40, 80);
            Label avatarLabel = new Label();
            avatarLabel.setPrefSize(30, 30);
            AnchorPane.setBottomAnchor(avatarLabel, 5.0);
            avatarPane.getChildren().add(avatarLabel);
            ImageView im = new ImageView(c.getAvatar());
            im.setFitHeight(30);
            im.setFitWidth(30);
            avatarLabel.setGraphic(im);
            headBox.getChildren().add(avatarPane);
            avatars.put(c.getUserName(), avatarPane);
        }
    }

    @FXML
    public void handleSendClick(MouseEvent event) {
        // 处理按钮点击事件
        sendButton.setStyle(
            "-fx-background-color: transparent;-fx-text-fill: #0A7BFD;");
        sendButton.applyCss();
        sendButton.layout();
        if (Text.getText() == null || Text.getText().equals("")) {
            // do nothing
            System.out.println(Text.getText());
            return;
        }
        try {
            System.out.println(members);
            Message newMess =
                new Message(Protocols.MESSAGE, LocalDateTime.now(), user.getUserName(),
                    members.stream().map(Client::getUserName)
                        .filter(userName -> !userName.equals(user.getUserName()))
                        .collect(Collectors.toCollection(CopyOnWriteArraySet::new))
                    , Text.getText());
            ClientApplication.sentMessage(newMess);
            addMessage(newMess);
        } catch (Exception e) {
            System.out.println("handleSendClick"+e.getMessage());
        } finally {
            Text.setText(null);
        }
//        initializeClients();
    }

    @FXML
    public void handleSendEnter(MouseEvent event) {
        // 处理鼠标进入事件
        sendButton.setStyle(
            "-fx-background-color: #0A7BFD;" +
                "-fx-text-fill: white;");
    }

    @FXML
    public void handleSendExit(MouseEvent event) {
        // 处理鼠标离开事件
        sendButton.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #0A7BFD;");
    }

    @FXML
    public void handleBrowseClick(MouseEvent event) {
        // 处理按钮点击事件
        browseButton.setStyle(
            "-fx-background-color: transparent;");
        browseButton.applyCss();
        browseButton.layout();

        Stage chooserStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select your files");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"));
        File initialDirectory = new File("./chatting-client/src/main/java/cn/edu/sustech/cs209/chatting/client/"+user.getUserName());
        if(!initialDirectory.exists()){
            boolean success = initialDirectory.mkdirs();
        }
        fileChooser.setInitialDirectory(initialDirectory);
        File file = fileChooser.showOpenDialog(chooserStage);
        if(file == null){
            return;
        }
        try {
            Message newMess = new Message(Protocols.FILE, LocalDateTime.now(), user.getUserName(),
                members.stream().map(Client::getUserName)
                    .filter(userName -> !userName.equals(user.getUserName()))
                    .collect(Collectors.toCollection(CopyOnWriteArraySet::new))
                , file);
            ClientApplication.sentMessage(newMess);
            addMessage(newMess);
        } catch (Exception e) {
            System.out.println("handleBrowseClick"+e.getMessage());;
        }
    }

    @FXML
    public void handleBrowseEnter(MouseEvent event) {
        // 处理鼠标进入事件
        sendButton.setStyle(
            "-fx-background-color: #F57130;");
    }

    @FXML
    public void handleBrowseExit(MouseEvent event) {
        // 处理鼠标离开事件
        sendButton.setStyle(
            "-fx-background-color: transparent;");
    }

    @FXML
    public void handleTextTab(Event event) {
        if (textTab.isSelected()) {
            Text.setFont(Font.font("EmojiOne", 14));
//            Text.setText("\uD83D\uDE01 Hello World!");
            textTab.setStyle(
                "    -fx-border-color: #0A7BFD;\n" +
                    "    -fx-border-width: 3px;\n" +
                    "    -fx-border-insets: 0px 0px -3px 0px;");
        } else {
            textTab.setStyle("\n" +
                "    -fx-border-color: transparent;\n");
        }
    }

    @FXML
    public void handleFileTab(Event event) {
        if (fileTab.isSelected()) {
            fileTab.setStyle("\n" +
                "    -fx-border-color: #0A7BFD;\n" +
                "    -fx-border-width: 3px;\n" +
                "    -fx-border-insets: 0px 0px -3px 0px;");
        } else {
            fileTab.setStyle("\n" +
                "    -fx-border-color: transparent;\n");
        }
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory
        implements Callback<ListView<HugeMessage>, ListCell<HugeMessage>> {
        @Override
        public ListCell<HugeMessage> call(ListView<HugeMessage> param) {
            return new ListCell<HugeMessage>() {

                @Override
                public void updateItem(HugeMessage hugeMsg, boolean empty) {
                    super.updateItem(hugeMsg, empty);
                    if (empty || Objects.isNull(hugeMsg)) {
                        setGraphic(null);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        return;
                    }

                    if (hugeMsg instanceof Message) {
                        Message msg = (Message) hugeMsg;
                        VBox nameAndAvatar = new VBox();
                        Label nameLabel = new Label(msg.getSentBy());
                        HBox msgBox = new HBox();
                        if(msg.getProtocol() == Protocols.MESSAGE){
                            Label msgLabel = new Label(msg.getData());
                            msgBox.getChildren().add(msgLabel);
                            msgBox.setAlignment(Pos.CENTER);
                        }
                        else{

                            ImageView iv = null;
                            try {
                                iv = new ImageView(new Image(getClass().getResource("image/File1.png").toURI().toURL().toString()));
                            } catch (MalformedURLException|URISyntaxException e) {
                                e.printStackTrace();
                            }
                            iv.setFitHeight(40);
                            iv.setFitWidth(40);
                            msgBox.getChildren().add(iv);
                            Label fileLabel = new Label(msg.getFileName());
                            msgBox.getChildren().add(fileLabel);
                            msgBox.setOnMouseClicked(e -> {
                                String relativePath = "cn.edu.sustech.cs209.chatting.client/"+user.getUserName();
                                File f = msg.write(relativePath);
                                try {
                                    Desktop.getDesktop().open(f.getAbsoluteFile());
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });

                        }
                        Label timeLabel = new Label(msg.getTimestamp().format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")));

                        nameLabel.setPrefSize(50, 20);
                        nameLabel.setWrapText(true);
                        nameLabel.setStyle(
                            "-fx-border-color: #0A7BFD; -fx-border-width: 2px; -fx-font-family: 'Comic Sans MS'");

                        Image image = null;
                        for (Client member : members) {
                            if (member.getUserName().equals(((Message) hugeMsg).getSentBy())) {
                                image = member.getAvatar();
                            }
                        }
                        Label avatar = new Label();
                        ImageView iv = new ImageView(image);
                        iv.setFitHeight(40);
                        iv.setFitWidth(40);
                        avatar.setGraphic(iv);
                        nameAndAvatar.getChildren().addAll(nameLabel, avatar);

                        GridPane gridPane = new GridPane();
                        gridPane.setHgap(10);
                        gridPane.setVgap(10);
                        gridPane.setPadding(new Insets(10));


                        timeLabel.setPrefSize(270,20);
                        nameLabel.setPrefSize(50, 20);
                        msgBox.setPrefSize(270, 50);
                        avatar.setPrefSize(50, 50);

                        gridPane.getChildren().addAll(timeLabel, nameLabel, msgBox, avatar);

                        if (user.getUserName().equals(msg.getSentBy())) {
//                            wrapper.setAlignment(Pos.TOP_RIGHT);
//                            wrapper.getChildren().addAll(msgLabel, nameAndAvatar);
                            GridPane.setConstraints(timeLabel, 0, 0);
                            GridPane.setConstraints(nameLabel, 1, 0);
                            GridPane.setConstraints(msgBox, 0, 1);
                            GridPane.setConstraints(avatar, 1, 1);

                            nameLabel.setPadding(new Insets(0, 20, 5, 0));
                        } else {
//                            wrapper.setAlignment(Pos.TOP_LEFT);
//                            wrapper.getChildren().addAll(nameAndAvatar, msgLabel);
                            GridPane.setConstraints(timeLabel, 1, 0);
                            GridPane.setConstraints(nameLabel, 0, 0);
                            GridPane.setConstraints(msgBox, 1, 1);
                            GridPane.setConstraints(avatar, 0, 1);
                            nameLabel.setTextAlignment(TextAlignment.RIGHT);
                            nameLabel.setPadding(new Insets(0, 0, 5, 20));
                        }

                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        setGraphic(gridPane);
                    }


                }
            };
        }
    }
}
