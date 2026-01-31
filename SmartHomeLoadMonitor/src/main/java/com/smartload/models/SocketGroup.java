package com.smartload.models;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model representing a socket group (circuit) in the home
 */
public class SocketGroup {
    private final StringProperty name;
    private final DoubleProperty totalCurrent;
    private final DoubleProperty ratedCapacity;
    private final ObjectProperty<Status> status;
    private final ObservableList<Appliance> appliances;
    
    public enum Status {
        OK, WARNING, DANGER
    }
    
    public SocketGroup(String name, double ratedCapacity) {
        this.name = new SimpleStringProperty(name);
        this.totalCurrent = new SimpleDoubleProperty(0.0);
        this.ratedCapacity = new SimpleDoubleProperty(ratedCapacity);
        this.status = new SimpleObjectProperty<>(Status.OK);
        this.appliances = FXCollections.observableArrayList();
    }
    
    // Getters
    public String getName() { return name.get(); }
    public double getTotalCurrent() { return totalCurrent.get(); }
    public double getRatedCapacity() { return ratedCapacity.get(); }
    public Status getStatus() { return status.get(); }
    public ObservableList<Appliance> getAppliances() { return appliances; }
    
    // Setters
    public void setName(String name) { this.name.set(name); }
    public void setTotalCurrent(double current) { this.totalCurrent.set(current); }
    public void setRatedCapacity(double capacity) { this.ratedCapacity.set(capacity); }
    public void setStatus(Status status) { this.status.set(status); }
    
    // Properties
    public StringProperty nameProperty() { return name; }
    public DoubleProperty totalCurrentProperty() { return totalCurrent; }
    public DoubleProperty ratedCapacityProperty() { return ratedCapacity; }
    public ObjectProperty<Status> statusProperty() { return status; }
    
    /**
     * Calculate total current from all valid appliances
     */
    public void calculateTotalCurrent() {
        double total = appliances.stream()
            .filter(Appliance::isValidReading)
            .mapToDouble(Appliance::getCurrentDraw)
            .sum();
        setTotalCurrent(total);
    }
    
    /**
     * Update status based on current load
     */
    public void updateStatus() {
        double current = getTotalCurrent();
        double capacity = getRatedCapacity();
        
        if (current < capacity * 0.77) {
            setStatus(Status.OK);
        } else if (current <= capacity) {
            setStatus(Status.WARNING);
        } else {
            setStatus(Status.DANGER);
        }
    }
    
    /**
     * Add an appliance to this socket group
     */
    public void addAppliance(Appliance appliance) {
        appliances.add(appliance);
        appliance.setSocketGroup(getName());
    }
    
    /**
     * Get the appliance drawing the most current
     */
    public Appliance getHighestCurrentAppliance() {
        return appliances.stream()
            .filter(Appliance::isValidReading)
            .max((a1, a2) -> Double.compare(a1.getCurrentDraw(), a2.getCurrentDraw()))
            .orElse(null);
    }
    
    /**
     * Get load percentage
     */
    public double getLoadPercentage() {
        return (getTotalCurrent() / getRatedCapacity()) * 100.0;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %.2f/%.2fA (%.1f%%) [%s]", 
            getName(), getTotalCurrent(), getRatedCapacity(), 
            getLoadPercentage(), getStatus());
    }
}
