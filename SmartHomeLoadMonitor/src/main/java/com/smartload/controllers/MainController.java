package com.smartload.controllers;

import com.smartload.models.*;
import com.smartload.services.MonitoringService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainController {
    
    private final MonitoringService monitoringService;
    private TableView<Appliance> applianceTable;
    private ListView<com.smartload.models.Alert> alertsList;
    private Label totalCurrentValueLabel;
    private Label totalPowerValueLabel;
    private Label energyValueLabel;
    private Label costValueLabel;
    private Label voltageValueLabel;
    private Button startBtn;
    private Button stopBtn;
    
    // Charts
    private LineChart<Number, Number> realTimeLineChart;
    private BarChart<String, Number> socketGroupBarChart;
    private PieChart appliancePieChart;
    private LineChart<String, Number> historicalComparisonChart;
    private BarChart<String, Number> dailyUsageChart;
    
    // Data storage
    private int timeCounter = 0;
    private static final int MAX_DATA_POINTS = 50;
    private XYChart.Series<Number, Number> currentSeries;
    private XYChart.Series<Number, Number> powerSeries;
    private XYChart.Series<Number, Number> voltageSeries;
    
    private LinkedList<Double> historicalData = new LinkedList<>();
    private Timeline updateTimeline;
    
    public MainController() {
        this.monitoringService = MonitoringService.getInstance();
        initializeDataStructures();
    }
    
    private void initializeDataStructures() {
        currentSeries = new XYChart.Series<>();
        currentSeries.setName("Current (A)");
        
        powerSeries = new XYChart.Series<>();
        powerSeries.setName("Power (W)");
        
        voltageSeries = new XYChart.Series<>();
        voltageSeries.setName("Voltage (V)");
        
        for (int i = 0; i < 24; i++) {
            historicalData.add(Math.random() * 30 + 10);
        }
    }
    
    public Parent createView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f7fa;");
        
        VBox mainContent = new VBox(0);
        mainContent.getChildren().addAll(
            createModernHeader(),
            createDashboardContent()
        );
        
        root.setCenter(mainContent);
        
        startUpdateTimeline();
        
        return root;
    }
    
    private void startUpdateTimeline() {
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            if (monitoringService.isRunning()) {
                updateRealTimeCharts();
            }
        }));
        updateTimeline.setCycleCount(Animation.INDEFINITE);
        updateTimeline.play();
    }
    
    private HBox createModernHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);"
        );
        
        VBox titleBox = new VBox(3);
        Label title = new Label("⚡ Smart Home Load Monitor");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        
        Label subtitle = new Label("Advanced Analytics & Real-time Monitoring");
        subtitle.setFont(Font.font("Arial", 13));
        subtitle.setTextFill(Color.web("#ffffff", 0.8));
        
        titleBox.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER_RIGHT);
        
        startBtn = createHeaderButton("▶ Start", "#10ac84");
        stopBtn = createHeaderButton("⏹ Stop", "#ee5a6f");
        Button settingsBtn = createHeaderButton("⚙", "#34495e");
        
        startBtn.setOnAction(e -> {
            monitoringService.start();
            startBtn.setDisable(true);
            stopBtn.setDisable(false);
        });
        
        stopBtn.setOnAction(e -> {
            monitoringService.stop();
            startBtn.setDisable(false);
            stopBtn.setDisable(true);
        });
        
        stopBtn.setDisable(true);
        settingsBtn.setOnAction(e -> showSettingsDialog());
        
        controlBox.getChildren().addAll(startBtn, stopBtn, settingsBtn);
        
        header.getChildren().addAll(titleBox, spacer, controlBox);
        return header;
    }
    
    private Button createHeaderButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setPadding(new Insets(10, 20, 10, 20));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        
        return btn;
    }
    
    private ScrollPane createDashboardContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(20, 30, 20, 30));
        
        // Top Statistics Row
        HBox topStats = createTopStatsRow();
        
        // Real-time Line Chart Section
        VBox realTimeSection = createRealTimeChartCard();
        
        // Middle Row: Socket Group Bar Chart + Pie Chart
        HBox middleRow = new HBox(20);
        VBox socketBarCard = createSocketGroupBarChart();
        VBox pieCard = createAppliancePieChart();
        HBox.setHgrow(socketBarCard, Priority.ALWAYS);
        HBox.setHgrow(pieCard, Priority.ALWAYS);
        middleRow.getChildren().addAll(socketBarCard, pieCard);
        
        // Historical Comparison Section
        VBox historicalSection = createHistoricalComparisonCard();
        
        // Daily Usage Pattern
        VBox dailyUsageSection = createDailyUsageCard();
        
        // Appliance Table
        VBox applianceCard = createApplianceTableCard();
        
        // Alerts
        VBox alertsCard = createAlertsCard();
        
        content.getChildren().addAll(
            topStats,
            realTimeSection,
            middleRow,
            historicalSection,
            dailyUsageSection,
            applianceCard,
            alertsCard
        );
        
        scrollPane.setContent(content);
        return scrollPane;
    }
    
    private HBox createTopStatsRow() {
        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER);
        
        totalPowerValueLabel = new Label("0 W");
        VBox powerStat = createQuickStatCard("Total Power", totalPowerValueLabel, "🔋", "#10ac84");
        
        voltageValueLabel = new Label("230 V");
        VBox voltageStat = createQuickStatCard("Voltage", voltageValueLabel, "⚡", "#f39c12");
        
        totalCurrentValueLabel = new Label("0.0 A");
        VBox currentStat = createQuickStatCard("Current", totalCurrentValueLabel, "📊", "#3498db");
        
        energyValueLabel = new Label("0.00 kWh");
        VBox energyStat = createQuickStatCard("Energy", energyValueLabel, "⚙", "#9b59b6");
        
        costValueLabel = new Label("GHS 0.00");
        VBox costStat = createQuickStatCard("Cost", costValueLabel, "💰", "#e74c3c");
        
        statsRow.getChildren().addAll(powerStat, voltageStat, currentStat, energyStat, costStat);
        
        // Update listener
        monitoringService.getEnergyTracker().currentPowerWattsProperty().addListener((obs, old, newVal) -> {
            updateTopStats();
        });
        
        return statsRow;
    }
    
    private void updateTopStats() {
        double totalCurrent = monitoringService.calculateTotalCurrent();
        double totalPower = monitoringService.calculateTotalPower();
        double voltage = monitoringService.getSettings().getVoltage();
        double energy = monitoringService.getEnergyTracker().getSessionEnergyKwh();
        double cost = monitoringService.getEnergyTracker().getSessionCostGhs();
        
        totalCurrentValueLabel.setText(String.format("%.2f A", totalCurrent));
        totalPowerValueLabel.setText(String.format("%.0f W", totalPower));
        voltageValueLabel.setText(String.format("%.0f V", voltage));
        energyValueLabel.setText(String.format("%.3f kWh", energy));
        costValueLabel.setText(String.format("GHS %.2f", cost));
    }
    
    private VBox createQuickStatCard(String title, Label valueLabel, String icon, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(28));
        
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        valueLabel.setTextFill(Color.web(color));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setTextFill(Color.web("#7f8c8d"));
        
        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }
    
    // ==================== REAL-TIME LINE CHART ====================
    
    private VBox createRealTimeChartCard() {
        VBox card = createDashboardCard("📈 Real-Time Power Trends");
        
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");
        yAxis.setAutoRanging(true);
        
        realTimeLineChart = new LineChart<>(xAxis, yAxis);
        realTimeLineChart.setTitle("Live System Metrics");
        realTimeLineChart.setAnimated(false);
        realTimeLineChart.setCreateSymbols(false);
        realTimeLineChart.setPrefHeight(300);
        
        realTimeLineChart.getData().addAll(currentSeries, powerSeries);
        
        VBox.setVgrow(realTimeLineChart, Priority.ALWAYS);
        card.getChildren().add(realTimeLineChart);
        return card;
    }
    
    private void updateRealTimeCharts() {
        double current = monitoringService.calculateTotalCurrent();
        double power = monitoringService.calculateTotalPower() / 10; // Scale down for visibility
        
        currentSeries.getData().add(new XYChart.Data<>(timeCounter, current));
        powerSeries.getData().add(new XYChart.Data<>(timeCounter, power));
        
        // Keep only last MAX_DATA_POINTS
        if (currentSeries.getData().size() > MAX_DATA_POINTS) {
            currentSeries.getData().remove(0);
        }
        if (powerSeries.getData().size() > MAX_DATA_POINTS) {
            powerSeries.getData().remove(0);
        }
        
        timeCounter++;
        
        // Update other charts
        updateSocketGroupBarChart();
        updateAppliancePieChart();
    }
    
    // ==================== SOCKET GROUP BAR CHART ====================
    
    private VBox createSocketGroupBarChart() {
        VBox card = createDashboardCard("🔌 Socket Group Comparison");
        
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Socket Groups");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Current (A)");
        
        socketGroupBarChart = new BarChart<>(xAxis, yAxis);
        socketGroupBarChart.setTitle("Current Usage by Circuit");
        socketGroupBarChart.setLegendVisible(false);
        socketGroupBarChart.setPrefHeight(350);
        socketGroupBarChart.setBarGap(3);
        socketGroupBarChart.setCategoryGap(10);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Current");
        
        for (SocketGroup group : monitoringService.getSocketGroups()) {
            series.getData().add(new XYChart.Data<>(group.getName(), group.getTotalCurrent()));
        }
        
        socketGroupBarChart.getData().add(series);
        
        VBox.setVgrow(socketGroupBarChart, Priority.ALWAYS);
        card.getChildren().add(socketGroupBarChart);
        return card;
    }
    
    private void updateSocketGroupBarChart() {
        if (socketGroupBarChart.getData().isEmpty()) return;
        
        XYChart.Series<String, Number> series = socketGroupBarChart.getData().get(0);
        series.getData().clear();
        
        for (SocketGroup group : monitoringService.getSocketGroups()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(group.getName(), group.getTotalCurrent());
            series.getData().add(data);
        }
    }
    
    // ==================== APPLIANCE PIE CHART ====================
    
    private VBox createAppliancePieChart() {
        VBox card = createDashboardCard("🥧 Energy Distribution");
        
        appliancePieChart = new PieChart();
        appliancePieChart.setTitle("Power Consumption by Appliance");
        appliancePieChart.setLegendSide(Side.RIGHT);
        appliancePieChart.setPrefHeight(350);
        appliancePieChart.setLabelsVisible(true);
        
        updateAppliancePieChart();
        
        VBox.setVgrow(appliancePieChart, Priority.ALWAYS);
        card.getChildren().add(appliancePieChart);
        return card;
    }
    
    private void updateAppliancePieChart() {
        appliancePieChart.getData().clear();
        
        List<Appliance> activeAppliances = monitoringService.getAppliances().stream()
            .filter(a -> a.isOn() && a.getCurrentDraw() > 0)
            .toList();
        
        if (activeAppliances.isEmpty()) {
            appliancePieChart.getData().add(new PieChart.Data("No Active Appliances", 1));
            return;
        }
        
        for (Appliance appliance : activeAppliances) {
            double power = appliance.calculatePower(monitoringService.getSettings().getVoltage());
            if (power > 0) {
                PieChart.Data slice = new PieChart.Data(
                    appliance.getName() + " (" + String.format("%.0fW", power) + ")", 
                    power
                );
                appliancePieChart.getData().add(slice);
            }
        }
        
        // Apply colors
        String[] colors = {"#3498db", "#2ecc71", "#f39c12", "#9b59b6", "#e74c3c", "#1abc9c", "#34495e", "#95a5a6"};
        for (int i = 0; i < appliancePieChart.getData().size(); i++) {
            final PieChart.Data data = appliancePieChart.getData().get(i);
            data.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
        }
    }
    
    // ==================== HISTORICAL COMPARISON CHART ====================
    
    private VBox createHistoricalComparisonCard() {
        VBox card = createDashboardCard("📊 Historical Comparison");
        
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time Period");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Energy (kWh)");
        
        historicalComparisonChart = new LineChart<>(xAxis, yAxis);
        historicalComparisonChart.setTitle("Energy Consumption - Last 24 Hours");
        historicalComparisonChart.setPrefHeight(300);
        historicalComparisonChart.setCreateSymbols(true);
        
        XYChart.Series<String, Number> todaySeries = new XYChart.Series<>();
        todaySeries.setName("Today");
        
        XYChart.Series<String, Number> yesterdaySeries = new XYChart.Series<>();
        yesterdaySeries.setName("Yesterday");
        
        for (int i = 0; i < 24; i++) {
            String hour = String.format("%02d:00", i);
            todaySeries.getData().add(new XYChart.Data<>(hour, historicalData.get(i)));
            yesterdaySeries.getData().add(new XYChart.Data<>(hour, historicalData.get(i) * 0.85));
        }
        
        historicalComparisonChart.getData().addAll(todaySeries, yesterdaySeries);
        
        VBox.setVgrow(historicalComparisonChart, Priority.ALWAYS);
        card.getChildren().add(historicalComparisonChart);
        return card;
    }
    
    // ==================== DAILY USAGE PATTERN ====================
    
    private VBox createDailyUsageCard() {
        VBox card = createDashboardCard("📅 Daily Usage Pattern");
        
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Day of Week");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Energy (kWh)");
        
        dailyUsageChart = new BarChart<>(xAxis, yAxis);
        dailyUsageChart.setTitle("Weekly Energy Consumption");
        dailyUsageChart.setPrefHeight(300);
        dailyUsageChart.setLegendVisible(true);
        
        XYChart.Series<String, Number> currentWeek = new XYChart.Series<>();
        currentWeek.setName("This Week");
        
        XYChart.Series<String, Number> lastWeek = new XYChart.Series<>();
        lastWeek.setName("Last Week");
        
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        Random rand = new Random();
        
        for (String day : days) {
            currentWeek.getData().add(new XYChart.Data<>(day, 15 + rand.nextDouble() * 25));
            lastWeek.getData().add(new XYChart.Data<>(day, 12 + rand.nextDouble() * 20));
        }
        
        dailyUsageChart.getData().addAll(currentWeek, lastWeek);
        
        VBox.setVgrow(dailyUsageChart, Priority.ALWAYS);
        card.getChildren().add(dailyUsageChart);
        return card;
    }
    
    // ==================== APPLIANCE TABLE ====================
    
    private VBox createApplianceTableCard() {
        VBox card = createDashboardCard("⚡ Appliance Monitor");
        
        applianceTable = new TableView<>();
        applianceTable.setItems(monitoringService.getAppliances());
        applianceTable.setPrefHeight(300);
        
        TableColumn<Appliance, String> nameCol = new TableColumn<>("Appliance");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);
        
        TableColumn<Appliance, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(120);
        
        TableColumn<Appliance, String> groupCol = new TableColumn<>("Circuit");
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
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });
        currentCol.setPrefWidth(100);
        
        TableColumn<Appliance, Double> powerCol = new TableColumn<>("Power (W)");
        powerCol.setCellValueFactory(cellData -> {
            double power = cellData.getValue().calculatePower(230);
            return new javafx.beans.property.SimpleDoubleProperty(power).asObject();
        });
        powerCol.setCellFactory(col -> new TableCell<Appliance, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.0f W", item));
                }
            }
        });
        powerCol.setPrefWidth(100);
        
        TableColumn<Appliance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().isOn() ? "● ON" : "○ OFF";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        statusCol.setCellFactory(col -> new TableCell<Appliance, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("ON")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #95a5a6;");
                    }
                }
            }
        });
        statusCol.setPrefWidth(80);
        
        applianceTable.getColumns().addAll(nameCol, locationCol, groupCol, currentCol, powerCol, statusCol);
        
        VBox.setVgrow(applianceTable, Priority.ALWAYS);
        card.getChildren().add(applianceTable);
        return card;
    }
    
    // ==================== ALERTS ====================
    
    private VBox createAlertsCard() {
        VBox card = createDashboardCard("🔔 System Alerts");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button clearBtn = new Button("Clear All");
        clearBtn.setStyle(
            "-fx-background-color: #e74c3c;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 6 12 6 12;"
        );
        clearBtn.setOnAction(e -> monitoringService.clearAlerts());
        
        header.getChildren().addAll(spacer, clearBtn);
        
        alertsList = new ListView<>();
        alertsList.setItems(monitoringService.getAlerts());
        alertsList.setPrefHeight(200);
        alertsList.setCellFactory(lv -> new ListCell<com.smartload.models.Alert>() {
            @Override
            protected void updateItem(com.smartload.models.Alert alert, boolean empty) {
                super.updateItem(alert, empty);
                if (empty || alert == null) {
                    setGraphic(null);
                } else {
                    HBox alertBox = new HBox(15);
                    alertBox.setAlignment(Pos.CENTER_LEFT);
                    alertBox.setPadding(new Insets(10));
                    alertBox.setStyle("-fx-background-radius: 8;");
                    
                    Label icon = new Label();
                    icon.setFont(Font.font(20));
                    
                    VBox textBox = new VBox(3);
                    Label messageLabel = new Label(alert.getMessage());
                    messageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    messageLabel.setWrapText(true);
                    
                    Label timeLabel = new Label(alert.getFormattedTimestamp());
                    timeLabel.setFont(Font.font("Arial", 10));
                    timeLabel.setTextFill(Color.web("#7f8c8d"));
                    
                    textBox.getChildren().addAll(messageLabel, timeLabel);
                    
                    switch (alert.getSeverity()) {
                        case INFO:
                            icon.setText("ℹ️");
                            alertBox.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 8;");
                            messageLabel.setTextFill(Color.web("#1976d2"));
                            break;
                        case WARNING:
                            icon.setText("⚠️");
                            alertBox.setStyle("-fx-background-color: #fff8e1; -fx-background-radius: 8;");
                            messageLabel.setTextFill(Color.web("#f57c00"));
                            break;
                        case DANGER:
                            icon.setText("🚨");
                            alertBox.setStyle("-fx-background-color: #ffebee; -fx-background-radius: 8;");
                            messageLabel.setTextFill(Color.web("#c62828"));
                            break;
                    }
                    
                    alertBox.getChildren().addAll(icon, textBox);
                    setGraphic(alertBox);
                }
            }
        });
        
        VBox.setVgrow(alertsList, Priority.ALWAYS);
        card.getChildren().addAll(header, alertsList);
        return card;
    }
    
    // ==================== UTILITY METHODS ====================
    
    private VBox createDashboardCard(String title) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 15;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
        );
        
        Label heading = new Label(title);
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        heading.setTextFill(Color.web("#2c3e50"));
        
        card.getChildren().add(heading);
        return card;
    }
    
    private void showSettingsDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("⚙ System Settings");
        dialog.setHeaderText("Configure Monitoring Parameters");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
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
                } catch (NumberFormatException e) {
                    showError("Invalid input. Please enter valid numbers.");
                }
            }
        });
    }
    
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void shutdown() {
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
        if (monitoringService.isRunning()) {
            monitoringService.stop();
        }
    }
}