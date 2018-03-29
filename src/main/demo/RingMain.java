package main.demo;

import main.node.InternalNode;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This class create the Chord ring with a single node.
 *
 *
 * @author Alfonso Marco
 * @author Martini Davide
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

        while(true){
            System.out.print("Insert command: ");
            String lineCommand = sc.nextLine();
            if(lineCommand.equals("")){
                continue;
            }
            UtilMain.readExecInternalNodeCommand(lineCommand, n);
        }

    }
}
