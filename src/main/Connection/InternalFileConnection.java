package main.Connection;

import main.InternalNode;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Created by Davide on 25/02/2018.
 */
public class InternalFileConnection extends FileConnection{


    public InternalFileConnection(InternalNode n, InetSocketAddress nodeAd){
        super(n, nodeAd);
    }

    public boolean getFileRequest(String fileName) {
        return super.getFileRequest(fileName);
    }

    public ArrayList<String> fileIntervalRequest(BigInteger from, BigInteger to){
        if(from == null || to == null)
            return null;

        ArrayList<String> retVal = null;
        try {
            startConnection();
            oos.writeObject("GET_FILE_INTERVAL"); // insert flush here?
            oos.writeObject(from);
            oos.writeObject(to);
            oos.flush();
            retVal = (ArrayList<String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            retVal = null;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        return retVal;
    }

    public boolean insertFileRequest(String fileName){
        return super.insertFileRequest(fileName);
    }

    public boolean deleteFileRequest(String fileName){
        return super.deleteFileRequest(fileName);
    }

    public boolean deleteFilesRequest(BigInteger from, BigInteger to){
        if(from == null || to == null){
            return false;
        }

        boolean retVal = false;

        try {
            startConnection();
            oos.writeObject("DELETE_REPLICAS");
            oos.writeObject(from);
            oos.writeObject(to);
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

    public boolean hasFileRequest(String fileName){
        if(fileName == null || fileName.equals(""))
           return false;

        boolean result = false;
        try {
            startConnection();
            oos.writeObject("HAS_FILE");
            oos.writeObject(fileName);
            oos.flush();

            result = ois.readBoolean();
        } catch (IOException e) {
            result = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }


        return result;
    }
}
