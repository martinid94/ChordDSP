package main;

import java.io.IOException;
import java.net.InetSocketAddress;

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

            try {
                if(!pred.getAddress().isReachable(3000)){
                    node.setPredecessor(null);
                }
            } catch (IOException e) {}

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
