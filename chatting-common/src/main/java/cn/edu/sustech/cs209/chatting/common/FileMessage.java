package cn.edu.sustech.cs209.chatting.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileMessage extends HugeMessage {
  private final List<byte[]> contents = new ArrayList<>();
  private final String fileName;

  public FileMessage(Protocols protocols, File file) {
    super(protocols);
    fileName = file.getName();
    if (!file.exists()) {
      System.out.println("File don't exist");
      return;
    }
    try (InputStream is = Files.newInputStream(file.toPath());
         BufferedInputStream bis = new BufferedInputStream(is)) {
      byte[] buffer = new byte[1024];
      int len;
      while ((len = bis.read(buffer)) > 0) {
        byte[] data = new byte[len];
        System.arraycopy(buffer, 0, data, 0, len);
        contents.add(data);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void write(String path) {
    File file = new File(path, fileName);
    try {
      boolean isCreated = file.createNewFile();
      if (isCreated) {
        System.out.println("文件创建成功");
      } else {
        System.out.println("文件已经存在");
      }
      try (BufferedOutputStream bos = new BufferedOutputStream(
          Files.newOutputStream(file.toPath()))) {
        contents.forEach(b -> {
          try {
            bos.write(b);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
