package main.connection;

import main.node.InternalNode;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Created by Davide on 25/02/2018.
 */
public class InternalFileConnection extends FileConnection{

    /**
     * Unique constructor for the class
     * @param n needed to perform file operations
     * @param nodeAd contact node
     */
    public InternalFileConnection(InternalNode n, InetSocketAddress nodeAd){
        super(n, nodeAd);
    }

    /**
     * Method used to get a file form an internal node. It wraps the superclass getFileRequest() method
     * @param fileName is the desired file name
     * @return true if the operation is performed correctly
     */
    public boolean getFileRequest(String fileName){
        return super.getFileRequest(fileName);
    }

    /**
     * Method used to request a set of files whose id belongs in a given interval
     * @param from beginning of the interval
     * @param to end of the interval
     * @return true if the operation is performed correctly
     */
    public ArrayList<String> fileIntervalRequest(BigInteger from, BigInteger to){

        //invalid arguments
        if(from == null || to == null)
            return null;

        ArrayList<String> retVal = null;
        try {
            startConnection();
            //send GET_FILE_INTERVAL command with parameters
            oos.writeObject("GET_FILE_INTERVAL");
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

    /**
     * Method used to insert a file into an internal node. It wraps the superclass insertFileRequest() method
     * @param fileName is the file name
     * @return true if the operation is performed correctly
     */
    public boolean insertFileRequest(String fileName){
        return super.insertFileRequest(fileName);
    }

    /**
     * Method used to remove a file form an internal node. It wraps the superclass deleteFileRequest() method
     * @param fileName is the file name
     * @return true if the operation is performed correctly
     */
    public boolean deleteFileRequest(String fileName){
        return super.deleteFileRequest(fileName);
    }

    /**
     * Method used to request multiple file deletion from the contact node.
     * Files to be removed has id which belongs to a given interval
     * @param from beginning of the interval
     * @param to end of the interval
     * @return true if the operation is performed correctly
     */
    public boolean deleteFilesRequest(BigInteger from, BigInteger to){

        //invalid arguments
        if(from == null || to == null){
            return false;
        }

        boolean retVal = false;

        try {
            startConnection();
            //send DELETE_REPLICAS command with parameters
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

    /**
     * Method used to request a node if it has a file
     * @param fileName searched file
     * @return true if the contact has the specified file, false otherwise
     */
    public boolean hasFileRequest(String fileName){

        //invalid arguments
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
