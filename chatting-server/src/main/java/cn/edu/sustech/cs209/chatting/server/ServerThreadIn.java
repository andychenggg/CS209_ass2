package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Client;
import cn.edu.sustech.cs209.chatting.common.CreateGroupMessage;
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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ServerThreadIn implements Runnable {
  private ObjectInputStream is;
  private ObjectOutputStream os;
  private final Socket socket;

  ServerThreadIn(Socket socket) throws IOException {
    this.socket = socket;
  }

  // read the message
  // judge the message
  //
  @Override
  public void run() {
    try {
      is = new ObjectInputStream(socket.getInputStream());
      os = new ObjectOutputStream(socket.getOutputStream());
      Server.putInIsOs(is, os);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    while (true) {
      try {
        HugeMessage hugeMessage = (HugeMessage) is.readObject();
        if (hugeMessage == null) {
          continue;
        }
        if (hugeMessage instanceof CreateGroupMessage) {
          CreateGroupMessage cgm = (CreateGroupMessage) hugeMessage;
          cgm.getClients().forEach(s -> {
            System.out.println("CreateGroupMessage: " + s.getUserName());
          });
        }
        switch (hugeMessage.getProtocol()) {
          case LOGIN: {
            if (hugeMessage instanceof LoginSignUpMessage) {
              LoginSignUpMessage l = (LoginSignUpMessage) hugeMessage;

              if (Server.CheckPassword(l.getUsername(), l.getPassword())) {
                // put in userOs
                Server.putInUserOs(l.getUsername(), os);
                // return successful response
                Server.write(is, new LoginSignupResponse(Protocols.LOGIN, States.SUCCESSFUL,
                    Server.getUserClient().get(l.getUsername())));
                Server.notifyNewClient(l.getUsername(), true);
              } else {
                Server.write(is, new LoginSignupResponse(Protocols.LOGIN, States.FAILED, null));
              }
            }
            break;
          }
          case SIGNUP: {
            if (hugeMessage instanceof LoginSignUpMessage) {
              LoginSignUpMessage l = (LoginSignUpMessage) hugeMessage;

              if (Server.CreateUser(l.getUsername(), l.getPassword())) {
                // put in userOs
                Server.putInUserOs(l.getUsername(), os);
                // return successful response
                Server.write(is, new LoginSignupResponse(Protocols.SIGNUP, States.SUCCESSFUL,
                    Server.getUserClient().get(l.getUsername())));
                Server.notifyNewClient(l.getUsername(), true);
              } else {
                Server.write(is, new LoginSignupResponse(Protocols.SIGNUP, States.FAILED, null));
              }
            }
            break;
          }
          case OFFLINE: {
            if (hugeMessage instanceof OfflineMessage) {
              OfflineMessage l = (OfflineMessage) hugeMessage;
              Set<Set<String>> sets = l.getGroups();
              String off = l.getUsername();
              sets.forEach(s -> {
                s.forEach(t -> {
                    if (!t.equals(off)) {
                        Server.write(t, new QuitMessage(Protocols.QUIT_GROUP, off, s));
                    }
                });
              });
              System.out.println("notifyClients");
              Server.notifyNewClient(l.getUsername(), false);
              Server.write(is, new OfflineMessage(Protocols.OFFLINE, null, null));
              Server.removeUserOs(off);
            }
            break;
          }
          case MESSAGE:
          case FILE: {
            if (hugeMessage instanceof Message) {
              Message l = (Message) hugeMessage;
              Server.sentMessageInAGroup(l, l.getSendTo());
            }
            break;
          }
          case CREATE_GROUP: {
            if (hugeMessage instanceof CreateGroupMessage) {
              CreateGroupMessage cgm = (CreateGroupMessage) hugeMessage;
              System.out.println("CREATE_GROUP: " + cgm.getNames());
              cgm.getClients().forEach(s -> {
                System.out.println("CREATE_GROUP: " + s.getUserName());
                Set<Client> cls = new CopyOnWriteArraySet<>(cgm.getClients());
                cls.removeIf(t -> t.getUserName().equals(s.getUserName()));
                cls.add(cgm.getInitiator());
                Server.write(s,
                    new CreateGroupMessage(Protocols.CREATE_GROUP, cls, cgm.getInitiator()));
              });
//                            cgm.getNames().forEach(s -> {
//                                Set<String> us = new HashSet<>(cgm.getNames());
//                                us.removeIf(e->e.equals(s));
//                                us.add(cgm.getInitiator().getUserName());
//
//                            });
            }
            break;
          }
        }
      } catch (IOException | ClassNotFoundException e) {
        System.out.println(e.getMessage());
        Server.removeIsOs(is);
        break;
      }
    }
  }
}
