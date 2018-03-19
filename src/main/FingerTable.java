package main;

import main.Connection.RingConnection;

import java.net.InetSocketAddress;

/**
 * Created by Marco on 24/02/2018.
 */
public class FingerTable {

    InetSocketAddress[] table;
    InetSocketAddress oldSucc;

    public FingerTable(){
        table = new InetSocketAddress[Util.m];
        oldSucc = null;
    }

    /**
     *
     * @param i: index of the table to update
     * @param node: node address to insert
     */
    public synchronized void updateIthFinger(int i, InetSocketAddress node){
        if(i < 0 || i >= Util.m){
            throw new IllegalArgumentException();
        }
        if(i == 0 && table[i] != null){
            oldSucc = table[i];
        }
        table[i] = node;
    }

    /**
     *
     * @param i: index of the table
     * @return address node in the ith line of table
     */
    public synchronized InetSocketAddress getIthFinger(int i){
        if(i < 0 || i >= Util.m){
            throw new IllegalArgumentException();
        }
        return table[i];
    }

    public synchronized void deleteNode(InetSocketAddress addr) {
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
