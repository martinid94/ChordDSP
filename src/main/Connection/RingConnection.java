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

        try{
            startConnection();
            oos.writeObject("FIND_SUCC"); //the bootstrap node must execute a find_successor(localId) procedure
            oos.writeObject(localId);
            oos.flush();

            mySucc = (InetSocketAddress) ois.readObject();
        }
        catch(IOException ioe) {
            return null;
        }
        catch(ClassNotFoundException cnfe) {
            return null;
        }

        finally {
            try
            {
                closeConnection();
            }
            catch(IOException ioe2)
            {
                ioe2.printStackTrace();
            }
        }

        return mySucc;
    }


    public InetSocketAddress joinRequest(InetSocketAddress myself){
        return null;
    }
}
