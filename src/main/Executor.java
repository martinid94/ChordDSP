package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Marco on 24/02/2018.
 */
public class Executor extends Thread{

    private Socket sock;
    private InternalNode internalNode;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Executor(Socket s, InternalNode n){
        if(s == null || n == null){
            throw new IllegalArgumentException();
        }
        sock = s;
        this.internalNode = n;
        oos = null;
        ois = null;
    }


    public void run(){
        try {
            oos = new ObjectOutputStream(sock.getOutputStream());
            ois = new ObjectInputStream(sock.getInputStream());
        } catch (IOException e) {
            return;
        }

        String request = null;
        try {
            request = (String) ois.readObject();

            switch (request){
                //RingConnection requests
                case "FIND_SUCC":
                    findSuccessor();
                    break;
                case "JOIN":
                    joinRequest();
                    break;
                case "GET_SUCC":
                    getSuccessor();
                    break;
                case "GET_PRED":
                    getPredecessor();
                    break;
                case "GET_CLOSEST":
                    getClosestPrecedingNode();
                    break;
                case "GET&SET_JA":
                    getAndSetAvailability();
                    break;
                case "SET_SUCC":
                    setSuccessor();
                    break;
                case "SET_PRED":
                    setPredecessor();
                    break;
                case "GET_FILE":
                    getFile();
                    break;
                case "GET_FILE_INTERVAL":
                    getFileInterval();
                    break;
                case "INSERT_FILE":
                    insertFile();
                    break;
                case "INSERT_REPLICA":
                    insertReplica();
                    break;
                case "DELETE_FILE":
                    deleteFile();
                    break;
                case "DELETE_REPLICA":
                    deleteReplica();
                    break;
                case "DELETE_REPLICAS":
                    deleteReplicas();
                    break;
                case "HAS_FILE":
                    hasFile();
                    break;
                default:
                    System.out.println("Error");
                    break;

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            return;
        }
        finally {
            try
            {
                ois.close();
                oos.close();
                sock.close();
            }
            catch(IOException ioe2)
            {
                //ioe2.printStackTrace();
            }
        }

    }

    private void findSuccessor() throws IOException, ClassNotFoundException {

        BigInteger id = (BigInteger) ois.readObject();
        if(id == null) {
            oos.writeObject(null);
            oos.flush();
            return;
        }

        InetSocketAddress successor = internalNode.findSuccessor(id); //new InetSocketAddress(InetAddress.getByName("192.168.1.3"), 4544);
        oos.writeObject(successor);
        oos.flush();
    }

    private void joinRequest() throws IOException, ClassNotFoundException {

        InetSocketAddress newPred = (InetSocketAddress) ois.readObject();
        //joinAvailable variable is set to false in the if condition: a join operation can be performed now
        if(newPred == null || !internalNode.getAndSetJoinAvailable(false)){
            //respond with message error
            oos.writeObject(null);
            oos.flush();
            return;
        }

        InetSocketAddress pred = internalNode.getPredAddress();
        oos.writeObject(internalNode.getPredAddress());
        if(pred != null){
            internalNode.setPredecessor(newPred);
        }

        oos.flush();
    }

    private void getSuccessor() throws IOException {

        oos.writeObject(internalNode.getSuccAddress());
        oos.flush();
    }

    private void getPredecessor() throws IOException {

        oos.writeObject(internalNode.getPredAddress());
        oos.flush();
    }

    private void getClosestPrecedingNode() throws IOException, ClassNotFoundException {

        BigInteger id = (BigInteger) ois.readObject();
        if(id == null) {
            oos.writeObject(null);
            oos.flush();
            return;
        }

        oos.writeObject(internalNode.closestPrecedingNode(id));
        oos.flush();
    }

    private void getAndSetAvailability() throws IOException, ClassNotFoundException {

        boolean valueAvailable = (boolean) ois.readObject();

        oos.writeObject(internalNode.getAndSetJoinAvailable(valueAvailable));
        oos.flush();
    }

    private void setSuccessor() throws IOException, ClassNotFoundException {
        InetSocketAddress newSucc = (InetSocketAddress) ois.readObject();

        if(newSucc == null){
            //respond with false
            oos.writeObject(false);
            oos.flush();
            return;
        }

        oos.writeObject(internalNode.setSuccessor(newSucc));
        oos.flush();
    }

    private void setPredecessor() throws IOException, ClassNotFoundException {

        InetSocketAddress newPred = (InetSocketAddress) ois.readObject();

        if(internalNode.getPredAddress() != null && internalNode.getPredAddress().equals(newPred)){
            oos.writeObject(true);
            oos.flush();
            return;
        }
        //joinAvailable variable is set to false in the if condition: after changing predecessor,
        // no join is available since files must be updated
        if(newPred == null){ // || !internalNode.getAndSetJoinAvailable(false)
            //respond with false
            oos.writeObject(false);
            oos.flush();
            return;
        }


        oos.writeObject(internalNode.setPredecessor(newPred));
        oos.flush();
    }

    private void getFile() throws  IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();
        if(internalNode.hasFile(fileName)) {
            oos.writeBoolean(true); //file found
            oos.flush();

            internalNode.get(sock, fileName);
        }
        else{
            oos.writeBoolean(false); //file not found
            oos.flush();
        }
    }

    private void getFileInterval() throws  IOException, ClassNotFoundException {
        BigInteger from = (BigInteger) ois.readObject();
        BigInteger to = (BigInteger) ois.readObject();

        oos.writeObject(internalNode.getFilesInterval(from, to));
    }

    private void insertFile() throws IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();

        oos.writeBoolean(internalNode.insertFile(sock, fileName)); //files map is updated by insertFile method
        oos.flush();
    }

    private void insertReplica() throws IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();
        oos.writeBoolean(internalNode.singleInsert(sock, fileName)); //files map is updated by singleInsert method
        oos.flush();
    }

    private void deleteFile() throws IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();

        oos.writeBoolean(internalNode.delete(fileName)); //files map is updated by delete method
        oos.flush();
    }

    private void deleteReplica() throws IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();

        oos.writeBoolean(internalNode.singleDelete(fileName)); //files map is updated by singleDelete method
        oos.flush();
    }

    private void deleteReplicas() throws IOException, ClassNotFoundException {
        BigInteger from = (BigInteger) ois.readObject();
        BigInteger to = (BigInteger) ois.readObject();

        oos.writeBoolean(internalNode.deleteFilesInterval(from, to)); //files map is updated by deleteFilesInterval method
    }

    private void hasFile() throws IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();

        oos.writeBoolean(internalNode.hasFile(fileName));
    }
}
