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

    public boolean read(Socket s) {
        readLock.lock();
        try {
            readManager(s);
            readLock.unlock();
        }
        catch(IOException ioe) {
            readLock.unlock();
            return false;

        }
        return true;
    }


    public boolean write(Socket s) {
        writeLock.lock();

        try {
            writeManager(s);
            writeLock.unlock();
        }
        catch(IOException ioe) {
            writeLock.unlock();
            return false;

        }
        return true;
    }

    public boolean remove() {
        writeLock.lock();
        boolean value = this.delete();
        writeLock.unlock();
        return value;
    }

    private void writeManager(Socket s) throws IOException {
        InputStream is = s.getInputStream();
        byte[] buffer = new byte[1024];
        FileOutputStream fos = new FileOutputStream(this);
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int readBytes = 0;
        while ((readBytes = bis.read(buffer)) != -1)
            bos.write(buffer, 0, readBytes);
        bos.flush();
        bos.close();

        bis.close();
        fos.close();
        is.close();
    }

    private void readManager(Socket s) throws IOException {
        OutputStream os = s.getOutputStream();
        byte[] buffer = new byte[1024];
        FileInputStream fis = new FileInputStream(this);
        BufferedInputStream bis = new BufferedInputStream(fis);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        int readBytes = 0;
        while ((readBytes = bis.read(buffer)) != -1)
            bos.write(buffer, 0, readBytes);
        bos.flush();
        bos.close();

        fis.close();
        bis.close();
        os.close();
    }
}
