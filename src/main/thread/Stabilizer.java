package main.thread;

import main.connection.RingConnection;
import main.node.InternalNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Marco on 04/03/2018.
 */
public class Stabilizer extends Thread {

    private InternalNode internalNode;

    public Stabilizer(InternalNode n) {
        internalNode = n;
    }

    @Override
    public void run() {

        while(true){

            InetSocketAddress succ = internalNode.getSuccAddress();
//            if(succ == null){
//                internalNode.getfTable().fillSuccessor(internalNode.getLocalAddress());
//            }

            boolean isReachable = false;
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

            if(!isReachable){
                internalNode.getfTable().deleteNode(succ);
                internalNode.getfTable().fillSuccessor(internalNode);
                succ = internalNode.getSuccAddress();
                internalNode.setSuccessor(succ);
                RingConnection rc = new RingConnection(succ);
                //notify
                rc.setPredecessorRequest(internalNode.getLocalAddress());
            }


//            InetSocketAddress predSucc = rc.addressRequest("GET_PRED");
//
//            //bad connection
//            if(predSucc == null){
//                continue;
//            }



            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
