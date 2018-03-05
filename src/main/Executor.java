package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

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
                case "FIND_SUCC":
                    findSuccessor();
                case "JOIN":
                    joinRequest();
                case "GET_SUCC":
                    getSuccessor();
                case "GET_PRED":
                    getPredecessor();
                case "GET&SET_JA":
                    getAndSetAvailability();
                case "SET_SUCC":
                    setSuccessor();
                case "SET_PRED":
                    setPredecessor();
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

    private void findSuccessor() throws IOException, ClassNotFoundException{

        BigInteger id = (BigInteger) ois.readObject();

        //TODO chiama il findsuccessor di node
        InetSocketAddress successor = null; //new InetSocketAddress(InetAddress.getByName("192.168.1.3"), 4544);
        oos.writeObject(successor);
        oos.flush();
    }

    private void joinRequest() throws IOException, ClassNotFoundException {

        InetSocketAddress newPred = (InetSocketAddress) ois.readObject();

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

        if(newPred == null || !node.getAndSetJoinAvailable(false)){
            //respond with false
            oos.writeBoolean(false);
            oos.flush();
            return;
        }

        oos.writeBoolean(node.setPredecessor(newPred));
        oos.flush();

    }

}
