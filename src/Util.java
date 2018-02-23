import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util{

    public BigInteger hashAdress(InetSocketAddress address){
        BigInteger result = null;
        int h = address.hashCode();

        byte[] hashbytes = new byte[4];
        hashbytes[0] = (byte) (h >> 24);
        hashbytes[1] = (byte) (h >> 16);
        hashbytes[2] = (byte) (h >> 8);
        hashbytes[3] = (byte) (h );

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
            result = new BigInteger(md.digest());
        }

        return result;
    }

    public BigInteger hashFile(InetSocketAddress fileName){
        return null;
    }

    public static void main(String[] args){
        try {
            System.out.println(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //InetSocketAddress ad = new InetSocketAddress(new InetAddress())
    }
}
