package com.smartload.models;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model representing a system alert
 */
public class Alert {
    private final ObjectProperty<LocalDateTime> timestamp;
    private final StringProperty message;
    private final ObjectProperty<Severity> severity;
    private final StringProperty affectedItem;
    private final BooleanProperty acknowledged;
    
    public enum Severity {
        INFO, WARNING, DANGER
    }
    
    public Alert(String message, Severity severity, String affectedItem) {
        this.timestamp = new SimpleObjectProperty<>(LocalDateTime.now());
        this.message = new SimpleStringProperty(message);
        this.severity = new SimpleObjectProperty<>(severity);
        this.affectedItem = new SimpleStringProperty(affectedItem);
        this.acknowledged = new SimpleBooleanProperty(false);
    }
    
    // Getters
    public LocalDateTime getTimestamp() { return timestamp.get(); }
    public String getMessage() { return message.get(); }
    public Severity getSeverity() { return severity.get(); }
    public String getAffectedItem() { return affectedItem.get(); }
    public boolean isAcknowledged() { return acknowledged.get(); }
    
    // Setters
    public void setTimestamp(LocalDateTime time) { this.timestamp.set(time); }
    public void setMessage(String msg) { this.message.set(msg); }
    public void setSeverity(Severity sev) { this.severity.set(sev); }
    public void setAffectedItem(String item) { this.affectedItem.set(item); }
    public void setAcknowledged(boolean ack) { this.acknowledged.set(ack); }
    
    // Properties
    public ObjectProperty<LocalDateTime> timestampProperty() { return timestamp; }
    public StringProperty messageProperty() { return message; }
    public ObjectProperty<Severity> severityProperty() { return severity; }
    public StringProperty affectedItemProperty() { return affectedItem; }
    public BooleanProperty acknowledgedProperty() { return acknowledged; }
    
    /**
     * Get formatted timestamp
     */
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return getTimestamp().format(formatter);
    }
    
    /**
     * Get full timestamp
     */
    public String getFullTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return getTimestamp().format(formatter);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s (%s)", 
            getFormattedTimestamp(), getSeverity(), getMessage(), getAffectedItem());
    }
}
