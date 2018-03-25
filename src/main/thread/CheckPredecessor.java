package main.thread;

import main.node.InternalNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * This class represents a thread that aims to check if the predecessor node is alive. In case the predecessor
 * is not reachable, it is removed. Each internal node has a CheckPredecessor thread which is started during
 * the join procedure and repeats its operation after a small timeout.
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class CheckPredecessor extends Thread{

    private InternalNode internalNode;

    /**
     * Unique constructor of the class
     * @param n It is the internal node on which the thread runs
     */
    public CheckPredecessor(InternalNode n) {
        internalNode = n;
    }

    @Override
    public void run() {

        while(true){
            InetSocketAddress pred = internalNode.getPredAddress();
            boolean isReachable = false;

            //try to create a socket to the predecessor
            try {
                if(pred == null){
                    isReachable = false;
                }
                else{
                    (new Socket(pred.getAddress(), pred.getPort())).close();
                    isReachable = true;
                }

            } catch (IOException e) {
                isReachable = false;
            }

            //if not reachable, disable join operation and set invalid value as predecessor
            if(!isReachable){
                    internalNode.getAndSetJoinAvailable(false);
                    internalNode.setPredecessor(null);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
