package cn.edu.sustech.cs209.chatting.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class testClient {
    static Socket  socket;
    static ObjectInputStream is;
    public static void main(String[] args) throws IOException {
        socket = new Socket("localhost", 6666);
        System.out.println("Connected!");
        is = new ObjectInputStream(socket.getInputStream());
        System.out.println("obj");
    }
}
