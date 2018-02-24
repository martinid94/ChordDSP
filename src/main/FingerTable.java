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

    public synchronized void updateIthFinger(int i, InetSocketAddress node){
        if(i < 0 || i >= Util.m){
            throw new IllegalArgumentException();
        }
        table[i] = node;
    }

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
