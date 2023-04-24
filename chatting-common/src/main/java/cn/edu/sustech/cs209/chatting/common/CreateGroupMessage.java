package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateGroupMessage extends HugeMessage implements Serializable {
    private Set<Client> clients;
    private Set<String> names;
    private final Client initiator;
    public CreateGroupMessage(Protocols protocols, Set<Client> clients, Client initiator){
        super(protocols);
        this.clients = clients;
        names = clients.stream().map(Client::getUserName).collect(Collectors.toSet());
        this.initiator = initiator;
    }
    public Set<Client> getClients(){
        return clients;
    }
    public Set<String> getNames(){
        return names;
    }
    public Client getInitiator(){
        return initiator;
    }
}
