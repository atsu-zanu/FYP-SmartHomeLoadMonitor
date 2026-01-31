package com.smartload.simulation;

import com.smartload.models.Appliance;
import com.smartload.models.SystemSettings;
import java.util.Random;
import java.util.List;

/**
 * Simulation engine for generating realistic current readings
 */
public class SimulationEngine {
    private final Random random;
    private final SystemSettings settings;
    private int tickCount;
    
    // Typical current ranges for appliances (in Amps)
    private static final double FRIDGE_MIN = 0.8;
    private static final double FRIDGE_MAX = 2.5;
    private static final double TV_MIN = 0.3;
    private static final double TV_MAX = 1.2;
    private static final double AC_MIN = 4.0;
    private static final double AC_MAX = 8.0;
    private static final double MICROWAVE_MIN = 3.0;
    private static final double MICROWAVE_MAX = 6.0;
    private static final double IRON_MIN = 3.5;
    private static final double IRON_MAX = 5.0;
    private static final double KETTLE_MIN = 8.0;
    private static final double KETTLE_MAX = 12.0;
    private static final double LAPTOP_MIN = 0.5;
    private static final double LAPTOP_MAX = 1.5;
    private static final double LIGHT_MIN = 0.1;
    private static final double LIGHT_MAX = 0.5;
    
    public SimulationEngine() {
        this.random = new Random();
        this.settings = SystemSettings.getInstance();
        this.tickCount = 0;
    }
    
    /**
     * Update all appliances with simulated readings
     */
    public void updateAppliances(List<Appliance> appliances) {
        tickCount++;
        
        if (settings.getSimulationMode() == SystemSettings.SimulationMode.RANDOM) {
            updateRandomMode(appliances);
        } else {
            updateScriptedMode(appliances);
        }
    }
    
    /**
     * Random simulation mode
     */
    private void updateRandomMode(List<Appliance> appliances) {
        for (Appliance appliance : appliances) {
            if (!appliance.isOn()) {
                appliance.setCurrentDraw(0.0);
                appliance.setStatus(Appliance.Status.OK);
                continue;
            }
            
            double current = generateRealisticCurrent(appliance.getName());
            
            // Occasionally generate invalid readings (5% chance)
            if (random.nextDouble() < 0.05) {
                current = generateInvalidReading(appliance);
            }
            
            // Occasionally generate surges (3% chance)
            if (random.nextDouble() < 0.03) {
                current = appliance.getCurrentDraw() + (random.nextDouble() * 5.0);
            }
            
            appliance.setCurrentDraw(current);
            
            // Update status
            if (!appliance.isValidReading()) {
                appliance.setStatus(Appliance.Status.INVALID);
            } else if (Math.abs(current - appliance.getPreviousCurrent()) >= settings.getSurgeThreshold()) {
                appliance.setStatus(Appliance.Status.SURGE);
            } else if (current > appliance.getMaxCurrent() * 0.9) {
                appliance.setStatus(Appliance.Status.WARNING);
            } else {
                appliance.setStatus(Appliance.Status.OK);
            }
        }
    }
    
    /**
     * Scripted simulation mode - follows predefined scenario
     */
    private void updateScriptedMode(List<Appliance> appliances) {
        if (tickCount < 5) {
            // 0-10s: Normal operation
            normalOperation(appliances);
        } else if (tickCount < 10) {
            // 10-20s: Kitchen load increases
            increaseKitchenLoad(appliances);
        } else if (tickCount < 15) {
            // 20-30s: Add microwave - trigger kitchen overload
            triggerKitchenOverload(appliances);
        } else if (tickCount < 20) {
            // 30-40s: AC surge event
            triggerAcSurge(appliances);
        } else if (tickCount < 25) {
            // 40-50s: Total house exceeds main limit
            triggerHouseOverload(appliances);
        } else if (tickCount < 30) {
            // 50-60s: Invalid sensor reading
            triggerInvalidReading(appliances);
        } else {
            // 60s+: Return to normal
            normalOperation(appliances);
            if (tickCount > 35) {
                tickCount = 0;
            }
        }
    }
    
    private void normalOperation(List<Appliance> appliances) {
        for (Appliance appliance : appliances) {
            if (appliance.isOn()) {
                double current = generateRealisticCurrent(appliance.getName());
                appliance.setCurrentDraw(current);
                appliance.setStatus(Appliance.Status.OK);
            } else {
                appliance.setCurrentDraw(0.0);
            }
        }
    }
    
