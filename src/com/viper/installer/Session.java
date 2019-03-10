/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)filename.java    1.00 2008/06/15
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
 * @version 1.0, 06/15/2008 
 *
 * @note 
 *        
 * -----------------------------------------------------------------------------
 */

package com.viper.installer;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.Property;

public class Session extends HashMap<String, Object> {

    static final Session instance = new Session();

    private Session() {

    }

    public static Session getInstance() {
        return instance;
    }

    public Property getProperty(String name) {
        if (get(name) instanceof Property) {
            return (Property) get(name);
        }
        return null;
    }

    public Map<String, Object> getParameters() {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String key : keySet()) {
            if (get(key) instanceof Property) {
                map.put(key, ((Property) get(key)).getValue());
            } else {
                map.put(key, get(key));
            }
        }
        return map;
    }

    public String replaceTokens(String str) {
        for (String key : this.keySet()) {
            if (key != null) {
                String token = buildToken(key);
                str = str.replace(token, this.get(key).toString());
            }
        }
        return str;
    }

    public String buildToken(String key) {
        StringBuilder buf = new StringBuilder();
        buf.append("#");
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (Character.isUpperCase(c)) {
                buf.append(".");
            }
            buf.append(Character.toLowerCase(c));
        }
        buf.append("#");
        return buf.toString();
    }

}
