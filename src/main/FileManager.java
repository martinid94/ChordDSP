package main;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Davide on 25/02/2018.
 */
public class FileManager extends File {
    private ReadWriteLock rwl;
    private Lock readLock;
    private Lock writeLock;

    public FileManager(String pathName) {
        super(pathName);
        rwl = new ReentrantReadWriteLock();
        readLock = rwl.readLock();
        writeLock = rwl.writeLock();
    }

    public boolean write(Socket s, InetSocketAddress pred) {
        writeLock.lock();
        try {
            InputStream is = s.getInputStream();
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(this);
            BufferedInputStream bis = new BufferedInputStream(is);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int readBytes = 0;
            while ((readBytes = bis.read(buffer)) != -1)
                bos.write(buffer, 0, readBytes); //controlla se all'ultima iterazione ho lasciato un pezzo nel buffer
            fos.close();
            bis.close();
            bos.close();
            is.close();
            writeLock.unlock();
        }
        catch(IOException ioe) {
            writeLock.unlock();
            return false;

        }
        return true;
    }

    public boolean delete(InetSocketAddress succ, String fileName) {
        return true;
    }

    public boolean read(Socket s) {
        readLock.lock();
        try {
            OutputStream os = s.getOutputStream();
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(this);
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            int readBytes = 0;
            while ((readBytes = bis.read(buffer)) != -1)
                bos.write(buffer, 0, readBytes); //controlla se all'ultima iterazione ho lasciato un pezzo nel buffer
            bos.flush();
            fis.close();
            bis.close();
            bos.close();
            os.close();
            readLock.unlock();
        }
        catch(IOException ioe) {
            readLock.unlock();
            return false;

        }
        return true;
    }

    public ArrayList<String> getFilesInterval(BigInteger from, BigInteger to) {
        return null;
    }

    public boolean insertReplica(Socket s, String fileName) {
        return true;
    }

    public boolean deleteReplica(String fileName) {
        return true;
    }
}
