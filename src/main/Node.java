package main;

import main.Connection.RingConnection;

import java.io.File;
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
public class Node {

    private BigInteger localId;
    private InetSocketAddress localAddress;
    private AtomicBoolean joinAvailable;
    private InetSocketAddress predAddress;
    private FingerTable fTable;
    private Listener listener;
    private ConcurrentHashMap<String, FileManager> files;
    //file manager da inserire
    //eventuali altri thread

    public Node(InetSocketAddress local){

        if(local == null){
            throw new IllegalArgumentException();
        }

        localAddress = local;
        fTable = new FingerTable();
        localId = Util.hashAdress(local);
        joinAvailable = new AtomicBoolean();
        predAddress = null;
        listener = new Listener(this);
        files = new ConcurrentHashMap<>();
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
        InetSocketAddress mySucc = null;

        try {
            mySucc = rc.bootstapRequest(localId);
        } catch (IOException | ClassNotFoundException ei) {
            return false;
        }


        //error in bootstapRequest
        if(mySucc == null){
            return false;
        }

        rc = new RingConnection(mySucc);
        InetSocketAddress myPred = null;
        try {
            myPred = rc.joinRequest(localAddress);
        } catch (IOException | ClassNotFoundException ei) {
            return false;
        }

        //myPred == null if there is an error (join is not available)
        if(myPred == null){
            return false;
        }

        predAddress = myPred;
        fTable.updateIthFinger(0, mySucc);

        listener.start();

        //TODO sendRequestSetSucc

        //TODO attiva thread FileUpdater

        //TODO attiva checkPredecessor, fixFingers, stabilizer

        return true;
    }

    public boolean leave(){

        return false;
    }

    public InetSocketAddress findSuccessor(BigInteger id){

        InetSocketAddress successor = fTable.getIthFinger(0);

        if(Util.belongsToInterval(id, localId, Util.hashAdress(successor))){
            return successor;
        }
        //TODO
        return null;
    }

    public InetSocketAddress closestPrecedingNode(BigInteger id){

        for(int i = Util.m - 1; i >= 0; i--){
            InetSocketAddress ith = fTable.getIthFinger(i);

            if(ith == null){
                continue;
            }

            //if(ith.getAddress().isReachable(1000)){

            //}
        }
        return null;
    }

    public boolean insertFile(Socket s, InetSocketAddress pred, String fileName) {
        if(s == null || pred == null || fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm =files.get(fileName);
        if(fm == null){
            fm = new FileManager(fileName);
        }
        boolean value = fm.write(s, pred);

        if(value){
            files.put(fileName, fm);
        }
        return value;
    }

    public boolean delete(InetSocketAddress pred, String fileName) {
        if(pred == null || fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm = files.get(fileName);
        if(fm == null){
            return false;
        }
        boolean value = fm.remove(pred);
        if(value){
            files.remove(fileName);
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

    public ArrayList<String> getFilesInterval(BigInteger from, BigInteger to) {
        ArrayList<String> result = new ArrayList<>();

        for(String s : files.keySet()){
            if(Util.belongsToInterval(Util.hashFile(s), from, to)){
                result.add(s);
            }
        }
        return result;
    }

    public boolean singleInsert(Socket s, String fileName) {
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm =files.get(fileName);
        if(fm == null){
            fm = new FileManager(fileName);
        }
        boolean value = fm.singleWrite(s);

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
        boolean value = fm.singleRemove();
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

    //TODO da aggiornare con i parametri (vari casi) e concorrenza
    public boolean setPredecessor(InetSocketAddress newPred){
        predAddress = newPred;
        return true;
    }

    public boolean setSuccessor(InetSocketAddress newSucc){
        fTable.updateIthFinger(0, newSucc);
        return true;
    }

    /**
     *
     * @return
     */
    public InetSocketAddress getPredAddress() {
        return predAddress;
    }

    public InetSocketAddress getSuccAddress(){
        return fTable.getIthFinger(0);
    }

    public FingerTable getfTable() {
        return fTable;
    }
}
