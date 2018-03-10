package test;

import main.Node;
import main.Util;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by Marco on 10/03/2018.
 */
public class TestJoin {

    public static void main(String[] args) throws Exception {

        InetSocketAddress n1Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 4444);
        InetSocketAddress n2Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 5782);
        InetSocketAddress n3Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 6112);
        Node n1 = new Node(n1Soc);
        Node n2 = new Node(n2Soc);
        Node n3 = new Node(n3Soc);
        System.out.println("Position node1: " + Util.keyPosition(Util.hashAdress(n1Soc)));
        System.out.println("Position node2: " + Util.keyPosition(Util.hashAdress(n2Soc)));
        System.out.println("Position node3: " + Util.keyPosition(Util.hashAdress(n3Soc)));
        n1.bootstrapJoin();
        System.out.println(n2.join(n1Soc));
        Thread.sleep(500);
        System.out.println(n3.join(n1Soc));
        System.out.println("ok");
        Thread.sleep(10000);
        System.out.println("Succ of node1: " + n1.getSuccAddress());
        System.out.println("Succ of node2: " + n2.getSuccAddress());
        System.out.println("Succ of node3: " + n3.getSuccAddress());
        System.out.println("Pred of node1: " + n1.getPredAddress());
        System.out.println("Pred of node2: " + n2.getPredAddress());
        System.out.println("Pred of node3: " + n3.getPredAddress());
        Thread.sleep(15000);
        System.out.println("--------------------------------------");
        System.out.println("Succ of node1: " + n1.getSuccAddress());
        System.out.println("Succ of node2: " + n2.getSuccAddress());
        System.out.println("Succ of node3: " + n3.getSuccAddress());
        System.out.println("Pred of node1: " + n1.getPredAddress());
        System.out.println("Pred of node2: " + n2.getPredAddress());
        System.out.println("Pred of node3: " + n3.getPredAddress());

//        System.out.println("joinAvailable n1: " + n1.isJoinAvailable());
//        System.out.println("joinAvailable n2: " + n2.isJoinAvailable());
//        System.out.println("joinAvailable n3: " + n3.isJoinAvailable());
//        System.out.println(n2.leave());
//        Thread.sleep(500);
//        System.out.println("After leave---------------------");
//        System.out.println("Succ of node1: " + n1.getSuccAddress());
//        System.out.println("Succ of node3: " + n3.getSuccAddress());
//        System.out.println("Pred of node1: " + n1.getPredAddress());
//        System.out.println("Pred of node3: " + n3.getPredAddress());


    }
}
