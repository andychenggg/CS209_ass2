package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class LoginSignUpMessage extends HugeMessage implements Serializable {
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    private String username;
    private String password;

    public LoginSignUpMessage(Protocols protocol, String username, String password) {
        super(protocol);
        this.username = username;
        this.password = password;
    }



}
