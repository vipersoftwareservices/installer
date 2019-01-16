/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)Utils.java	1.00 2008/06/15
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

package com.viper.installer.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.viper.installer.InstallWizard;
import com.viper.installer.annotation.ActionTag;
import com.viper.installer.util.CSVUtil;
import com.viper.installer.util.Logs;

@ActionTag
public class Utils implements ErrorHandler {

    private static List<String> EXECUTABLE_DIRECTORIES = new ArrayList<String>();

    static {
        EXECUTABLE_DIRECTORIES.add("/sbin");
        EXECUTABLE_DIRECTORIES.add("/usr/sbin");
        EXECUTABLE_DIRECTORIES.add("/usr/local/sbin");
        EXECUTABLE_DIRECTORIES.add("/bin");
        EXECUTABLE_DIRECTORIES.add("/usr/bin");
        EXECUTABLE_DIRECTORIES.add("c:/windows/system");
        EXECUTABLE_DIRECTORIES.add("c:/windows/system32");
        EXECUTABLE_DIRECTORIES.add("c:/windows/system32/wbem");
    }

    private static List<String> SHELL_COMMANDS = new ArrayList<String>();

    static {
        SHELL_COMMANDS.add("bash");
        SHELL_COMMANDS.add("bash.exe");
        SHELL_COMMANDS.add("sh");
        SHELL_COMMANDS.add("sh.exe");
        SHELL_COMMANDS.add("cmd.exe");
    }

    // -------------------------------------------------------------------------

    public static final InputStream getInputStream(String filename) throws Exception {
        return getInputStream(InstallWizard.class, filename);
    }

    public static final InputStream getInputStream(Class clazz, String filename) throws Exception {
        InputStream in = null;
        if (filename == null) {
        } else if (filename.startsWith("http:")) {
            in = new URL(filename).openStream();
        } else if (filename.startsWith("res:")) {
            if (clazz == null) {
                clazz = InstallWizard.class;
            }
            in = clazz.getResourceAsStream(filename.substring("res:".length()));
            if (in == null) {
                throw new IOException("Resource file not found: " + filename);
            }
        } else {
            in = new FileInputStream(filename);
        }
        return in;
    }

    // -------------------------------------------------------------------------

    public static final boolean fileExists(String filename) {
        try {
            InputStream in = getInputStream(filename);
            if (in != null) {
                in.close();
            }
            return true;
        } catch (Throwable t) {
        }
        return false;
    }

    // -------------------------------------------------------------------------

