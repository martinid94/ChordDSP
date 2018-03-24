package main.connection;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;

/**
 * This class provides all the methods that internal nodes (those who belong to the Chord ring) call in order to
 * request information on the Chord ring structure.
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class RingConnection extends Connection {

    /**
     * Unique constructor of the class
     * @param nodeAd specifies the contact node
     */
    public RingConnection(InetSocketAddress nodeAd) {
        super(nodeAd);
    }

    /**
     * This method is called in the join procedure in order to ask a bootstrap node for the invoker's successor in the ring
     * @param localId It is invoker's id
     * @return The invoker's successor in the ring
     */
    public InetSocketAddress findSuccessorRequest(BigInteger localId){

        //invalid argument
        if (localId == null) {
            return null;
        }

        InetSocketAddress mySucc = (InetSocketAddress) sendReceive("FIND_SUCC", localId);
        return mySucc;
    }

    /**
     * This method is called to request a node for its successor or predecessor
     * @param req It specifies whether successor or predecessor is requested
     * @return The successor or predecessor of a node
     */
    public InetSocketAddress addressRequest(String req) {

        //the request must be either GET_SUCC or GET_PRED
        if (!(req.equals("GET_SUCC") || req.equals("GET_PRED"))) {
            return null;
        }

        return (InetSocketAddress) sendReceive(req, null);
    }

    /**
     * This method is called to request for the closest node in the ring preceding a specific id.
     * @param id It specifies the start of the research
     * @return The closest preceding node computed by the contact node
     */
    public InetSocketAddress closestRequest(BigInteger id){

        //invalid argument
        if(id == null){
            return null;
        }

        return (InetSocketAddress) sendReceive("GET_CLOSEST", id);
    }

    /**
     * This method is called when some (bootstrap) nodes are active in the ring.
     * It aims to contact the invoker's successor and begin a join procedure if it can perform it.
     * @param myself It is invoker's address
     * @return The old predecessor of the invoker's successor (which can be now set as the invoker's predecessor)
     */
    public InetSocketAddress joinRequest(InetSocketAddress myself){

        //invalid argument
        if (myself == null) {
            return null;
        }

        return (InetSocketAddress) sendReceive("JOIN", myself);
    }

    /**
     * This method is called to request and set a node's joinAvailable variable status
     * @return A node's joinAvailable value, null if the connection failed
     */
    public Boolean getAndSetAvailabilityRequest(boolean status){

        return (Boolean) sendReceive("GET&SET_JA", status);
    }

    /**
     * This method is called to set a node's predecessor.
     * @param newPred New predecessor's address
     * @return True if the communication and the variable setting has been performed correctly
     */
    public Boolean setPredecessorRequest(InetSocketAddress newPred){

        //invalid argument
        if (newPred == null) {
            return false;
        }

        Boolean ack = (Boolean) sendReceive("SET_PRED", newPred);
        if(ack == null){
            return false;
        }

        return ack;
    }

    /**
     * This method is called to set a node's successor.
     * @param newSucc New successor's address
     * @return True if the communication and the variable setting has been performed correctly
     */
    public Boolean setSuccessorRequest(InetSocketAddress newSucc){

        //invalid argument
        if (newSucc == null) {
            return false;
        }

        Boolean ack = (Boolean) sendReceive("SET_SUCC", newSucc);

        if(ack == null){
            return false;
        }

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