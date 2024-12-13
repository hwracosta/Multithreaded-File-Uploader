package com.example.multithreadedfileuploader;

import com.example.multithreadedfileuploader.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.multithreadedfileuploader.repository") // Ensure correct package for JPA repositories
@EntityScan("com.example.multithreadedfileuploader.entity") // Ensure correct package for entities
public class MultiThreadedFileUploaderApplication extends Application {

    private static ApplicationContext applicationContext;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML file and set up Spring controller factory
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean); // Enable Spring Dependency Injection

            // Load the Scene
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            primaryStage.setTitle("Multithreaded File Uploader");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error initializing JavaFX application:");
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        try {
            // Initialize Spring ApplicationContext
            applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        } catch (Exception e) {
            System.err.println("Error initializing Spring context:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Launch JavaFX application
        launch(args);
    }
}
