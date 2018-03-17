package test;

import main.Connection.ExternalFileConnection;
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
        String fileName = "prova.pdf";

        BigInteger fileId = Util.hashFile(fileName);
        System.out.println(fileId);


        ExternalFileConnection efc = new ExternalFileConnection(n1Soc, fileName);
//        efc.insertFileRequest(pathFile);
        efc.getFileRequest(pathFile);
        System.out.println("ok");

    }
}
