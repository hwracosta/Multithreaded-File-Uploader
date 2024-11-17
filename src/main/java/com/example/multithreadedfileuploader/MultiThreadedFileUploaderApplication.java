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
@EnableJpaRepositories("com.example.multithreadedfileuploader.repository") // Adjust the package name if needed
@EntityScan("com.example.multithreadedfileuploader.entity") // Adjust the package name if needed
public class MultiThreadedFileUploaderApplication extends Application {

    private static ApplicationContext applicationContext;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean); // Integrate Spring DI
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            primaryStage.setTitle("Multithreaded File Uploader");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
