/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)filename.java    1.00 2003/06/15
 *
 * Copyright 1998-2003 by Viper Software Services
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
 * @version 1.0, 06/15/2003 
 *
 * @note 
 *        
 * -----------------------------------------------------------------------------
 */

package com.viper.installer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.viper.installer.actions.ActionManager;
import com.viper.installer.model.Action;
import com.viper.installer.model.Actions;
import com.viper.installer.model.Page;
import com.viper.installer.util.Logs;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class Wizard extends BorderPane {

    public final static String ACTION_NEXT = "next";
    public final static String ACTION_BACK = "previous";
    public final static String ACTION_CANCEL = "cancel";

    private ButtonBar bar = new ButtonBar();
    private StackPane stackPane = new StackPane();
    private Session session = null;

    public Wizard(Session session, BranchFlow flow) {
        super();

        this.session = session;
        this.getStyleClass().add("wizard-pane");

        stackPane.getStyleClass().add("wizard-center-pane");

        BorderPane banner  = new BorderPane();
        banner.getStyleClass().add("wizard-left-pane");

        bar.getStyleClass().add("wizard-button-pane");
        bar.getButtons().add(createButton(ACTION_BACK));
        bar.getButtons().add(createButton(ACTION_NEXT));
        bar.getButtons().add(createButton(ACTION_CANCEL));

        setCenter(stackPane);
        setBottom(bar);
        setLeft(banner);

        final Button previousButton = (Button) bar.getButtons().get(0);
        previousButton.addEventFilter(ActionEvent.ACTION, event -> {
            WizardPage page = flow.previous(getCurrentPane());
            show(page, false);
            event.consume();
        });

        final Button nextButton = (Button) bar.getButtons().get(1);
        nextButton.addEventFilter(ActionEvent.ACTION, event -> {
            WizardPage pane = getCurrentPane();
            if (flow.canAdvance(pane)) {
                com.viper.installer.model.Button button = findButton(pane.getPage(), ACTION_NEXT);
                WizardPage next = flow.advance(pane, button);
                if (next != null) {
                    show(next, true);
                    event.consume();
                }
            }
        });

        final Button cancelButton = (Button) bar.getButtons().get(2);
        cancelButton.addEventFilter(ActionEvent.ACTION, event -> {
            flow.cancel(getCurrentPane());
        });
    }

    public void runConcurrentAction(Session session, WizardPage next) {

        if (next.getPage() == null || next.getPage().getActions() == null) {
            return;
        }

        final Actions actions = next.getPage().getActions();

        Task task = new Task<Void>() {
            @Override
            public Void call() {

                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        Button button = findButton(bar, ACTION_NEXT);
                        if (button != null) {
                            button.setDisable(true);
                        }
                    }
                });
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
               
                setConsole(next.getPage().getActions());
                boolean success = true;
                for (int i = 0; i < actions.getAction().size(); i++) {
                    Action action = actions.getAction().get(i);
                    Logs.info("ACTION: " + action.getValue());
                    if (ActionManager.executeAction(action, session) == null) {
                        success = false;
                    }
                    updateProgress(i + 1, actions.getAction().size());
                }

                if (success) {
                    unsetConsole("Completed.", next.getPage().getActions());
                } else {
                    unsetConsole("Failed.", next.getPage().getActions());
                }

                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        Button button = findButton(bar, ACTION_NEXT);
                        if (button != null) {
                            button.setDisable(false);
                        }
                    }
                });
                return null;
            }
        };

        Object obj = session.get(actions.getProgress() + ".component");
        if (obj != null && obj instanceof ProgressBar) {
            ProgressBar progressBar = (ProgressBar) obj;
            progressBar.progressProperty().bind(task.progressProperty());
        }

        session.put("TASK", task);
        new Thread(task).start();
    }

    public WizardPage getCurrentPane() {
        for (int i = 0; i < stackPane.getChildren().size(); i++) {
            WizardPage pane = (WizardPage) stackPane.getChildren().get(i);
            if (pane.isVisible()) {
                return pane;
            }
        }
        return null;
    }

    public void addCard(WizardPage page) {
        stackPane.getChildren().add(page);
    }

    public void show(WizardPage pane, boolean isForward) {
        for (Node node : stackPane.getChildren()) {
            node.setVisible(false);
        }

        Page page = pane.getPage();

        updateButton(ACTION_BACK, bar, page);
        updateButton(ACTION_CANCEL, bar, page);
        updateButton(ACTION_NEXT, bar, page);

        pane.setVisible(true);

        if (isForward) {
            runConcurrentAction(session, pane);
        }

    }

    public Button createButton(String name) {
        Button button = new Button(name);
        button.setId(name);
        return button;
    }

    public void updateButton(String name, ButtonBar bar, Page page) {
        Button button = findButton(bar, name);
        if (button == null) {
            return;
        }
        com.viper.installer.model.Button item = findButton(page, name);
        if (item == null) {
            button.setVisible(false);
            return;
        }
        if (item.getLabel() != null) {
            button.setText(item.getLabel());
        } else {
            String newlabel = button.getId().toUpperCase();
            if (button.getId().length() > 1) {
                newlabel = Character.toUpperCase(button.getId().charAt(0)) + button.getId().substring(1);
            }
            button.setText(newlabel);
        }
        button.setVisible(true);
        button.setUserData(item);
    }

    public Button findButton(ButtonBar bar, String name) {
        for (Node button : bar.getButtons()) {
            if (button.getId().equals(name)) {
                return (Button) button;
            }
        }
        return null;
    }

    public com.viper.installer.model.Button findButton(Page page, String name) {
        for (com.viper.installer.model.Button button : page.getButton()) {
            if (button.getName().equals(name)) {
                return button;
            }
        }
        return null;
    }

    private void setConsole(Actions actions) {
        if (actions.getOut() != null) {
            session.put("SAVE_OUT", System.out);
            session.put("SAVE_ERR", System.err);

            Object obj = session.get(actions.getOut() + ".component");
            if (obj instanceof TextArea) {
                PrintStream ps = new PrintStream(new Console(session, (TextArea) obj), true);

                System.setOut(ps);
                System.setErr(ps);
            }

            if (obj instanceof WebView) {
                PrintStream ps = new PrintStream(new Console(session, (WebView) obj), true);

                System.setOut(ps);
                System.setErr(ps);
            }
        }
    }

    private void unsetConsole(String msg, Actions actions) {
        if (actions.getOut() != null) {
            Logs.msg(msg);
            if (session.get("SAVE_OUT") != null) {
                System.setOut((PrintStream) session.get("SAVE_OUT"));
                System.setErr((PrintStream) session.get("SAVE_ERR"));
            }
        }
    }

    public static class Console extends OutputStream {

        private TextArea textpane = null;
        private WebView webview = null;
        private Session session = null;

        public Console(Session session, TextArea ta) {
            this.textpane = ta;
            this.session = session;
        }

        public Console(Session session, WebView webview) {
            this.webview = webview;
            this.session = session;
        }

        public void appendText(String valueOf) {
            if (textpane != null) {
                Platform.runLater(() -> textpane.appendText(valueOf));
            }
            if (webview != null) {
                Platform.runLater(() -> appendWebView(valueOf));
            }

            if (session.get("SAVE_OUT") != null) {
                PrintStream out1 = (PrintStream) session.get("SAVE_OUT");
                out1.print(valueOf);
            }
        }

        @Override
        public void write(int i) throws IOException {
            appendText(String.valueOf((char) i));
        }

        private void appendWebView(String valueOf) {
            if (webview == null || webview.getEngine() == null || webview.getEngine().getDocument() == null) {
                return;
            }
            Document document = webview.getEngine().getDocument();
            Element element = document.getElementById("insert-text");
            if (element == null) {
                ;
            } else if ("\n".equals(valueOf)) {
                element.appendChild(document.createElement("br"));
            } else {
                element.appendChild(document.createTextNode(valueOf));
            }
        }
    }
}