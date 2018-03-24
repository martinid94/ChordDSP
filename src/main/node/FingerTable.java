package main.node;

import main.Util;
import main.connection.RingConnection;

import java.net.InetSocketAddress;

/**
 * This class represents the finger table of an internal node. It contains the node's information about
 * the ring structure. It provides several methods to recover the information in case of a remote node failure
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class FingerTable {

    private InetSocketAddress[] table;
    private InetSocketAddress oldSucc;

    /**
     * Unique constructor of the class
     */
    public FingerTable(){
        table = new InetSocketAddress[Util.m];
        oldSucc = null;
    }

    /**
     * This method is called to update the ith finger of the table
     * @param i Index of the table to update
     * @param node Node address to be inserted
     */
    public synchronized void updateIthFinger(int i, InetSocketAddress node){

        //invalid argument
        if(i < 0 || i >= Util.m){
            throw new IllegalArgumentException();
        }

        if(i == 0 && table[i] != null && !table[i].equals(node)){
            oldSucc = table[i];
        }
        table[i] = node;
    }

    /**
     * This method is called to get the ith finger in the table
     * @param i Index of the table
     * @return The address node in the ith line of table
     */
    public synchronized InetSocketAddress getIthFinger(int i){

        //invalid argument
        if(i < 0 || i >= Util.m){
            throw new IllegalArgumentException();
        }
        return table[i];
    }

    /**
     * This method is called by the Stabilizer to delete a node form the table. It is called when the successor
     * node fails and it sets all the unreachable entries to null.
     * @param addr Address of the node to be deleted
     */
    public synchronized void deleteNode(InetSocketAddress addr) {

        //invalid argument
        if(addr == null){
            return;
        }
        for (int i = Util.m -1; i >= 0; i--) {
            InetSocketAddress ithfinger = table[i];
            if (ithfinger != null && ithfinger.equals(addr)){
                if(i == 0 && table[i] != null){
                    oldSucc = table[i];
                }
                table[i] = null;
            }
        }
    }

    /**
     * This method is called by the Stabilizer to fill the finger table after a deleteNode() invocation
     * @param node It is a reference of the invoker node
     */
    public synchronized void fillSuccessor(InternalNode node){
        InetSocketAddress succ = table[0];

        //proceed only after a deleteNode(successor)
        if(succ == null){
            //select as candidate the first not null entry of the table and fill the array
            for (int k = 1; k < Util.m; k++) {
                InetSocketAddress ithfinger = table[k];
                if (ithfinger!=null && !ithfinger.equals(node.getLocalAddress())) {
                    for (int j = k-1; j >= 0; j--) {
                        table[j] = ithfinger;
                    }
                    break;
                }
            }

            //if a valid candidate has been found, check if some nodes have been missed
            //try to move backwards in the ring from the candidate node
            if(table[0] != null){
                RingConnection rc = new RingConnection(table[0]);
                InetSocketAddress pred = rc.addressRequest("GET_PRED");
                InetSocketAddress validPred = table[0];
                while(pred != null){
                    validPred = pred;
                    rc = new RingConnection(pred);
                    pred = rc.addressRequest("GET_PRED");
                }
                table[0] = validPred;
            }
            //otherwise (i.e. all the table entries were equal to the previous successor)
            //try to move backwards in the ring from the invoker's predecessor
            else{
                InetSocketAddress myPred = node.getPredAddress();
                InetSocketAddress validPred = myPred;
                //if the invoker's predecessor is also null, then it is the only one in the ring
                if(myPred == null){
                    table[0] = node.getLocalAddress();
                    node.setPredecessor(node.getLocalAddress());
                    node.getAndSetJoinAvailable(true);
                    return;
                }
                while (myPred != null){
                    validPred = myPred;
                    RingConnection rc = new RingConnection(myPred);
                    myPred = rc.addressRequest("GET_PRED");
                }
                table[0] = validPred;
            }
        }

    }

    /**
     * This method is called to get the invoker node's old successor with concurrency control
     * @return The address of the old successor
     */
    public synchronized InetSocketAddress getOldSucc(){
        return oldSucc;
    }

    /**
     * This method is called to print the finger table
     * @return The string representing the finger table
     */
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(InetSocketAddress ad : table){
            sb.append(ad + "\n");
        }
        return sb.toString();
    }
}
