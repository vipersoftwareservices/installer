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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.Timer;

import com.viper.installer.annotation.ActionTag;
import com.viper.installer.util.Logs;

import javafx.scene.control.TableView;
import javafx.scene.paint.Color;

@ActionTag
public class SystemStatusAction implements ActionListener {

    public static int SERVER_NOT_INITIALIZED = -2;
    public static int SERVER_NOT_PINGED = 0;
    public static int SERVER_PING_ERROR = -1;
    public static int SERVER_RUNNING = 200;

    private static final int interval = 5000;
    private Timer timer;
    private TableView table = null;

    public void update() throws Exception {

        if (timer == null) {
            timer = new Timer(interval, this);
            timer.setInitialDelay(interval);
            timer.start();
        }
    }

    public TableView getTable() {
        return table;
    }

    public void setTable(TableView table) {
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (table != null && table.getItems() != null) {
            for (Object bean : table.getItems()) {
                String url = null; // getValue(bean, "url");
                int status = ping(url);
                // setValue(table, "status", Integer.toString(status));
            }
        }
    }

    Color getStatusColor(int status) {
        if (status == SERVER_NOT_PINGED) {
            return Color.WHITE;
        } else if (status == SERVER_PING_ERROR) {
            return Color.YELLOW;
        } else if (status == SERVER_RUNNING) {
            return Color.GREEN;
        }
        return Color.RED;
    }

    public int ping(String url) {
        int code = SERVER_PING_ERROR;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            code = connection.getResponseCode();
        } catch (Exception ex) {
            Logs.error("ping: " + url, ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return code;
    }
}
