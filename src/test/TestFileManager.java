package test;

import main.node.FileManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Davide on 27/02/2018.
 */
public class TestFileManager {

    public TestFileManager(){

    }

    public static void main(String[] args) {
        TestFileManager t = new TestFileManager();
        Server server =  t.new Server();
        Client1 client1 = t.new Client1();
        Client2 client2 = t.new Client2();

        //server.start();
        client1.start();
        //client2.start();
    }

    private class Server extends Thread{

        public void run(){
            ServerSocket ss = null;
            Socket s1 = null;
            Socket s2 = null;
            try {
                ss = new ServerSocket(4444);
                s1 = ss.accept();
                s2 = ss.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileManager fm = new FileManager("C:\\Users\\Marco\\Desktop\\Server\\prova.pdf");



            (this.new Exec(false, fm, s1)).start();
            (this.new Exec(false, fm, s2)).start();

            System.out.println("Server OK!");

        }

        private class Exec extends Thread{

            private boolean read;
            private FileManager fm;
            private Socket s;

            public Exec(boolean read, FileManager fm, Socket s){
                this.read = read;
                this.fm = fm;
                this.s = s;
            }

            public void run(){

                if(read){
                    fm.read(s);
                    System.out.println("Read OK!");
                }
                else{
                    //fm.write(s, null);
                    System.out.println("Write OK!");
                }
            }
        }
    }



    private class Client1 extends Thread{

        public void run(){

            Socket s = null;
            try {
                s = new Socket("localhost", 4444);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileManager fm = new FileManager("C:\\Users\\Marco\\Desktop\\Client1\\prova.pdf");

            //fm.read(s);



            //System.out.println("Remove client1 " + fm.singleRemove());
        }
    }

    private class Client2 extends Thread{

        public void run(){

            Socket s = null;
            try {
                s = new Socket("localhost", 4444);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileManager fm = new FileManager("C:\\Users\\Marco\\Desktop\\Client2\\prova.pdf");

            fm.read(s);

            System.out.println("Read client2 OK!");
        }
    }
}
