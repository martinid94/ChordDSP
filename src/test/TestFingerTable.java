package test;

import main.node.FingerTable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by Marco on 24/02/2018.
 */
public class TestFingerTable extends Thread{

    private boolean testUpdate;
    private FingerTable tb;

    public TestFingerTable(boolean testUpdate, FingerTable tb){
        this.testUpdate = testUpdate;
        this.tb = tb;
    }
    public static void main(String[] args){
        FingerTable t = new FingerTable();
        (new TestFingerTable(true, t)).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        (new TestFingerTable(false, t)).start();
    }

    public void run(){
        if(testUpdate){
            try {
                tb.updateIthFinger(0, (new InetSocketAddress(InetAddress.getByName("192.168.1.1"), 4444)));
                tb.updateIthFinger(1, (new InetSocketAddress(InetAddress.getByName("192.168.1.2"), 4444)));
                tb.updateIthFinger(2, (new InetSocketAddress(InetAddress.getByName("192.168.1.3"), 4344)));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        }
        else{
            System.out.println(tb.getIthFinger(0));
            System.out.println(tb.getIthFinger(1));
            System.out.println(tb.getIthFinger(2));
        }
    }
}
