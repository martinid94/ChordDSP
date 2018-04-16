package main.connection;

import main.node.Node;

import java.net.InetSocketAddress;

/**
 * This class provides all the methods that an external node (a host external form the Chord ring) may call in order to
 * download, upload or delete a file stored in the Chord ring.
 *
 *
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class ExternalFileConnection extends FileConnection {

    private RingConnection rc;

    /**
     * Unique constructor of the class
     * @param n It is the reference of the node that requests file operations.
     * @param node The address to contact the node
     */
    public ExternalFileConnection(Node n, InetSocketAddress node){
        super(n, node);
        rc = new RingConnection(node);
    }

    /**
     * This method is called to get a file form an internal node. It wraps the superclass getFileRequest() method
     * @param fileName It is the desired file name
     * @return True if the operation is performed correctly
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
     * This method is called to insert a file form a node external to the ring. It wraps the superclass insertFileRequest() method
     * @param fileName It is the name of the file be inserted
     * @return True if the operation is performed correctly
     */
    public boolean insertFileRequest(String fileName){
        return super.insertFileRequest(fileName);
    }

    /**
     * This method is called to delete a file stored in an internal node. It wraps the superclass deleteFileRequest() method
     * @param fileName It is the name of the file to be deleted
     * @return True if the operation is performed correctly
     */
    public boolean deleteFileRequest(String fileName){
        return super.deleteFileRequest(fileName);
    }

    /**
     * This method is called to request an node if it has a file
     * @param fileName It is the desired file name
     * @return True if the contact has the specified file, false otherwise
     */
    public boolean hasFileRequest(String fileName){
        return super.hasFileRequest(fileName);
    }
}
