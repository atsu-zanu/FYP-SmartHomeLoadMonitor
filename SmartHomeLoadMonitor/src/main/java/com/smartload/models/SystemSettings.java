package com.smartload.models;

import javafx.beans.property.*;

/**
 * System-wide configuration settings
 */
public class SystemSettings {
    private final DoubleProperty voltage;
    private final DoubleProperty mainLimit;
    private final DoubleProperty surgeThreshold;
    private final DoubleProperty tariff;
    private final ObjectProperty<SimulationMode> simulationMode;
    private final IntegerProperty updateInterval;
    
    public enum SimulationMode {
        RANDOM, SCRIPTED
    }
    
    private static SystemSettings instance;
    
    private SystemSettings() {
        this.voltage = new SimpleDoubleProperty(230.0);
        this.mainLimit = new SimpleDoubleProperty(40.0);
        this.surgeThreshold = new SimpleDoubleProperty(3.0);
        this.tariff = new SimpleDoubleProperty(0.50);
        this.simulationMode = new SimpleObjectProperty<>(SimulationMode.RANDOM);
        this.updateInterval = new SimpleIntegerProperty(2000);
    }
    
    public static SystemSettings getInstance() {
        if (instance == null) {
            instance = new SystemSettings();
        }
        return instance;
    }
    
    // Getters
    public double getVoltage() { return voltage.get(); }
    public double getMainLimit() { return mainLimit.get(); }
    public double getSurgeThreshold() { return surgeThreshold.get(); }
    public double getTariff() { return tariff.get(); }
    public SimulationMode getSimulationMode() { return simulationMode.get(); }
    public int getUpdateInterval() { return updateInterval.get(); }
    
    // Setters
    public void setVoltage(double v) { this.voltage.set(v); }
    public void setMainLimit(double limit) { this.mainLimit.set(limit); }
    public void setSurgeThreshold(double threshold) { this.surgeThreshold.set(threshold); }
    public void setTariff(double t) { this.tariff.set(t); }
    public void setSimulationMode(SimulationMode mode) { this.simulationMode.set(mode); }
    public void setUpdateInterval(int interval) { this.updateInterval.set(interval); }
    
    // Properties
    public DoubleProperty voltageProperty() { return voltage; }
    public DoubleProperty mainLimitProperty() { return mainLimit; }
    public DoubleProperty surgeThresholdProperty() { return surgeThreshold; }
    public DoubleProperty tariffProperty() { return tariff; }
    public ObjectProperty<SimulationMode> simulationModeProperty() { return simulationMode; }
    public IntegerProperty updateIntervalProperty() { return updateInterval; }
    
    public double calculatePower(double current) {
        return getVoltage() * current;
    }
    
    public double calculateCost(double energyKwh) {
        return energyKwh * getTariff();
    }
    
    public boolean isOverMainLimit(double totalCurrent) {
        return totalCurrent > getMainLimit();
    }
    
    public boolean isApproachingLimit(double totalCurrent) {
        return totalCurrent >= (getMainLimit() * 0.8) && totalCurrent <= getMainLimit();
    }
    
    public void resetToDefaults() {
        setVoltage(230.0);
        setMainLimit(40.0);
        setSurgeThreshold(3.0);
        setTariff(0.50);
        setSimulationMode(SimulationMode.RANDOM);
        setUpdateInterval(2000);
    }
}
