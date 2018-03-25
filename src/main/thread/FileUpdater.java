package main.thread;

import main.connection.InternalFileConnection;
import main.connection.RingConnection;
import main.node.InternalNode;
import main.Util;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * This class represents a thread that aims to deal with file exchange after a Chord ring instability (i.e. a join,
 * leave or crash of a node occurs). It is activated explicitly during the join() operation and when particular
 * conditions are met in setPredecessor() and setSuccessor() methods. In particular, it is responsible to download
 * and delete file replicas in order to keep consistency in the Chord ring
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class FileUpdater extends Thread {

    private InetSocketAddress contactAddr;
    private InternalNode internalNode;
    private BigInteger from;
    private BigInteger to;
    private boolean isJoin;

    /**
     * Unique constructor of the class
     * @param contact The address of the node to be contacted
     * @param internalNode A reference to the invoker node (needed to file management)
     * @param from The beginning of the file interval
     * @param to The end of the file interval
     * @param isJoin It specifies if the thread is called during a join operation or not
     */
    public FileUpdater(InetSocketAddress contact, InternalNode internalNode, BigInteger from, BigInteger to, boolean isJoin){
        contactAddr = contact;
        this.internalNode = internalNode;
        this.from =from;
        this.to = to;
        this.isJoin = isJoin;
    }

    @Override
    public void run() {
        //download all the files from contact node that belong to the interval (from to)
        if(!update()){
            //something bad happened, retry
            update();
        }

        //if it's a join operation, files must be deleted from predecessor and successor to keep consistency
        if(isJoin){
            RingConnection rc = new RingConnection(contactAddr);
            if(rc.getAndSetAvailabilityRequest(true) == null){
                //bad connection, retry
                rc.getAndSetAvailabilityRequest(true);
            }

            internalNode.getAndSetJoinAvailable(true);

            InternalFileConnection fc = new InternalFileConnection(internalNode, internalNode.getSuccAddress());
            if(!fc.deleteFilesRequest(Util.hashAddress(internalNode.getPredAddress()), internalNode.getLocalId())){
                //something bad happened, retry
                fc.deleteFilesRequest(Util.hashAddress(internalNode.getPredAddress()), internalNode.getLocalId());
            }

            fc = new InternalFileConnection(internalNode, internalNode.getPredAddress());
            if(!fc.deleteFilesRequest(internalNode.getLocalId(), Util.hashAddress(internalNode.getSuccAddress()))){
                //something bad happened, retry
                fc.deleteFilesRequest(internalNode.getLocalId(), Util.hashAddress(internalNode.getSuccAddress()));
            }
        }
        else{
            internalNode.getAndSetJoinAvailable(true);
        }

    }

    private boolean update(){

        InternalFileConnection fc = new InternalFileConnection(internalNode, contactAddr);
        ArrayList<String> fileToGet = fc.fileIntervalRequest(from, to);

        if(fileToGet == null){
            return false;
        }

        for(String s : fileToGet){
            if(!fc.getFileRequest(s)){
                fc.getFileRequest(s);
            }
        }

        return true;
    }

}
