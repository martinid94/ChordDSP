import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util{

    public static BigInteger hashAdress(InetSocketAddress address){
        int h = address.hashCode();
        return hashSH1(h);

    }

    public static BigInteger hashFile(String fileName){
        int h = fileName.hashCode();
        return hashSH1(h);
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

    public static void main(String[] args){
        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        InetSocketAddress ad = null;
        try {
            ad = new InetSocketAddress(InetAddress.getLocalHost(), 720);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println(hashAdress(ad));
        System.out.println(hashFile("prova.txt"));
    }
}
