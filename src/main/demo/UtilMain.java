package main.demo;

import main.util.Util;
import main.connection.ExternalFileConnection;
import main.connection.RingConnection;
import main.node.ExternalNode;
import main.node.InternalNode;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * This class implements some utilities method for ExternalNodeMain, InternalNodeMain and RingMain classes.
 *
 *
 * @author Alfonso Marco
 *
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */

public class UtilMain {

    private enum FileRequest {
        insert,
        get,
        delete,
        hasFile
    }

    /**
     * This method is called to read and execute a specific command for an internal node
     *
     * @param lineCom It is the line that contains the command followed by params
     * @param node It is the internal node that will execute the command
     */
    public static void readExecInternalNodeCommand(String lineCom, InternalNode node){
        Scanner c = new Scanner(lineCom);
        String command = c.next();
        String param = null;

        if(c.hasNext()){
            param = c.next();
        }

        switch (command){
            case "join":
                if(param == null || !c.hasNext()){
                    System.out.println("\rYou must insert ip address and port of a bootstrap node!");
                    break;
                }

                InetSocketAddress bootstrapAddress = null;

                try {
                    bootstrapAddress = new InetSocketAddress(InetAddress.getByName(param), Integer.parseInt(c.next()));
                }catch (UnknownHostException e) {
                    System.out.println("\rYou must insert the correct ip address (or host name)!");
                    break;
                }catch (NumberFormatException nfe){
                    System.err.println("\rYou must insert a correct port number!");
                    break;
                }

                if(node.join(bootstrapAddress)){
                    System.out.println("\rThe node joins the ring successfully!\n" + "Position in the ring: " +
                            Util.keyPosition(node.getLocalId()) + "%");
                }
                else{
                    System.out.println("\rError! The node doesn't join the ring!");
                }

                break;
            case "leave":
                if(node.leave()){
                    System.out.println("\rThe node leaves the ring successfully!");
                    System.exit(0);
                }
                else{
                    System.out.println("\rError! The node doesn't leave the ring!");
                }
                break;
            case "mySuccessor":
                System.out.println("\rSuccessor is: " + node.getSuccAddress());
                break;
            case "myPredecessor":
                System.out.println("\rPredecessor is: " + node.getPredAddress());
                break;
            case "fingerTable":
                System.out.println("\rFinger table ------------------------------------\n" + node.getfTable().toString());
                break;
            case "position":
                System.out.println("\rPosition in the ring: " + Util.keyPosition(node.getLocalId()) + "%");
                break;
            case "key":
                System.out.println("\rKey of the node: " + node.getLocalId());
                break;
            case "ring":
                ring(node);
                break;
            case "fileList":
                System.out.println("\rFile stored in this node:");
                System.out.println(node.getFiles());
                break;
            case "help":
                helpInternalNode();
                break;
            case "exit":
                System.exit(0);
                break;
            default:
                System.out.println("\rYou must insert a correct command!");
                helpInternalNode();
                break;
        }

    }

    /**
     * This method is called to read and execute a specific command for an external node
     *
     * @param lineCom It is the line that contains the command followed by params
     * @param node It is the external node that will execute the command
     */
    public static void readExecExternalNodeCommand(String lineCom, ExternalNode node){
        Scanner c = new Scanner(lineCom);
        String command = c.next();


        switch (command){
            case "insert":
                manageFile(c, node, FileRequest.insert);
                break;
            case "get":
                manageFile(c, node, FileRequest.get);
                break;
            case "delete":
                manageFile(c, node, FileRequest.delete);
                break;
            case "hasFile":
                manageFile(c, node, FileRequest.hasFile);
                break;
            case "exit":
                System.exit(0);
                break;
            case "help":
                helpExternalNode();
                break;
            default:
                System.out.println("\rYou must insert a correct command!");
                helpExternalNode();
                break;
        }
    }

