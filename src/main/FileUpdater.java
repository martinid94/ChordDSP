package main;

import main.Connection.FileConnection;
import main.Connection.RingConnection;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Created by Marco on 04/03/2018.
 */
public class FileUpdater extends Thread {

    private InetSocketAddress contactAddr;
    private Node node;
    private BigInteger from;
    private BigInteger to;
    private boolean isJoin;

    public FileUpdater(InetSocketAddress contact, Node node, BigInteger from, BigInteger to, boolean isJoin){
        contactAddr = contact;
        this.node = node;
        this.from =from;
        this.to = to;
        this.isJoin = isJoin;
    }

    @Override
    public void run() {
        if(!update()){
            update();
        }

        if(isJoin){
            RingConnection rc = new RingConnection(contactAddr);
            if(rc.getAndSetAvailabilityRequest(true) == null){
                rc.getAndSetAvailabilityRequest(true);
            }

            node.getAndSetJoinAvailable(true);

            FileConnection fc = new FileConnection(node, node.getSuccAddress());
            if(!fc.deleteFilesRequest(Util.hashAdress(node.getPredAddress()), node.getLocalId())){
                fc.deleteFilesRequest(Util.hashAdress(node.getPredAddress()), node.getLocalId());
            }

            fc = new FileConnection(node, node.getPredAddress());
            if(!fc.deleteFilesRequest(node.getLocalId(), Util.hashAdress(node.getSuccAddress()))){
                fc.deleteFilesRequest(node.getLocalId(), Util.hashAdress(node.getSuccAddress()));
            }
        }else{

            node.getAndSetJoinAvailable(true);
        }

    }

    private boolean update(){

        FileConnection fc = new FileConnection(node, contactAddr);
        ArrayList<String> fileToGet = fc.fileIntervalRequest(from, to);

        if(fileToGet == null){
            return false;
        }

        for(String s : fileToGet){
            if(!fc.getFileRequest(s)){
                fc.getFileRequest(s);
            }
        }

        return true;
    }

}
