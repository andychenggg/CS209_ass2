package cn.edu.sustech.cs209.chatting.common;

import java.util.Set;

public class OfflineMessage extends HugeMessage{
    private final String username;
    private final Set<Set<String>> groups;
    public OfflineMessage(Protocols protocols, String username, Set<Set<String>> groups){
        super(protocols);
        this.username = username;
        this.groups = groups;
    }

    public String getUsername() {
        return username;
    }
    public Set<Set<String>> getGroups(){
        return groups;
    }
}
