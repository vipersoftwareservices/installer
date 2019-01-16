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

import java.util.List;
import java.util.Optional;

import com.viper.installer.actions.ActionManager;
import com.viper.installer.model.Action;
import com.viper.installer.model.Button;
import com.viper.installer.model.Validation;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class BranchFlow {

    Session session = null;
    List<WizardPage> wizardPages = null;

    public BranchFlow(Session session, List<WizardPage> panes) {
        wizardPages = panes;
        this.session = session;
    }

    public void execute(Button button) {

        if (button.getAction() != null && button.getAction().size() > 0) {
            ActionManager.executeActions(button.getAction(), session);
        }
    }

    public WizardPage advance(WizardPage currentPage, Button button) {

        if (button.getAction() != null && button.getAction().size() > 0) {
            ActionManager.executeActions(button.getAction(), session);
        }
        return next(currentPage);
    }

    public boolean canAdvance(WizardPage currentPage) {
        WizardPage page = currentPage;
        if (!validPage(page, true)) {
            return false;
        }
        return true;
    }

    public WizardPage next(WizardPage currentPage) {
        int index = wizardPages.indexOf(currentPage);
        if (index == -1) {
            return currentPage;
        }
        if ((index + 1) >= wizardPages.size()) {
            return currentPage;
        }
        return wizardPages.get(index + 1);
    }

    public WizardPage previous(WizardPage currentPage) {
        int index = wizardPages.indexOf(currentPage);
        if (index == -1) {
            return currentPage;
        }
        if ((index - 1) < 0) {
            return currentPage;
        }
        return wizardPages.get(index - 1);
    }

    public WizardPage cancel(WizardPage currentPage) {
        if (showConfirmation("Confirm Cancel", "Are you sure you want to cancel this installation?")) {
            // TODO Application destroy
            Platform.exit();
            System.exit(1);
            return null;
        }
        return currentPage;
    }

    private boolean validPage(WizardPage wizardPage, boolean showMessages) {
        if (wizardPage == null || wizardPage.getPage() == null) {
            // Logs.in("validPage: validating0: " + wizardPage);
            return false;
        }
        for (Validation validate : wizardPage.getPage().getValidation()) {

            String value = (String) session.getProperty(validate.getName()).getValue();
            if ((value == null || value.trim().length() == 0) && validate.isRequired()) {
                if (showMessages) {
                    showError(validate.getMsg() + ":" + value + ":");
                }
                return false;
            }

            for (Action action : validate.getAction()) {
                Boolean result = (Boolean) ActionManager.executeAction(action, session);
                if (result == null || !result) {
                    if (showMessages) {
                        showError(action.getMsg());
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public static void showError(String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Installation Alert");
        alert.setContentText(msg);
        alert.setHeaderText("Installation Error");
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String question) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(question);
        alert.setResizable(true);
        final Optional<ButtonType> result = alert.showAndWait();

        return result.get() == ButtonType.OK;
    }
}
