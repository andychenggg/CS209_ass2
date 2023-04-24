package cn.edu.sustech.cs209.chatting.server.SafetyDataStruture;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockList implements Serializable {


    private final String identifier;


    private final List<Message> messages;
    private transient Lock reentrantLock;
    LockList(String identifier){
        messages = new ArrayList<>();
        reentrantLock = new ReentrantLock();
        this.identifier = identifier;
    }
    public void add(Message ms){
        reentrantLock.lock();
        messages.add(ms);
        reentrantLock.unlock();
    }

    public void newLock(){
        reentrantLock = new ReentrantLock();
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<Message> getMessages() {
        return messages;
    }

    //test main
    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        LockList ls = new LockList("1__2");
//        for (int i = 0; i < 5; i++) {
//            ls.add(new Message(1L, "client1", "client2", "hello"));
//        }
//        ObjectOutputStream os = new ObjectOutputStream(
//            Files.newOutputStream(Paths.get("./LockList/client1client2.txt")));
//        os.writeObject(ls);
//
//        ObjectInputStream is = new ObjectInputStream(Files.newInputStream(Paths.get("./LockList/client1client2.txt")));
//        LockList lt = (LockList) is.readObject();
//        System.out.println(lt.reentrantLock);
//        System.out.println(lt.messages);
    }

//    private void writeObject(ObjectOutputStream os) throws IOException {
//        os.defaultWriteObject();
//        os.writeInt(messages.size());
//        for(Message m: messages){
//            os.writeObject(messages);
//        }
//    }
//    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
//        is.defaultReadObject();
//        int size = is.readInt();
//        messages = new ArrayList<>();
//        reentrantLock = new ReentrantLock();
//        for (int i = 0; i < size; i++) {
//            messages.add((Message) is.readObject());
//        }
//    }
}
