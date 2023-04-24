package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class testServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);
        while(true){
            Socket socket = serverSocket.accept();
            System.out.println("hello4");

//                InputStream inputStream = socket.getInputStream();
            System.out.println("hello5");
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            System.out.println("hello5");
        }
    }
}
