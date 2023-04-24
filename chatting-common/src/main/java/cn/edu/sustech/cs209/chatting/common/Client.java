package cn.edu.sustech.cs209.chatting.common;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

public class Client implements Serializable {
  private String userName;

  private byte[] bytes;
  private boolean isLogIn;
  private transient String relativePath =
      "chatting-common/src/main/java/cn/edu/sustech/cs209/chatting/common/image/";

//    public static void main(String[] args) throws IOException, URISyntaxException {
//        Client c = new Client("111");
//
//        // transfer the
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        File file = new File(c.relativePath+"Avatar1.png");
//        BufferedImage image = ImageIO.read(file);
//        ImageIO.write(image, "png", baos);
//        baos.flush();
//        c.bytes = baos.toByteArray();
//        baos.close();
//
//
//    }

  public Client(String userName) {
    this.userName = userName;

    // transfer the
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    File file = null;
    try {
      file = new File(relativePath + "AvatarDefault.png");
      BufferedImage image = ImageIO.read(file);
      ImageIO.write(image, "png", baos);
      baos.flush();
      bytes = baos.toByteArray();
      baos.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Client(String userName, String imageName) {
    this.userName = userName;

    // transfer the
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    File file = null;
    try {
      System.out.println();
      file = new File(relativePath + imageName);
      BufferedImage image = ImageIO.read(file);
      ImageIO.write(image, "png", baos);
      baos.flush();
      bytes = baos.toByteArray();
      baos.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }


  public void setLogIn(boolean logIn) {
    isLogIn = logIn;
  }

  public boolean getLogIn() {
    return isLogIn;
  }

  public String getUserName() {
    return userName;
  }

  public Image getAvatar() {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    try {
      BufferedImage bufferedImage = ImageIO.read(bais);
      return SwingFXUtils.toFXImage(bufferedImage, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