    private void increaseKitchenLoad(List<Appliance> appliances) {
        for (Appliance appliance : appliances) {
            if (appliance.getSocketGroup().equalsIgnoreCase("Kitchen") && appliance.isOn()) {
                double current = generateRealisticCurrent(appliance.getName()) * 1.3;
                appliance.setCurrentDraw(Math.min(current, appliance.getMaxCurrent()));
            } else if (appliance.isOn()) {
                appliance.setCurrentDraw(generateRealisticCurrent(appliance.getName()));
            }
        }
    }
    
    private void triggerKitchenOverload(List<Appliance> appliances) {
        for (Appliance appliance : appliances) {
            if (appliance.getName().toLowerCase().contains("microwave")) {
                appliance.setOn(true);
                appliance.setCurrentDraw(5.5);
            } else if (appliance.getSocketGroup().equalsIgnoreCase("Kitchen") && appliance.isOn()) {
                appliance.setCurrentDraw(generateRealisticCurrent(appliance.getName()) * 1.4);
            } else if (appliance.isOn()) {
                appliance.setCurrentDraw(generateRealisticCurrent(appliance.getName()));
            }
        }
    }
    
    private void triggerAcSurge(List<Appliance> appliances) {
        for (Appliance appliance : appliances) {
            if (appliance.getName().toLowerCase().contains("ac") && appliance.isOn()) {
                double surgeCurrent = appliance.getCurrentDraw() + 4.2;
                appliance.setCurrentDraw(surgeCurrent);
                appliance.setStatus(Appliance.Status.SURGE);
            } else if (appliance.isOn()) {
                appliance.setCurrentDraw(generateRealisticCurrent(appliance.getName()));
            }
        }
    }
    
    private void triggerHouseOverload(List<Appliance> appliances) {
        for (Appliance appliance : appliances) {
            if (appliance.isOn()) {
                double current = generateRealisticCurrent(appliance.getName()) * 1.5;
                appliance.setCurrentDraw(Math.min(current, appliance.getMaxCurrent()));
            }
        }
    }
    
    private void triggerInvalidReading(List<Appliance> appliances) {
        if (!appliances.isEmpty()) {
            Appliance target = appliances.get(random.nextInt(appliances.size()));
            if (target.isOn()) {
                target.setCurrentDraw(-1.5);
                target.setStatus(Appliance.Status.INVALID);
            }
        }
        
        for (Appliance appliance : appliances) {
            if (appliance.isOn() && appliance.getCurrentDraw() >= 0) {
                appliance.setCurrentDraw(generateRealisticCurrent(appliance.getName()));
            }
        }
    }
    
    /**
     * Generate realistic current based on appliance name
     */
    private double generateRealisticCurrent(String applianceName) {
        String name = applianceName.toLowerCase();
        double min, max;
        
        if (name.contains("fridge") || name.contains("refrigerator")) {
            min = FRIDGE_MIN;
            max = FRIDGE_MAX;
        } else if (name.contains("tv") || name.contains("television")) {
            min = TV_MIN;
            max = TV_MAX;
        } else if (name.contains("ac") || name.contains("air con")) {
            min = AC_MIN;
            max = AC_MAX;
        } else if (name.contains("microwave")) {
            min = MICROWAVE_MIN;
            max = MICROWAVE_MAX;
        } else if (name.contains("iron")) {
            min = IRON_MIN;
            max = IRON_MAX;
        } else if (name.contains("kettle")) {
            min = KETTLE_MIN;
            max = KETTLE_MAX;
        } else if (name.contains("laptop") || name.contains("computer")) {
            min = LAPTOP_MIN;
            max = LAPTOP_MAX;
        } else if (name.contains("light") || name.contains("bulb")) {
            min = LIGHT_MIN;
            max = LIGHT_MAX;
        } else {
            min = 0.5;
            max = 3.0;
        }
        
        double base = min + (random.nextDouble() * (max - min));
        double variation = base * 0.1 * (random.nextDouble() - 0.5) * 2;
        return Math.max(0, base + variation);
    }
    
    /**
     * Generate an invalid reading for testing
     */
    private double generateInvalidReading(Appliance appliance) {
        int type = random.nextInt(3);
        switch (type) {
            case 0: return -1.0 * random.nextDouble() * 5.0;
            case 1: return 0.0;
            case 2: return appliance.getMaxCurrent() + random.nextDouble() * 10.0;
            default: return 0.0;
        }
    }
    
    public void reset() {
        tickCount = 0;
    }
    
    public int getTickCount() {
        return tickCount;
    }
}