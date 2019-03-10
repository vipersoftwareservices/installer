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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.viper.installer.actions.ActionManager;
import com.viper.installer.actions.BasicActions;
import com.viper.installer.model.Body;
import com.viper.installer.model.Component;
import com.viper.installer.model.Footer;
import com.viper.installer.model.Page;
import com.viper.installer.model.Row;
import com.viper.installer.model.Table;
import com.viper.installer.util.Logs;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.converter.BooleanStringConverter;

public class WizardPage extends BorderPane {

    private Page page = null;
    private Stage stage = null;

    public WizardPage(Stage stage, Page page, Session session) throws Exception {
        super();

        this.stage = stage;
        this.page = page;

        Body body = page.getBody();

        setTop(createTitlePane(page));

        VBox pane = new VBox();
        if (page.isScrollable()) {
            ScrollPane scrollPane = newVerticalScrollPane(pane);
            setCenter(scrollPane);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
        } else {
            setCenter(pane);
            VBox.setVgrow(pane, Priority.ALWAYS);
        }

        pane.setSpacing(15.0);
        pane.setAlignment(Pos.TOP_LEFT);

        int counter = -1;
        for (Object item : body.getChoiceOrCheckboxOrCanvas()) {
            counter = counter + 1;

            String name = "name" + counter;
            String str = null;
            boolean isReadOnly = false;
            boolean isScrollable = false;

            // Get the text with the object if any
            if (item instanceof Component) {
                Component component = (Component) item;
                if (component.getFilename() != null) {
                    try {
                        str = BasicActions.getInstallationItem(component.getFilename());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    str = component.getValue();
                }
                str = trimAll(str);
                isReadOnly = component.isReadOnly();
                isScrollable = component.isScrollable();
                name = component.getName();
            }

            // Logs.info("WizardPage: " + name + "," +
            // item.toString());

            StringProperty property = getStringProperty(name, session);

            if (item instanceof com.viper.installer.model.Log) {
                com.viper.installer.model.Log log = (com.viper.installer.model.Log) item;

                TextArea text = new TextArea();
                if (log.getNumColumns() != 0) {
                    text.setPrefColumnCount(log.getNumColumns());
                }
                if (log.getNumRows() != 0) {
                    text.setPrefRowCount(log.getNumRows());
                }

                pane.getChildren().add(text);
                VBox.setVgrow(text, Priority.ALWAYS);

                session.put(name + ".component", text);

            } else if (item instanceof com.viper.installer.model.Text) {
                com.viper.installer.model.Text text = (com.viper.installer.model.Text) item;

                if (text.getFilename() != null) {
                    String htmlstr = BasicActions.getInstallationItem(text.getFilename());
                    
                    WebView htmlPane = new WebView();
                    htmlPane.getEngine().loadContent(htmlstr);

                    pane.getChildren().add(newVerticalScrollPane(htmlPane)); 

                    session.put(name + ".component", htmlPane);
                    
                } else if (str != null && str.indexOf("<html") != -1) {
                    WebView htmlPane = new WebView();
                    htmlPane.getEngine().loadContent(str);

                    pane.getChildren().add(newVerticalScrollPane(htmlPane)); 

                    session.put(name + ".component", htmlPane);

                } else if (str != null) {
                    Label textPane = newLabel(str, "tooltip");
                    textPane.getStyleClass().add("text-area-readonly");
                    textPane.setWrapText(true);

                    if (!isScrollable) {
                        pane.getChildren().add(textPane);
                    } else {
                        Node child = newVerticalScrollPane(textPane);
                        pane.getChildren().add(child);
                        VBox.setVgrow(child, Priority.ALWAYS);
                    }
                }

            } else if (item instanceof com.viper.installer.model.Label) {
                Label lbl = newLabel(str, "");
                pane.getChildren().add(lbl);

            } else if (item instanceof com.viper.installer.model.Image) {
                com.viper.installer.model.Image img = (com.viper.installer.model.Image) item; 
                ImageView image = newImageView(getClass().getResourceAsStream(img.getSrc()));

                Node child = (!isScrollable) ? image : newScrollPane(image);
                pane.getChildren().add(child);
                VBox.setVgrow(child, Priority.ALWAYS);

            } else if (item instanceof com.viper.installer.model.Filename) {
                com.viper.installer.model.Filename filename = (com.viper.installer.model.Filename) item;

                TextField tf = newTextField(property, "<Enter>", 0);
                tf.setEditable(!isReadOnly);

                HBox rowPane = new HBox();
                rowPane.setAlignment(Pos.CENTER_LEFT);
                rowPane.setSpacing(10.0);
                rowPane.getChildren().add(newLabel(str, ""));
                rowPane.getChildren().add(tf);
                rowPane.getChildren().add(newButton("Browse...", new BrowseFilename(tf)));
                pane.getChildren().add(rowPane);

                HBox.setHgrow(tf, Priority.ALWAYS);

            } else if (item instanceof com.viper.installer.model.ProgressBar) {

                com.viper.installer.model.ProgressBar bar = (com.viper.installer.model.ProgressBar) item;
                ProgressBar progressBar = new ProgressBar();
                progressBar.setId(name);
                progressBar.setMaxSize(0, 100);
                progressBar.setMaxWidth(Double.MAX_VALUE);
                if ("indeterminate".equals(bar.getMode())) {
                    progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                }

                double height = progressBar.getPrefHeight();
                if (bar.getHeight() != null) {
                    progressBar.setPrefHeight(height);
                }
                pane.getChildren().add(progressBar);

                session.put(name + ".component", progressBar);

            } else if (item instanceof com.viper.installer.model.Prompt) {
                com.viper.installer.model.Prompt prompt = (com.viper.installer.model.Prompt) item;

                Label label = newLabel(str, "");
                TextField tf = newTextField(property, name, 0);
                tf.setEditable(!isReadOnly);

                FlowPane rowPane = new FlowPane(10.0, 10.0);
                rowPane.getChildren().add(label);
                rowPane.getChildren().add(tf);

                pane.getChildren().add(rowPane);

            } else if (item instanceof com.viper.installer.model.Password) {

                Label label = newLabel(str, "");
                PasswordField tf = newPasswordField(property, name, 0);
                tf.setEditable(!isReadOnly);

                FlowPane rowPane = new FlowPane(10.0, 10.0);
                rowPane.getChildren().add(label);
                rowPane.getChildren().add(tf);

                pane.getChildren().add(rowPane);

            } else if (item instanceof com.viper.installer.model.Choice) {
                com.viper.installer.model.Choice choice = (com.viper.installer.model.Choice) item;

                List<Object> choices = toChoices(choice.getList(), session);
                final ComboBox cb = new ComboBox();
                if (choices != null) {
                    cb.getItems().addAll(choices);
                }
                cb.valueProperty().bindBidirectional(property);
                cb.setEditable(!isReadOnly);

                FlowPane rowPane = new FlowPane();
                rowPane.getChildren().add(newLabel(str, ""));
                rowPane.getChildren().add(cb);

                pane.getChildren().add(rowPane);

            } else if (item instanceof com.viper.installer.model.CheckBox) {

                CheckBox box = new CheckBox(str);
                box.setDisable(isReadOnly);
                box.setSelected(Boolean.parseBoolean(property.getValue()));
                Bindings.bindBidirectional(property, box.selectedProperty(), new BooleanStringConverter());
                pane.getChildren().add(box);

                session.put(name + ".component", box);

            } else if (item instanceof com.viper.installer.model.Canvas) {

                com.viper.installer.model.Canvas canvasBean = (com.viper.installer.model.Canvas) item;
                InstallCanvas canvas = new InstallCanvas(canvasBean.getAction(), session);
                canvas.setWidth(canvasBean.getWidth());
                canvas.setHeight(canvasBean.getHeight());
                pane.getChildren().add(canvas);

            } else if (item instanceof com.viper.installer.model.Separator) {

                pane.getChildren().add(new Separator());

            } else if (item instanceof com.viper.installer.model.Table) {

                com.viper.installer.model.Table tableBean = (com.viper.installer.model.Table) item;
                List<String> columnNames = toColumnNames(tableBean);
                TableView uiTable = newTableView("", columnNames, toTableData(columnNames, tableBean));
                Node child = newVerticalScrollPane(uiTable);
                pane.getChildren().add(child);
                VBox.setVgrow(child, Priority.ALWAYS);

                ActionManager.executeActions(tableBean.getAction(), session);
            }
        }

        setBottom(createFooterPane(page));
    }

    public Page getPage() {
        return page;
    }

    public Pane createTitlePane(Page page) {

        Label titleLabel = new Label(page.getHeader().getValue());

        Reflection r = new Reflection();
        r.setFraction(0.7f);
        // titleLabel.setEffect(r);

        FlowPane titlePane = new FlowPane(Orientation.HORIZONTAL);
        titlePane.getStyleClass().add("wizard-header-pane");
        titlePane.getChildren().add(titleLabel);

        // VBox.setVgrow(titlePane, Priority.NEVER);

        return titlePane;
    }

    public Pane createFooterPane(Page page) {

        Reflection r = new Reflection();
        r.setFraction(0.7f);
        // titleLabel.setEffect(r);

        FlowPane titlePane = new FlowPane(Orientation.HORIZONTAL);
        titlePane.getStyleClass().add("wizard-footer-pane");

        for (Footer footer : page.getFooter()) {
            Label titleLabel = new Label(footer.getValue());
            titleLabel.setWrapText(true);
            titlePane.getChildren().add(titleLabel);
        }

        // VBox.setVgrow(titlePane, Priority.NEVER);

        return titlePane;
    }

    class BrowseFilename implements EventHandler<ActionEvent> {
        private TextField tf;

        public BrowseFilename(TextField tf) {
            this.tf = tf;
        }

        @Override
        public void handle(ActionEvent e) {

            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select Installation Directory");
            File selectedFile = fileChooser.showDialog(stage);
            if (selectedFile != null) {
                tf.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    private List<Object> toChoices(String expression, Session session) throws Exception {
        return (List<Object>) ActionManager.eval(expression, session.getParameters());
    }

    private List<String> toColumnNames(Table tableBean) {

        List<String> names = new ArrayList<String>();
        for (String columnName : tableBean.getTr().get(0).getTd()) {
            names.add(columnName);
        }
        return names;
    }

    private StringProperty getStringProperty(String name, Session session) {
        if (session.get(name) == null) {
            session.put(name, new SimpleStringProperty(null));
        }
        return (StringProperty) session.get(name);
    }

    private ObservableList<Map<String, String>> toTableData(List<String> names, Table tableBean) {

        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();

        for (Row row : tableBean.getTr()) {
            Map<String, String> item = new HashMap<String, String>();
            int columnNo = 0;
            for (String value : row.getTd()) {
                String name = names.get(columnNo++);
                item.put(name, value);
            }
            items.add(item);
        }
        Logs.info("toTableData: no items: " + items.size());
        return items;
    }

    public static ScrollPane newVerticalScrollPane(Node content) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setContent(content);
        return scrollPane;
    }

    public static Label newLabel(String title, String tooltip) {
        Label item = new Label();
        item.setText(title);
        item.setWrapText(true);
        item.setAlignment(Pos.BASELINE_LEFT);
        item.setTextOverrun(OverrunStyle.ELLIPSIS);
        item.setTooltip(new Tooltip(tooltip));
        return item;
    }

    public static TextField newTextField(Property<String> property, String promptText, int preferredColumnCount) {
        TextField item = new TextField();
        if (promptText != null) {
            item.setPromptText(promptText);
        }
        if (preferredColumnCount != 0) {
            item.setPrefColumnCount(preferredColumnCount);
        }
        Bindings.bindBidirectional(item.textProperty(), property);
        return item;
    }

    public static PasswordField newPasswordField(Property<String> property, String promptText, int preferredColumnCount) {
        PasswordField item = new PasswordField();
        if (promptText != null) {
            item.setPromptText(promptText);
        }
        if (preferredColumnCount != 0) {
            item.setPrefColumnCount(preferredColumnCount);
        }
        Bindings.bindBidirectional(item.textProperty(), property);
        return item;
    }

    public static ImageView newImageView(InputStream imageStream) {
        Image image = new Image(imageStream);

        ImageView view = new ImageView(image);
        view.setFitHeight(image.getHeight());
        view.setFitWidth(image.getWidth());
        view.setPreserveRatio(false);
        view.setSmooth(true);
        view.setCache(true);
        return view;
    }

    public static ScrollPane newScrollPane(Node content) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(content);
        return scrollPane;
    }

    public static TableView newTableView(String id, List<String> columns, ObservableList<Map<String, String>> beans) {
        TableView table = new TableView();
        table.setId(id);
        for (String column : columns) {
            TableColumn tableColumn = new TableColumn(column);
            tableColumn.setCellValueFactory(new MapValueFactory(column));
            // tableColumn.setMinWidth(130);
            table.getColumns().add(tableColumn);
        }
        table.getItems().addAll(beans);
        table.autosize();
        return table;
    }

    public static Button newButton(String title, EventHandler<ActionEvent> action) {
        Button item = new Button(title);
        item.setOnAction(action);
        return item;
    }

    private String trimAll(String str) {
        return str.trim().replaceAll("\\s+", " ");
    }
}
