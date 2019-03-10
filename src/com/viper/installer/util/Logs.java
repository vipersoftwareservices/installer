/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)filename.java    1.00 2008/06/15
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

import java.io.File;
import java.io.PrintStream;

public class Logs {

    public static final void redirect(String filename) throws Exception {
        
        if (filename == null || filename.isEmpty()) {
            return;
        }

        PrintStream ps = new PrintStream(new File(filename));

        System.setOut(ps);
        System.setErr(ps);
    }

    public static final void error(Object message) {
        System.err.println("ERROR-" + getCallerMethodName() + ":" + message);
    }

    public static final void error(Object message, Throwable t) {
        System.err.println("ERROR-" + getCallerMethodName() + ":" + message);
        t.printStackTrace(System.err);

    }

    public static final void warn(Object message) {
        System.err.println("WARN-" + getCallerMethodName() + ":" + message);

    }

    public static final void warn(Object message, Throwable t) {
        System.err.println("WARN-" + getCallerMethodName() + ":" + message);
        t.printStackTrace(System.err);

    }

    public static final void info(Object message) {
        System.err.println("INFO-" + getCallerMethodName() + ":" + message);

    }

    public static final void info(Object message, Throwable t) {
        System.err.println("INFO-" + getCallerMethodName() + ":" + message);
        t.printStackTrace(System.err);

    }

    public static final void debug(Object message) {
        System.err.println("DEBUG-" + getCallerMethodName() + ":" + message);
    }

    public static final void debug(Object message, Throwable t) {
        System.err.println("DEBUG-" + getCallerMethodName() + ":" + message);
        t.printStackTrace(System.err);
    }

    public static final void trace(Object message) {
        System.err.println("TRACE-" + getCallerMethodName() + ":" + message);
    }

    public static final void trace(Object message, Throwable t) {
        System.err.println("TRACE-" + getCallerMethodName() + ":" + message);
        t.printStackTrace(System.err);

    }

    public static final void msg(Object message) {
        System.err.println(message);
    }

    public static final void msg(Object message, Throwable t) {
        System.err.println(message);
        t.printStackTrace(System.err);

    }

    public static final String getCallerMethodName() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

}
