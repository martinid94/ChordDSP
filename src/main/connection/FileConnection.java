package main.connection;

import main.node.ExternalNode;
import main.node.Node;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * This class provides all the methods that both internal nodes (those who belong to the Chord ring) and
 * external ones may call in order to request several file operations. These include the download, the upload
 * and the deletion of files stored in the Chord ring.
 *
 *
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class FileConnection extends Connection {

    private Node node;

    /**
     * Unique constructor of the class
     * @param n It is the reference of the node that requests file operations.
     * @param nodeAd The address to contact the node
     */
    public FileConnection(Node n, InetSocketAddress nodeAd){
        super(nodeAd);
        node = n;
    }

    /**
     * This method is called to request a get file operation from a specific node.
     * The contact node sends back the file which is downloaded.
     * @param fileName It is the desired file name
     * @return True if the whole operation has been performed correctly
     */
    public boolean getFileRequest(String fileName) {

        //invalid arguments
        if(fileName == null || fileName.equals(""))
            return false;

        boolean retVal = false;
        try {
            startConnection();
            //send GET_FILE command
            oos.writeObject("GET_FILE");
            oos.writeObject(fileName);
            oos.flush();

            //control if the contact node has the file
            boolean hasFile = ois.readBoolean();
            if(!hasFile) {
                return false;
            }
            else {
                //perform the download
                retVal = node.singleInsert(s, fileName);
            }
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

    /**
     * This method is called to request an insert file operation into a specific node.
     * It distinguishes whether to perform a single insert or to make the contact know that it
     * has to send the replica to the correct node
     * @param fileName It is the file name to be inserted
     * @return True if the whole operation is performed correctly
     */
    public boolean insertFileRequest(String fileName){

        //invalid arguments
        if(fileName == null || fileName.equals(""))
            return false;

        boolean retVal = false;
        try {
            startConnection();
            if(node instanceof ExternalNode){
                //send INSERT_FILE command to perform file insert in the contact node
                //and make it know it has to send the replica to the correct node
                oos.writeObject("INSERT_FILE");
            }
            else{
                //otherwise perform single insert in the contact node
                oos.writeObject("INSERT_REPLICA");
            }

            oos.writeObject(fileName);
            oos.flush();

            //upload the file from local node
            retVal = node.get(s, fileName);
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

    /**
     * This method is called to request the deletion of a file which is stored in a specific internal node
     * It distinguishes whether to perform a single delete or to make the contact know that it
     * has to delete also the replica from the correct node
     * @param fileName It is the name of the file to be deleted
     * @return True if the contact node performs it
     */
    public boolean deleteFileRequest(String fileName){

        //invalid arguments
        if(fileName == null || fileName.equals(""))
            return false;

        boolean retVal = false;
        try {
            startConnection();
            if(node instanceof ExternalNode) {
                //send DELETE_FILE command to perform file deletion in the contact node
                //and make it know it has to remove the replica from the correct node
                oos.writeObject("DELETE_FILE");
            }
            else {
                //otherwise perform single removal in the contact node
                oos.writeObject("DELETE_REPLICA");
            }
            oos.writeObject(fileName);
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

    /**
     * This method is called to request an node if it has a file
     * @param fileName It is the desired file name
     * @return True if the contact has the specified file, false otherwise
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

            result = (Boolean) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            result = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
