package main.Connection;

import main.Node;

import java.net.InetSocketAddress;

/**
 * Created by Marco on 15/03/2018.
 */
public class ExternalFileConnection extends FileConnection {

    private RingConnection rc;

    public ExternalFileConnection(Node n, InetSocketAddress node){
        super(n, node);
        rc = new RingConnection(node);
    }

    public boolean getFileRequest(String fileName) {
        boolean value = super.getFileRequest(fileName);

        if(!value){
            InetSocketAddress newContact = rc.addressRequest("GET_PRED");
            nodeAddress = newContact;
            value = super.getFileRequest(fileName);
        }

        return value;
    }

    public boolean insertFileRequest(String fileName){
        return super.insertFileRequest(fileName);

    }

    public boolean deleteFileRequest(String fileName){
        return super.deleteFileRequest(fileName);
    }
}
