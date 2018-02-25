package main;

import main.Connection.RingConnection;

import java.math.BigInteger;
import java.net.InetSocketAddress;
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
        InetSocketAddress mySucc = rc.bootstapRequest(localId);

        //error in bootstapRequest
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

    /**
     *
     * @return
     */
    public InetSocketAddress getPredAddress() {
        return predAddress;
    }

    public FingerTable getfTable() {
        return fTable;
    }
}
