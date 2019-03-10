/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)ActionManager.java	1.00 2012/11/15
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
 * @version 1.0, 11/15/2012 
 *
 * @note 
 *        
 * -----------------------------------------------------------------------------
 */

package com.viper.installer.actions;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import com.viper.installer.Session;
import com.viper.installer.annotation.ActionTag;
import com.viper.installer.model.Action;
import com.viper.installer.util.Logs;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;

/**
 * The Action Manager class is called to process any actions which are part of
 * the installation process. Actions can occur when a button is pressed on the
 * installation UI. They can also occur, after a screen is displayed, and it is
 * desired for a part of the screen to be updated.
 * <p>
 * Actions are implemented as a JEXL string expression, JEXL is part of the Java
 * release. Included as part of JEXL, is the ability to dynamically load
 * classes. This allows the installation tool to be extensible, customizable, by
 * specific product installations.
 * <p>
 * As per MVC design pattern, the ActionManager is the controller (high level).
 * 
 * @author Tom_Nevin
 *
 */

public class ActionManager {

    /**
     * This method handles executing a complete list of actions, stopping after
     * the list is executed or after an action fails. A failed action prevents
     * further actions from being executed in the list.
     *
     * @param actions
     *            the list of actions to be executed
     * @param session
     *            the current list of session parameters.
     * @return the status of executing the action, true = success, false action
     *         failed.
     */
    public final static boolean executeActions(List<Action> actions, Session session) {
        if (actions != null) {
            for (int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);
                if (executeAction(action, session) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method handles executing a single action. The action is evaluated,
     * and the result of the action is returned as an object, typically, a
     * string.
     *
     * @param action
     *            the action to be executed
     * @param session
     *            the current list of session parameters.
     * @return the status of executing the action, true = success, false action
     *         failed.
     */
    public final static Object executeAction(Action action, Session session) {
        Object result = null;
        if (action != null && action.getValue() != null) {
            String value = action.getValue().trim();
            try {
                result = eval(value, session.getParameters());
                System.out.println("executeAction: " + value + ","+ result);
            } catch (Throwable ex) {
                showErrorDialog("Failure with action class: " + value + ", " + ex);
                Logs.error("Failure with action class: " + value, ex);
            }
            if ((boolean) session.get("ui") != false) {
                if (action.getCheckbox() != null) {
                    CheckBox cb = (CheckBox) session.get(action.getCheckbox() + ".component");
                    if (cb != null) {
                        cb.setSelected((result != null && result instanceof Boolean && (Boolean) result == true) ? true : false);
                    }
                }
            }
        }
        Logs.info("Leaving ExecuteAction: " + action.getValue());
        return result;
    }

    /**
     * Show a popup message as an alert and wait for a response.
     * 
     * @param msg
     *            the alert message to be displayed.
     */
    private final static void showErrorDialog(String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Given the expression and a map of parameters, evaluate the expression
     * returning the result.
     * 
     * @param expression
     *            the jexl expression e.g. (A + B / C)
     * @param params
     *            the key value map if parameters (A = 2, B = 4, D = 2)
     * @return the result of evaluating the expression against the parameters
     *         e.g.( 4 )
     * @throws Exception
     */

    public final static Object eval(String expression, Map<String, Object> params) throws Exception {

        if (expression == null) {
            return null;
        }

        JexlContext jexlContext = createContext(params);
        JexlEngine engine = new JexlEngine();
        engine.setDebug(true);
        engine.setSilent(false);

        return engine.createExpression(expression).evaluate(jexlContext);
    }

    /**
     * 
     * @param namespace
     * @return
     */
    private final static JexlContext createContext(Map<String, Object> namespace) throws Exception {
        JexlContext jexlContext = new MapContext();
        jexlContext.set("String", java.lang.String.class);

        initializeClasses(jexlContext, "com.viper.installer.actions");
        String customPackage = (String) namespace.get("custom.package");
        if (customPackage != null) {
            initializeClasses(jexlContext, customPackage);
        }

        if (namespace != null) {
            for (String key : namespace.keySet()) {
                jexlContext.set(key, namespace.get(key));
            }
        }
        return jexlContext;
    }

    /**
     * Initialize all classes in the package which have the ActionTag
     * annotation. These classes will be referred to in the installation
     * actions.
     * 
     * @param jexlContext
     *            the context of parameters which JEXL will use for evaluation
     *            expressions.
     * @param packageName
     *            the name of the package to scan all classes for ActionTag
     *            annotation.
     * @throws Exception
     *             when there is problem finding or creating the found classes.
     */
    private final static void initializeClasses(JexlContext jexlContext, String packageName) throws Exception {
        List<Class> clazzes = getClassesWithAnnotation(packageName, ActionTag.class);
        for (Class clazz : clazzes) {
            jexlContext.set(clazz.getName(), clazz.newInstance());
            jexlContext.set(clazz.getSimpleName(), clazz.newInstance());
        }
    }

    /**
     * Scans all classes accessible from the context class loader which belong
     * to the given package and subpackages.
     * 
     * @param packageName
     *            The base package
     * @return The classes
     * @throws Exception
     */
    private final static List<Class> getClasses(String packageName) throws Exception {

        List<Class> classes = new ArrayList<Class>();
        if (packageName == null) {
            return classes;
        }

        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            List<Class> clazzes = null;
            if ("file".equalsIgnoreCase(resource.getProtocol())) {
                clazzes = findClassesInFile(classLoader, new File(resource.getFile()), packageName);
            }
            if ("jar".equalsIgnoreCase(resource.getProtocol())) {
                clazzes = findClassesInJar(classLoader, resource, packageName);
            }
            if (clazzes != null) {
                for (Class clazz : clazzes) {
                    if (!classes.contains(clazz)) {
                        classes.add(clazz);
                    }
                }
            }
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and
     * sub-directories.
     * 
     * @param directory
     *            The base directory
     * @param packageName
     *            The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private final static List<Class> findClassesInFile(ClassLoader classLoader, File directory, String packageName) throws Exception {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClassesInFile(classLoader, file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and
     * sub-directories.
     * 
     * @param directory
     *            The base directory
     * @param packageName
     *            The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private final static List<Class> findClassesInJar(ClassLoader classLoader, URL resource, String packageName) throws Exception {

        List<Class> classes = new ArrayList<Class>();

        packageName = packageName.replace('.', '/');

        JarURLConnection uc = (JarURLConnection) resource.openConnection();
        JarFile jarFile = uc.getJarFile();

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry == null) {
                break;
            }
            if (jarEntry.getName().startsWith(packageName) && jarEntry.getName().endsWith(".class")) {
                String classname = jarEntry.getName().replace('/', '.').substring(0, jarEntry.getName().length() - ".class".length());

                classes.add(classLoader.loadClass(classname));
            }
        }
        jarFile.close();
        return classes;
    }

    /**
     * Given the class loader, the package name and the database name find all
     * table classes for this database.
     * 
     * @param dottedPackage
     *            - package name where database classes can be scanned for.
     * @param annotationClass
     *            - the name of the annotation in which to scan for classes
     *            containing annotations.
     * @return the list of all matching classes in the package.
     * @throws Exception
     */
    private final static List<Class> getClassesWithAnnotation(String dottedPackage, Class annotationClass) throws Exception {

        List<Class> items = new ArrayList<Class>();
        List<Class> classes = getClasses(dottedPackage);
        for (Class clazz : classes) {
            if (clazz.isAnnotationPresent(annotationClass)) {
                items.add(clazz);
            }
        }
        return items;
    }
}