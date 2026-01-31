package com.smartload.services;

import com.smartload.models.*;
import com.smartload.simulation.SimulationEngine;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core service managing the monitoring system
 */
public class MonitoringService {
    
    private final ObservableList<Appliance> appliances;
    private final ObservableList<SocketGroup> socketGroups;
    private final ObservableList<Alert> alerts;
    private final SystemSettings settings;
    private final SimulationEngine simulationEngine;
    private final EnergyTracker energyTracker;
    
    private AnimationTimer updateTimer;
    private boolean isRunning;
    private long lastUpdateTime;
    
    private static MonitoringService instance;
    
    private MonitoringService() {
        this.appliances = FXCollections.observableArrayList();
        this.socketGroups = FXCollections.observableArrayList();
        this.alerts = FXCollections.observableArrayList();
        this.settings = SystemSettings.getInstance();
        this.simulationEngine = new SimulationEngine();
        this.energyTracker = new EnergyTracker();
        this.isRunning = false;
        this.lastUpdateTime = 0;
        
        initializeDefaultAppliances();
    }
    
    public static MonitoringService getInstance() {
        if (instance == null) {
            instance = new MonitoringService();
        }
        return instance;
    }
    
    /**
     * Initialize with default appliances for testing
     */
    private void initializeDefaultAppliances() {
        // Create socket groups
        SocketGroup kitchen = new SocketGroup("Kitchen", 13.0);
        SocketGroup livingRoom = new SocketGroup("Living Room", 13.0);
        SocketGroup bedroom = new SocketGroup("Bedroom", 13.0);
        SocketGroup acHeavy = new SocketGroup("AC/Heavy Load", 13.0);
        
        socketGroups.addAll(kitchen, livingRoom, bedroom, acHeavy);
        
        // Create appliances
        Appliance fridge = new Appliance("Refrigerator", "Kitchen", "Kitchen", 
            10.0, Appliance.Priority.ESSENTIAL);
        fridge.setOn(true);
        
        Appliance microwave = new Appliance("Microwave", "Kitchen", "Kitchen", 
            8.0, Appliance.Priority.NON_ESSENTIAL);
        microwave.setOn(false);
        
        Appliance kettle = new Appliance("Electric Kettle", "Kitchen", "Kitchen", 
            13.0, Appliance.Priority.NON_ESSENTIAL);
        kettle.setOn(false);
        
        Appliance tv = new Appliance("TV", "Living Room", "Living Room", 
            3.0, Appliance.Priority.NON_ESSENTIAL);
        tv.setOn(true);
        
        Appliance decoder = new Appliance("Decoder/Router", "Living Room", "Living Room", 
            2.0, Appliance.Priority.ESSENTIAL);
        decoder.setOn(true);
        
        Appliance iron = new Appliance("Iron", "Living Room", "Living Room", 
            6.0, Appliance.Priority.NON_ESSENTIAL);
        iron.setOn(false);
        
        Appliance laptop = new Appliance("Laptop Charger", "Bedroom", "Bedroom", 
            2.0, Appliance.Priority.ESSENTIAL);
        laptop.setOn(true);
        
        Appliance lights = new Appliance("LED Lights", "Bedroom", "Bedroom", 
            1.0, Appliance.Priority.ESSENTIAL);
        lights.setOn(true);
        
        Appliance ac = new Appliance("Air Conditioner", "Living Room", "AC/Heavy Load", 
            10.0, Appliance.Priority.NON_ESSENTIAL);
        ac.setOn(true);
        
        appliances.addAll(fridge, microwave, kettle, tv, decoder, iron, laptop, lights, ac);
        
        kitchen.getAppliances().addAll(fridge, microwave, kettle);
        livingRoom.getAppliances().addAll(tv, decoder, iron);
        bedroom.getAppliances().addAll(laptop, lights);
        acHeavy.getAppliances().add(ac);
        
        System.out.println("Initialized " + appliances.size() + " appliances in " + 
            socketGroups.size() + " socket groups");
    }
    
