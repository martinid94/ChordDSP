package main.connection;

import main.node.ExternalNode;
import main.node.Node;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Marco on 17/03/2018.
 */
public class FileConnection extends Connection {

    private Node node;

    /**
     * Unique constructor of the class
     * @param n needed to perform file operations
     * @param nodeAd address to contact the node
     */
    public FileConnection(Node n, InetSocketAddress nodeAd){
        super(nodeAd);
        node = n;
    }

    /**
     * Method used to request a get file operation to a specific node.
     * The contact node sends back the file which is downloaded.
     * @param fileName is the desired file name
     * @return true if the whole operation has been performed correctly
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
            } catch (IOException e) {}
        }

        return retVal;
    }

    /**
     * Method used to request an insert file operation into a specific node.
     * It distinguishes whether to perform a single insert or to make the contact know that it
     * has to send the replica to the correct node
     * @param fileName is the file name to be inserted
     * @return true if the whole operation is performed correctly
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
            } catch (IOException e) {}
        }

        return retVal;
    }

    /**
     * Method used to request the deletion of a file which is stored in a specific node
     * It distinguishes whether to perform a single delete or to make the contact know that it
     * has to delete also the replica from the correct node
     * @param fileName is the file name to be deleted
     * @return true if the contact node performs it
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
            } catch (IOException e) {}
        }

        return retVal;
    }
}
