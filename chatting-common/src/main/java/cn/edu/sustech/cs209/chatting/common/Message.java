package cn.edu.sustech.cs209.chatting.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Message extends HugeMessage implements Serializable {

    private LocalDateTime timestamp;

    private String sentBy;

    private Set<String> sendTo = new CopyOnWriteArraySet<>();

    private String data;
    private final List<byte []> contents;
    private final String fileName;

    public Message(Protocols protocols, LocalDateTime timestamp, String sentBy, Set<String> sendTo, String data) {
        super(protocols);
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo.addAll(sendTo);
        // It's a String
        this.data = data;
        contents = null;
        fileName = null;

    }

    public Message(Protocols protocols, LocalDateTime timestamp, String sentBy, Set<String> sendTo, File file) {
        super(protocols);
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo.addAll(sendTo);
        this.data = null;
        // It's a file
        fileName = file.getName();
        contents = new ArrayList<>();
        if(!file.exists()){
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public Set<String> getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public String toString(){
        return timestamp+sentBy+sendTo+data;
    }
    public String getFileName(){
        return fileName;
    }

    public File write(String path){
        File file = new File(path, fileName);
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }
        try {
            boolean isCreated = file.createNewFile();
            if (isCreated) {
                System.out.println("文件创建成功");
            } else {
                System.out.println("文件已经存在");
            }
            try(BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()))){
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
        return file;
    }
}
