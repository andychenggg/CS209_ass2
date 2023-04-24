package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class HugeMessage implements Serializable {
  protected Protocols protocol;

  protected HugeMessage(Protocols protocol) {
    this.protocol = protocol;
  }

  protected HugeMessage() {
    protocol = Protocols.NULL;
  }

  public Protocols getProtocol() {
    return protocol;
  }

}
