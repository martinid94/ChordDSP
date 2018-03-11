package main;

import java.net.InetSocketAddress;

/**
 * Created by Marco on 04/03/2018.
 */
public class FixFinger extends Thread {

    private Node node;

    public FixFinger(Node n){
        node = n;
    }

    public void run(){
        int next = 0;
        while(true){
            next++;
            if(next >= Util.m){
                next = 1;
            }

            InetSocketAddress ith = node.findSuccessor(Util.ithStart(next + 1, node.getLocalId()));
            node.getfTable().updateIthFinger(next, ith);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
