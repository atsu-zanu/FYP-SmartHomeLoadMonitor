package com.smartload;

import com.smartload.controllers.MainController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Application Entry Point
 * Smart Home Load Monitor for Ghanaian Homes
 */
public class SmartLoadApp extends Application {
    private static final String APP_TITLE = "Smart Home Load Monitor";
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting Smart Home Load Monitor...");
            
            // Create UI
            MainController controller = new MainController();
            Parent root = controller.createView();
            
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            // Load CSS if available
            try {
                String css = getClass().getResource("/css/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("CSS file not found, using default styling");
            }
            
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);
            
            // Handle window close
            primaryStage.setOnCloseRequest(event -> {
                System.out.println("Application closing...");
                controller.shutdown();
            });
            
            primaryStage.show();
            System.out.println("Application started successfully!");
            
        } catch (Exception e) {
            System.err.println("Failed to start application:");
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Launching JavaFX application...");
        launch(args);
    }
}
