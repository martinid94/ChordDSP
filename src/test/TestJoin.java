package test;

import main.node.InternalNode;
import main.util.Util;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by Marco on 10/03/2018.
 */
public class TestJoin {

    public static void main(String[] args) throws Exception {

        InetSocketAddress n1Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 4444);
        InetSocketAddress n2Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 5782);
        InetSocketAddress n6Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 7116);
        InetSocketAddress n4Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 5712);
        InetSocketAddress n3Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 6112);

        InternalNode n1 = new InternalNode(n1Soc, "C:\\Users\\Marco\\Desktop\\Ring\\n1\\");
        InternalNode n2 = new InternalNode(n2Soc, "C:\\Users\\Marco\\Desktop\\Ring\\n2\\");
        InternalNode n3 = new InternalNode(n3Soc, "C:\\Users\\Marco\\Desktop\\Ring\\n3\\");
        InternalNode n4 = new InternalNode(n4Soc, "C:\\Users\\Marco\\Desktop\\Ring\\n4\\");
        InternalNode n6 = new InternalNode(n6Soc, "C:\\Users\\Marco\\Desktop\\Ring\\n6\\");

        System.out.println("Position node1: " + Util.keyPosition(Util.hashAddress(n1Soc)) + " id: " + n1.getLocalId());
        System.out.println("Position node2: " + Util.keyPosition(Util.hashAddress(n2Soc)) + " id: " + n2.getLocalId());
        System.out.println("Position node3: " + Util.keyPosition(Util.hashAddress(n3Soc)) + " id: " + n3.getLocalId());
        System.out.println("Position node4: " + Util.keyPosition(Util.hashAddress(n4Soc)) + " id: " + n4.getLocalId());
        System.out.println("Position node6: " + Util.keyPosition(Util.hashAddress(n6Soc)) + " id: " + n6.getLocalId());

        n1.bootstrapJoin();
        System.out.println(n2.join(n1Soc));
        Thread.sleep(50);
        System.out.println(n6.join(n1Soc));
        Thread.sleep(50);
        System.out.println(n4.join(n1Soc));
        Thread.sleep(50);
        System.out.println(n3.join(n1Soc));

        Thread.sleep(10000);
        System.out.println("Succ of node1: " + n1.getSuccAddress());
        System.out.println("Succ of node2: " + n2.getSuccAddress());
        System.out.println("Succ of node4: " + n4.getSuccAddress());
        System.out.println("Succ of node3: " + n3.getSuccAddress());
        System.out.println("Pred of node3: " + n3.getPredAddress());
        System.out.println("Pred of node1: " + n1.getPredAddress());
        System.out.println("Pred of node2: " + n2.getPredAddress());
        System.out.println("Succ of node6: " + n6.getSuccAddress());
        System.out.println("Pred of node6: " + n6.getPredAddress());
        System.out.println("Pred of node4: " + n4.getPredAddress());

    }
}
