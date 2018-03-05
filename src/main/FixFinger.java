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
        int next = -1;
        while(true){
            next++;
            if(next >= Util.m){
                next = 0;
            }

            InetSocketAddress ith = node.findSuccessor(node.getLocalId().add(Util.powerOfTwo(next)));
            node.getfTable().updateIthFinger(next, ith);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
