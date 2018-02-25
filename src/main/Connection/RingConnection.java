package main.Connection;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;

/**
 * Created by Davide on 24/02/2018.
 */
public class RingConnection extends Connection {
    public RingConnection(InetSocketAddress nodeAd){
        super(nodeAd);
    }

    /**
     * Method used to ask a bootstrap node for my successor in the ring
     * @param localId
     * @return invoker's successor in the ring
     */
    public InetSocketAddress bootstapRequest(BigInteger localId){
        InetSocketAddress mySucc = null;

        if(localId == null) {
            throw new IllegalArgumentException("Argument can not be null");
        }
        else {
            try {
                startConnection();
                oos.writeObject("FIND_SUCC"); //the bootstrap node must execute a find_successor(localId) procedure
                oos.writeObject(localId);
                oos.flush();

                mySucc = (InetSocketAddress) ois.readObject();
            }
            catch (IOException ioe) {
                return null;
            }
            catch (ClassNotFoundException cnfe) {
                return null;
            }
            finally {
                try {
                    closeConnection();
                }
                catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
        return mySucc;
    }

    /**
     * Method used to request a node for its successor or predecessor
     * @param req: specifies whether successor or predecessor is requested
     * @return the successor or predecessor of a node
     */
    public InetSocketAddress addressRequest(String req) {
        InetSocketAddress addressRequested = null;

        if(!(req.equals("GET_SUCC") || req.equals("GET_PRED"))) {//req must be either GET_SUCC or GET_PRED
            throw new IllegalArgumentException("The argument must be either GET_SUCC or GET_PRED");
        }
        else {
            try {
                startConnection();
                oos.writeObject(req);
                oos.flush();

                addressRequested = (InetSocketAddress) ois.readObject();
            }
            catch (IOException ioe) {
                return null;
            }
            catch (ClassNotFoundException cnfe) {
                return null;
            }
            finally {
                try {
                    closeConnection();
                }
                catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
        return addressRequested;
    }

    /**
     * Method used after a bootstrapRequest() call.
     * It aims to contact the invoker's successor and begin a join procedure if it can perform it.
     * @param myself
     * @return my successor's old predecessor (which can be now set as my predecessor)
     */
    public InetSocketAddress joinRequest(InetSocketAddress myself){
        InetSocketAddress myPred = null;

        if(myself == null) {
            throw new IllegalArgumentException("Argument can not be null");
        }
        else {
            try {
                startConnection();
                oos.writeObject("JOIN"); //the bootstrap node must execute a find_successor(localId) procedure
                oos.writeObject(myself);
                oos.flush();

                myPred = (InetSocketAddress) ois.readObject();
            }
            catch (IOException ioe) {
                return null;
            }
            catch (ClassNotFoundException cnfe) {
                return null;
            }
            finally {
                try {
                    closeConnection();
                }
                catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
        return myPred;
    }

    /**
     * Method used to set a node's joinAvailable variable to a specified status
     * @param status
     * @return true if the communication and the variable setting has been performed correctly
     */
    public Boolean setAvailabilityRequest(boolean status) {
        Boolean ack = null;
        try {
            startConnection();
            oos.writeObject("SET_JA");
            oos.writeObject(status);
            oos.flush();

            ack = ois.readBoolean(); //true if the variable joinAvailable has been set properly
        }
        catch (IOException ioe) {
            return null;
        }
        finally {
            try {
                closeConnection();
            }
            catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        return ack;
    }

    /**
     * Method used to request a node's joinAvailable variable status
     * @return a node's joinAvailable value, null if the connection failed
     */
    public Boolean getAvailabilityRequest() {
        Boolean joinAvailability = null;
        try {
            startConnection();
            oos.writeObject("GET_JA");
            oos.flush();

            joinAvailability = ois.readBoolean();
        }
        catch (IOException ioe) {
            return null;
        }
        finally {
            try {
                closeConnection();
            }
            catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        return joinAvailability;
    }

    /**
     * Method used to set a node's predecessor. It is used in the leave() procedure
     * @param newPred
     * @return true if the communication and the variable setting has been performed correctly
     */
    public Boolean setPredecessorRequest(InetSocketAddress newPred) {
        Boolean ack = null;
        try {
            startConnection();
            oos.writeObject("SET_PRED");
            oos.writeObject(newPred);
            oos.flush();

            ack = ois.readBoolean(); //true if the new predecessor has been set properly
        }
        catch (IOException ioe) {
            return null;
        }
        finally {
            try {
                closeConnection();
            }
            catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        return ack;
    }

    /**
     * Method used to set a node's successor. It is used in the leave() procedure
     * @param newSucc
     * @return true if the communication and the variable setting has been performed correctly
     */
    public Boolean setSuccessorRequest(InetSocketAddress newSucc) {
        Boolean ack = null;
        try {
            startConnection();
            oos.writeObject("SET_SUCC");
            oos.writeObject(newSucc);
            oos.flush();

            ack = ois.readBoolean(); //true if the new successor has been set properly
        }
        catch (IOException ioe) {
            return null;
        }
        finally {
            try {
                closeConnection();
            }
            catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        return ack;
    }
}
