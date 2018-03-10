package main;

import main.Connection.RingConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
                (new Socket(succ.getAddress(), succ.getPort())).close();
                isReachable = true;

            } catch (IOException e) {
                isReachable = false;
            }

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
