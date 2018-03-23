package main.connection;

import main.node.Node;

import java.net.InetSocketAddress;

/**
 * Created by Marco on 15/03/2018.
 */
public class ExternalFileConnection extends FileConnection {

    private RingConnection rc;

    /**
     * Unique construstor of the class
     * @param n needed to perform file operations
     * @param node contact node
     */
    public ExternalFileConnection(Node n, InetSocketAddress node){
        super(n, node);
        rc = new RingConnection(node);
    }

    /**
     * Method used to get a file form an internal node. It wraps the superclass getFileRequest() method
     * @param fileName is the desired file name
     * @return true if the operation is performed correctly
     */
    public boolean getFileRequest(String fileName) {
        boolean value = super.getFileRequest(fileName);

        //if the original file is not available, look for the replica
        if(!value){
            nodeAddress = rc.addressRequest("GET_PRED");
            value = super.getFileRequest(fileName);
        }

        return value;
    }

    /**
     * Method used to insert a file form a node external to the ring. It wraps the superclass insertFileRequest() method
     * @param fileName is the file name to be inserted
     * @return true if the operation is performed correctly
     */
    public boolean insertFileRequest(String fileName){
        return super.insertFileRequest(fileName);
    }

    /**
     * Method used to delete a file stored in an internal node. It wraps the superclass deleteFileRequest() method
     * @param fileName is the file name to be deleted
     * @return true if the operation is performed correctly
     */
    public boolean deleteFileRequest(String fileName){
        return super.deleteFileRequest(fileName);
    }
}
