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

import java.awt.Graphics;
import java.util.List;

import javafx.scene.canvas.Canvas;

import com.viper.installer.actions.ActionManager;
import com.viper.installer.model.Action;

public class InstallCanvas extends Canvas {
    List<Action> actions = null;
    Session session = null;

    public InstallCanvas(List<Action> actions, Session session) {
        super();

        this.actions = actions;
        this.session = session;
    }

    public void paintComponent(Graphics g) {
        if (actions != null) {
            // params.add(new Parameter("g", g));
            ActionManager.executeActions(actions, session);
        }
    }
}
