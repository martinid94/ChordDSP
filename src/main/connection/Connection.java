package main.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Davide on 24/02/2018.
 */
public abstract class Connection {

    protected InetSocketAddress nodeAddress;
    protected Socket s;
    protected ObjectOutputStream oos;
    protected ObjectInputStream ois;

    /**
     * Unique constructor of the class
     * @param nodeAd specifies the address of the node we want to contact
     */
    public Connection(InetSocketAddress nodeAd){
        nodeAddress = nodeAd;
        s = null;
        oos = null;
        ois = null;
    }

    /**
     * Method to start a connection with a node
     * @throws IOException
     */
    protected void startConnection() throws  IOException, IllegalArgumentException {
        if(nodeAddress == null){
            throw new IOException();
        }
        s = new Socket(nodeAddress.getAddress(), nodeAddress.getPort());
        oos = new ObjectOutputStream(s.getOutputStream());
        ois = new ObjectInputStream(s.getInputStream());
    }

    /**
     * Method to close a connection with a node
     * @throws IOException
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
