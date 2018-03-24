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
     * @param node Node address to insert
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
     * node fails and sets all the unreachable entries to null.
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
     * This method is called by the Stabilizer to fill the finger table after a deleteNode() invocation.
     * At first it tries to fill the successor 
     * @param node
     */
    public synchronized void fillSuccessor(InternalNode node){
        InetSocketAddress succ = table[0];

        if(succ == null){
            for (int k = 1; k < Util.m; k++) {
                InetSocketAddress ithfinger = table[k];
                if (ithfinger!=null && !ithfinger.equals(node.getLocalAddress())) {
                    for (int j = k-1; j >= 0; j--) {
                        table[j] = ithfinger;
                    }
                    break;
                }
            }

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
            else{
                InetSocketAddress myPred = node.getPredAddress();
                InetSocketAddress validPred = myPred;
                if(myPred == null){
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

        /*problem if all elements in the fingerTable are null
        (possible solution: try to find the node woth predessor equals to null moving backward in the ring)*/

//        successor = getSuccessor();
//        if ((successor == null || successor.equals(localAddress)) && predecessor!=null && !predecessor.equals(localAddress)) {
//            updateIthFinger(1, predecessor);
//        }
    }

    public synchronized InetSocketAddress getOldSucc(){
        return oldSucc;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(InetSocketAddress ad : table){
            sb.append(ad + "\n");
        }
        return sb.toString();
    }
}
