package main.Connection;

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

    public Connection(InetSocketAddress nodeAd) {
        nodeAddress = nodeAd;
        s = null;
        oos = null;
        ois = null;
    }

    /**
     * Method to start a connection
     * @throws IOException
     */
    protected void startConnection() throws  IOException {
        s = new Socket(nodeAddress.getAddress(), nodeAddress.getPort());
        oos = new ObjectOutputStream(s.getOutputStream());
        ois = new ObjectInputStream(s.getInputStream());
    }

    /**
     * Method to close a connection
     * @throws IOException
     */
    protected void closeConnection() throws IOException {
        ois.close();
        oos.close();
        s.close();
    }
}
