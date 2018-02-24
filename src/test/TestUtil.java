package test;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static main.Util.*;

/**
 * Created by Marco on 24/02/2018.
 */
public class TestUtil {
    public static void main(String[] args){
        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        InetSocketAddress ad = null;
        try {
            ad = new InetSocketAddress(InetAddress.getLocalHost(), 4040);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println(hashAdress(ad));
        System.out.println(hashFile("prova.pdf"));
        System.out.println(keyPosition(hashAdress(ad)));
        System.out.println(keyPosition(hashFile("prova.txt")));
        System.out.println("Test ithStart: " + testithStart());

    }

    private static BigInteger testithStart(){
        int i = 3;
        byte[] base = {4};
        BigInteger id = new BigInteger(base);
        return ithStart(i, id);
    }
}
