package test;

import main.Connection.ExternalFileConnection;
import main.Connection.RingConnection;
import main.ExternalNode;
import main.Util;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by Marco on 15/03/2018.
 */
public class TestInsertFile {

    public static void main(String[] args) throws Exception {
        InetSocketAddress n1Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 4444);

        String pathFile = "C:\\Users\\Marco\\Desktop\\Client1\\";
        String fileName1 = "prova.pdf";
        String fileName2 = "ER-model.pdf";
        String fileName3 = "organisation.pdf";
        String fileName4 = "introduction.pdf";
        String fileName5 = "design.pdf";

        ExternalNode mySelf = new ExternalNode(pathFile);

        System.out.println(fileName1 + " hash: " + Util.hashFile(fileName1));
        System.out.println(fileName2 + " hash: " + Util.hashFile(fileName2));
        System.out.println(fileName3 + " hash: " + Util.hashFile(fileName3));
        System.out.println(fileName4 + " hash: " + Util.hashFile(fileName4));
        System.out.println(fileName5 + " hash: " + Util.hashFile(fileName5));

        RingConnection rc = new RingConnection(n1Soc);
        InetSocketAddress n = rc.findSuccessorRequest(Util.hashFile(fileName1));
        ExternalFileConnection efc = new ExternalFileConnection(mySelf, n);
        System.out.println("Return value: " + efc.insertFileRequest(fileName1));


        n = rc.findSuccessorRequest(Util.hashFile(fileName2));
        efc = new ExternalFileConnection(mySelf, n);
        System.out.println("Return value: " + efc.insertFileRequest(fileName2));

        n = rc.findSuccessorRequest(Util.hashFile(fileName3));
        efc = new ExternalFileConnection(mySelf, n);
        System.out.println("Return value: " + efc.insertFileRequest(fileName3));

        n = rc.findSuccessorRequest(Util.hashFile(fileName4));
        efc = new ExternalFileConnection(mySelf, n);
        System.out.println("Return value: " + efc.insertFileRequest(fileName4));

        n = rc.findSuccessorRequest(Util.hashFile(fileName5));
        efc = new ExternalFileConnection(mySelf, n);
        System.out.println("Return value: " + efc.insertFileRequest(fileName5));

    }
}
