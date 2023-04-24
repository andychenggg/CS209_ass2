package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public enum Protocols implements Serializable {
    NULL,
    CONNECT_SERVER,
    LOGIN,
    SIGNUP,
    NEW_ONLINE_CLIENT,
    OFFLINE,
    CREATE_GROUP,
    MESSAGE,
    QUIT_GROUP,
    SAVE_MESSAGE,
    FILE
}
