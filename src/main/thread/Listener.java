package main.thread;

import main.node.InternalNode;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * This class extends the class Thread and it keeps listening to a port waiting for a request.
 * When a connection and so a request are performed, the class Listener passes the request to a different thread (that executes the request) and then it keeps listening again on the same port.
 *
 * @author Alfonso Marco
 *
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class Listener extends Thread {
    private InternalNode internalNode;
    private ServerSocket ss;
    private ExecutorService es; //thread pool

    /**
     * Unique constructor of the class
     *
     * @param internalNode It is the internal node in which this thread keeps listening.
     */
    public Listener(InternalNode internalNode){
        this.internalNode = internalNode;
        int port = internalNode.getLocalAddress().getPort();

        /*Creates a thread pool that creates new threads as needed,
        but will reuse previously constructed threads when they are available.
        These pools will typically improve the performance of programs
        that execute many short-lived asynchronous tasks.
         */
        es = java.util.concurrent.Executors.newCachedThreadPool();

        try {
            ss = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException("\nCannot open server port " + port + ".\n", e);
        }
    }

    public void run(){
        while(true){
            Socket s;

            try {
                s = ss.accept();
            } catch (IOException e) {
                throw new RuntimeException("\nError on receiving request!\n", e);
            }

            /*It will reuse previously constructed threads if available.
            If no existing thread is available, a new thread will be created and added to the pool.
            Threads that have not been used for sixty seconds are terminated and removed from the cache.
             */
            es.submit(new Executor(s, internalNode));
        }
    }
}
