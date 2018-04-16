package main.demo;

import main.node.InternalNode;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This class create a node which is in the Chord ring.
 * It implements an interactive command line in order to execute task in the internal node.
 *
 * @author Alfonso Marco
 *
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class InternalNodeMain {

    public static void main(String[] args) {

        InetSocketAddress nodeAddress = UtilMain.verifyArgs(args);
        InternalNode n = new InternalNode(nodeAddress, args[2]);
        Scanner sc = new Scanner(System.in);
        System.out.print("Insert command: ");

        while(sc.hasNextLine()){

            String lineCommand = sc.nextLine();
            if(!lineCommand.equals("")){
                UtilMain.readExecInternalNodeCommand(lineCommand, n);
            }

            System.out.print("Insert command: ");
        }

    }


}
