package main.node;

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
        boolean value = readManager(s);
        readLock.unlock();
        return value;
    }


    public boolean write(Socket s) {
        writeLock.lock();
        boolean value = writeManager(s);
        writeLock.unlock();
        return value;
    }

    public boolean remove() {
        writeLock.lock();
        boolean value = this.delete();
        writeLock.unlock();
        return value;
    }

    private boolean writeManager(Socket s){
        InputStream is = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        boolean value = false;

        try {
            is = s.getInputStream();
            byte[] buffer = new byte[1024];

            fos = new FileOutputStream(this);
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(fos);
            int readBytes = 0;

            while ((readBytes = bis.read(buffer)) != -1){
                bos.write(buffer, 0, readBytes);
            }


            value = true;
        } catch (IOException e) {
            value = false;
        }
        finally {
            try {
                bos.flush();
                bos.close();

                bis.close();
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    private boolean readManager(Socket s){
        OutputStream os = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        boolean value = false;

        try {
            os = s.getOutputStream();
            byte[] buffer = new byte[1024];
            fis = new FileInputStream(this);
            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(os);
            int readBytes = 0;
            while ((readBytes = bis.read(buffer)) != -1){
                bos.write(buffer, 0, readBytes);
            }


            value = true;
        } catch (IOException e) {
            value = false;
        }
        finally {
            try {
                bos.flush();
                bos.close();

                fis.close();
                bis.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return value;

    }
}
