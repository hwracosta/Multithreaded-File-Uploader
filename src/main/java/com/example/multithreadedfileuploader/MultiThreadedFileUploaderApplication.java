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

/**
 * The main entry point for the Multithreaded File Uploader application.
 *
 * <p>This class initializes the JavaFX application and integrates it with the
 * Spring framework for dependency injection, enabling the use of Spring-managed
 * beans and configurations within a JavaFX application.</p>
 *
 * <h2>Usage</h2>
 * <ul>
 *   <li>Initializes the Spring ApplicationContext to manage application components.</li>
 *   <li>Loads the JavaFX scene from an FXML file and sets up the primary stage.</li>
 * </ul>
 *
 * <h2>Important Methods</h2>
 * <ul>
 *   <li><b>main</b>: Launches the JavaFX application.</li>
 *   <li><b>start</b>: Configures and displays the primary JavaFX stage.</li>
 *   <li><b>init</b>: Initializes the Spring ApplicationContext.</li>
 * </ul>
 */
@SpringBootApplication
@EnableJpaRepositories("com.example.multithreadedfileuploader.repository") // Enable JPA repositories for database interactions
@EntityScan("com.example.multithreadedfileuploader.entity") // Specify the package containing JPA entities
public class MultiThreadedFileUploaderApplication extends Application {

    private static ApplicationContext applicationContext;

    /**
     * The main method, serving as the entry point for the application.
     *
     * @param args Command-line arguments passed during application startup.
     */
    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }

    /**
     * Initializes the Spring ApplicationContext before the JavaFX application starts.
     *
     * <p>This method sets up the Spring context by loading configurations from
     * the {@link AppConfig} class. The ApplicationContext enables the use of
     * Spring-managed beans within the JavaFX application.</p>
     */
    @Override
    public void init() {
        try {
            applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        } catch (Exception e) {
            System.err.println("Error initializing Spring context:");
            e.printStackTrace();
        }
    }

    /**
     * Configures and displays the primary JavaFX stage.
     *
     * <p>This method loads the FXML layout for the main application view and
     * integrates it with Spring's dependency injection system, ensuring that
     * controllers and other components are managed by Spring.</p>
     *
     * @param primaryStage The primary stage for the JavaFX application.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML layout and configure Spring dependency injection
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            // Set up the JavaFX scene
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            primaryStage.setTitle("Multithreaded File Uploader");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error initializing JavaFX application:");
            e.printStackTrace();
        }
    }
}
