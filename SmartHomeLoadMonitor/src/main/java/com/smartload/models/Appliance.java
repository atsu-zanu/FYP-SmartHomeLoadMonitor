package com.smartload.models;

import javafx.beans.property.*;

/**
 * Model representing an electrical appliance in the home
 */
public class Appliance {
    private final StringProperty name;
    private final StringProperty location;
    private final StringProperty socketGroup;
    private final DoubleProperty currentDraw;
    private final DoubleProperty maxCurrent;
    private final StringProperty priority;
    private final ObjectProperty<Status> status;
    private final BooleanProperty isOn;
    
    private double previousCurrent;
    
    public enum Priority {
        ESSENTIAL, NON_ESSENTIAL
    }
    
    public enum Status {
        OK, WARNING, DANGER, SURGE, INVALID
    }
    
    public Appliance(String name, String location, String socketGroup, 
                     double maxCurrent, Priority priority) {
        this.name = new SimpleStringProperty(name);
        this.location = new SimpleStringProperty(location);
        this.socketGroup = new SimpleStringProperty(socketGroup);
        this.currentDraw = new SimpleDoubleProperty(0.0);
        this.maxCurrent = new SimpleDoubleProperty(maxCurrent);
        this.priority = new SimpleStringProperty(priority.name());
        this.status = new SimpleObjectProperty<>(Status.OK);
        this.isOn = new SimpleBooleanProperty(false);
        this.previousCurrent = 0.0;
    }
    
    // Getters
    public String getName() { return name.get(); }
    public String getLocation() { return location.get(); }
    public String getSocketGroup() { return socketGroup.get(); }
    public double getCurrentDraw() { return currentDraw.get(); }
    public double getMaxCurrent() { return maxCurrent.get(); }
    public String getPriority() { return priority.get(); }
    public Status getStatus() { return status.get(); }
    public boolean isOn() { return isOn.get(); }
    public double getPreviousCurrent() { return previousCurrent; }
    
    // Setters
    public void setName(String name) { this.name.set(name); }
    public void setLocation(String location) { this.location.set(location); }
    public void setSocketGroup(String socketGroup) { this.socketGroup.set(socketGroup); }
    public void setCurrentDraw(double current) { 
        this.previousCurrent = this.currentDraw.get();
        this.currentDraw.set(current); 
    }
    public void setMaxCurrent(double max) { this.maxCurrent.set(max); }
    public void setPriority(String priority) { this.priority.set(priority); }
    public void setStatus(Status status) { this.status.set(status); }
    public void setOn(boolean on) { this.isOn.set(on); }
    
    // Properties for JavaFX binding
    public StringProperty nameProperty() { return name; }
    public StringProperty locationProperty() { return location; }
    public StringProperty socketGroupProperty() { return socketGroup; }
    public DoubleProperty currentDrawProperty() { return currentDraw; }
    public DoubleProperty maxCurrentProperty() { return maxCurrent; }
    public StringProperty priorityProperty() { return priority; }
    public ObjectProperty<Status> statusProperty() { return status; }
    public BooleanProperty isOnProperty() { return isOn; }
    
    /**
     * Validate if the current reading is valid
     */
    public boolean isValidReading() {
        double current = getCurrentDraw();
        
        if (current <= 0 && isOn()) {
            return false;
        }
        
        if (current > getMaxCurrent()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate power consumption in watts
     */
    public double calculatePower(double voltage) {
        return voltage * getCurrentDraw();
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - %.2fA [%s]", 
            getName(), getLocation(), getCurrentDraw(), getStatus());
    }
}
