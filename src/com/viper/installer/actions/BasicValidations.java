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

import java.io.File;
import java.net.URL;

import com.viper.installer.annotation.ActionTag;

@ActionTag
public class BasicValidations {

    private Utils utils = new Utils();

    public boolean isTrue(String value) throws Exception {
        return ("true".equalsIgnoreCase(value));
    }

    public boolean isEqual(String value1, String value2) throws Exception {
        return ((value1 == null && value2 == null) || (value1 != null && value1.equalsIgnoreCase(value2)));
    }

    public boolean validateInput(String str) throws Exception {
        return (str != null && !str.trim().isEmpty());
    }

    public boolean validateUser(String username) throws Exception {
        return utils.listUsers().contains(username);
    }

    public boolean validateGroup(String groupname) throws Exception {
        return utils.listGroups().contains(groupname);
    }

    public void validateUserGroup(String user, String group) throws Exception {

        boolean foundUser = utils.listUsers().contains(user);
        boolean foundGroup = utils.listGroups().contains(group);

        if (!foundUser) {
            throw new Exception("No such user (" + user + ") is known on this system, installation aborted.");
        }

        if (!foundGroup) {
            throw new Exception("No such group (" + group + ") is known on this system, installation aborted.");
        }
    }

    public boolean validateFilename(String filename) throws Exception {
        return new File(filename).exists();
    }

    public boolean validateUrl(String urlStr) {
        try {
            new URL(urlStr);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
