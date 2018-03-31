package main.node;

import main.util.Util;
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
        localId = Util.hashAddress(local);
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
        new FileUpdater(mySucc, this, Util.hashAddress(myPred), Util.hashAddress(mySucc), true).start();

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

        //until id does not belong to (n, succ] cycle
        while(!Util.belongsToInterval(id, n, Util.hashAddress(succ))){

            if(n.equals(localId)){
                //compute with local information the closest preceding node of id
                nAddr = closestPrecedingNode(id);
                //update n
                n = Util.hashAddress(nAddr);
                RingConnection rc = new RingConnection(nAddr);
                //update succ
                succ = rc.addressRequest("GET_SUCC");

                if(succ == null){
                    //something bad happened
                    return null;
                }
            }
            else{
                //send to nAddr the request to compute the closest preceding node of id
                RingConnection rc = new RingConnection(nAddr);
                InetSocketAddress temp = null;
                temp = rc.closestRequest(id);

                if(temp == null){
                    //something bad happened
                    return null;
                }
                else if(temp.equals(nAddr)){
                    return nAddr;
                }

                nAddr = temp;
                n = Util.hashAddress(nAddr);
                rc = new RingConnection(nAddr);
                succ = rc.addressRequest("GET_SUCC");

                if(succ == null){
                    //something bad happened
                    return null;
                }
            }
        }
        return nAddr;
    }

    /**
     * This method is called to compute the closest preceding node of a given id. It is based on the information
     * provided by the finger table of the current node
     * @param id The id of an internal node
     * @return The address of the closest preceding node
     */
    public InetSocketAddress closestPrecedingNode(BigInteger id){
        //loop on the whole finger table from the furthest node to the closest to local node
        for(int i = Util.m - 1; i >= 0; i--){
            InetSocketAddress ith = fTable.getIthFinger(i);
            if(ith == null){
                continue;
            }

            //if ith finger belongs to the interval (localId, id) it is a candidate
            if(Util.belongsToOpenInterval(Util.hashAddress(ith), localId, id)){
                boolean value = false;

                //check if the candidate is still active
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

    /**
     * This method is called to download and insert a file into the pool of files whose
     * responsibility relies on the invoker
     * @param s It is the socket where to download the file
     * @param fileName The name of the file to be inserted
     * @return True if the whole operation is performed correctly
     */
    public boolean insertFile(Socket s, String fileName) {
        //invalid arguments
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        //check if the file is already present in the map (i.e. a modified version is coming),
        //otherwise create a new entry in the map
        FileManager fm = files.get(fileName);
        if(fm == null){
            fm = new FileManager(path + fileName);
        }
        //physically download the file
        boolean value = fm.write(s);

        //if everything is ok, add it to the map and send the replica to the predecessor
        if(value){
            files.put(fileName, fm);

            InternalFileConnection fc = new InternalFileConnection(this, predAddress);
            if(!fc.insertFileRequest(fileName)){
                //something bad happened, retry
                fc = new InternalFileConnection(this, predAddress);
                fc.insertFileRequest(fileName);
            }
        }
        return value;
    }

    /**
     * This method is called to delete a file from the pool and remove it physically
     * @param fileName The name of the file to be deleted
     * @return True if the operation is performed correctly
     */
    public boolean delete(String fileName) {
        //invalid arguments
        if(fileName == null || fileName.equals("")){
            return false;
        }

        //check if the file is present
        FileManager fm = files.get(fileName);
        if(fm == null){
            return false;
        }

        //at first remove it form the map, then remove it following the path
        files.remove(fileName);
        boolean value = fm.remove();

        //contact the predecessor to delete the replica
        InetSocketAddress pred = predAddress;
        RingConnection rc = new RingConnection(pred);
        InternalFileConnection fc = new InternalFileConnection(this, pred);

        if(!fc.deleteFileRequest(fileName)){
            //something bad happened, retry
            fc = new InternalFileConnection(this, pred);
            fc.deleteFileRequest(fileName);
        }

        //try to delete it also from the predecessor of the predecessor.
        //this is due to particular situations that may occur when the ring structure is not stable
        //(i.e. a join or a leave is performed during file exchange)
        InetSocketAddress predPred = null;
        predPred = rc.addressRequest("GET_PRED");

        if(predPred == null){
            return value;
        }

        fc = new InternalFileConnection(this, predPred);

        if(!fc.deleteFileRequest(fileName)){
            //something bad happened, retry
            fc = new InternalFileConnection(this, predPred);
            fc.deleteFileRequest(fileName);
        }

        return value;
    }

    /**
     * This method is called to upload a file which is present in the pool
     * @param s It is the socket where to upload the file
     * @param fileName It represents the desired file name
     * @return True if the whole operation is performed correctly
     */
    public boolean get(Socket s, String fileName) {
        //invalid arguments
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        //check if the specified file is available
        FileManager fm = files.get(fileName);
        if(fm == null){
            return false;
        }

        //upload it
        return fm.read(s);
    }

    /**
     * This method is called to verify if a given file is available from the pool
     * @param fileName The desired file name
     * @return True if the current node is responsible for it (thus it stores the file), false otherwise
     */
    public boolean hasFile(String fileName){
        //invalid arguments
        if(fileName == null || fileName.equals("")){
            return false;
        }
        return files.containsKey(fileName);
    }

    /**
     * This method is called to get a list of files (present in the invoker's pool)
     * whose name belongs to a specific interval
     * @param from The beginning of the interval
     * @param to The end of the interval
     * @return The list of all the file names that belong to the interval
     */
    public ArrayList<String> getFilesInterval(BigInteger from, BigInteger to) {
        //invalid arguments
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

    /**
     * This method is called to delete all the files whose names belong to the specified interval.
     * The files are both removed from the pool and physically
     * @param from The beginning of the interval
     * @param to The end of the interval
     * @return True if the operation is performed correctly
     */
    public boolean deleteFilesInterval(BigInteger from, BigInteger to) {
        //invalid arguments
        if(from == null || to == null) {
            return false;
        }

        for(String s : files.keySet()){
            if(Util.belongsToInterval(Util.hashFile(s), from, to)){
                singleDelete(s);
            }
        }
        return true;
    }

    /**
     * This method is called to download a file. The difference between this and insertFile() method is that
     * here no file replica is passed to the predecessor
     * @param s It is the socket where to download the file
     * @param fileName The name of the file to be downloaded
     * @return True if the operation is performed correctly
     */
    public boolean singleInsert(Socket s, String fileName) {
        //invalid arguments
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        //check if the file is already present in the pool, otherwise create the entry and finally download the file
        FileManager fm = files.get(fileName);
        if(fm == null){
            fm = new FileManager(path + fileName);
        }
        boolean value = fm.write(s);

        //if everything ok during the download, insert the file in the pool
        if(value){
            files.put(fileName, fm);
        }

        return value;
    }

    /**
     * This method is called to delete a file. The difference between this and deleteFile() method is that
     * here the predecessor is not notified to remove its file replica
     * @param fileName The name of the file to be deleted
     * @return True if the operation is performed correctly
     */
    public boolean singleDelete(String fileName) {
        //invalid arguments
        if(fileName == null || fileName.equals("")){
            return false;
        }

        //check if the file is actually present in the pool and if so, remove it
        FileManager fm = files.get(fileName);
        if(fm == null){
            return false;
        }

        boolean value = fm.remove();
        if(value){
            //physically remove the file
            files.remove(fileName);
        }
        return value;
    }

    /**
     * This method is called to get the invoker's id
     * @return The id of the internal node
     */
    public BigInteger getLocalId() {
        return localId;
    }

    /**
     * This method is called to get the invoker's address
     * @return The IP address and port number of the internal node
     */
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * This method is called to check if the invoker can sustain a join operation
     * @return True if the node can sustain a join operation, false otherwise
     */
    public boolean isJoinAvailable() {
        return joinAvailable.get();
    }

    /**
     * This method is called to get and set the value of the joinAvailable variable
     * @param value It is the new joinAvailable value
     * @return The joinAvailable value before the setting
     */
    public boolean getAndSetJoinAvailable(boolean value){
        return joinAvailable.getAndSet(value);
    }

    /**
     * This method is called to set a new predecessor to the current node. When the predecessor is changed,
     * the FileUpdater may start to stabilize the file location in the ring
     * @param newPred The address of the new predecessor
     * @return True if the operation is performed correctly
     */
    public boolean setPredecessor(InetSocketAddress newPred){
        //update oldPred
        synchronized (oldPred){
            if(predAddress != null){
                oldPred = predAddress;
            }
            predAddress = newPred;
        }
        //check if the invoker is the only node in the ring
        if(localAddress.equals(newPred)){
            joinAvailable.set(true);
            return true;
        }
        //if oldPred belongs to the interval (newPred localId) then start the FileUpdater
        //(i.e. the setPredecessor() method is called after a leave operation or a crash of the predecessor)
        if(newPred != null && Util.belongsToOpenInterval(Util.hashAddress(oldPred), Util.hashAddress(newPred), localId)){
            (new FileUpdater(newPred, this, Util.hashAddress(newPred), Util.hashAddress(oldPred), false)).start();
        }
        return true;
    }

    /**
     * This method is called to set a new successor to the current node. When the successor is changed,
     * the FileUpdater may start to stabilize the file location in the ring
     * @param newSucc The address of the new successor
     * @return True if the operation is performed correctly
     */
    public boolean setSuccessor(InetSocketAddress newSucc){
        fTable.updateIthFinger(0, newSucc);
        //check if the invoker is the only node in the ring
        if(localAddress.equals(newSucc)){
            joinAvailable.set(true);
            return true;
        }
        //if oldSucc belongs to the interval (localId newSucc) then start the FileUpdater
        //(i.e. the setSuccessor() method is called after a leave operation or a crash of the predecessor)
        if(newSucc != null && Util.belongsToOpenInterval(Util.hashAddress(fTable.getOldSucc()), localId, Util.hashAddress(newSucc))){
            (new FileUpdater(newSucc, this, Util.hashAddress(fTable.getOldSucc()), Util.hashAddress(newSucc), false)).start();
        }
        return true;
    }

    /**
     * This method is called to get the address of the invoker's predecessor
     * @return The predecessor's address
     */
    public synchronized InetSocketAddress getPredAddress() {
        return predAddress;
    }

    /**
     * This method is called to get the address of the invoker's successor
     * @return The successor's address
     */
    public InetSocketAddress getSuccAddress(){
        return fTable.getIthFinger(0);
    }

    /**
     * This method is called to get the invoker's finger table
     * @return The finger table of the internal node
     */
    public FingerTable getfTable() {
        return fTable;
    }

    /**
     * This method is called to print the files whose responsibility relies on the current node
     * @return The string representing the files in the pool
     */
    public String getFiles(){
        StringBuilder sb = new StringBuilder();
        for(String file : files.keySet()){
            sb.append("key:" + Util.hashFile(file) + " fileName:" + file + "\n");
        }
        return sb.toString();
    }
}