    /**
     * Start the monitoring system
     */
    public void start() {
        if (isRunning) {
            System.out.println("Monitoring service already running");
            return;
        }
        
        System.out.println("Starting monitoring service...");
        isRunning = true;
        lastUpdateTime = System.currentTimeMillis();
        energyTracker.reset();
        
        updateTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                long currentTime = now / 1_000_000;
                
                if (currentTime - lastUpdate >= settings.getUpdateInterval()) {
                    update();
                    lastUpdate = currentTime;
                }
            }
        };
        
        updateTimer.start();
        addAlert("System started", Alert.Severity.INFO, "System");
    }
    
    /**
     * Stop the monitoring system
     */
    public void stop() {
        if (!isRunning) {
            System.out.println("Monitoring service not running");
            return;
        }
        
        System.out.println("Stopping monitoring service...");
        isRunning = false;
        
        if (updateTimer != null) {
            updateTimer.stop();
        }
        
        addAlert("System stopped", Alert.Severity.INFO, "System");
    }
    
    /**
     * Main update cycle
     */
    private void update() {
        try {
            long currentTime = System.currentTimeMillis();
            double deltaTimeSeconds = (currentTime - lastUpdateTime) / 1000.0;
            lastUpdateTime = currentTime;
            
            simulationEngine.updateAppliances(appliances);
            
            for (SocketGroup group : socketGroups) {
                group.calculateTotalCurrent();
                SocketGroup.Status oldStatus = group.getStatus();
                group.updateStatus();
                
                if (oldStatus != group.getStatus()) {
                    handleSocketGroupStatusChange(group, oldStatus);
                }
            }
            
            double totalCurrent = calculateTotalCurrent();
            checkForOverloads(totalCurrent);
            checkForSurges();
            checkForInvalidReadings();
            energyTracker.update(totalCurrent, settings.getVoltage(), deltaTimeSeconds);
            
        } catch (Exception e) {
            System.err.println("Error during update cycle: " + e.getMessage());
            e.printStackTrace();
            addAlert("System error: " + e.getMessage(), Alert.Severity.DANGER, "System");
        }
    }
    
    /**
     * Calculate total house current
     */
    public double calculateTotalCurrent() {
        return appliances.stream()
            .filter(Appliance::isValidReading)
            .mapToDouble(Appliance::getCurrentDraw)
            .sum();
    }
    
    /**
     * Calculate total power
     */
    public double calculateTotalPower() {
        return calculateTotalCurrent() * settings.getVoltage();
    }
    
    /**
     * Check for overload conditions
     */
    private void checkForOverloads(double totalCurrent) {
        if (settings.isOverMainLimit(totalCurrent)) {
            addAlert(String.format("Total load (%.1fA) exceeded main limit (%.0fA)", 
                totalCurrent, settings.getMainLimit()), 
                Alert.Severity.DANGER, "Main");
            
            generateLoadSheddingRecommendations(totalCurrent);
            
        } else if (settings.isApproachingLimit(totalCurrent)) {
            addAlert(String.format("High load: %.1fA of %.0fA limit", 
                totalCurrent, settings.getMainLimit()), 
                Alert.Severity.WARNING, "Main");
        }
    }
    
    /**
     * Handle socket group status changes
     */
    private void handleSocketGroupStatusChange(SocketGroup group, SocketGroup.Status oldStatus) {
        SocketGroup.Status newStatus = group.getStatus();
        
        if (newStatus == SocketGroup.Status.DANGER) {
            Appliance highest = group.getHighestCurrentAppliance();
            String applianceName = highest != null ? highest.getName() : "unknown";
            
            addAlert(String.format("%s socket group overloaded (%.1fA). Highest: %s", 
                group.getName(), group.getTotalCurrent(), applianceName), 
                Alert.Severity.DANGER, group.getName());
                
        } else if (newStatus == SocketGroup.Status.WARNING && oldStatus != SocketGroup.Status.WARNING) {
            addAlert(String.format("%s socket group high load (%.1fA). Avoid adding appliances.", 
                group.getName(), group.getTotalCurrent()), 
                Alert.Severity.WARNING, group.getName());
        }
    }
    
    /**
     * Check for surge events
     */
    private void checkForSurges() {
        for (Appliance appliance : appliances) {
            if (!appliance.isValidReading()) continue;
            
            double delta = appliance.getCurrentDraw() - appliance.getPreviousCurrent();
            
            if (delta >= settings.getSurgeThreshold()) {
                addAlert(String.format("Surge detected on %s: +%.1fA", 
                    appliance.getName(), delta), 
                    Alert.Severity.WARNING, appliance.getName());
            }
        }
    }
    
    /**
     * Check for invalid sensor readings
     */
    private void checkForInvalidReadings() {
        for (Appliance appliance : appliances) {
            if (!appliance.isValidReading() && appliance.isOn()) {
                addAlert(String.format("Sensor fault on %s. Reading ignored.", 
                    appliance.getName()), 
                    Alert.Severity.WARNING, appliance.getName());
            }
        }
    }
    
    /**
     * Generate load shedding recommendations
     */
    private void generateLoadSheddingRecommendations(double totalCurrent) {
        double excess = totalCurrent - settings.getMainLimit();
        
        List<Appliance> nonEssential = appliances.stream()
            .filter(a -> a.getPriority().equals(Appliance.Priority.NON_ESSENTIAL.name()))
            .filter(a -> a.isOn() && a.isValidReading())
            .sorted((a1, a2) -> Double.compare(a2.getCurrentDraw(), a1.getCurrentDraw()))
            .collect(Collectors.toList());
        
        double sheddingTotal = 0.0;
        List<String> recommendations = new ArrayList<>();
        
        for (Appliance appliance : nonEssential) {
            if (sheddingTotal < excess) {
                recommendations.add(String.format("Switch off %s (%.1fA)", 
                    appliance.getName(), appliance.getCurrentDraw()));
                sheddingTotal += appliance.getCurrentDraw();
            } else {
                break;
            }
        }
        
        if (!recommendations.isEmpty()) {
            String message = "Load shedding recommendation: " + String.join(", ", recommendations);
            System.out.println(message);
        }
    }
    
    /**
     * Add an alert to the system
     */
    public void addAlert(String message, Alert.Severity severity, String affectedItem) {
        long now = System.currentTimeMillis();
        boolean isDuplicate = alerts.stream()
            .filter(a -> a.getMessage().equals(message))
            .anyMatch(a -> now - a.getTimestamp().atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli() < 5000);
        
        if (!isDuplicate) {
            Alert alert = new Alert(message, severity, affectedItem);
            alerts.add(0, alert);
            System.out.println("Alert: " + alert);
            
            if (alerts.size() > 100) {
                alerts.remove(alerts.size() - 1);
            }
        }
    }
    
    // Getters
    public ObservableList<Appliance> getAppliances() { return appliances; }
    public ObservableList<SocketGroup> getSocketGroups() { return socketGroups; }
    public ObservableList<Alert> getAlerts() { return alerts; }
    public SystemSettings getSettings() { return settings; }
    public EnergyTracker getEnergyTracker() { return energyTracker; }
    public boolean isRunning() { return isRunning; }
    public SimulationEngine getSimulationEngine() { return simulationEngine; }
    
    public void clearAlerts() {
        alerts.clear();
        System.out.println("Alerts cleared");
    }
    
    public void toggleAppliance(Appliance appliance) {
        appliance.setOn(!appliance.isOn());
        System.out.println("Toggled " + appliance.getName() + ": " + 
            (appliance.isOn() ? "ON" : "OFF"));
    }
}