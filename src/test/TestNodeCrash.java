package test;

import main.InternalNode;
import main.Util;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by Marco on 10/03/2018.
 */
public class TestNodeCrash {
    public static void main(String[] args) throws Exception {

        InetSocketAddress n5Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 7812);

        InternalNode n5 = new InternalNode(n5Soc, "C:\\Users\\Marco\\Desktop\\Client2\\");
        System.out.println("Position node5: " + Util.keyPosition(Util.hashAdress(n5Soc)) + " id: " + n5.getLocalId());

        InetSocketAddress n1Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 4444);
        System.out.println(n5.join(n1Soc));


        Thread.sleep(5000);
        System.out.println("Succ of node5: " + n5.getSuccAddress());
        System.out.println("Pred of node5: " + n5.getPredAddress());

        System.out.println("Finger Table node5 ---------------------------------\n" + n5.getfTable());

//        InetSocketAddress n3Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 6112);
//
//        InternalNode n3 = new InternalNode(n3Soc, "C:\\Users\\Marco\\Desktop\\Client2\\");
//        System.out.println("Position node3: " + Util.keyPosition(Util.hashAdress(n3Soc)) + " id: " + n3.getLocalId());
//
//        InetSocketAddress n1Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 4444);
//        System.out.println(n3.join(n1Soc));
//
//
//        Thread.sleep(5000);
//        System.out.println("Succ of node3: " + n3.getSuccAddress());
//        System.out.println("Pred of node3: " + n3.getPredAddress());
//
//        System.out.println("Finger Table node3 ---------------------------------\n" + n3.getfTable());
    }
}
