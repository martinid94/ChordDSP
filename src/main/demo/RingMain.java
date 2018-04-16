package main.demo;

import main.node.InternalNode;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This class create the Chord ring with a single node.
 *
 *
 * @author Alfonso Marco
 *
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class RingMain {

    public static void main(String[] args) {

        InetSocketAddress nodeAddress = UtilMain.verifyArgs(args);
        InternalNode n = new InternalNode(nodeAddress, args[2]);
        if(n.bootstrapJoin()){
            System.out.println("Ring create successfully!");
        }
        else{
            System.out.println("Error! The ring is not created!");
            System.exit(1);
        }

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
