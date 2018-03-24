package main.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * This class provides the basic methods to start and close a connection with a specified host
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class Connection {

    protected InetSocketAddress nodeAddress;
    protected Socket s;
    protected ObjectOutputStream oos;
    protected ObjectInputStream ois;

    /**
     * Unique constructor of the class
     * @param nodeAd It specifies the address of the node that the invoker wants to contact
     */
    public Connection(InetSocketAddress nodeAd){
        nodeAddress = nodeAd;
        s = null;
        oos = null;
        ois = null;
    }

    /**
     * This method starts a connection with a contact node
     * @throws IOException In case the contact node is null or the connection fails
     */
    protected void startConnection() throws  IOException {

        //invalid contact
        if(nodeAddress == null){
            throw new IOException();
        }
        s = new Socket(nodeAddress.getAddress(), nodeAddress.getPort());
        oos = new ObjectOutputStream(s.getOutputStream());
        ois = new ObjectInputStream(s.getInputStream());
    }

    /**
     * This method closes a connection which was opened with a contact node
     * @throws IOException In case the connection closing fails
     */
    protected void closeConnection() throws IOException {
        if(ois != null)
            ois.close();

        if(oos != null)
            oos.close();

        if(s != null)
            s.close();
    }
}
