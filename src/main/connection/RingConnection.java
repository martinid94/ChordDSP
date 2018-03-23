package main.connection;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;

/**
 * Created by Davide on 24/02/2018.
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
     * Method used in the join procedure in order to ask a bootstrap node for the invoker's successor in the ring
     * @param localId invoker's id
     * @return invoker's successor in the ring
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
     * Method used to request a node for its successor or predecessor
     * @param req: specifies whether successor or predecessor is requested
     * @return the successor or predecessor of a node
     */
    public InetSocketAddress addressRequest(String req) {

        //the request must be either GET_SUCC or GET_PRED
        if (!(req.equals("GET_SUCC") || req.equals("GET_PRED"))) {
            return null;
        }

        return (InetSocketAddress) sendReceive(req, null);
    }

    /**
     * Method used to request for the closest node in the ring preceding a specific id.
     * @param id: specifies the start of the research
     * @return closest preceding node computed by the contact node
     */
    public InetSocketAddress closestRequest(BigInteger id){

        //invalid argument
        if(id == null){
            return null;
        }

        return (InetSocketAddress) sendReceive("GET_CLOSEST", id);
    }

    /**
     * Method used used when some (bootstrap) nodes are active in the ring.
     * It aims to contact the invoker's successor and begin a join procedure if it can perform it.
     * @param myself invoker's address
     * @return my successor's old predecessor (which can be now set as my predecessor)
     */
    public InetSocketAddress joinRequest(InetSocketAddress myself){

        //invalid argument
        if (myself == null) {
            return null;
        }

        return (InetSocketAddress) sendReceive("JOIN", myself);
    }

    /**
     * Method used to request and set a node's joinAvailable variable status
     * @return a node's joinAvailable value, null if the connection failed
     */
    public Boolean getAndSetAvailabilityRequest(boolean status){

        return (Boolean) sendReceive("GET&SET_JA", status);
    }

    /**
     * Method used to set a node's predecessor.
     * @param newPred new predecessor's address
     * @return true if the communication and the variable setting has been performed correctly
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
     * Method used to set a node's successor.
     * @param newSucc new successor's address
     * @return true if the communication and the variable setting has been performed correctly
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