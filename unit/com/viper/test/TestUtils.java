/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)filename.java    1.00 2012/06/15
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

package com.viper.test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.viper.benchmarks.BenchmarkRule;
import com.viper.installer.actions.ActionManager;
import com.viper.installer.actions.Utils;

public class TestUtils extends AbstractTestCase {

    @Rule
    public TestRule benchmarkRule = new BenchmarkRule();

    @Test
    public void testWMICExists() throws Exception {
        assertTrue("c:/ does not exists", new File("c:/").exists());
        assertTrue("c:/windows does not exists", new File("c:/windows").exists());
        assertTrue("c:/windows/system32 does not exists", new File("c:/windows/system32").exists());
        assertTrue("c:/windows/system32/wbem does not exists", new File("c:/windows/system32/wbem").exists());
        assertTrue("c:/windows/system32/wbem/wmic.exe does not exists", new File("c:/windows/system32/wbem/wmic.exe").exists());
    }

    @Test
    public void testListOfGroups() throws Exception {

        List<String> groups = new Utils().listGroups();

        assertNotNull(getCallerMethodName() + ", list of groups is null", groups);
        assertTrue(getCallerMethodName() + ", list of groups does not contain Tom", groups.contains("Tom"));
    }

    @Test
    public void testListOfUsers() throws Exception {

        List<String> users = new Utils().listUsers();

        assertNotNull(getCallerMethodName() + ", list of users is null", users);
        assertTrue(getCallerMethodName() + ", list of users does not contain Tom", users.contains("Tom"));
    }
    
    @Test
    public void testEval() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        String expression = "'C:/Program Files/Symantec/Industrial Anomaly Detection'";
        
        Object value = ActionManager.eval(expression, params);
    }
}
