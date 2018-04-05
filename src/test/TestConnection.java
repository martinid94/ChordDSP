package test;

import main.connection.RingConnection;
import main.thread.Listener;
import main.node.InternalNode;
import main.util.Util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by Marco on 24/02/2018.
 */
public class TestConnection extends Thread{

    private InetSocketAddress serverAddress;

    public TestConnection(InetSocketAddress serverAddress){
        this.serverAddress = serverAddress;
    }

    public static void main(String[] args) {

        InetSocketAddress serverAddress = null;
        try {
            serverAddress = new InetSocketAddress(InetAddress.getByName("localhost"), 4444);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Listener l = new Listener(new InternalNode(serverAddress, "C:\\Users\\Marco\\Desktop\\Client2\\"));
        l.start();

        TestConnection t1 = new TestConnection(serverAddress);
        TestConnection t2 = new TestConnection(serverAddress);

        t1.start();
        t2.start();
    }

    public void run() {
        RingConnection rc = new RingConnection(serverAddress);
        System.out.println(rc.findSuccessorRequest(Util.powerOfTwo(3)));


    }
}
