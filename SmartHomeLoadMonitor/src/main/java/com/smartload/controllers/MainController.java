package com.smartload.controllers;

import com.smartload.models.*;
import com.smartload.services.MonitoringService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Main controller for the application UI
 */
public class MainController {
    
    private final MonitoringService monitoringService;
    private TableView<Appliance> applianceTable;
    private ListView<com.smartload.models.Alert> alertsList;  // Fixed: Full class name
    private HBox totalCurrentLabel;  // Fixed: Changed from Label to HBox
    private HBox totalPowerLabel;    // Fixed: Changed from Label to HBox
    private HBox energyLabel;        // Fixed: Changed from Label to HBox
    private HBox costLabel;          // Fixed: Changed from Label to HBox
    private Label statusLabel;
    private VBox socketGroupsBox;
    
    public MainController() {
        this.monitoringService = MonitoringService.getInstance();
    }
    
    public Parent createView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        root.setTop(createToolbar());
        root.setLeft(createAppliancePanel());
        root.setRight(createSummaryPanel());
        root.setBottom(createAlertsPanel());
        
        root.setStyle("-fx-background-color: #f4f4f4;");
        
        return root;
    }
    
    /**
     * Create top toolbar with controls
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 5;");
        
        Label title = new Label("⚡ Smart Home Load Monitor");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button startBtn = new Button("▶ Start");
        startBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setOnAction(e -> {
            monitoringService.start();
            startBtn.setDisable(true);
        });
        
        Button stopBtn = new Button("⏸ Stop");
        stopBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        stopBtn.setOnAction(e -> {
            monitoringService.stop();
            startBtn.setDisable(false);
        });
        
        Button settingsBtn = new Button("⚙ Settings");
        settingsBtn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        settingsBtn.setOnAction(e -> showSettingsDialog());
        
        toolbar.getChildren().addAll(title, spacer, startBtn, stopBtn, settingsBtn);
        return toolbar;
    }
    
    /**
     * Create appliance table panel
     */
    private VBox createAppliancePanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(600);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        Label heading = new Label("📊 Appliances");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        applianceTable = new TableView<>();
        applianceTable.setItems(monitoringService.getAppliances());
        
        TableColumn<Appliance, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        TableColumn<Appliance, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(120);
        
        TableColumn<Appliance, String> groupCol = new TableColumn<>("Socket Group");
        groupCol.setCellValueFactory(new PropertyValueFactory<>("socketGroup"));
        groupCol.setPrefWidth(120);
        
        TableColumn<Appliance, Double> currentCol = new TableColumn<>("Current (A)");
        currentCol.setCellValueFactory(new PropertyValueFactory<>("currentDraw"));
        currentCol.setCellFactory(col -> new TableCell<Appliance, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f A", item));
                    if (item > 8.0) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (item > 5.0) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60;");
                    }
                }
            }
        });
        currentCol.setPrefWidth(100);
        
        TableColumn<Appliance, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setPrefWidth(100);
        
        applianceTable.getColumns().addAll(nameCol, locationCol, groupCol, currentCol, priorityCol);
        
        VBox.setVgrow(applianceTable, Priority.ALWAYS);
        panel.getChildren().addAll(heading, applianceTable);
        
        return panel;
    }
    
    /**
     * Create summary panel with stats
     */
    private VBox createSummaryPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(400);
        
        VBox summaryCard = createSummaryCard();
        VBox socketGroupsCard = createSocketGroupsCard();
        
        panel.getChildren().addAll(summaryCard, socketGroupsCard);
        return panel;
    }
    
    /**
     * Create summary statistics card
     */
    private VBox createSummaryCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        
        Label heading = new Label("📈 System Summary");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        totalCurrentLabel = createStatLabel("Total Current:", "0.0 A");
        totalPowerLabel = createStatLabel("Power:", "0.0 W");
        energyLabel = createStatLabel("Session Energy:", "0.00 kWh");
        costLabel = createStatLabel("Estimated Cost:", "GHS 0.00");
        statusLabel = new Label("Status: OK");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.web("#27ae60"));
        
        monitoringService.getEnergyTracker().sessionEnergyKwhProperty().addListener((obs, old, newVal) -> {
            updateSummaryLabels();
        });
        
        card.getChildren().addAll(heading, new Separator(), totalCurrentLabel, 
            totalPowerLabel, energyLabel, costLabel, new Separator(), statusLabel);
        
        return card;
    }
    
    /**
     * Create socket groups status card
     */
    private VBox createSocketGroupsCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        
        Label heading = new Label("🔌 Socket Groups");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        socketGroupsBox = new VBox(8);
        
        for (SocketGroup group : monitoringService.getSocketGroups()) {
            HBox groupDisplay = createSocketGroupDisplay(group);
            socketGroupsBox.getChildren().add(groupDisplay);
        }
        
        card.getChildren().addAll(heading, new Separator(), socketGroupsBox);
        VBox.setVgrow(card, Priority.ALWAYS);
        
        return card;
    }
    
    /**
     * Create display for a single socket group
     */
    private HBox createSocketGroupDisplay(SocketGroup group) {
        HBox display = new HBox(10);
        display.setAlignment(Pos.CENTER_LEFT);
        display.setPadding(new Insets(5));
        display.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 3;");
        
        Label nameLabel = new Label(group.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nameLabel.setPrefWidth(120);
        
        Label currentLabel = new Label("0.0 A");
        currentLabel.setPrefWidth(60);
        
        ProgressBar loadBar = new ProgressBar(0);
        loadBar.setPrefWidth(100);
        
        Label statusLabel = new Label("OK");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.web("#27ae60"));
        
        group.totalCurrentProperty().addListener((obs, old, newVal) -> {
            currentLabel.setText(String.format("%.1f A", newVal.doubleValue()));
            loadBar.setProgress(newVal.doubleValue() / group.getRatedCapacity());
            
            SocketGroup.Status status = group.getStatus();
            statusLabel.setText(status.toString());
            switch (status) {
                case OK:
                    statusLabel.setTextFill(Color.web("#27ae60"));
                    loadBar.setStyle("-fx-accent: #27ae60;");
                    break;
                case WARNING:
                    statusLabel.setTextFill(Color.web("#f39c12"));
                    loadBar.setStyle("-fx-accent: #f39c12;");
                    break;
                case DANGER:
                    statusLabel.setTextFill(Color.web("#e74c3c"));
                    loadBar.setStyle("-fx-accent: #e74c3c;");
                    break;
            }
        });
        
        display.getChildren().addAll(nameLabel, currentLabel, loadBar, statusLabel);
        return display;
    }
    
    /**
     * Create alerts panel
     */
    private VBox createAlertsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefHeight(200);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label heading = new Label("🔔 Alerts");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button clearBtn = new Button("Clear All");
        clearBtn.setOnAction(e -> monitoringService.clearAlerts());
        
        header.getChildren().addAll(heading, spacer, clearBtn);
        
        alertsList = new ListView<>();
        alertsList.setItems(monitoringService.getAlerts());
        alertsList.setCellFactory(lv -> new ListCell<com.smartload.models.Alert>() {  // Fixed: Full class name
            @Override
            protected void updateItem(com.smartload.models.Alert alert, boolean empty) {  // Fixed: Full class name
                super.updateItem(alert, empty);
                if (empty || alert == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("[%s] %s: %s", 
                        alert.getFormattedTimestamp(), 
                        alert.getSeverity(), 
                        alert.getMessage()));
                    
                    switch (alert.getSeverity()) {
                        case INFO:
                            setStyle("-fx-text-fill: #3498db;");
                            break;
                        case WARNING:
                            setStyle("-fx-text-fill: #f39c12;");
                            break;
                        case DANGER:
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
        
        VBox.setVgrow(alertsList, Priority.ALWAYS);
        panel.getChildren().addAll(header, alertsList);
        
        return panel;
    }
    
    /**
     * Create a styled stat label
     */
    private HBox createStatLabel(String title, String initialValue) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setPrefWidth(130);
        
        Label valueLabel = new Label(initialValue);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        valueLabel.setTextFill(Color.web("#2c3e50"));
        
        container.getChildren().addAll(titleLabel, valueLabel);
        return container;
    }
    
    /**
     * Update summary labels
     */
    private void updateSummaryLabels() {
        double totalCurrent = monitoringService.calculateTotalCurrent();
        double totalPower = monitoringService.calculateTotalPower();
        double energy = monitoringService.getEnergyTracker().getSessionEnergyKwh();
        double cost = monitoringService.getEnergyTracker().getSessionCostGhs();
        
        // Fixed: Cast to HBox properly
        ((Label) totalCurrentLabel.getChildren().get(1))
            .setText(String.format("%.2f A", totalCurrent));
        ((Label) totalPowerLabel.getChildren().get(1))
            .setText(String.format("%.0f W", totalPower));
        ((Label) energyLabel.getChildren().get(1))
            .setText(String.format("%.3f kWh", energy));
        ((Label) costLabel.getChildren().get(1))
            .setText(String.format("GHS %.2f", cost));
        
        SystemSettings settings = monitoringService.getSettings();
        if (settings.isOverMainLimit(totalCurrent)) {
            statusLabel.setText("Status: DANGER - Overload!");
            statusLabel.setTextFill(Color.web("#e74c3c"));
        } else if (settings.isApproachingLimit(totalCurrent)) {
            statusLabel.setText("Status: WARNING - High Load");
            statusLabel.setTextFill(Color.web("#f39c12"));
        } else {
            statusLabel.setText("Status: OK");
            statusLabel.setTextFill(Color.web("#27ae60"));
        }
    }
    
    /**
     * Show settings dialog
     */
    private void showSettingsDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Settings");
        dialog.setHeaderText("System Configuration");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        SystemSettings settings = monitoringService.getSettings();
        
        TextField voltageField = new TextField(String.valueOf(settings.getVoltage()));
        TextField limitField = new TextField(String.valueOf(settings.getMainLimit()));
        TextField surgeField = new TextField(String.valueOf(settings.getSurgeThreshold()));
        TextField tariffField = new TextField(String.valueOf(settings.getTariff()));
        
        ComboBox<SystemSettings.SimulationMode> modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll(SystemSettings.SimulationMode.values());
        modeCombo.setValue(settings.getSimulationMode());
        
        grid.add(new Label("Voltage (V):"), 0, 0);
        grid.add(voltageField, 1, 0);
        grid.add(new Label("Main Limit (A):"), 0, 1);
        grid.add(limitField, 1, 1);
        grid.add(new Label("Surge Threshold (A):"), 0, 2);
        grid.add(surgeField, 1, 2);
        grid.add(new Label("Tariff (GHS/kWh):"), 0, 3);
        grid.add(tariffField, 1, 3);
        grid.add(new Label("Simulation Mode:"), 0, 4);
        grid.add(modeCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    settings.setVoltage(Double.parseDouble(voltageField.getText()));
                    settings.setMainLimit(Double.parseDouble(limitField.getText()));
                    settings.setSurgeThreshold(Double.parseDouble(surgeField.getText()));
                    settings.setTariff(Double.parseDouble(tariffField.getText()));
                    settings.setSimulationMode(modeCombo.getValue());
                    
                    System.out.println("Settings updated: " + settings);
                } catch (NumberFormatException e) {
                    showError("Invalid input. Please enter valid numbers.");
                }
            }
        });
    }
    
    /**
     * Show error alert
     */
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);  // Fixed: Full class name
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shutdown cleanup
     */
    public void shutdown() {
        if (monitoringService.isRunning()) {
            monitoringService.stop();
        }
        System.out.println("Controller shutdown complete");
    }
}