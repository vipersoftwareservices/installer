/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)ListOfGroups.java	1.00 2008/06/15
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

import java.util.ArrayList;
import java.util.List;

import com.viper.installer.annotation.ActionTag;

@ActionTag
public class BasicLists {

    /**
     * 
     * @return
     */
    public List<Object> listOfUsers() {
        try {
            return (List) new Utils().listUsers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<Object>();
    }
    
    /**
     * 
     * @return
     */
    public List<Object> listOfGroups() {
        try {
            return (List) new Utils().listGroups();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<Object>();
    }
}