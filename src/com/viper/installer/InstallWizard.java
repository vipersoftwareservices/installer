/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)InstallWizard.java	1.00 2014/06/15
 *
 * Copyright 1998-2014 by Viper Software Services
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
 * @version 1.0, 06/15/2014 
 *
 * @note 
 *        
 * -----------------------------------------------------------------------------
 */

package com.viper.installer;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

import com.viper.installer.actions.ActionManager;
import com.viper.installer.actions.Utils;
import com.viper.installer.model.Installation;
import com.viper.installer.model.Page;
import com.viper.installer.model.Parameter;
import com.viper.installer.util.Logs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class InstallWizard extends Application {

    private static final String installFilename = "/Installation.xml";

    // -------------------------------------------------------------------------

    public InstallWizard() {
    }

    @Override
    public final void start(Stage stage) {

        try {
            Installation doc = unmarshal(Installation.class, installFilename);
            if (doc == null) {
                throw new Exception("Unable to process intallation commands." + installFilename);
            }

            Session session = Session.getInstance();

            // Process all the external parameters
            processParameters(session, doc.getParam());
            

            session.put("ui", true);

            // Redirect the log file
            Logs.redirect((String) session.getParameters().get("LogFilename"));

            List<WizardPage> wizardPages = new ArrayList<WizardPage>();
            for (Page page : doc.getPage()) {
                wizardPages.add(new WizardPage(stage, page, session));
            }

            BranchFlow flow = new BranchFlow(session, wizardPages);
            Wizard wizard = new Wizard(session, flow);
            for (WizardPage page : wizardPages) {
                wizard.addCard(page);
            }
            wizard.show(wizardPages.get(0), true);

            Scene scene = new Scene(wizard, doc.getWidth(), doc.getHeight());
            // scene.getStylesheets().add(AquaSkin.getStylesheet());
            if (doc.getCss() != null) {
                scene.getStylesheets().add(getClass().getResource(doc.getCss()).toExternalForm());
            }

            if (doc.getWidth() > 0 && doc.getHeight() > 0) {
                stage.setWidth(doc.getWidth());
                stage.setHeight(doc.getHeight());
            }

            stage.setTitle(doc.getName());
            stage.getIcons().add(new Image(getClass().getResourceAsStream(doc.getProgramIcon())));
            stage.setScene(scene);
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------

    private static final Hashtable<String, JAXBContext> contextStore = new Hashtable<String, JAXBContext>();

    private final static <T> JAXBContext getJAXBContext(Class<T> clazz) throws JAXBException {
        String packageName = clazz.getPackage().getName();
        JAXBContext context = contextStore.get(packageName);
        if (context == null) {
            try {
                if (classExists(packageName + ".ObjectFactory")) {
                    context = JAXBContext.newInstance(packageName);
                } else {
                    context = JAXBContext.newInstance(clazz);
                }
            } catch (JAXBException je) {
                je.printStackTrace();
                throw new JAXBException("Create the JAXBContext failed for the package " + packageName, je);
            }
            contextStore.put(packageName, context);
        }
        if (context == null) {
            throw new JAXBException("No JAXBContext for package " + packageName);
        }
        return context;
    }

    private final static boolean classExists(String classname) {
        try {
            return (Class.forName(classname) != null);
        } catch (Exception ex) {
            return false;
        }
    }

    private final static <T> T unmarshal(Class<T> clazz, String filename) throws Exception {
        File file = new File(filename);
        if (file.exists()) {
            return (T)getJAXBContext(clazz).createUnmarshaller().unmarshal(new StreamSource(file), clazz).getValue();
        } else {
            StreamSource source = new StreamSource(clazz.getResourceAsStream(filename));
            return getJAXBContext(clazz).createUnmarshaller().unmarshal(source, clazz).getValue();
        }
    }

    private final static void processParameters(Session session, List<Parameter> params) throws Exception {
        // Process all the external parameters
        for (Parameter param : params) {
            session.put(param.getName(), new SimpleStringProperty(param.getValue()));
            String key = toOSKey(param.getName());
            if (key != null && param.getValue() != null) {
                session.put(key, new SimpleStringProperty(param.getValue()));
            }
        }
    }

    private final static String toOSKey(String key) {
        String osname = Utils.getOSName();
        if (key.endsWith("." + osname)) {
            int index = key.lastIndexOf(".");
            return key.substring(0, index);
        }
        return null;
    }

    public final static void quickInstall(String filename) {
        try {

            // Load installation file as resource
            Installation doc = unmarshal(Installation.class, installFilename);
            if (doc == null) {
                throw new Exception("Unable to process installation commands: " + installFilename);
            }

            // Load custom parameters
            com.viper.installer.model.Parameters custom = unmarshal(com.viper.installer.model.Parameters.class, filename);
            if (custom == null) {
                throw new Exception("Unable to process installation commands: " + filename);
            }

            Session session = Session.getInstance();

            // Process all the external parameters
            processParameters(session, doc.getParam());

            // Process all the custom parameters
            processParameters(session, custom.getParam());
            
            session.put("ui", false);

            // 1. Output name of installation
            Logs.msg("Quick Installation: " + doc.getName());
            // 2. Output licensing
            // 3. Output directory to install
            Logs.msg("    installing into directory: " + session.get("InstallHome"));
            // 4. install
            Logs.msg("    installing ...");

            for (Page page : doc.getPage()) {
                if (page.getActions() != null) {
                    ActionManager.executeActions(page.getActions().getAction(), session);
                }
            }

            Logs.msg("    installing complete.");

        } catch (Exception ex) {
            Logs.error("", ex);
        }
        Platform.exit();
    }

    public static void main(String args[]) {
        String installType = "ui";
        String filename = "";

        for (int i = 0; i < args.length; i++) {
            if ("-quick".equalsIgnoreCase(args[i])) {
                installType = "quick";
                filename = args[++i];
            }
        }

        if ("ui".equalsIgnoreCase(installType)) {
            launch(InstallWizard.class, args);

        } else if ("quick".equalsIgnoreCase(installType)) {
            InstallWizard.quickInstall(filename);

        }
    }
}