    /**
     * This method is called to verify the correctness of argument
     *
     * @param args
     * @return An InetSocketAddress that represents the address of the node
     */
    public static InetSocketAddress verifyArgs(String[] args){
        if(args.length != 3){
            System.err.println("\rError! You must insert the correct arguments: ip address (or host name), port number and file path.\n" +
                    "Example: InternalNodeMain 192.168.1.14 4444 C:\\Users\\NomeUtente\\Files");
            System.exit(1);
        }

        InetSocketAddress nodeAddress = null;

        try {
            nodeAddress = new InetSocketAddress(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
        }catch (UnknownHostException e) {
            System.err.println("\rYou must insert the correct ip address (or host name)!");
            System.exit(1);
        }catch (NumberFormatException nfe){
            System.err.println("\rYou must insert a correct port number!");
            System.exit(1);
        }

        return nodeAddress;
    }

    private static void manageFile(Scanner c, ExternalNode node, FileRequest fileRequest){
        String fileName = null;
        String address = "";
        int port = 0;


        if(c.hasNext()){
            fileName = c.next();
        }
        else{
            System.out.println("\rYou must insert the name of file!");
            return;
        }

        if(c.hasNext()){
            address = c.next();
            if(c.hasNextInt()){
                port = c.nextInt();
            }
            else{
                System.out.println("\rYou must insert a port number of the node!");
                return;
            }
        }
        else{
            System.out.println("\rYou must insert an address of the node!");
            return;
        }

        BigInteger fileId = Util.hashFile(fileName);
        InetSocketAddress contact = null;
        try {
            contact = new InetSocketAddress(InetAddress.getByName(address), port);
        } catch (UnknownHostException e) {
            System.out.println("\rYou must insert a valid address (or host name) of the node!");
            return;
        }

        RingConnection rc = new RingConnection(contact);
        InetSocketAddress dest = rc.findSuccessorRequest(fileId);
        ExternalFileConnection efc = new ExternalFileConnection(node, dest);

        switch (fileRequest){
            case insert:
                if(efc.insertFileRequest(fileName)){
                    System.out.println("\rFile " + fileName + " uploaded successfully!");
                }
                else{
                    System.out.println("\rError! File " + fileName + " not uploaded!");
                }
                break;
            case get:
                if(efc.getFileRequest(fileName)){
                    System.out.println("\rFile " + fileName + " downloaded successfully!");
                }
                else{
                    System.out.println("\rError! File " + fileName + " not downloaded!");
                }
                break;
            case delete:
                if(efc.deleteFileRequest(fileName)){
                    System.out.println("\rFile" + fileName + " deleted successfully!");
                }
                else{
                    System.out.println("\rError! File " + fileName + " not deleted!");
                }
                break;
            case hasFile:
                if(efc.hasFileRequest(fileName)){
                    System.out.println("\rFile" + fileName + " is in the ring!");
                }
                else{
                    System.out.println("\rFile " + fileName + " is not in the ring!!");
                }
                break;
        }

    }

    private static void ring(InternalNode node){
        System.out.println("\rNode position: " + Util.keyPosition(node.getLocalId()) + "% address: " + node.getLocalAddress());
        if(node.getPredAddress() == null || node.getPredAddress().equals(node.getLocalAddress())){
            return;
        }

        System.out.println("\rNode position: " + Util.keyPosition(Util.hashAddress(node.getPredAddress())) + "% address: " + node.getPredAddress());
        RingConnection rc = new RingConnection(node.getPredAddress());
        InetSocketAddress temp = rc.addressRequest("GET_PRED");

        while(temp != null && !node.getLocalAddress().equals(temp)){
            System.out.println("\rNode position: " + Util.keyPosition(Util.hashAddress(temp)) + "% address: " + temp);
            rc = new RingConnection(temp);
            temp = rc.addressRequest("GET_PRED");
        }
    }

    private static void helpInternalNode(){
        System.out.println("\rList of commands----------------------------");
        System.out.println("join [ip address] [port]            join the ring network");
        System.out.println("leave                               leave the ring network");
        System.out.println("mySuccessor                         get successor address");
        System.out.println("myPredecessor                       get predecessor address");
        System.out.println("fingerTable                         get finger table");
        System.out.println("position                            get percent position of node address in the ring");
        System.out.println("key                                 get id (key) of node in the ring");
        System.out.println("ring                                print the address of the nodes in the ring");
        System.out.println("fileList                            print all files stored in the node");
        System.out.println("exit                                close the application");
    }

    private static void helpExternalNode(){
        System.out.println("\rList of commands----------------------------");
        System.out.println("insert  [fileName] [ip address] [port]       upload a file");
        System.out.println("get     [fileName] [ip address] [port]       download a file");
        System.out.println("delete  [fileName] [ip address] [port]       delete a file");
        System.out.println("hasFile [fileName] [ip address] [port]       search a file in the ring");
        System.out.println("exit                                close the application");
    }


}
