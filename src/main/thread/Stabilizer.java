package main.thread;

import main.connection.RingConnection;
import main.node.InternalNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * This class represents a thread that aims to stabilize the Chord ring after an unexpected instability (i.e. a
 * node crashes). Each internal node has a Stabilizer thread which is started during the join procedure.
 * In particular, it is responsible to check if the successor is not alive. If this situation happens, it updates the
 * current node's finger table and sets the new successor's predecessor (i.e. the current node)
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class Stabilizer extends Thread {

    private InternalNode internalNode;

    /**
     * Unique constructor of the class
     * @param n It is the internal node on which the thread runs
     */
    public Stabilizer(InternalNode n) {
        internalNode = n;
    }

    @Override
    public void run() {

        while(true){
            InetSocketAddress succ = internalNode.getSuccAddress();
            boolean isReachable = false;

            //try to create a socket to successor
            try {
                if(succ == null){
                    isReachable = false;
                }
                else{
                    (new Socket(succ.getAddress(), succ.getPort())).close();
                    isReachable = true;
                }

            } catch (IOException e) {
                isReachable = false;
            }

            //if it is not reachable, delete it from the finger table then find the new one
            //and contact it to set the current node as its new predecessor
            if(!isReachable){
                internalNode.getfTable().deleteNode(succ);
                internalNode.getfTable().fillSuccessor(internalNode);
                succ = internalNode.getSuccAddress();
                internalNode.setSuccessor(succ);
                RingConnection rc = new RingConnection(succ);
                //notify
                rc.setPredecessorRequest(internalNode.getLocalAddress());
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
