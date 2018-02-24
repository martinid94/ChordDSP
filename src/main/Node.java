package main;

import java.math.BigInteger;
import java.net.InetSocketAddress;

/**
 * Created by Marco on 24/02/2018.
 */
public class Node {

    private BigInteger localId;
    private InetSocketAddress localAddress;
    private boolean joinAvailable;
    private InetSocketAddress predAddress;
    private FingerTable fTable;
    //file manager da inserire
    //eventuali altri thread

    public Node(InetSocketAddress local){
        localAddress = local;
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
        return joinAvailable;
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
