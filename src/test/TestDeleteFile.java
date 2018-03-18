package test;

import main.Connection.ExternalFileConnection;
import main.Connection.RingConnection;
import main.ExternalNode;
import main.Util;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by Marco on 18/03/2018.
 */
public class TestDeleteFile {

    public static void main(String[] args) throws Exception {
        InetSocketAddress n1Soc = new InetSocketAddress(InetAddress.getByName("localhost"), 4444);

        String pathFile = "C:\\Users\\Marco\\Desktop\\Client1\\";
        String fileName = "prova.pdf";

        BigInteger fileId = Util.hashFile(fileName);
        System.out.println(fileId);
        RingConnection rc = new RingConnection(n1Soc);
        InetSocketAddress n = rc.findSuccessorRequest(fileId);


        ExternalNode mySelf = new ExternalNode(pathFile);
        ExternalFileConnection efc = new ExternalFileConnection(mySelf, n);

        //efc.getFileRequest(pathFile); efc.insertFileRequest(fileName)
        System.out.println("Return value: " + efc.deleteFileRequest(fileName));

    }
}
