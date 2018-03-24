package main.node;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is an extension of the java.io.File class. It provides several methods that allows to physically
 * read or write a file in/from the file system with concurrency control.
 *
 * @author Alfonso Marco
 * @author Martini Davide
 *
 * Distributed Systems class (AY 2017/2018), University of Padua, Master's degree in Computer Engineering.
 */
public class FileManager extends File {
    private ReadWriteLock rwl;
    private Lock readLock;
    private Lock writeLock;

    /**
     * Unique constructor of the class
     * @param pathName It is the path with the name of the file to be read or written
     */
    public FileManager(String pathName) {
        super(pathName);
        rwl = new ReentrantReadWriteLock();
        readLock = rwl.readLock();
        writeLock = rwl.writeLock();
    }

    /**
     * This method reads the file and uploads it to the specified socket
     * @param s It is the socket where to upload the file
     * @return True if the operation is performed correctly
     */
    public boolean read(Socket s) {
        readLock.lock();
        boolean value = readManager(s);
        readLock.unlock();
        return value;
    }

    /**
     * This method writes the file from a specified socket and writes it in the file system
     * @param s It is the socket where to download the file
     * @return True if the operation is performed correctly
     */
    public boolean write(Socket s) {
        writeLock.lock();
        boolean value = writeManager(s);
        writeLock.unlock();
        return value;
    }

    /**
     * This method removes the file from the file system
     * @return True if the operation is performed correctly
     */
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
