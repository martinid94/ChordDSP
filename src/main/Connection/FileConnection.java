package main.Connection;

import main.ExternalNode;
import main.Node;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Marco on 17/03/2018.
 */
public class FileConnection extends Connection {

    private Node node;


    public FileConnection(Node n, InetSocketAddress nodeAd){
        super(nodeAd);
        node = n;
    }

    public boolean getFileRequest(String fileName) {

        if(fileName == null || fileName.equals(""))
            return false;

        boolean retVal = false;

        try {
            startConnection();
            oos.writeObject("GET_FILE");
            oos.writeObject(fileName);
            oos.flush();

            boolean hasFile = ois.readBoolean();
            if(!hasFile) {
                return false;
            }
            else {
                retVal = node.singleInsert(s, fileName);
            }
        } catch (IOException e) {
            retVal = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        return retVal;
    }

    public boolean insertFileRequest(String fileName){
        if(fileName == null || fileName.equals(""))
            return false;

        boolean retVal = false;
        try {
            startConnection();
            if(node instanceof ExternalNode){
                oos.writeObject("INSERT_FILE"); //local insert plus replica
            }
            else{
                oos.writeObject("INSERT_REPLICA"); //single insert request
            }

            oos.writeObject(fileName);
            oos.flush();

            node.get(s, fileName);
            retVal = ois.readBoolean();
        } catch (IOException e) {
//            System.out.println("ok \n");
//            e.printStackTrace();
            retVal = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        return retVal;
    }

    public boolean deleteFileRequest(String fileName){
        if(fileName == null || fileName.equals(""))
            return false;

        boolean retVal = false;
        try {
            startConnection();
            if(node instanceof ExternalNode)
                oos.writeObject("DELETE_FILE"); //replica removal needed
            else
                oos.writeObject("DELETE_REPLICA"); //single local removal
            oos.writeObject(fileName);
            oos.flush();

            retVal = ois.readBoolean();
        } catch (IOException e) {
            retVal = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        return retVal;
    }
}
