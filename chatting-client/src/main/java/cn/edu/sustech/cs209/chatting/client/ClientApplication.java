package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Client;
import cn.edu.sustech.cs209.chatting.common.CreateGroupMessage;
import cn.edu.sustech.cs209.chatting.common.FileMessage;
import cn.edu.sustech.cs209.chatting.common.HugeMessage;
import cn.edu.sustech.cs209.chatting.common.LoginSignUpMessage;
import cn.edu.sustech.cs209.chatting.common.LoginSignupResponse;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.OfflineMessage;
import cn.edu.sustech.cs209.chatting.common.Protocols;
import cn.edu.sustech.cs209.chatting.common.QuitMessage;
import cn.edu.sustech.cs209.chatting.common.States;
import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApplication extends Application {
    private static InputStream inputStream;
    private static OutputStream outputStream;

    private static ObjectOutputStream os;

    private static Socket socket;
    private static LoginController loginController;
    private static AtomicReference<Controller> controller = new AtomicReference<>();


    public static Lock controllerLock = new ReentrantLock();
    private static Condition controllerCondition = controllerLock.newCondition();
    static boolean Over = false;



    private static final AtomicReference<LoginSignupResponse> loginSignupResponse = new AtomicReference<>(new LoginSignupResponse());

    public static void setLoginSignupResponse(LoginSignupResponse l) {
        loginSignupResponse.set(l);
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        Logger.getLogger("javafx.css").setLevel(Level.SEVERE);

        ClientAppLaunch();
    }

    public static void ClientAppLaunch(){
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        loginController = loginLoader.getController();
        Scene scene = new Scene(loginLoader.load());
        stage.setScene(scene);
        stage.show();
    }

    // determine to what kind of operation
    public static void sentLoginSignup(Protocols protocols, String username, String password) throws IOException {
        LoginSignUpMessage message = new LoginSignUpMessage(protocols, username, password);
        os.writeObject(message);
        os.flush();
    }

    public static void sentOffline(Protocols protocols, String username) throws IOException {
        Set<Set<String>> users = new CopyOnWriteArraySet<>(controller.get().getCurrentGroups());
        os.writeObject(new OfflineMessage(protocols, username, users));
        os.flush();
    }

    public static void sentMessage(Message message)  {
        try{
            controller.get().putClientAhead(message.getSendTo());
            os.writeObject(message);
            os.flush();
        }catch (IOException e){
            System.out.println("sentMessage: "+e.getMessage());
        }
    }


    public static Client checkLoginSignup() {
        while(loginSignupResponse.get().getProtocol() == Protocols.NULL){}
        if(loginSignupResponse.get().getState() == States.SUCCESSFUL){
            Client c = loginSignupResponse.get().getClient();
            loginSignupResponse.set(new LoginSignupResponse());
            return c;
        }
        else {
            loginSignupResponse.set(new LoginSignupResponse());
            return null;
        }
    }



    public static void connectServer(String hostname, int port) throws IOException{
        socket = new Socket(hostname, port);
        System.out.println("Connected!");
        Thread readThread = new Thread(new ClientThreadIn(socket));
        readThread.start();
        os = new ObjectOutputStream(socket.getOutputStream());
    }

    public static void setController(Controller controller){
        controllerLock.lock();
        ClientApplication.controller.set(controller);
        controllerCondition.signalAll();
        controllerLock.unlock();
    }
    public static void updateClientList(Collection<Client> clientSet) {
        controllerLock.lock();
        if(controller.get() == null){
            try {
                controllerCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        controllerLock.unlock();
        controller.get().updateOnlineClient(clientSet);
    }

    public static Controller getController(){
        return controller.get();
    }

    public static void createGroup(Set<Client> clients, Client initiator) throws IOException {
        CreateGroupMessage cgm = new CreateGroupMessage(Protocols.CREATE_GROUP, clients, initiator);
        cgm.getClients().forEach(e -> System.out.print(e.getUserName()+" "));
        System.out.println(initiator.getUserName());
        os.writeObject(cgm);
        cgm.getClients().forEach(e -> System.out.print(e.getUserName()+" "));
        System.out.println("CREATE_GROUP: "+cgm.getNames());
        os.flush();
    }
    public static void closeOs(){
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
