package main;

import main.Connection.InternalFileConnection;
import main.Connection.RingConnection;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Created by Marco on 04/03/2018.
 */
public class FileUpdater extends Thread {

    private InetSocketAddress contactAddr;
    private InternalNode internalNode;
    private BigInteger from;
    private BigInteger to;
    private boolean isJoin;

    public FileUpdater(InetSocketAddress contact, InternalNode internalNode, BigInteger from, BigInteger to, boolean isJoin){
        contactAddr = contact;
        this.internalNode = internalNode;
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

            internalNode.getAndSetJoinAvailable(true);

            InternalFileConnection fc = new InternalFileConnection(internalNode, internalNode.getSuccAddress());
            if(!fc.deleteFilesRequest(Util.hashAdress(internalNode.getPredAddress()), internalNode.getLocalId())){
                fc.deleteFilesRequest(Util.hashAdress(internalNode.getPredAddress()), internalNode.getLocalId());
            }

            fc = new InternalFileConnection(internalNode, internalNode.getPredAddress());
            if(!fc.deleteFilesRequest(internalNode.getLocalId(), Util.hashAdress(internalNode.getSuccAddress()))){
                fc.deleteFilesRequest(internalNode.getLocalId(), Util.hashAdress(internalNode.getSuccAddress()));
            }
        }else{

            internalNode.getAndSetJoinAvailable(true);
        }

    }

    private boolean update(){

        InternalFileConnection fc = new InternalFileConnection(internalNode, contactAddr);
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
