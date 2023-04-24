package cn.edu.sustech.cs209.chatting.common;

import cn.edu.sustech.cs209.chatting.common.HugeMessage;
import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MessageRecord implements Serializable {
  private String userName;
  private ConcurrentHashMap<Set<String>, List<HugeMessage>> Record = new ConcurrentHashMap<>();

  public MessageRecord(String userName) {
    this.userName = userName;
  }

  public ConcurrentHashMap<Set<String>, List<HugeMessage>> getRecord() {
    return Record;
  }
}
