package main;

import java.net.Socket;

/**
 * Created by Davide on 17/03/2018.
 */
public class ExternalNode implements Node {
    private String path;

    public ExternalNode(String path){
        this.path = path;
    }

    public boolean singleInsert(Socket s, String fileName){
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm = new FileManager(path + fileName);
        return fm.write(s);
    }

    public boolean get(Socket s, String fileName){
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm = new FileManager(path + fileName);
        return fm.read(s);
    }

}
