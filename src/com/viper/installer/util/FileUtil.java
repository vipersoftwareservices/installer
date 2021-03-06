/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)filename.java	1.00 2008/06/15
 *
 * Copyright 1998-2008 by Viper Software Services
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Viper Software Services. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Viper Software Services.
 *
 * @author Tom Nevin (TomNevin@pacbell.net)
 *
 * @version 1.0, 06/15/2008 
 *
 * @note 
 *        
 * -----------------------------------------------------------------------------
 */

package com.viper.installer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FileUtil {

    private static final String CHARSET_ENCODING = "UTF8";
    private static FileUtil instance = new FileUtil();

    private FileUtil() {
    }

    /**
     * @deprecated (Use FileUtil as static class)
     * 
     *             Singleton instance to retrieval of file utilities.
     * @return the instanceof FileUtil
     */
    public static FileUtil getInstance() {
        return instance;
    }

    /**
     * Opens a file for writing to as print stream, making sure file path exists.
     * 
     * @param filename
     * @return descriptor of print stream
     */
    public static PrintWriter openFile(String filename) {
        try {
            mkPath(filename);

            return new PrintWriter(filename, CHARSET_ENCODING);
        } catch (Throwable e) {
            Logs.error("ERROR openFile: " + filename, e);
        }
        return null;
    }

    /**
     * @param out
     */
    public static void closeFile(PrintWriter out) {
        if (out != null) {
            out.flush();
            out.close();
        }
    }

    public static boolean exists(String filename) {
        try {
            return new File(filename).exists();
        } catch (Exception e) {

        }
        return false;
    }

    // -------------------------------------------------------------------------

    public static InputStream getInputStream(Class clazz, String filename) throws IOException {
        InputStream in = null;
        if (filename == null) {
            throw new IOException("Filename is null.");
        } else if (filename.startsWith("http:")) {
            in = new URL(filename).openStream();
        } else if (filename.startsWith("res:")) {
            in = clazz.getResourceAsStream(filename.substring("res:".length()));
        } else {
            in = new FileInputStream(filename);
        }
        return in;
    }

    public static OutputStream getOutputStream(Class clazz, String filename) throws IOException {
        OutputStream out = null;
        if (filename == null) {
        } else if (filename.startsWith("http:")) {
        } else if (filename.startsWith("res:")) {
        } else {
            out = new FileOutputStream(filename);
        }
        return out;
    }

    /**
     * @param filename
     * @return
     * @throws IOException
     */
    public static String readFile(String filename) throws IOException {
        return readFile(FileUtil.class, filename);
    }

    public static String readFile(Class clazz, String filename) throws IOException {
        try {
            return readFile(getInputStream(clazz, filename));
        } catch (IOException ioe) {
            Logs.error("readFile: " + filename, ioe);
        }
        return null;
    }

    public static String readFile(InputStream inputStream) {
        StringBuffer buffer = new StringBuffer();
        try {
            Reader f = new BufferedReader(new InputStreamReader(inputStream));
            int ch;
            while ((ch = f.read()) > -1) {
                buffer.append((char) ch);
            }
            f.close();
        } catch (IOException ioe) {
            Logs.error("readFileStream: ", ioe);
        }
        return buffer.toString();
    }

    public static byte[] readBytes(String filename) throws IOException {
        return Files.readAllBytes(new File(filename).toPath());
    }

    /**
     * @param filename
     * @return
     */
    public static List<String> readFileViaLines(String filename) throws IOException {
        return Files.readAllLines(new File(filename).toPath());
    }

    /**
     * @param filename
     * @param buf
     * @throws IOException
     */
    public static void writeFile(String filename, StringBuffer buf) throws IOException {
        writeFile(filename, buf.toString());
    }

    public static void writeFile(String filename, String str) throws IOException {
        writeFile(filename, str.getBytes());
    }

    public static void writeFile(String filename, byte b[]) throws IOException {
        mkPath(filename);

        Files.write(new File(filename).toPath(), b);
    }

    public static void writeLines(String filename, List<String> lines, String eol) throws IOException {
        writeLines(filename, lines, eol, false);
    }

    public static void writeLines(String filename, List<String> lines, String eol, boolean append) throws IOException {
        mkPath(filename);

        OutputStream f = new FileOutputStream(filename, append);
        if (lines != null) {
            for (String line : lines) {
                f.write(line.getBytes());
                f.write(eol.getBytes());
            }
        }
        f.flush();
        f.close();
    }

    /**
     * @param filename
     */
    public static void mkPath(String filename) {
        File file = new File(filename);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
            for (File parent = file; parent != null; parent = parent.getParentFile()) {
                new FilePermission(parent.getPath(), "read, write, delete, execute");
            }
        }
    }

    public static void copyFile(Class clazz, String src, String dst) throws IOException {
        copyFile(getInputStream(clazz, src), getOutputStream(clazz, dst), null);
    }

    public static void copyFile(Class clazz, String src, String dst, ChangeListener listener) throws IOException {
        copyFile(getInputStream(clazz, src), getOutputStream(clazz, dst), listener);
    }

    public static void copyResourcesToDirectory(Class clazz, String jar, String prefix, String dest, ChangeListener listener)
            throws IOException {

        int percent = 0, running = 0, total = 100;
        JarInputStream in = new JarInputStream(clazz.getResourceAsStream(jar));
        while (true) {
            JarEntry entry = in.getNextJarEntry();
            if (entry == null) {
                break;
            }

            String filename = entry.getName();
            String pathname = dest + "/" + filename;
            if (filename.startsWith(prefix)) {
                mkPath(pathname);
                copyToFile(in, pathname);

                percent = updateListener(running, total, percent, listener);
                running = running + 1;
            }
        }
    }

    private static void copyToFile(InputStream in, String filename) throws IOException {
        Files.copy(in, new File(filename).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void copyDirectory(String src, String dest) throws IOException {
        File srcFile = new File(src);
        if (srcFile.exists()) {
            mkPath(dest + "/.");
            copyFiles(srcFile, new File(dest));
        }
    }

    public static void copyFiles(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            File files[] = src.listFiles();
            if (files != null) {
                for (File f : files) {
                    copyFiles(f, new File(dest, f.getName()));
                }
            }
        } else {
            copyFile(src.getPath(), dest.getPath());
        }
    }

    public static void copyFile(InputStream in, OutputStream out, ChangeListener listener) throws IOException {
        int percent = 0, nbytes = 0, total = in.available();
        int ch;
        while ((ch = in.read()) != -1) {
            out.write(ch);
            nbytes = nbytes + 1;
            percent = updateListener(nbytes, total, percent, listener);
        }

        in.close();
        out.flush();
        out.close();
    }

    public static void copyFile(String sourcename, String destname) throws IOException {

        mkPath(destname);
        Files.copy(new File(sourcename).toPath(), new File(destname).toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static int updateListener(int running, int total, int last, ChangeListener listener) {
        int percent = last;
        if (listener != null) {
            percent = (running * 100) / total;
            if (percent > last) {
                listener.stateChanged(new ChangeEvent(percent));
            }
        }
        return percent;
    }

    // -------------------------------------------------------------------------

    public static void moveFile(String sourceFile, String destFile) throws IOException {
        File src = new File(sourceFile);
        if (!src.exists()) {
            return;
        }

        File dest = new File(destFile);
        if (dest.isDirectory()) {
            dest = new File(dest, src.getName());
        }

        mkPath(dest.getPath());
        Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void moveDirectory(String sourceDir, String destDir) {
        File src = new File(sourceDir);
        if (!src.exists()) {
            return;
        }

        mkPath(destDir);
        src.renameTo(new File(destDir));
    }

    // -------------------------------------------------------------------------

    public static void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            delete(f);
        }
    }

    public static void deleteDirectory(String directory) {
        File f = new File(directory);
        if (f.exists()) {
            delete(f);
        }
    }

    public static void delete(File file) {
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    delete(f);
                }
            }
        }
        file.delete();
    }

    public static void replaceString(String filename, String fromStr, String toStr) throws IOException {
        String str = readFile(filename).toString().replace(fromStr, toStr);
        writeFile(filename, str.getBytes());
    }
}
