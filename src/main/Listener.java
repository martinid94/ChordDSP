package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Marco on 24/02/2018.
 */
public class Listener extends Thread {
    private Node node;
    private ServerSocket ss;
    private boolean isLive;
    //eventuale pool esecutori

    public Listener(Node node){
        this.node = node;
        int port = node.getLocalAddress().getPort();

        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException("\nCannot open server port " + port + ".\n", e);
        }
    }

    public void run(){
        while(true){
            Socket s = null;

            try {
                s = ss.accept();
            } catch (IOException e) {
                throw new RuntimeException("\nError on receiving request!\n", e);
            }

            //TODO avviare thread esecutore o eventuale thread nel pool
            (new Executor(s, node)).start();
        }
    }
}
