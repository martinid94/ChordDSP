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
                case "JOIN_REQ":
                    joinRequest();
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
            return;
        }

        //TODO quando joinAvailable viene settata a true????

        oos.writeObject(node.getPredAddress());
        oos.flush();
        node.setPredecessor(newPred);
    }

    private void setSuccessor(){

    }

    private void setPredecessor(){

    }

}
