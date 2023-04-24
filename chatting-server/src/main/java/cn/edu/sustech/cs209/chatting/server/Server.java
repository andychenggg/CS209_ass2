package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Client;
import cn.edu.sustech.cs209.chatting.common.HugeMessage;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.NewClientOnlineMessage;
import cn.edu.sustech.cs209.chatting.common.Protocols;
import cn.edu.sustech.cs209.chatting.server.SafetyDataStruture.LockList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;


public class Server {

    private static final Map<String, LockList> lockLists = new HashMap<>();
    private static List<Client> clients = new ArrayList<>();

    private static ConcurrentHashMap<String, String> userPass;
    private static ConcurrentHashMap<String, Client> userClient;

    private static CopyOnWriteArraySet<CopyOnWriteArraySet<String>> groups = new CopyOnWriteArraySet<>();



    //should be removed when someone is offline
    private static final ConcurrentHashMap<ObjectInputStream, ObjectOutputStream> IsOs =
        new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ObjectOutputStream> userOs = new ConcurrentHashMap<>();
//    private static final ConcurrentHashMap<String, Boolean> hasBeenOff = new ConcurrentHashMap<>();



    // Not allow to instantiate a server
    private Server() {

    }

    private static void readUserPassFromFile(){
        try (ObjectInputStream inputStream = new ObjectInputStream(
            Files.newInputStream(Paths.get("./UsePassMap/User_Passwords")))) {
            userPass = (ConcurrentHashMap<String, String>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static void readUserClientFromFile()
        throws IOException, ClassNotFoundException {
        try (
            FileInputStream fis = new FileInputStream("./Client/User_Clients");
            ObjectInputStream is = new ObjectInputStream(fis)) {
            userClient = (ConcurrentHashMap<String, Client>) is.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeUserPassToFile() {
        try (
            FileOutputStream fos = new FileOutputStream("./UsePassMap/User_Passwords");
            ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(userPass);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void writeUserClientToFile() {
        try (
            FileOutputStream fos = new FileOutputStream("./Client/User_Clients");
            ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(userClient);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        File file = new File("chatting-common/src/main/java/cn/edu/sustech/cs209/chatting/common/image/Avatar1.png");
//        System.out.println(file.exists());
//
//        userClient = new ConcurrentHashMap<>();
//        userPass = new ConcurrentHashMap<>();
//        userPass.put("111", "111");
//        userPass.put("222", "222");
//        userPass.put("333", "333");
//
//        userClient.put("111", new Client("111",
//            "Avatar1.png"));
//        userClient.put("222", new Client("222",
//            "Avatar2.png"));
//        userClient.put("333", new Client("333",
//            "Avatar3.png"));
//        writeUserPassToFile();
//        writeUserClientToFile();
//        System.exit(0);

        readUserPassFromFile();
        readUserClientFromFile();
        userClient.values().forEach(e -> e.setLogIn(false));
        userClient.forEach((e1, e2) -> System.out.println(e1+" "+e2));
        userPass.forEach((e1, e2) -> System.out.println(e1+":"+e2));
        System.out.println("Starting server");
        try (ServerSocket serverSocket = new ServerSocket(6666)) {
            while (true) {
                Socket socket = serverSocket.accept();

                Thread t = new Thread(new ServerThreadIn(socket));
                t.start();
            }
        } catch (IOException | SecurityException e) {
            System.err.println("Fail to create a ServerSocket: " + e.getMessage());
        } finally {
            writeUserPassToFile();
            writeUserClientToFile();
        }

    }

    public static void putInIsOs(ObjectInputStream is, ObjectOutputStream os){
        IsOs.put(is, os);
    }
    public static void removeIsOs(ObjectInputStream is){
        IsOs.remove(is);
    }
    public static void putInUserOs(String username, ObjectOutputStream os){
        userOs.put(username, os);
    }
    public static void removeUserOs(String username){
        userOs.remove(username);
    }

    // From ServerThreadIn to check whether username match the password
    public static boolean CheckPassword(String username, String password) {
        System.out.println(username + " " + password);
        return userPass.containsKey(username) && userPass.get(username).equals(password);
    }

    // To sign up an account
    public static boolean CreateUser(String username, String password) {
        System.out.println("username: "+username);
        if(userPass.containsKey(username)){
            return false;
        }
        userPass.put(username, password);
        writeUserPassToFile();
        userClient.put(username, new Client(username));
        writeUserClientToFile();
        return true;
    }


    public static void write(ObjectInputStream is, HugeMessage h) {
        try {
            IsOs.get(is).writeObject(h);
            IsOs.get(is).flush();
        } catch (IOException e) {
            System.out.println(is+" is closed");
            try {
                IsOs.remove(is);
                is.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void write(Client user, HugeMessage h) {
        try {
            userOs.get(user.getUserName()).writeObject(h);
            userOs.get(user.getUserName()).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Set<Client> getClients(Set<String> usernames){
        return usernames.stream().map(e -> userClient.get(e)).collect(Collectors.toSet());
    }

    public static void write(String user, HugeMessage h) {
        try {
            if(userOs.containsKey(user)){
                userOs.get(user).writeObject(h);
                userOs.get(user).flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToAll(HugeMessage h) {

        IsOs.forEach((s, os) -> {
            try {
                os.writeObject(h);
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public static void notifyNewClient(String User, boolean On){
        userClient.get(User).setLogIn(On);
        ConcurrentHashMap<String, Client> online = new ConcurrentHashMap<>();
        userClient.forEach((u, v) -> {
            if(v.getLogIn()){
                online.put(u, v);
            }
        });
        System.out.println("newuser "+User+": "+userClient.get(User).getLogIn());
        writeToAll(new NewClientOnlineMessage(Protocols.NEW_ONLINE_CLIENT, online));
    }

    public static void sentMessageInAGroup(Message msg, Set<String> sentTo){
        sentTo.forEach(e -> {
            try {
                if(userOs.containsKey(e)){
                    userOs.get(e).writeObject(msg);
                    userOs.get(e).flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static ConcurrentHashMap<String, Client> getUserClient() {
        return userClient;
    }
}
