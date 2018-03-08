package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Marco on 24/02/2018.
 */
public class Executor extends Thread{

    private Socket sock;
    private Node node;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public Executor(Socket s, Node n){
        if(s == null || n == null){
            throw new IllegalArgumentException();
        }
        sock = s;
        this.node = n;
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
                case "JOIN":
                    joinRequest();
                case "GET_SUCC":
                    getSuccessor();
                case "GET_PRED":
                    getPredecessor();
                case "GET_CLOSEST":
                    getClosestPrecedingNode();
                case "GET&SET_JA":
                    getAndSetAvailability();
                case "SET_SUCC":
                    setSuccessor();
                case "SET_PRED":
                    setPredecessor();

                //FileConnection requests
                case "GET_FILE":
                    getFile();
                case "GET_FILE_INTERVAL":
                    getFileInterval();
                case "INSERT_FILE":
                    insertFile();
                case "INSERT_REPLICA":
                    insertReplica();
                case "DELETE_FILE":
                    deleteFile();
                case "DELETE_REPLICA":
                    deleteReplica();
                case "DELETE_REPLICAS":
                    deleteReplicas();
                case "HAS_FILE":
                    hasFile();
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
                ioe2.printStackTrace();
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

        InetSocketAddress successor = node.findSuccessor(id); //new InetSocketAddress(InetAddress.getByName("192.168.1.3"), 4544);
        oos.writeObject(successor);
        oos.flush();
    }

    private void joinRequest() throws IOException, ClassNotFoundException {

        InetSocketAddress newPred = (InetSocketAddress) ois.readObject();

        //joinAvailable variable is set to false in the if condition: a join operation can be performed now
        if(newPred == null || !node.getAndSetJoinAvailable(false)){
            //respond with message error
            oos.writeObject(null);
            oos.flush();
            return;
        }

        oos.writeObject(node.getPredAddress());
        oos.flush();
        node.setPredecessor(newPred);
    }

    private void getSuccessor() throws IOException {

        oos.writeObject(node.getSuccAddress());
        oos.flush();
    }

    private void getPredecessor() throws IOException {

        oos.writeObject(node.getPredAddress());
        oos.flush();
    }

    private void getClosestPrecedingNode() throws IOException, ClassNotFoundException {

        BigInteger id = (BigInteger) ois.readObject();
        if(id == null) {
            oos.writeObject(null);
            oos.flush();
            return;
        }

        oos.writeObject(node.closestPrecedingNode(id));
        oos.flush();
    }

    private void getAndSetAvailability() throws IOException, ClassNotFoundException {

        boolean valueAvailable = (boolean) ois.readObject();

        oos.writeBoolean(node.getAndSetJoinAvailable(valueAvailable));
        oos.flush();
    }

    private void setSuccessor() throws IOException, ClassNotFoundException {
        InetSocketAddress newSucc = (InetSocketAddress) ois.readObject();

        if(newSucc == null){
            //respond with false
            oos.writeBoolean(false);
            oos.flush();
            return;
        }

        oos.writeBoolean(node.setSuccessor(newSucc));
        oos.flush();
    }

    private void setPredecessor() throws IOException, ClassNotFoundException {

        InetSocketAddress newPred = (InetSocketAddress) ois.readObject();

        //joinAvailable variable is set to false in the if condition: after changing predecessor,
        // no join is available since files must be updated
        if(newPred == null || !node.getAndSetJoinAvailable(false)){
            //respond with false
            oos.writeBoolean(false);
            oos.flush();
            return;
        }

        InetSocketAddress myPred = node.getPredAddress();

        //if my current predecessor is null no check can be performed, so newPred is accepted
        //otherwise, it is updated only if the new one lies between me and my current predecessor (i.e. a join occurs)
        //this check must be performed in order to avoid Chord ring failures
        if(myPred == null || Util.belongsToOpenInterval(Util.hashAdress(newPred),
                Util.hashAdress(myPred), Util.hashAdress(node.getLocalAddress()))){
            oos.writeBoolean(node.setPredecessor(newPred));
            oos.flush();
        }

        oos.writeBoolean(false);
        oos.flush();
    }

    private void getFile() throws  IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();
        if(node.hasFile(fileName)) {
            oos.writeBoolean(true); //file found
            oos.flush();

            node.get(sock, fileName);
        }
        else
            oos.writeBoolean(false); //file not found
            oos.flush();
    }

    private void getFileInterval() throws  IOException, ClassNotFoundException {
        BigInteger from = (BigInteger) ois.readObject();
        BigInteger to = (BigInteger) ois.readObject();

        oos.writeObject(node.getFilesInterval(from, to));
    }

    private void insertFile() throws IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();
        node.insertFile(sock, fileName); //files map is updated by insertFile method
    }

    private void insertReplica() throws IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();
        node.singleInsert(sock, fileName); //files map is updated by singleInsert method
    }

    private void deleteFile() throws IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();

        oos.writeBoolean(node.delete(fileName)); //files map is updated by delete method
        oos.flush();
    }

    private void deleteReplica() throws IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();

        oos.writeBoolean(node.singleDelete(fileName)); //files map is updated by singleDelete method
        oos.flush();
    }

    private void deleteReplicas() throws IOException, ClassNotFoundException {
        BigInteger from = (BigInteger) ois.readObject();
        BigInteger to = (BigInteger) ois.readObject();

        oos.writeBoolean(node.deleteFilesInterval(from, to)); //files map is updated by deleteFilesInterval method
    }

    private void hasFile() throws IOException, ClassNotFoundException {
        String fileName = (String) ois.readObject();

        oos.writeBoolean(node.hasFile(fileName));
    }
}
