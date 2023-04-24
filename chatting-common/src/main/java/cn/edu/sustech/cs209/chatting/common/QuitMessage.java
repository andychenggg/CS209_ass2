package cn.edu.sustech.cs209.chatting.common;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class QuitMessage extends HugeMessage{
    private final String userName;
    private final Set<String> currentGroups;
    public QuitMessage(Protocols protocol, String u, Set<String> c){
        super(protocol);
        userName = u;
        currentGroups = c;
    }
    public String getUserName(){
        return userName;
    }
    public Set<String> getCurrentGroups(){
        return currentGroups;
    }
}
