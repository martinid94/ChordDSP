package main.node;

import main.Util;
import main.connection.InternalFileConnection;
import main.connection.RingConnection;
import main.thread.*;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represents a node that belongs to the Chord ring. It provides all the methods that deal with the
 * the ring structure and file management.
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class InternalNode implements Node{

    private BigInteger localId;
    private InetSocketAddress localAddress;
    private AtomicBoolean joinAvailable;
    private InetSocketAddress predAddress;
    private InetSocketAddress oldPred;
    private FingerTable fTable;
    private Listener listener;
    private CheckPredecessor checkPred;
    private FixFinger fixFinger;
    private Stabilizer stabilizer;
    private ConcurrentHashMap<String, FileManager> files;
    private String path;

    /**
     * Unique constructor of the class
     * @param local The IP address and port number of the node
     * @param path The path where to insert and retrieve files
     */
    public InternalNode(InetSocketAddress local, String path){

        //invalid arguments
        if(local == null || path == null || path.equals("")){
            throw new IllegalArgumentException();
        }

        localAddress = local;
        fTable = new FingerTable();
        localId = Util.hashAdress(local);
        joinAvailable = new AtomicBoolean(); //used as a flag to show if the node can sustain join operation
        predAddress = null;
        oldPred = null;
        listener = new Listener(this);
        checkPred = new CheckPredecessor(this);
        fixFinger = new FixFinger(this);
        stabilizer = new Stabilizer(this);
        files = new ConcurrentHashMap<>(); //files whose responsibility relies on the current node
        this.path = path;
    }


    /**
     * This method is called to perform the join of the node to the Chord ring
     * @param bootstrapNode Address of the bootstrap node
     * @return True if the join has been performed correctly
     */
    public boolean join(InetSocketAddress bootstrapNode){

        //invalid arguments
        if(bootstrapNode == null || localAddress.equals(bootstrapNode)){
            return false;
        }

        //unable to sustain other joins
        joinAvailable.set(false);

        //find the successor
        RingConnection rc = new RingConnection(bootstrapNode);
        InetSocketAddress mySucc = rc.findSuccessorRequest(localId);

        //error in findSuccessorRequest
        if(mySucc == null){
            return false;
        }

        //contact the successor and ask for join
        rc = new RingConnection(mySucc);
        InetSocketAddress myPred = rc.joinRequest(localAddress);

        //myPred == null if there is an error (join is not available)
        if(myPred == null){
            return false;
        }

        //set successor and predecessor
        predAddress = myPred;
        oldPred = myPred;
        fTable.updateIthFinger(0, mySucc);

        //allow to process arriving requests
        listener.start();

        //try to set the successor of my predecessor
        rc = new RingConnection(myPred);
        if(rc.setSuccessorRequest(localAddress) == null){
            //something went wrong, retry
            rc.setSuccessorRequest(localAddress);
        }

        //download files and communicate the replicas removal
        new FileUpdater(mySucc, this, Util.hashAdress(myPred), Util.hashAdress(mySucc), true).start();

        //start threads that monitor the ring
        checkPred.start();
        fixFinger.start();
        stabilizer.start();

        return true;
    }

    /**
     * This method is called by the first node that joins (and thus creates) the Chord ring
     * @return True if the operation is performed correctly
     */
    public boolean bootstrapJoin(){
        //set predecessor and successor as local address
        predAddress = localAddress;
        oldPred = localAddress;
        fTable.updateIthFinger(0, localAddress);

        //now the node can sustain join operations
        joinAvailable.set(true);

        //start threads that process arriving requests and monitor the ring
        listener.start();
        checkPred.start();
        fixFinger.start();
        stabilizer.start();

        return true;
    }

    /**
     * This method is called by an internal node that wants to leave the Chord ring intentionally.
     * @return True if the operation is performed correctly
     */
    public boolean leave(){
        //connect to successor and make it refuse join operations
        RingConnection rc = new RingConnection(getSuccAddress());
        Boolean succJoinAvailable = rc.getAndSetAvailabilityRequest(false);
        if(succJoinAvailable == null){
            //bad connection, retry
            succJoinAvailable = rc.getAndSetAvailabilityRequest(false);
        }

        //again bad connection or successor is performing a join no leave is allowed due to file exchange
        if(succJoinAvailable == null || !succJoinAvailable){
            return false;
        }

        //if successor accepts leave operation but the current node is still sustaining a join, leave not allowed
        if(!joinAvailable.getAndSet(false) && succJoinAvailable){
            //reset successor joinAvailable as true
            if(rc.getAndSetAvailabilityRequest(true) == null){
                //bad connection, retry
                rc.getAndSetAvailabilityRequest(true);
            }
            joinAvailable.getAndSet(true);
            return false;
        }

        //otherwise send to successor the predecessor (i.e. its new predecessor)
        //and to predecessor the successor (i.e. its new successor)
        if(!rc.setPredecessorRequest(getPredAddress())){
            rc.setPredecessorRequest(getPredAddress());
        }
        rc = new RingConnection(getPredAddress());

        if(!rc.setSuccessorRequest(getSuccAddress())){
            rc.setSuccessorRequest(getSuccAddress());
        }

        return true;
    }

    /**
     * This method is called to find the successor node of a given id
     * @param id The id of an internal node
     * @return The address of the successor node
     */
    public InetSocketAddress findSuccessor(BigInteger id){
        //find predecessor of node id
        InetSocketAddress predecessor = findPredecessor(id);
        if(predecessor == null){
            return null;
        }

        //if the current node is the predecessor of id then return the successor of this node
        if(predecessor.equals(localAddress)){
            return getSuccAddress();
        }

        //otherwise get the predecessor's successor
        RingConnection rc = new RingConnection(predecessor);
        InetSocketAddress ret = null;
        ret = rc.addressRequest("GET_SUCC");

        return ret;
    }

    /**
     * This method is called to find the predecessor node of a given id
     * @param id The id of an internal node
     * @return The address of the predecessor node
     */
    public InetSocketAddress findPredecessor(BigInteger id){
        BigInteger n = localId;
        InetSocketAddress nAddr = localAddress;
        InetSocketAddress succ = getSuccAddress();
        if(succ == null){
            return null;
        }

        //only one node in the ring
        if(succ.equals(localAddress) && succ.equals(predAddress)){
            return localAddress;
        }

        //until id does not belong to (n, succ] cycle changing
        while(!Util.belongsToInterval(id, n, Util.hashAdress(succ))){

            if(n.equals(localId)){
                nAddr = closestPrecedingNode(id);
                n = Util.hashAdress(nAddr);
                RingConnection rc = new RingConnection(nAddr);

                succ = rc.addressRequest("GET_SUCC");

                if(succ == null){
                    return null;
                }
            }
            else{

                RingConnection rc = new RingConnection(nAddr);
                InetSocketAddress temp = null;
                temp = rc.closestRequest(id);


                if(temp == null){
                    return null;
                }
                else if(temp.equals(nAddr)){
                    return nAddr;
                }

                nAddr = temp;
                n = Util.hashAdress(nAddr);
                rc = new RingConnection(nAddr);
                succ = rc.addressRequest("GET_SUCC");

                if(succ == null){
                    return null;
                }
            }
        }
        return nAddr;
    }

    public InetSocketAddress closestPrecedingNode(BigInteger id){

        for(int i = Util.m - 1; i >= 0; i--){
            InetSocketAddress ith = fTable.getIthFinger(i);

            if(ith == null){
                continue;
            }

            if(Util.belongsToOpenInterval(Util.hashAdress(ith), localId, id)){
                boolean value = false;

                try {
                    (new Socket(ith.getAddress(), ith.getPort())).close();
                    value = true;
                } catch (IOException e) {
                    value = false;
                }

                if(value){
                    return ith;
                }
//                else{
//                    fTable.deleteNode(ith);
//                }
            }

        }
        return localAddress;
    }

    public boolean insertFile(Socket s, String fileName) {
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm = files.get(fileName);
        if(fm == null){
            fm = new FileManager(path + fileName);
        }
        boolean value = fm.write(s);

        if(value){
            files.put(fileName, fm);

            InternalFileConnection fc = new InternalFileConnection(this, predAddress);

            if(!fc.insertFileRequest(fileName)){
                fc = new InternalFileConnection(this, predAddress);
                fc.insertFileRequest(fileName);
            }

        }
        return value;
    }

    public boolean delete(String fileName) {
        if(fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm = files.get(fileName);
        if(fm == null){
            return false;
        }

        files.remove(fileName);
        boolean value = fm.remove();

        InetSocketAddress pred = predAddress;
        RingConnection rc = new RingConnection(pred);
        InternalFileConnection fc = new InternalFileConnection(this, pred);

        if(!fc.deleteFileRequest(fileName)){
            fc = new InternalFileConnection(this, pred);
            fc.deleteFileRequest(fileName);
        }

        InetSocketAddress predPred = null;
        predPred = rc.addressRequest("GET_PRED");

        if(predPred == null){
            return value;
        }

        fc = new InternalFileConnection(this, predPred);

        if(!fc.deleteFileRequest(fileName)){
            fc = new InternalFileConnection(this, predPred);
            fc.deleteFileRequest(fileName);
        }

        return value;
    }

    public boolean get(Socket s, String fileName) {
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm = files.get(fileName);
        if(fm == null){
            return false;
        }

        return fm.read(s);
    }

    public boolean hasFile(String fileName){
        if(fileName == null || fileName.equals("")){
            return false;
        }
        return files.containsKey(fileName);
    }

    public ArrayList<String> getFilesInterval(BigInteger from, BigInteger to) {
        if(from == null || to == null) {
            return null;
        }

        ArrayList<String> result = new ArrayList<>();

        for(String s : files.keySet()){
            if(Util.belongsToInterval(Util.hashFile(s), from, to)){
                result.add(s);
            }
        }
        return result;
    }

    public boolean deleteFilesInterval(BigInteger from, BigInteger to) {
        if(from == null || to == null) {
            return false;
        }

        for(String s : files.keySet()){
            if(Util.belongsToInterval(Util.hashFile(s), from, to)){
                singleDelete(s); //what if something goes wrong here?
            }
        }
        return true;
    }

    public boolean singleInsert(Socket s, String fileName) {
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm = files.get(fileName);
        if(fm == null){
            fm = new FileManager(path + fileName);
        }
        boolean value = fm.write(s);

        if(value){
            files.put(fileName, fm);
        }

        return value;
    }

    public boolean singleDelete(String fileName) {
        if(fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm = files.get(fileName);
        if(fm == null){
            return false;
        }
        boolean value = fm.remove();
        if(value){
            files.remove(fileName);
        }
        return value;
    }

    /**
     * @return id of the node
     */
    public BigInteger getLocalId() {
        return localId;
    }

    /**
     * @return address (ip and port) of the node
     */
    public InetSocketAddress getLocalAddress() {

        return localAddress;
    }

    public boolean isJoinAvailable() {

        return joinAvailable.get();
    }

    public  boolean getAndSetJoinAvailable(boolean value){

        return joinAvailable.getAndSet(value);
    }

    public boolean setPredecessor(InetSocketAddress newPred){
        synchronized (oldPred){
            if(predAddress != null){
                oldPred = predAddress;
            }
            predAddress = newPred;
        }
        if(localAddress.equals(newPred)){
            joinAvailable.set(true);
            return true;
        }
        if(newPred != null && Util.belongsToOpenInterval(Util.hashAdress(oldPred), Util.hashAdress(newPred), localId)){
            (new FileUpdater(newPred, this, Util.hashAdress(newPred), Util.hashAdress(oldPred), false)).start();
        }
        return true;
    }

    public boolean setSuccessor(InetSocketAddress newSucc){
        fTable.updateIthFinger(0, newSucc);
        if(localAddress.equals(newSucc)){
            joinAvailable.set(true);
            return true;
        }
        if(newSucc != null && Util.belongsToOpenInterval(Util.hashAdress(fTable.getOldSucc()), localId, Util.hashAdress(newSucc))){
            (new FileUpdater(newSucc, this, Util.hashAdress(fTable.getOldSucc()), Util.hashAdress(newSucc), false)).start();
        }
        return true;
    }

    /**
     *
     * @return
     */
    public synchronized InetSocketAddress getPredAddress() {
        return predAddress;
    }

    public InetSocketAddress getSuccAddress(){
        return fTable.getIthFinger(0);
    }

    public FingerTable getfTable() {
        return fTable;
    }

    /**
     * This method is called to print the files whose responsibility relies on the current node
     * @return The string representing the finger table
     */
    public String getFiles(){
        StringBuilder sb = new StringBuilder();
        for(String file : files.keySet()){
            sb.append(file + "\n");
        }
        return sb.toString();
    }
}
