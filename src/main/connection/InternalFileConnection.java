package main.connection;

import main.node.InternalNode;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * This class provides all the methods that an node belonging to the Chord ring may call in order to
 * download, upload and delete one or more files stored in the Chord ring.
 *
 *
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class InternalFileConnection extends FileConnection{

    /**
     * Unique constructor for the class
     * @param n It is the reference of the node that requests file operations.
     * @param nodeAd The address to contact the node
     */
    public InternalFileConnection(InternalNode n, InetSocketAddress nodeAd){
        super(n, nodeAd);
    }

    /**
     * This method is called to get a file form an internal node. It wraps the superclass getFileRequest() method
     * @param fileName It is the desired file name
     * @return True if the operation is performed correctly
     */
    public boolean getFileRequest(String fileName){
        return super.getFileRequest(fileName);
    }

    /**
     * This method is called to request a set of files whose id belongs to a given interval
     * @param from It represents the beginning of the interval
     * @param to It represents the end of the interval
     * @return True if the operation is performed correctly
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return retVal;
    }

    /**
     * This method is called to insert a file into an internal node. It wraps the superclass insertFileRequest() method
     * @param fileName It is the name of the file to be inserted
     * @return True if the operation is performed correctly
     */
    public boolean insertFileRequest(String fileName){
        return super.insertFileRequest(fileName);
    }

    /**
     * This method is called to remove a file form an internal node. It wraps the superclass deleteFileRequest() method
     * @param fileName It is the name of the file to be deleted
     * @return True if the operation is performed correctly
     */
    public boolean deleteFileRequest(String fileName){
        return super.deleteFileRequest(fileName);
    }

    /**
     * This method is called to request multiple file deletion from the contact node.
     * Files to be removed have ids which belong to a given interval
     * @param from It represents the beginning of the interval
     * @param to It represents the end of the interval
     * @return True if the operation is performed correctly
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return retVal;
    }
}
