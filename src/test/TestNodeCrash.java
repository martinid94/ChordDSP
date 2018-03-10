package test;

import main.Node;
import main.Util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by Marco on 10/03/2018.
 */
public class TestNodeCrash {
    public static void main(String[] args) throws Exception {
        InetSocketAddress n4Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 7112);
        Node n4 = new Node(n4Soc);
        System.out.println("Position node4: " + Util.keyPosition(Util.hashAdress(n4Soc)));
        InetSocketAddress n1Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 4444);
        System.out.println(n4.join(n1Soc));
    }
}
