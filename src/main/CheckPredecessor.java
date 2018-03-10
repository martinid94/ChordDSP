package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Marco on 04/03/2018.
 */
public class CheckPredecessor extends Thread{

    private Node node;

    public CheckPredecessor(Node n) {
        node = n;
    }

    @Override
    public void run() {

        while(true){

            InetSocketAddress pred = node.getPredAddress();

            boolean isReachable = false;
            try {
                (new Socket(pred.getAddress(), pred.getPort())).close();
                isReachable = true;

            } catch (IOException e) {
                isReachable = false;
            }

            if(!isReachable){
                    node.getAndSetJoinAvailable(false);
                    node.setPredecessor(null);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
