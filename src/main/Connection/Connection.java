package main.Connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.InputMismatchException;

/**
 * Created by Davide on 24/02/2018.
 */
public abstract class Connection {

    protected InetSocketAddress nodeAddress;
    protected Socket s;
    protected ObjectOutputStream oos;
    protected ObjectInputStream ois;

    public Connection(InetSocketAddress nodeAd){
        nodeAddress = nodeAd;
        s = null;
        oos = null;
        ois = null;
    }

    public InetSocketAddress getNodeAddress() {
        return nodeAddress;
    }

//    public void setNodeAddress(InetSocketAddress nodeAddress) {
//        this.nodeAddress = nodeAddress;
//    }

    /**
     * Method to start a connection
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
     * Method to close a connection
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
