package main.Connection;

import main.Node;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Davide on 25/02/2018.
 */
public class FileConnection  extends Connection{
    private Node localNode;
    private RingConnection rc;

    public FileConnection(Node n, InetSocketAddress nodeAd) {
        super(nodeAd);
        localNode = n;
        rc = new RingConnection(nodeAd);
    }

    public boolean getFileRequest(String fileName) throws IOException, ClassNotFoundException {
        if(fileName == null || fileName.equals(""))
            throw new IllegalArgumentException("Invalid argument");

        for(int i = 0; i < 2; i++) {
            startConnection();
            oos.writeObject("GET_FILE");
            oos.writeObject(fileName);
            oos.flush();

            boolean hasFile = ois.readBoolean();
            if(!hasFile) {
                InetSocketAddress newContact = rc.addressRequest("GET_PRED");
                nodeAddress = newContact;
                closeConnection();
            }
            else {
                boolean retVal = localNode.singleInsert(s, fileName);
                closeConnection();
                return retVal;
            }
        }
        return false;
    }

    public ArrayList<String> fileIntervalRequest(BigInteger from, BigInteger to) throws IOException, ClassNotFoundException {
        if(from == null || to == null)
            throw new IllegalArgumentException("Arguments can not be null");

        startConnection();
        oos.writeObject("GET_FILE_INTERVAL"); // insert flush here?
        oos.writeObject(from);
        oos.writeObject(to);
        oos.flush();

        ArrayList<String> retVal = (ArrayList<String>) ois.readObject();
        closeConnection();

        return retVal;
    }

    public boolean insertFileRequest(String fileName, boolean replica) throws IOException, ClassNotFoundException {
        if(fileName == null || fileName.equals(""))
            throw new IllegalArgumentException("Invalid argument");

        startConnection();
        if(!replica)
            oos.writeObject("INSERT_FILE"); //local insert plus replica
        else
            oos.writeObject("INSERT_REPLICA"); //single insert request
        oos.writeObject(fileName);
        oos.flush();

        boolean retVal = localNode.get(s, fileName);
        closeConnection();
        return retVal;
    }

    public boolean deleteFileRequest(String fileName, boolean replica) throws IOException, ClassNotFoundException {
        if(fileName == null || fileName.equals(""))
            throw new IllegalArgumentException("Invalid argument");

        startConnection();
        if(!replica)
            oos.writeObject("DELETE_FILE"); //replica removal needed
        else
            oos.writeObject("DELETE_REPLICA"); //single local removal
        oos.writeObject(fileName);
        oos.flush();

        boolean retVal = ois.readBoolean();
        closeConnection();
        return retVal;
    }
}
