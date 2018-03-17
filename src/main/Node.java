package main;

import java.net.Socket;

/**
 * Created by Marco on 17/03/2018.
 */
public interface Node {


    boolean singleInsert(Socket s, String fileName);
    boolean get(Socket s, String fileName);
    boolean singleDelete(String fileName);
}
