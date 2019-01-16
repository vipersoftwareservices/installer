/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)SelfSignedCertificate.java	1.00 2012/06/15
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

import com.viper.installer.annotation.ActionTag;

@ActionTag
public class SelfSignedCertificate {

    public boolean createCertificate(String keystoreSelfSignedSslFilename, String keystoreSelfSignedSslAlias, String keystoreSelfSignedSslPassword,
            String keystoreSelfSignedSslCertificate, String CN, String OU, String O, String L, String ST, String C) {

        try {

            Utils.deleteFile(keystoreSelfSignedSslFilename);

            StringBuilder cmd1 = new StringBuilder();
            cmd1.append("keytool");
            cmd1.append("-genkey");
            cmd1.append("-noprompt");
            cmd1.append("-dname 'CN=Tom Nevin, OU=Internet Of Things, O=Symantec Corporation, L=Mountain View, ST=CA, C=US'");
            cmd1.append("-alias " + keystoreSelfSignedSslAlias);
            cmd1.append("-keyalg RSA");
            cmd1.append("-keysize 2048");
            cmd1.append("-keystore " + keystoreSelfSignedSslFilename);
            cmd1.append("-storepass " + keystoreSelfSignedSslPassword);
            cmd1.append("-validity 1000");

            Utils.exec(cmd1.toString(), null);

            StringBuilder cmd2 = new StringBuilder();
            cmd2.append("keytool");
            cmd2.append("-export");
            cmd2.append("-keystore " + keystoreSelfSignedSslFilename);
            cmd2.append("-alias " + keystoreSelfSignedSslAlias);
            cmd2.append("-storepass " + keystoreSelfSignedSslPassword);
            cmd2.append("-file " + keystoreSelfSignedSslCertificate);

            Utils.exec(cmd2.toString(), null);

            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