    public static final Document getDocument(String filename) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            builder.setErrorHandler(new Utils());
            return builder.parse(new InputSource(getInputStream(filename)));

        } catch (Exception e) {
            Logs.error("Failure to parse file=" + filename, e);
        }
        return null;
    }

    public static final String readFile(String filename) throws Exception {
        StringBuffer buffer = new StringBuffer();
        Reader f = new BufferedReader(new InputStreamReader(getInputStream(filename)));
        int ch;
        while ((ch = f.read()) > -1) {
            buffer.append((char) ch);
        }
        f.close();
        return buffer.toString();
    }

    public static final void copyToFile(Class clazz, String srcname, String destname) throws Exception {

        InputStream in = getInputStream(clazz, srcname);
        if (in == null) {
            throw new IOException("readResource not found=> " + srcname);
        }
        if (new File(destname).isDirectory()) {
            destname = destname + "/" + srcname.substring(srcname.lastIndexOf('/') + 1);
        }

        copyStream(in, new FileOutputStream(destname), true);
    }

    public static final void unjar(Class clazz, String jar, String prefix, String dest, boolean includePrefix) throws Exception {

        JarInputStream in = new JarInputStream(getInputStream(clazz, jar));
        while (true) {
            JarEntry entry = in.getNextJarEntry();
            if (entry == null) {
                break;
            }

            String filename = entry.getName();
            if (filename.startsWith(prefix)) {
                String pathname = dest + "/" + filename;
                if (!includePrefix) {
                    pathname = dest + filename.substring(prefix.length());
                }

                Logs.info("Extracting: " + filename + " to " + pathname);

                if (pathname.endsWith("/")) {
                    mkPath(pathname + ".");
                    continue;
                }

                mkPath(pathname);
                copyStream(in, new FileOutputStream(pathname), false);

            }
        }
        in.close();
    }

    public static final void copyDirectory(String src, String dest) throws IOException {
        File srcFile = new File(src);
        if (!srcFile.exists()) {
            throw new IOException("copyDirectory failed on source directory does not exist " + srcFile.getPath());
        }
        copyFiles(srcFile, new File(dest));
    }

    public static final void copyFiles(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            mkPath(dest + "/.");
            File files[] = src.listFiles();
            if (files != null) {
                for (File f : files) {
                    copyFiles(f, new File(dest, f.getName()));
                }
            }
        } else {
            copyFile(src, dest);
        }
    }

    private static final void copyFile(File src, File dest) throws IOException {
        copyStream(new FileInputStream(src), new FileOutputStream(dest), true);
    }

    public static final void copyFile(String src, String dest) throws IOException {
        copyStream(new FileInputStream(src), new FileOutputStream(dest), true);
    }

    public static final void copyStream(InputStream in, OutputStream out, boolean doClose) throws IOException {
        byte[] buffer = new byte[16384];

        int n, nbytes = 0;
        while ((n = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, n);
            nbytes = nbytes + n;
        }
        out.flush();
        out.close();
        if (doClose) {
            in.close();
        }
    }

    public static final void writeFile(String filename, String str) throws IOException {
        writeFile(filename, str.getBytes());
    }

    public static final void writeFile(String filename, byte b[]) throws IOException {
        mkPath(filename);

        OutputStream f = new FileOutputStream(filename);
        if (b != null && b.length != 0) {
            f.write(b);
        }
        f.flush();
        f.close();
    }

    public static final void mkPath(String filename) {
        File file = new File(filename);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
            for (File parent = file; parent != null; parent = parent.getParentFile()) {
                new FilePermission(parent.getPath(), "read, write, delete, execute");
            }
        }
    }

    public static final void moveFile(String sourceFile, String destFile) throws IOException {
        File src = new File(sourceFile);
        if (!src.exists()) {
            throw new IOException("moveFile failed src file does not exist: " + sourceFile);
        }

        File dest = new File(destFile);
        if (dest.isDirectory()) {
            dest = new File(dest, src.getName());
        }

        // dest.getParentFile().mkdirs();

        if (!src.renameTo(dest)) {
            copyFile(src.getPath(), dest.getPath());
            if (!src.delete()) {
                throw new IOException("moveFile failed to move file " + src.getPath() + " to " + dest.getPath());
            }
        }
    }

    public static final void moveDirectory(String sourceDir, String destDir) throws IOException {
        File src = new File(sourceDir);
        if (!src.exists()) {
            throw new IOException("moveDirectory failed on source directory does not exist " + src.getPath());
        }

        File dest = new File(destDir);
        if (!dest.exists()) {
            if (!dest.mkdirs()) {
                throw new IOException("moveDirectory failed on mkdirs for " + dest.getPath());
            }
        }
        if (!src.renameTo(dest)) {
            copyDirectory(sourceDir, destDir);
            deleteDirectory(sourceDir);
        }
    }

    public static final void deleteFile(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            delete(f);
        }
    }

    public static final void deleteDirectory(String directory) {
        File f = new File(directory);
        if (f.exists()) {
            delete(f);
        }
    }

    public static final void delete(File file) {
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

    public static final List<String> listUsers() throws Exception {
        if (existsOnPath("cmd.exe")) {
            return parseWindowsForUsers(exec("cmd /c dir c:\\users /b", null));
        } else if (existsOnPath("lookupd")) {
            return parseUnix(shell("lookupd -q user | cut -f2 -d':'", null));
        } else {
            return parseUnix(shell("cat /etc/passwd | cut -f1 -d':'", null));
        }
    }

    public static final List<String> listGroups() throws Exception {
        if (existsOnPath("net.exe")) {
            return parseWindows(exec("net localgroup", null));
        } else if (existsOnPath("lookupd")) {
            return parseUnix(shell("lookupd -q group | cut -f2 -d':'", null));
        } else {
            return parseUnix(shell("cat /etc/group | cut -f1 -d':'", null));
        }
    }

    public static final List<String> parseUnix(String result) {

        List<Map<String, String>> csv = CSVUtil.toListOfMapsFromFields(result, "[\\n\\r]+", "\\s+");

        List<String> groups = new ArrayList<String>();
        for (Map<String, String> row : csv) {
            groups.add(row.get("Name"));
        }
        return groups;
    }

    public static final List<String> parseWindows(String result) {

        String[] lines = result.trim().split("[\\n\\r]+");
        List<String> groups = new ArrayList<String>();
        for (String line : lines) {
            if (line.trim().startsWith("*")) {
                groups.add(line.trim().substring(1));
            }
        }
        return groups;
    }

    public static final List<String> parseWindowsForUsers(String result) {

        String[] lines = result.trim().split("[\\n\\r]+");
        List<String> items = new ArrayList<String>();
        for (String line : lines) {
            items.add(line.trim());
        }
        return items;
    }

    public static final boolean existsOnPath(String cmd) {
        return !cmd.equals(findPath(cmd));
    }

    public static final String findPath(List<String> cmds) {
        for (String cmd : cmds) {
            if (existsOnPath(cmd)) {
                return findPath(cmd);
            }
        }
        return null;
    }

    public static final String findPath(String cmd) {
        String[] paths = System.getenv("path").split(System.getProperty("path.separator"));
        for (String path : paths) {
            File file = new File(path + "/" + cmd);
            Logs.info("Trying file: " + file.getPath());
            if (file.exists()) {
                if (file.canExecute()) {
                    return file.getPath().replace('\\', '/');
                }
                Logs.info("File found: " + file.getPath() + ", but can not execute.");
            }
        }
        for (String dir : EXECUTABLE_DIRECTORIES) {
            File file = new File(dir + "/" + cmd);
            Logs.info("Trying file: " + file.getPath());
            if (file.exists()) {
                if (file.canExecute()) {
                    return file.getPath().replace('\\', '/');
                }
                Logs.info("File found: " + file.getPath() + ", but can not execute.");
            }
        }
        Logs.info("WARNING: shell '" + cmd + "' command not found, trying '" + cmd + "'");
        return cmd;
    }

    public static final String shell(String cmd, String cwd) throws IOException, InterruptedException {
        return exec(findPath(SHELL_COMMANDS), cmd, cwd);
    }

    public static final String exec(String cmd, String cwd) throws IOException, InterruptedException {
        return exec(cmd, null, cwd);
    }

    public static final String exec(String cmd, String inputStr, String cwd) throws IOException, InterruptedException {

        cwd = (cwd != null) ? cwd : System.getProperty("user.dir");
        Process proc = Runtime.getRuntime().exec(cmd, null, new File(cwd));

        BufferedWriter stdin = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        if (inputStr != null) {
            stdin.write(inputStr);
            stdin.newLine();
        }

        stdin.flush();
        stdin.close();
        StringBuffer stdout = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        new StreamThread(proc.getInputStream(), stdout).start();
        new StreamThread(proc.getErrorStream(), stderr).start();

        proc.waitFor();

        Logs.msg(stdout);
        Logs.msg(stderr);
        return stdout.toString();
    }

    static class StreamThread extends Thread {
        BufferedReader in;
        StringBuffer str;

        public StreamThread(InputStream in, StringBuffer str) {
            this.in = new BufferedReader(new InputStreamReader(in));
            this.str = str;
        }

        public void run() {
            try {
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    str.append(line);
                    str.append("\n");
                }
                in.close();
            } catch (IOException ioe) {
                Logs.error("Failed to read input stream,:", ioe);
            }
        }
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
    }

    public static boolean isSolaris() {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("sunos") >= 0);
    }

    public static String getOSName() {
        if (isWindows()) {
            return "Windows";
        }
        if (isMac()) {
            return "Mac";
        }
        if (isUnix()) {
            return "Unix";
        }
        if (isSolaris()) {
            return "Solaris";
        }
        return "Windows";
    }

    public static void replaceString(String filename, String fromStr, String toStr) throws Exception {
        String str = readFile(filename);
        str = str.replace(fromStr, toStr);
        writeFile(filename, str.getBytes());
    }

    /**
     * Receive notification of a warning.
     * <p>
     * SAX parsers will use this method to report conditions that are not errors
     * or fatal errors as defined by the XML recommendation. The default
     * behaviour is to take no action.
     * </p>
     * <p>
     * The SAX parser must continue to provide normal parsing events after
     * invoking this method: it should still be possible for the application to
     * process the document through to the end.
     * </p>
     * <p>
     * Filters may use this method to report other, non-XML warnings as well.
     * </p>
     * 
     * @param exception
     *            The warning information encapsulated in a SAX parse exception.
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void warning(SAXParseException exception) throws SAXException {
        Logs.warn("DefaultErrorHandlerImpl: Lineno" + exception.getLineNumber() + ": Column" + exception.getColumnNumber() + " "
                + exception.getPublicId() + " " + exception.getSystemId() + " " + exception.getMessage());
        throw exception;
    }

    /**
     * Receive notification of a recoverable error.
     * <p>
     * This corresponds to the definition of "error" in section 1.2 of the W3C
     * XML 1.0 Recommendation. For example, a validating parser would use this
     * callback to report the violation of a validity constraint. The default
     * behaviour is to take no action.
     * </p>
     * <p>
     * The SAX parser must continue to provide normal parsing events after
     * invoking this method: it should still be possible for the application to
     * process the document through to the end. If the application cannot do so,
     * then the parser should report a fatal error even if the XML
     * recommendation does not require it to do so.
     * </p>
     * <p>
     * Filters may use this method to report other, non-XML errors as well.
     * </p>
     * 
     * @param exception
     *            The error information encapsulated in a SAX parse exception.
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void error(SAXParseException exception) throws SAXException {
        Logs.warn("DefaultErrorHandlerImpl: Lineno" + exception.getLineNumber() + ": Column" + exception.getColumnNumber() + " "
                + exception.getPublicId() + " " + exception.getSystemId() + " " + exception.getMessage());
        throw exception;
    }

    /**
     * Receive notification of a non-recoverable error.
     * <p>
     * <strong>There is an apparent contradiction between the documentation for
     * this method and the documentation for
     * {@link org.xml.sax.ContentHandler#endDocument}. Until this ambiguity is
     * resolved in a future major release, clients should make no assumptions
     * about whether endDocument() will or will not be invoked when the parser
     * has reported a fatalError() or thrown an exception.</strong>
     * </p>
     * <p>
     * This corresponds to the definition of "fatal error" in section 1.2 of the
     * W3C XML 1.0 Recommendation. For example, a parser would use this callback
     * to report the violation of a well-formedness constraint.
     * </p>
     * <p>
     * The application must assume that the document is unusable after the
     * parser has invoked this method, and should continue (if at all) only for
     * the sake of collecting additional error messages: in fact, SAX parsers
     * are free to stop reporting any other events once this method has been
     * invoked.
     * </p>
     * 
     * @param exception
     *            The error information encapsulated in a SAX parse exception.
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.SAXParseException
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        Logs.error("DefaultErrorHandlerImpl: Lineno" + exception.getLineNumber() + ": Column" + exception.getColumnNumber() + " "
                + exception.getPublicId() + " " + exception.getSystemId(), exception);
        throw exception;
    }
}
