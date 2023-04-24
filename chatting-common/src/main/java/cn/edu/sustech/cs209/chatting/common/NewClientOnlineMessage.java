package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class NewClientOnlineMessage extends HugeMessage implements Serializable {
    private final ConcurrentHashMap<String, Client> userClient;
    public NewClientOnlineMessage(Protocols protocols, ConcurrentHashMap<String, Client> userClient){
        super(protocols);
        this.userClient = userClient;
    }

    public ConcurrentHashMap<String, Client> getUserClient() {
        return userClient;
    }
}
