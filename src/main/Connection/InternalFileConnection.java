package main.Connection;

import main.InternalNode;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Created by Davide on 25/02/2018.
 */
public class InternalFileConnection extends Connection{
    private InternalNode localInternalNode;
    private RingConnection rc;

    public InternalFileConnection(InternalNode n, InetSocketAddress nodeAd){
        super(nodeAd);
        localInternalNode = n;
        rc = new RingConnection(nodeAd);
    }

    public boolean getFileRequest(String fileName) {
        //Todo create an auxiliary method
        if(fileName == null || fileName.equals(""))
            return false;

        boolean retVal = false;

        try {
            startConnection();
            oos.writeObject("GET_FILE");
            oos.writeObject(fileName);
            oos.flush();

            boolean hasFile = ois.readBoolean();
            if(!hasFile) {
                InetSocketAddress newContact = rc.addressRequest("GET_PRED");
                nodeAddress = newContact;
            }
            else {
                retVal = localInternalNode.singleInsert(s, fileName);
            }
        } catch (IOException e) {
            retVal = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        if(retVal){
            return true;
        }

        try {
            startConnection();
            oos.writeObject("GET_FILE");
            oos.writeObject(fileName);
            oos.flush();

            boolean hasFile = ois.readBoolean();
            if(hasFile) {
                retVal = localInternalNode.singleInsert(s, fileName);
            }
        } catch (IOException e) {
            retVal = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        return retVal;
    }

    public ArrayList<String> fileIntervalRequest(BigInteger from, BigInteger to){
        if(from == null || to == null)
            return null;

        ArrayList<String> retVal = null;
        try {
            startConnection();
            oos.writeObject("GET_FILE_INTERVAL"); // insert flush here?
            oos.writeObject(from);
            oos.writeObject(to);
            oos.flush();
            retVal = (ArrayList<String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            retVal = null;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        return retVal;
    }

    public boolean insertFileRequest(String fileName, boolean replica){
        if(fileName == null || fileName.equals(""))
            return false;

        boolean retVal = false;
        try {
            startConnection();
            if(!replica)
                oos.writeObject("INSERT_FILE"); //local insert plus replica
            else
                oos.writeObject("INSERT_REPLICA"); //single insert request
            oos.writeObject(fileName);
            oos.flush();

            localInternalNode.get(s, fileName);
            retVal = ois.readBoolean();
        } catch (IOException e) {
            retVal = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        return retVal;
    }

    public boolean deleteFileRequest(String fileName, boolean replica){
        if(fileName == null || fileName.equals(""))
            return false;

        boolean retVal = false;
        try {
            startConnection();
            if(!replica)
                oos.writeObject("DELETE_FILE"); //replica removal needed
            else
                oos.writeObject("DELETE_REPLICA"); //single local removal
            oos.writeObject(fileName);
            oos.flush();

            retVal = ois.readBoolean();
        } catch (IOException e) {
            retVal = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        return retVal;
    }

    public boolean deleteFilesRequest(BigInteger from, BigInteger to){
        if(from == null || to == null){
            return false;
        }

        boolean retVal = false;

        try {
            startConnection();
            oos.writeObject("DELETE_REPLICAS");
            oos.writeObject(from);
            oos.writeObject(to);
            oos.flush();

            retVal = ois.readBoolean();
        } catch (IOException e) {
            retVal = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }

        return retVal;

    }

    public boolean hasFileRequest(String fileName){
        if(fileName == null || fileName.equals(""))
           return false;

        boolean result = false;
        try {
            startConnection();
            oos.writeObject("HAS_FILE");
            oos.writeObject(fileName);
            oos.flush();

            result = ois.readBoolean();
        } catch (IOException e) {
            result = false;
        }
        finally {
            try {
                closeConnection();
            } catch (IOException e) {}
        }


        return result;
    }
}
