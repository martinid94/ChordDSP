package main;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util{

    private static final int m = 160;

    public static BigInteger hashAdress(InetSocketAddress address){
        int h = address.hashCode();
        return hashSH1(h);

    }

    public static BigInteger hashFile(String fileName){
        int h = fileName.hashCode();
        return hashSH1(h);
    }

    public static int keyPosition(BigInteger id){
        byte[] b = {100};
        byte[] base = {2};
        BigInteger maxValue = (new BigInteger(base)).pow(m);
        int percent = ((id.multiply(new BigInteger(b))).divide(maxValue)).intValue();
        return percent;
    }

    private static BigInteger hashSH1(int key){
        BigInteger result = null;
        byte[] hashbytes = new byte[4];
        hashbytes[0] = (byte) (key >> 24);
        hashbytes[1] = (byte) (key >> 16);
        hashbytes[2] = (byte) (key >> 8);
        hashbytes[3] = (byte) (key );

        // try to create SHA1 digest
        MessageDigest md =  null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if(md != null){
            md.reset();
            md.update(hashbytes);
            result = new BigInteger(1, md.digest());
        }

        return result;
    }


}
