package test;

import main.node.InternalNode;
import main.util.Util;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by Marco on 10/03/2018.
 */
public class TestNodeCrash {
    public static void main(String[] args) throws Exception {

        InetSocketAddress n5Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 5000);

        InternalNode n5 = new InternalNode(n5Soc, "C:\\Users\\Marco\\Desktop\\Client2\\");
        System.out.println("Position node5: " + Util.keyPosition(Util.hashAddress(n5Soc)) + " id: " + n5.getLocalId());

        InetSocketAddress n1Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 4444);
        System.out.println(n5.join(n1Soc));


        Thread.sleep(5000);
        System.out.println("Succ of node5: " + n5.getSuccAddress());
        System.out.println("Pred of node5: " + n5.getPredAddress());

        Thread.sleep(10000);
        System.out.println("leave: " + n5.leave());

    }
}
