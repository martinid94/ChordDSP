package main;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util{

    public static final int m = 160;

    public static BigInteger hashAdress(InetSocketAddress address){
        if(address == null){
            return null;
        }
        int h = address.hashCode();
        return hashSH1(h);

    }

    public static BigInteger hashFile(String fileName){
        if(fileName == null){
            return null;
        }
        int h = fileName.hashCode();
        return hashSH1(h);
    }


    public static int keyPosition(BigInteger id){
        if(id == null){
            return -1;
        }
        byte[] b = {100};
        BigInteger maxValue = poweOfTwo(m);
        int percent = ((id.multiply(new BigInteger(b))).divide(maxValue)).intValue();
        return percent;
    }

    public static boolean belongsToOpenInterval(BigInteger id, BigInteger from, BigInteger to){
        if(id == null || from == null || to == null){
            throw new IllegalArgumentException();
        }

        if(from.compareTo(to) <= 0){ //from <= to
            if(id.compareTo(from) > 0 && id.compareTo(to) < 0) { // id > from && id < to
                return true;
            }
            else{
                return false;
            }
        }
        else{
            byte[] p = {0};
            BigInteger maxValue = poweOfTwo(m); //2^m
            BigInteger minValue = new BigInteger(p);
            if((id.compareTo(from) > 0 && id.compareTo(maxValue) < 0) ||
                    (id.compareTo(minValue) >= 0 && id.compareTo(to) < 0)){
                return true;
            }
        }
        return false;
    }

    public static BigInteger ithStart(int i, BigInteger id){
        if(i > m || i <= 0 || id == null){
            return null;
        }
        BigInteger value = poweOfTwo(i-1); //2^(i-1)
        BigInteger maxValue = poweOfTwo(m);
        return (id.add(value)).mod(maxValue);

    }

    public static BigInteger poweOfTwo(int i){
        byte[] base = {2};
        return (new BigInteger(base)).pow(i);
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
