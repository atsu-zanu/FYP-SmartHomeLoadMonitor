package com.smartload.services;

import com.smartload.models.SystemSettings;
import javafx.beans.property.*;

/**
 * Tracks energy consumption and calculates costs
 */
public class EnergyTracker {
    private final DoubleProperty sessionEnergyKwh;
    private final DoubleProperty sessionCostGhs;
    private final DoubleProperty currentPowerWatts;
    private final SystemSettings settings;
    
    public EnergyTracker() {
        this.sessionEnergyKwh = new SimpleDoubleProperty(0.0);
        this.sessionCostGhs = new SimpleDoubleProperty(0.0);
        this.currentPowerWatts = new SimpleDoubleProperty(0.0);
        this.settings = SystemSettings.getInstance();
    }
    
    /**
     * Update energy accumulation
     */
    public void update(double current, double voltage, double deltaTimeSeconds) {
        // Calculate instantaneous power
        double power = voltage * current;
        currentPowerWatts.set(power);
        
        // Calculate energy: Energy (Wh) = Power (W) × Time (h)
        double energyWh = power * (deltaTimeSeconds / 3600.0);
        double energyKwh = energyWh / 1000.0;
        
        // Accumulate total session energy
        sessionEnergyKwh.set(sessionEnergyKwh.get() + energyKwh);
        
        // Calculate cost
        double cost = sessionEnergyKwh.get() * settings.getTariff();
        sessionCostGhs.set(cost);
    }
    
    /**
     * Reset session tracking
     */
    public void reset() {
        sessionEnergyKwh.set(0.0);
        sessionCostGhs.set(0.0);
        currentPowerWatts.set(0.0);
    }
    
    // Getters
    public double getSessionEnergyKwh() { return sessionEnergyKwh.get(); }
    public double getSessionCostGhs() { return sessionCostGhs.get(); }
    public double getCurrentPowerWatts() { return currentPowerWatts.get(); }
    public double getCurrentPowerKw() { return currentPowerWatts.get() / 1000.0; }
    
    // Properties for binding
    public DoubleProperty sessionEnergyKwhProperty() { return sessionEnergyKwh; }
    public DoubleProperty sessionCostGhsProperty() { return sessionCostGhs; }
    public DoubleProperty currentPowerWattsProperty() { return currentPowerWatts; }
    
    /**
     * Get formatted energy string
     */
    public String getFormattedEnergy() {
        double kwh = getSessionEnergyKwh();
        if (kwh < 1.0) {
            return String.format("%.0f Wh", kwh * 1000);
        }
        return String.format("%.2f kWh", kwh);
    }
    
    /**
     * Get formatted cost string
     */
    public String getFormattedCost() {
        return String.format("GHS %.2f", getSessionCostGhs());
    }
    
    /**
     * Get formatted power string
     */
    public String getFormattedPower() {
        double watts = getCurrentPowerWatts();
        if (watts < 1000) {
            return String.format("%.0f W", watts);
        }
        return String.format("%.2f kW", watts / 1000);
    }
    
    @Override
    public String toString() {
        return String.format("Energy: %s, Cost: %s, Power: %s", 
            getFormattedEnergy(), getFormattedCost(), getFormattedPower());
    }
}