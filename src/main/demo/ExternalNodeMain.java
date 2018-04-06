package main.demo;

import main.node.ExternalNode;

import java.util.Scanner;

/**
 * This class create a node which is external from the Chord ring.
 * It implements an interactive command line in order to execute task in the external node e connection with the ring.
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class ExternalNodeMain {

    public static void main(String[] args) {

        if(args.length != 1){
            System.err.println("Error! You must insert the correct path where to find the file to upload or to put the file to be downloaded\n" +
                    "Example: InternalNodeMain C:\\Users\\NomeUtente\\Files");
            System.exit(1);
        }

        ExternalNode node = new ExternalNode(args[0]);

        Scanner sc = new Scanner(System.in);
        System.out.print("Insert command: ");

        while(sc.hasNextLine()){
            String lineCommand = sc.nextLine();
            if(!lineCommand.equals("")){
                UtilMain.readExecExternalNodeCommand(lineCommand, node);
            }

            System.out.print("Insert command: ");
        }
    }
}
