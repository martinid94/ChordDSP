package main.Connection;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;

/**
 * Created by Davide on 24/02/2018.
 */
public class RingConnection extends Connection {
    public RingConnection(InetSocketAddress nodeAd) {
        super(nodeAd);
    }

    /**
     * Method used to ask a bootstrap node for my successor in the ring
     *
     * @param localId
     * @return invoker's successor in the ring
     */
    public InetSocketAddress bootstapRequest(BigInteger localId){
        if (localId == null) {
            return null;
        }

        InetSocketAddress mySucc = (InetSocketAddress) sendReceive("FIND_SUCC", null);
        return mySucc;
    }

    /**
     * Method used to request a node for its successor or predecessor
     *
     * @param req: specifies whether successor or predecessor is requested
     * @return the successor or predecessor of a node
     */
    public InetSocketAddress addressRequest(String req) {
        if (!(req.equals("GET_SUCC") || req.equals("GET_PRED"))) {//req must be either GET_SUCC or GET_PRED
            return null;
        }

        InetSocketAddress addressRequested = (InetSocketAddress) sendReceive(req, null);
        return addressRequested;
    }

    /**
     * Method used to request a node for its successor or predecessor
     *
     * @param id: specifies id
     * @return the successor or predecessor of a node
     */
    public InetSocketAddress closestRequest(BigInteger id){
        if(id == null){
            return null;
        }
        InetSocketAddress addressRequested = (InetSocketAddress) sendReceive("GET_CLOSEST", id);

        return addressRequested;
    }

    /**
     * Method used after a bootstrapRequest() call.
     * It aims to contact the invoker's successor and begin a join procedure if it can perform it.
     *
     * @param myself
     * @return my successor's old predecessor (which can be now set as my predecessor)
     */
    public InetSocketAddress joinRequest(InetSocketAddress myself){
        if (myself == null) {
            return null;
        }

        InetSocketAddress myPred = (InetSocketAddress) sendReceive("JOIN", myself);
        return myPred;
    }

    /**
     * Method used to request a node's joinAvailable variable status
     *
     * @return a node's joinAvailable value, null if the connection failed
     */
    public boolean getAndSetAvailabilityRequest(boolean status){
        boolean joinAvailability = (Boolean) sendReceive("GET&SET_JA", new Boolean(status));

        return joinAvailability;
    }

    /**
     * Method used to set a node's predecessor. It is used in the leave() procedure
     *
     * @param newPred
     * @return true if the communication and the variable setting has been performed correctly
     */
    public boolean setPredecessorRequest(InetSocketAddress newPred){
        if (newPred == null) {
            return false;
        }

        boolean ack = (Boolean) sendReceive("SET_PRED", newPred);
        return ack;
    }

    /**
     * Method used to set a node's successor. It is used in the leave() procedure
     *
     * @param newSucc
     * @return true if the communication and the variable setting has been performed correctly
     */
    public boolean setSuccessorRequest(InetSocketAddress newSucc){
        if (newSucc == null) {
            return false;
        }

        boolean ack = (Boolean) sendReceive("SET_SUCC", newSucc);
        return ack;
    }

    private Object sendReceive(String request, Object param){
        Object obj = null;
        try {
            startConnection();
            if (request != null) {
                oos.writeObject(request);
            }
            if (param != null) {
                oos.writeObject(param);
            }
            oos.flush();
            obj = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            obj = null;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        return obj;
    }
}