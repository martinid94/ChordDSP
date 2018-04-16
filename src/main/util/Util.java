package main.util;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class provides a set fo utility methods widely used in the project
 *
 * 
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class Util{

    public static final int m = 160; //bits of the hash function used as id (160 means all for SHA1)

    /**
     * This method is called to compute the hash (with SHA1) of an internal node address
     * @param address It is the address of an internal node
     * @return The id of the node
     */
    public static BigInteger hashAddress(InetSocketAddress address){
        //invalid argument
        if(address == null){
            return null;
        }
        int h = address.hashCode();
        return hashSHA1(h);

    }

    /**
     * This method is called to compute the hash (with SHA1) of a file name
     * @param fileName It is the name of the file
     * @return The id of the file
     */
    public static BigInteger hashFile(String fileName){
        //invalid argument
        if(fileName == null){
            return null;
        }
        int h = fileName.hashCode();
        return hashSHA1(h);
    }

    /**
     * This method is called to compute the position (in percentage) of an internal node or file in the ring
     * @param id It is the id of a file or a node
     * @return The position of the id in the Chord ring
     */
    public static int keyPosition(BigInteger id){
        //invalid argument
        if(id == null){
            return -1;
        }
        byte[] b = {100};
        BigInteger maxValue = powerOfTwo(m);
        int percent = ((id.multiply(new BigInteger(b))).divide(maxValue)).intValue();
        return percent;
    }

    /**
     * This method is called to compute if a file or internal node has id that belongs to the interval (from, to)
     * @param id It is the id of a file or a node
     * @param from The beginning of the interval
     * @param to The end of the interval
     * @return True if id belongs to the interval (from, to), false otherwise
     */
    public static boolean belongsToOpenInterval(BigInteger id, BigInteger from, BigInteger to){
        //invalid argument
        if(id == null || from == null || to == null){
            throw new IllegalArgumentException();
        }

        if(from.compareTo(to) <= 0){ //from <= to
            if(id.compareTo(from) > 0 && id.compareTo(to) < 0) { //id > from && id < to
                return true;
            }
            else{
                return false;
            }
        }
        else{
            byte[] p = {0};
            BigInteger maxValue = powerOfTwo(m); //2^m
            BigInteger minValue = new BigInteger(p);
            if((id.compareTo(from) > 0 && id.compareTo(maxValue) < 0) ||
                    (id.compareTo(minValue) >= 0 && id.compareTo(to) < 0)){
                return true;
            }
        }
        return false;
    }

    /**
     * This method is called to compute if a file or internal node has id that belongs to the interval (from, to]
     * @param id The id of a file or a node
     * @param from The beginning of the interval
     * @param to The end of the interval
     * @return True if id belongs to the interval (from, to], false otherwise
     */
    public static boolean belongsToInterval(BigInteger id, BigInteger from, BigInteger to){
        boolean value = belongsToOpenInterval(id, from, to);
        return (value || id.equals(to));
    }

    /**
     * This method is called to compute the ith start of the finger table of an internal node
     * @param id It is the id of an internal node
     * @param i The position in the node's finger table
     * @return The id of the node finger[i].start
     */
    public static BigInteger ithStart(int i, BigInteger id){
        //invalid arguments
        if(i > m || i <= 0 || id == null){
            return null;
        }
        BigInteger value = powerOfTwo(i-1); //2^(i-1)
        BigInteger maxValue = powerOfTwo(m);
        return (id.add(value)).mod(maxValue);

    }

    /**
     * This method is called to compute the power of two
     * @param i The exponent
     * @return The result of 2^i
     */
    public static BigInteger powerOfTwo(int i){
        byte[] base = {2};
        return (new BigInteger(base)).pow(i);
    }

    private static BigInteger hashSHA1(int key){
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
            e.printStackTrace();
        }

        if(md != null){
            md.reset();
            md.update(hashbytes);
            //byte[] bi =  {md.digest()[1]};
            result = new BigInteger(1, md.digest());
        }

        return result;
    }
}
