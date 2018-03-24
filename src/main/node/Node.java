package main.node;

import java.net.Socket;

/**
 * This interface provides all the methods that both internal and external nodes have to implement.
 * These mainly deal with file management.
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering..
 */
public interface Node {

    boolean singleInsert(Socket s, String fileName);
    boolean get(Socket s, String fileName);
}
