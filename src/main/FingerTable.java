package main;

import java.net.InetSocketAddress;

/**
 * Created by Marco on 24/02/2018.
 */
public class FingerTable {

    InetSocketAddress[] table;

    public FingerTable(){
        table = new InetSocketAddress[Util.m];
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

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(InetSocketAddress ad : table){
            sb.append(ad.toString());
        }
        return sb.toString();
    }
}
