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
}
