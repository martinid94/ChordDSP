package main;

import main.Connection.RingConnection;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Marco on 04/03/2018.
 */
public class Stabilizer extends Thread {

    private Node node;

    public Stabilizer(Node n) {
        node = n;
    }

    @Override
    public void run() {

        while(true){

            InetSocketAddress succ = node.getSuccAddress();
            if(succ == null){
                node.getfTable().fillSuccessor(node.getLocalAddress());
            }

            boolean isReachable = false;
            try {
                isReachable = succ.getAddress().isReachable(3000);

            } catch (IOException e) {}

            if(!isReachable){
                node.getfTable().deleteNode(succ);
                node.getfTable().fillSuccessor(node.getLocalAddress());
            }

            succ = node.getSuccAddress();
            RingConnection rc = new RingConnection(succ);
//            InetSocketAddress predSucc = rc.addressRequest("GET_PRED");
//
//            //bad connection
//            if(predSucc == null){
//                continue;
//            }

            //notify
            rc.setPredecessorRequest(node.getLocalAddress());


            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
