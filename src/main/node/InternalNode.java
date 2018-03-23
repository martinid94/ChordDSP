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
 * Created by Marco on 24/02/2018.
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
    //file manager da inserire
    //eventuali altri thread

    public InternalNode(InetSocketAddress local, String path){

        if(local == null || path == null || path.equals("")){
            throw new IllegalArgumentException();
        }

        localAddress = local;
        fTable = new FingerTable();
        localId = Util.hashAdress(local);
        joinAvailable = new AtomicBoolean();
        predAddress = null;
        oldPred = null;
        listener = new Listener(this);
        checkPred = new CheckPredecessor(this);
        fixFinger = new FixFinger(this);
        stabilizer = new Stabilizer(this);
        files = new ConcurrentHashMap<>();
        this.path = path;
    }


    /**
     *
     * @param bootstrapNode: address of the bootstrap node
     * @return true if join has been performed otherwise false
     */
    public boolean join(InetSocketAddress bootstrapNode){

        if(bootstrapNode == null){
            return false;
        }

        joinAvailable.set(false);

        RingConnection rc = new RingConnection(bootstrapNode);
        InetSocketAddress mySucc = rc.findSuccessorRequest(localId);


        //error in findSuccessorRequest
        if(mySucc == null){
            return false;
        }

        rc = new RingConnection(mySucc);
        InetSocketAddress myPred = rc.joinRequest(localAddress);


        //myPred == null if there is an error (join is not available)
        if(myPred == null){
            return false;
        }

        predAddress = myPred;
        oldPred = myPred;
        fTable.updateIthFinger(0, mySucc);

        listener.start();

        rc = new RingConnection(myPred);
        if(rc.setSuccessorRequest(localAddress) == null){
            rc.setSuccessorRequest(localAddress);
        }

        new FileUpdater(mySucc, this, Util.hashAdress(myPred), Util.hashAdress(mySucc), true).start();
        checkPred.start();
        fixFinger.start();
        stabilizer.start();

        return true;
    }

    public boolean bootstrapJoin(){
        predAddress = localAddress;
        oldPred = localAddress;
        fTable.updateIthFinger(0, localAddress);

        joinAvailable.set(true);
        listener.start();
        checkPred.start();
        fixFinger.start();
        stabilizer.start();

        return true;
    }

    public boolean leave(){

        RingConnection rc = new RingConnection(getSuccAddress());
        Boolean succJoinAvailable = rc.getAndSetAvailabilityRequest(false);
        if(succJoinAvailable == null){
            succJoinAvailable = rc.getAndSetAvailabilityRequest(false);
        }

        if(succJoinAvailable == null || !succJoinAvailable){
            return false;
        }

        if(!joinAvailable.getAndSet(false) && succJoinAvailable){
            if(rc.getAndSetAvailabilityRequest(true) == null){
                rc.getAndSetAvailabilityRequest(true);
            }
            joinAvailable.getAndSet(true);
            return false;
        }

        if(!rc.setPredecessorRequest(getPredAddress())){
            rc.setPredecessorRequest(getPredAddress());
        }
        rc = new RingConnection(getPredAddress());

        if(!rc.setSuccessorRequest(getSuccAddress())){
            rc.setSuccessorRequest(getSuccAddress());
        }

        //TODO bloccare i thread listener, fixfinger, askpredecessor, stabilizer

        return true;
    }

    public InetSocketAddress findSuccessor(BigInteger id){

        InetSocketAddress predecessor = findPredecessor(id);

        if(predecessor == null){
            return null;
        }

        //if this node is the predecessor of id then return the successor of this node
        if(predecessor.equals(localAddress)){
            return getSuccAddress();
        }

        RingConnection rc = new RingConnection(predecessor);

        InetSocketAddress ret = null;
        ret = rc.addressRequest("GET_SUCC");

        return ret;
    }

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
        if(newPred != null && Util.belongsToOpenInterval(Util.hashAdress(oldPred), Util.hashAdress(newPred), localId)){
            (new FileUpdater(newPred, this, Util.hashAdress(newPred), Util.hashAdress(oldPred), false)).start();
        }
        return true;
    }

    public boolean setSuccessor(InetSocketAddress newSucc){
        fTable.updateIthFinger(0, newSucc);
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
}
