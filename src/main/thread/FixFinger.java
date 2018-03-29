package main.thread;

import main.node.InternalNode;
import main.util.Util;

import java.net.InetSocketAddress;

/**
 * This class extends the class Thread and it performs an update of a single row of the finger table every 100 milliseconds..
 * In particular this class provides a simple and effective way to refresh finger table entries (one entry is updated every 100 milliseconds).
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class FixFinger extends Thread {

    private InternalNode internalNode;

    /**
     * Unique constructor of the class
     *
     * @param n It is the internal node in which finger table entries are updated.
     */
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
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
