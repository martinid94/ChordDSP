package main.node;

import java.net.Socket;

/**
 * This class represents a node which is external from the Chord ring.
 * It can preform the upload and the download of a file.
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class ExternalNode implements Node {
    private String path;

    /**
     * Unique constructor of the class
     * @param path It specifies the path where to find the file to upload or to put the file to be downloaded
     */
    public ExternalNode(String path){
        this.path = path;
    }

    /**
     * This method is called to perform the physical download of a file
     * @param s It is the socket from which download the file
     * @param fileName It is the name of the file to be downloaded
     * @return True if the operation is performed correctly
     */
    public boolean singleInsert(Socket s, String fileName){
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }
        FileManager fm = new FileManager(path + fileName);
        return fm.write(s);
    }

    /**
     * This method is called to perform the physical upload of a file
     * @param s It is the socket through which upload the file
     * @param fileName It is the name of the file to be uploaded
     * @return True if the operation is performed correctly
     */
    public boolean get(Socket s, String fileName){
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm = new FileManager(path + fileName);

        if(!fm.exists()){
            return false;
        }
        return fm.read(s);
    }

}
