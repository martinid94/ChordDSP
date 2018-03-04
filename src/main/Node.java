package main;

import main.Connection.FileConnection;
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


        InetSocketAddress predessor = findPredecessor(id);

        RingConnection rg = new RingConnection(predessor);

        InetSocketAddress ret = null;
        try {
            ret = rg.addressRequest("GET_SUCC");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            return null;
        }

        return ret;
    }

    public InetSocketAddress findPredecessor(BigInteger id){

        return null;
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
                    value = ith.getAddress().isReachable(1000);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(value){
                    return ith;
                }
                else{
                    fTable.deleteNode(ith);
                }
            }

        }
        return localAddress;
    }

    public boolean insertFile(Socket s, String fileName) {
        if(s == null || fileName == null || fileName.equals("")){
            return false;
        }

        FileManager fm =files.get(fileName);
        if(fm == null){
            fm = new FileManager(fileName);
        }
        boolean value = fm.write(s);

        if(value){
            files.put(fileName, fm);

            FileConnection fc = new FileConnection(this, predAddress);
            try{
                if(!fc.insertFileRequest(fileName, true)){
                    fc.insertFileRequest(fileName, true);
                }
            }
            catch(IOException | ClassNotFoundException exc) {
                try {
                    fc = new FileConnection(this, predAddress);
                    fc.insertFileRequest(fileName, true);
                } catch (IOException | ClassNotFoundException e) {
                    return true;
                }
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
        FileConnection fc = new FileConnection(this, pred);
        try {
            if(fc.hasFileRequest(fileName)){
                fc.deleteFileRequest(fileName, true);
            }
        }
        catch (IOException | ClassNotFoundException exc) {
            try {
                if(fc.hasFileRequest(fileName)){
                    fc.deleteFileRequest(fileName, true);
                }
            } catch (IOException | ClassNotFoundException e) {}
        }

        InetSocketAddress predPred = null;
        try {
            predPred = rc.addressRequest("GET_PRED");
        } catch (IOException | ClassNotFoundException e) {
            return value;
        }

        if(predPred == null){
            return value;
        }

        fc = new FileConnection(this, predPred);
        try {
            if(fc.hasFileRequest(fileName)){
                fc.deleteFileRequest(fileName, true);
            }
        }
        catch (IOException | ClassNotFoundException exc) {
            try {
                if(fc.hasFileRequest(fileName)){
                    fc.deleteFileRequest(fileName, true);
                }
            } catch (IOException | ClassNotFoundException e) {
                return value;
            }
            return value;
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
