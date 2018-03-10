package main;

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
        for (int i = Util.m -1; i >= 0; i--) {
            InetSocketAddress ithfinger = this.getIthFinger(i);
            if (ithfinger != null && ithfinger.equals(addr)){
                if(i == 0 && table[i] != null){
                    oldSucc = table[i];
                }
                table[i] = null;
            }
        }
    }

    public synchronized void fillSuccessor(InetSocketAddress localAddress){
        InetSocketAddress succ = table[0];

        if(succ == null){
            for (int k = 1; k < Util.m; k++) {
                InetSocketAddress ithfinger = table[k];
                if (ithfinger!=null && !ithfinger.equals(localAddress)) {
                    for (int j = k-1; j >= 0; j--) {
                        table[j] = ithfinger;
                    }
                    break;
                }
            }
        }

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
            sb.append(ad.toString());
        }
        return sb.toString();
    }
}
