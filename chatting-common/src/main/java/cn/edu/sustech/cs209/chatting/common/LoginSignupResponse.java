package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class LoginSignupResponse extends HugeMessage implements Serializable {
  private final States state;
  private final Client client;

  public LoginSignupResponse(Protocols protocols, States states, Client client) {
    super(protocols);
    state = states;
    this.client = client;
  }

  public LoginSignupResponse() {
    super();
    state = States.NULL;
    client = null;
  }

  public States getState() {
    return state;
  }

  public Client getClient() {
    return client;
  }
}
