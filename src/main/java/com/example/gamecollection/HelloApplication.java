package com.example.gamecollection;

import com.example.gamecollection.repository.GameRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private GameRepository gameRepository;
    
    @Override
    public void init() {
        // Initialize the game repository
        gameRepository = new GameRepository();
    }
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        
        // Apply BootstrapFX styles to the scene
        scene.getStylesheets().add(org.kordamp.bootstrapfx.BootstrapFX.bootstrapFXStylesheet());
        
        // Get controller and pass repository to it
        HelloController controller = fxmlLoader.getController();
        controller.setGameRepository(gameRepository);
        
        stage.setTitle("Game Collection");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}