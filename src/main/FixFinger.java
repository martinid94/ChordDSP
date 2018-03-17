package main;

import java.net.InetSocketAddress;

/**
 * Created by Marco on 04/03/2018.
 */
public class FixFinger extends Thread {

    private InternalNode internalNode;

    public FixFinger(InternalNode n){
        internalNode = n;
    }

    public void run(){
        int next = 0;
        while(true){
            next++;
            if(next >= Util.m){
                next = 1;
            }

            InetSocketAddress ith = internalNode.findSuccessor(Util.ithStart(next + 1, internalNode.getLocalId()));
            internalNode.getfTable().updateIthFinger(next, ith);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
