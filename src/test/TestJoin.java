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
        InetSocketAddress n4Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 5712);
        InetSocketAddress n5Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 7812);
        Node n1 = new Node(n1Soc);
        Node n2 = new Node(n2Soc);
        Node n3 = new Node(n3Soc);
        Node n4 = new Node(n4Soc);
        Node n5 = new Node(n5Soc);
        System.out.println("Position node1: " + Util.keyPosition(Util.hashAdress(n1Soc)) + " id: " + n1.getLocalId());
        System.out.println("Position node2: " + Util.keyPosition(Util.hashAdress(n2Soc)) + " id: " + n2.getLocalId());
        System.out.println("Position node3: " + Util.keyPosition(Util.hashAdress(n3Soc)) + " id: " + n3.getLocalId());
        System.out.println("Position node4: " + Util.keyPosition(Util.hashAdress(n4Soc)) + " id: " + n4.getLocalId());
        System.out.println("Position node5: " + Util.keyPosition(Util.hashAdress(n5Soc)) + " id: " + n5.getLocalId());
        n1.bootstrapJoin();
        System.out.println(n2.join(n1Soc));
        Thread.sleep(50);
        System.out.println(n3.join(n1Soc));
        Thread.sleep(50);
        System.out.println(n4.join(n1Soc));
        Thread.sleep(50);
        System.out.println(n5.join(n1Soc));
        Thread.sleep(30000);
        System.out.println("Succ of node1: " + n1.getSuccAddress());
        System.out.println("Succ of node2: " + n2.getSuccAddress());
        System.out.println("Succ of node3: " + n3.getSuccAddress());
        System.out.println("Succ of node4: " + n4.getSuccAddress());
        System.out.println("Succ of node5: " + n5.getSuccAddress());
        System.out.println("Pred of node1: " + n1.getPredAddress());
        System.out.println("Pred of node2: " + n2.getPredAddress());
        System.out.println("Pred of node3: " + n3.getPredAddress());
        System.out.println("Pred of node4: " + n4.getPredAddress());
        System.out.println("Pred of node5: " + n5.getPredAddress());
        System.out.println("Finger Table node1 ---------------------------------\n" + n1.getfTable());
        System.out.println("Finger Table node2 ---------------------------------\n" + n2.getfTable());
        System.out.println("Finger Table node3 ---------------------------------\n" + n3.getfTable());
        System.out.println("Finger Table node4 ---------------------------------\n" + n4.getfTable());
        System.out.println("Finger Table node5 ---------------------------------\n" + n5.getfTable());
//        Thread.sleep(15000);
//        System.out.println("--------------------------------------");
//        System.out.println("Succ of node1: " + n1.getSuccAddress());
//        System.out.println("Succ of node2: " + n2.getSuccAddress());
//        System.out.println("Succ of node3: " + n3.getSuccAddress());
//        System.out.println("Pred of node1: " + n1.getPredAddress());
//        System.out.println("Pred of node2: " + n2.getPredAddress());
//        System.out.println("Pred of node3: " + n3.getPredAddress());

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
