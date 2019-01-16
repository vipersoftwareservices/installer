/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)filename.java	1.00 2012/06/15
 *
 * Copyright 1998-2012 by Viper Software Services
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
 * @version 1.0, 06/15/2012 
 *
 * @note 
 *        
 * -----------------------------------------------------------------------------
 */

package com.viper.installer.actions;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.viper.installer.annotation.ActionTag;
import com.viper.installer.util.FileUtil;
import com.viper.installer.util.Logs;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressBar;

@ActionTag
public class BasicActions {

    /**
     * Copy the filename to the destination directory (dir).
     * 
     * @param dir
     *            the directory file is to be copied to.
     * @param filename
     *            the filename of the file to copy.
     * @return value indicating the status of the copy (false failed, true
     *         success).
     */

    public boolean copy(String dir, String filename) {
        try {
            new File(dir).mkdirs();
            InputStream in = new URL(filename).openStream();
            Files.copy(in, new File(dir + "/" + filename).toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Move the filename to the destination directory (dir).
     * 
     * @param dir
     *            the directory file is to be moved to.
     * @param filename
     *            the filename of the file to move.
     * 
     * @return boolean value indicating the status of the move (false failed,
     *         true success).
     */
    public boolean move(String dir, String filename) {
        try {
            File src = new File(filename);
            if (!src.exists()) {
                return false;
            }

            File dest = new File(dir);
            if (dest.isDirectory()) {
                dest = new File(dest, src.getName());
            }

            dest.getParentFile().mkdirs();
            src.renameTo(dest);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * General purpose command to install a shortcut, can be used for other
     * purposes.
     * 
     * @param createShortCut
     *            is a flag indicating if the shortcut should be installed
     *            matches UI checkbox.
     * @param installHome
     *            the directory of the home of the installation.
     * @param scriptCommand
     *            the script command line t be executed, but to include the
     *            template
     * @param scriptTemplateFilename
     *            the filename of the script template, in which, tokens are to
     *            be replaced with actual values.
     * 
     * @return boolean value indicating the status of installing shortcut (false
     *         failed, true success).
     */

    public boolean installShortcut(String createShortCut, String installHome, String scriptCommand, String scriptTemplateFilename) {
        try {
            Boolean result = Boolean.parseBoolean(createShortCut);
            if (result == null || !result) {
                return false;
            }
            if (scriptCommand == null || scriptCommand.trim().length() == 0) {
                return false;
            }
            if (scriptTemplateFilename == null || scriptTemplateFilename.trim().length() == 0) {
                return false;
            }

            String scriptTemplate = FileUtil.readFile("res:/" + scriptTemplateFilename);
            String script = scriptTemplate.replace("#install.home#", installHome).replace("#user.home#", System.getProperty("user.home"));
            String tmpDirectory = System.getProperty("java.io.tmpdir");

            Logs.info("Installing ShortCut: " + tmpDirectory + "/" + scriptTemplateFilename);
            FileUtil.writeFile(tmpDirectory + "/" + scriptTemplateFilename, script);

            String cmd = scriptCommand + " \"" + tmpDirectory + "/" + scriptTemplateFilename + "\"";
            Utils.exec(cmd, installHome);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Install file named filename from the installation bundle to the specified
     * directory. Create all the directory and all ancestors if missing.
     * 
     * @param directory
     *            the directory where file is to be copied
     * @param filename
     *            the filename of the file to be copied
     * @param user
     *            unused
     * @param group
     *            unused
     * @return boolean value indicating the status of the copy (false failed,
     *         true success).
     */
    public boolean installFile(String directory, String filename, String user, String group) {

        try {
            // Check if installation directory exists.
            new File(directory).mkdirs();

            showStatus("Processing filename: " + filename);

            Utils.copyToFile(getClass(), "res:/" + filename, directory + "/" + filename);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Unzip the file named filename from the installation bundle, into the
     * directory. Create all the directory and all ancestors if missing.
     * 
     * @param directory
     *            the target directory where files are to be unzipped
     * @param filename
     *            the name of the zip file to be unzipped
     * @param progressBar
     *            unused
     * 
     * @return boolean value indicating the status of the unzip (false failed,
     *         true success).
     */

    public boolean unzip(String directory, String filename, ProgressBar progressBar) {

        try {
            Logs.info("Unzip: " + directory + ":" + filename);

            // Check if installation directory exists.
            new File(directory).mkdirs();

            Class clazz = getClass();

            ZipInputStream zipStream = new ZipInputStream(Utils.getInputStream(clazz, filename));

            while (true) {
                ZipEntry entry = zipStream.getNextEntry();
                if (entry == null) {
                    break;
                }

                Logs.info("Extracting: " + entry.getName());

                String toFilename = directory + "/" + entry.getName();
                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then
                    // children.
                    Utils.mkPath(toFilename + "/.");
                    continue;
                } else {
                    Utils.mkPath(toFilename);
                }

                Utils.copyStream(zipStream, new BufferedOutputStream(new FileOutputStream(toFilename)), false);
            }
            zipStream.close();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return false;
    }

    public void postInstall() {
    }

    public final boolean checkFileExists(String filename) {
        return new File(filename).exists();
    }

    public final boolean checkFileExists(String installHome, String filename) {
        return new File(installHome + "/" + filename).exists();
    }

    /**
     * If possible this method opens the default browser to the specified web
     * page. If not it notifies the user of webpage's url so that they may
     * access it manually.
     * 
     * @param readReleaseNotes
     *            a flag indicating if we should launch the browser to read
     *            release notes, or do nothing.
     * @param directory
     *            the installation directory where the release notes can be
     *            found.
     * @param filename
     *            the filename of the release notes
     * @return boolean value indicating the status of the launching the release
     *         notes (false failed, true success).
     */
    public boolean readReleaseNotes(String readReleaseNotes, String directory, String filename) {
        if (readReleaseNotes == null || !Boolean.parseBoolean(readReleaseNotes)) {
            return false;
        }

        File file = new File(directory + "/" + filename);
        try {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(file.toURI());
                return true;
            } else if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(file);
                return true;
            }
        } catch (Exception e) {
            /*
             * I know this is bad practice but we don't want to do anything
             * clever for a specific error
             */
            e.printStackTrace();

            // Copy URL to the clipboard so the user can paste it into their
            // browser
            StringSelection stringSelection = new StringSelection(file.toURI().toString());
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
            // Notify the user of the failure
            Logs.error("This program just tried to open a webpage." + "\n"
                    + "The URL has been copied to your clipboard, simply paste into your browser to access." + " Webpage: " + file.toURI());
        }
        return false;
    }

    /**
     * 
     * @param installHome 
     * @param fname 
     *            file or directory to set permissions, including children (if
     *            directory).
     * @return boolean value indicating the status of the setting the execute
     *         file status (false failed, true success).
     */
    public boolean setExecutePermission(String installHome, String fname) {
        String filename = (installHome == null) ? fname : installHome + "/" + fname;
        try {
            File f = new File(filename);
            if (f.isDirectory()) {
                for (File filename1 : f.listFiles()) {
                    setExecutePermission(null, filename1.getAbsolutePath());
                }
            } else {
                new FilePermission(f.getPath(), "read,write,delete,execute");

                if (!Utils.isWindows()) {
                    Set<PosixFilePermission> perms = new HashSet<>();
                    perms.add(PosixFilePermission.OWNER_READ);
                    perms.add(PosixFilePermission.OWNER_WRITE);
                    perms.add(PosixFilePermission.OWNER_EXECUTE);
                    perms.add(PosixFilePermission.GROUP_READ);
                    perms.add(PosixFilePermission.GROUP_WRITE);
                    perms.add(PosixFilePermission.GROUP_EXECUTE);

                    Path path = FileSystems.getDefault().getPath(f.getAbsolutePath());
                    Files.setPosixFilePermissions(path, perms);
                }
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check the both flags runScript and runScriptConfirmed for positive
     * values, if found, then run the command/script in the specified wrking
     * directory (directory).
     * 
     * @param runScript
     *            flag indicating whether to run the script
     * @param runScriptConfirmed
     *            flag confirming to run he script
     * @param directory
     *            directory where script is to be run
     * @param cmd
     *            command/script which is to be executed.
     * 
     * @return boolean value indicating the status of running the
     *         command/.script (false failed, true success).
     */
    public boolean runScript(String runScript, String runScriptConfirmed, String directory, String cmd) {
        if (runScript == null || !Boolean.parseBoolean(runScript)) {
            return false;
        }
        if (runScriptConfirmed == null || !Boolean.parseBoolean(runScriptConfirmed)) {
            return false;
        }
        try {
            String result = Utils.exec(cmd, directory);
            Logs.info("RUNSCRIPT: " + directory + "," + cmd + "," + result);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check the both flags runScript and runScriptConfirmed for positive
     * values, if found, then run the command/script in the specified wrking
     * directory (directory).
     * 
     * @param runScript
     *            flag indicating whether to run the script
     * @param runScriptConfirmed
     *            flag confirming to run he script
     * @param directory
     *            directory where script is to be run
     * @param cmd
     *            command/script which is to be executed.
     * 
     * @return boolean value indicating the status of running the
     *         command/.script (false failed, true success).
     */
    public boolean runConfigScript(String installHome, String runScript, String group, String section, String name, String value) {

        try {
            if (value == null || value.trim().length() == 0) {
                value = "''";
            } else {
                value = "'" + value + "'";
            }
            String cmd = runScript + " " + group + " " + section + " " + name + " " + value + "";
            String result = Utils.exec(cmd, installHome);
            Logs.info("RUNSCRIPT: " + installHome + "," + cmd + "," + result);

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 
     * @param runScript
     *            flag indicating whether to run the script
     * @param directory
     * @param installHome
     * @param cmd
     *            command/script which is to be executed.
     * @return boolean value indicating the status of running the
     *         command/.script (false failed, true success).
     */
    public boolean runScriptTemplate(String runScript, String directory, String installHome, String cmd) {
        if (runScript == null || !Boolean.parseBoolean(runScript)) {
            return false;
        }
        try {
            String theCmd = cmd.replace("#install.home#", installHome).replace("#user.home#", System.getProperty("user.home"));

            Utils.exec(theCmd, directory);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 
     */
    public boolean createDirectory(String directory) {
        try {
            Utils.mkPath(directory + "/foo");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 
     */
    public boolean runScript(String cmd) {
        try {
            Utils.exec(cmd, null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 
     */
    public boolean runScript(String directory, String cmd) {
        try {
            Utils.exec(cmd, directory);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 
     */
    public boolean runScript(String runScript, String directory, String cmd) {
        if (runScript == null || !Boolean.parseBoolean(runScript)) {
            return false;
        }
        try {
            Utils.exec(cmd, directory);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 
     */
    public final boolean checkScript(String directory, String cmd, String name, String result) {
        try {

            String results = Utils.exec(cmd, directory);
            return (results == null) ? false : results.contains(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 
     */
    public boolean canUpdate(String directory) {
        try {
            File file = new File(directory);
            return file.canExecute() && file.canRead() && file.canWrite();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public final static boolean checkWindows7ServicePack1() {

        try {
            if (Utils.isWindows()) {
                String results = Utils.exec("systeminfo", null);
                if (results.contains("Microsoft Windows 7")) {
                    if (results.contains("Service Pack")) {
                        Logs.msg("Success: Windows 7 found with Service Pack installed.");
                    } else {
                        Logs.msg("ERROR: Windows 7 found but no Service Pack installed.");
                    }
                } else {
                    Logs.msg("Success: Windows is supported.");
                }

            } else if (Utils.isUnix()) {
                Logs.msg("Success: Linux/Unix systems are supported.");

            } else if (Utils.isMac()) {
                Logs.msg("Success: MacOs systems are supported.");

            } else {
                Logs.msg("Warning: Unable to determine if system is supported.");

            }
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Logs.warn("Warning: Unable to determine if system is supported.");

        return true;
    }

    public final static boolean checkJava(String version) {

        try {
            String results = Utils.exec("java -version", null);

            String token = "java version";
            int i0 = results.indexOf(token);
            if (i0 == -1) {
                Logs.warn("Warning: Unable to determine if proper version of java is installed.");
                return true;
            }
            int i1 = results.indexOf("\"", i0);
            if (i1 == -1) {
                Logs.warn("Warning: Unable to determine if proper version of java is installed.");
                return true;
            }
            int i2 = results.indexOf(".", i1);
            if (i2 == -1) {
                Logs.warn("Warning: Unable to determine if proper version of java is installed.");
                return true;
            }
            int i3 = results.indexOf(".", i2 + 1);
            if (i3 == -1) {
                Logs.warn("Warning: Unable to determine if proper version of java is installed.");
                return true;
            }

            double v1 = Double.parseDouble(version);
            double v2 = Double.parseDouble(results.substring(i1 + 1, i3));

            if (v1 > v2) {
                Logs.msg("ERROR: java version " + v2 + " found, but require version " + v1 + " or better.");
            } else {
                Logs.msg("Success: java version " + v2 + " found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Logs.warn("Warning: Unable to determine if java is installed.");
        return true;
    }

    /**
     * Check if the file exists, that is a requirement met. If so output message
     * saying tool has been installed.
     * 
     * @param toolname
     *            the name of the tool for message purposes
     * @param argument
     *            the list of filenames, comma separated, only one of the files
     *            need exist. To say that tool requirement has been met.
     * @return whether the file exists or not (true file exists, false doesn't)
     */
    public final boolean checkFilesExists(String toolname, String argument) {

        String[] filenames = argument.split(",");
        for (String filename : filenames) {
            Logs.info("Confirm if file " + filename + " exists.");
            if (new File(filename.trim()).exists()) {
                Logs.info("Success: Able to confirm " + toolname + " is installed.");
                return true;
            }
        }
        Logs.info("ERROR: Unable to find if " + toolname + " is installed.");
        return false;
    }

    public void finish() {
        System.exit(0);
    }

    public static void showStatus(String title) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.showAndWait();
    }

    public static void showErrorDialog(String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}