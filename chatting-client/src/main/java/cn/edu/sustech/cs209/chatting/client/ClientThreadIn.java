package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.CreateGroupMessage;
import cn.edu.sustech.cs209.chatting.common.HugeMessage;
import cn.edu.sustech.cs209.chatting.common.LoginSignUpMessage;
import cn.edu.sustech.cs209.chatting.common.LoginSignupResponse;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.NewClientOnlineMessage;
import cn.edu.sustech.cs209.chatting.common.Protocols;
import cn.edu.sustech.cs209.chatting.common.QuitMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import javafx.application.Platform;

public class ClientThreadIn implements Runnable{
    Socket socket;
    ObjectInputStream is;
    ClientThreadIn(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try {
            is = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tab:
        while(!ClientApplication.Over){
            System.out.println("ClientApplication.Over:"+ClientApplication.Over);
            try {
                HugeMessage hugeMessage = (HugeMessage) is.readObject();
                if(hugeMessage == null) {
                    continue;
                }
                switch (hugeMessage.getProtocol()){
                    case LOGIN:
                    case SIGNUP: {
                        if(hugeMessage instanceof LoginSignupResponse){
                            ClientApplication.setLoginSignupResponse((LoginSignupResponse) hugeMessage);
                        }
                        break;
                    }
                    case NEW_ONLINE_CLIENT:{
                        if(hugeMessage instanceof NewClientOnlineMessage){
                            ((NewClientOnlineMessage) hugeMessage).getUserClient().forEach((u, v) -> {
                                System.out.println("NEW_ONLINE_CLIENT:"+u+":"+v.getLogIn());
                            });
                            ClientApplication.updateClientList(((NewClientOnlineMessage) hugeMessage).getUserClient().values());
                        }
                        break;
                    }
                    case OFFLINE:{
                        break tab;
                    }
                    case MESSAGE:
                    case FILE:{
                        if(hugeMessage instanceof Message){
                            System.out.println("Receive hm in Client ThreadIn"+ClientApplication.getController());
                            System.out.println(((Message) hugeMessage).getSendTo());
                            ClientApplication.getController().addRecent(((Message) hugeMessage).getSentBy());
                            ClientApplication.getController().putClientAhead(Collections.singletonList(((Message) hugeMessage).getSentBy()));
                            ClientApplication.getController().receiveMessage(hugeMessage);
                        }
                        break;
                    }
                    case CREATE_GROUP:{
                        if(hugeMessage instanceof CreateGroupMessage){
                            System.out.print("Receive hm in Client ThreadIn: ");
                            System.out.println(((CreateGroupMessage) hugeMessage).getClients());
                            ClientApplication.getController().showChatStage(((CreateGroupMessage) hugeMessage).getClients());
                        }
                        break;
                    }
                    case QUIT_GROUP:{
                        if(hugeMessage instanceof QuitMessage){
                            QuitMessage q = (QuitMessage) hugeMessage;
                            ClientApplication.getController().getChatCTR(q.getCurrentGroups()).removeAvatar(q.getUserName());
                            // give a warning
                            ClientApplication.getController().showWarningInController("User "+q.getUserName()+" has been offline. ");
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                try {
                    ClientApplication.closeOs();
                    Platform.runLater(() -> {
                        ClientApplication.getController().showWrongMessageInController("Server has been shut down for unknown reason!\n Application will be closed in 3 seconds.");
                        new Thread(() -> {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            System.exit(-1);
                        }).start();
                    });
                    is.close();
                    socket.close();
                } catch (IOException  ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }
        try {
            is.close();
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
