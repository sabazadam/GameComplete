package com.example.gamecollection.controller;

import com.example.gamecollection.HelloController;
import com.example.gamecollection.model.Game;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for displaying detailed information about a selected game.
 */
public class GameDetailsController {
    @FXML private ImageView coverImageView;
    @FXML private Label titleLabel;
    @FXML private Label developerLabel;
    @FXML private Label publisherLabel;
    @FXML private Label releaseYearLabel;
    @FXML private Label ratingLabel;
    @FXML private Label formatLabel;
    @FXML private Label languageLabel;
    @FXML private Label playtimeLabel;
    @FXML private Label steamIdLabel;
    
    @FXML private ListView<String> genresListView;
    @FXML private ListView<String> platformsListView;
    @FXML private ListView<String> tagsListView;
    @FXML private ListView<String> translatorsListView;
    
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    
    private Game currentGame;
    private HelloController mainController;
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // No action needed for initialization
    }
    
    /**
     * Sets the main controller.
     *
     * @param mainController The main controller
     */
    public void setMainController(HelloController mainController) {
        this.mainController = mainController;
    }
    
    /**
     * Displays the details of the specified game.
     *
     * @param game The game to display
     */
    public void setGame(Game game) {
        this.currentGame = game;
        
        if (game != null) {
            // Set basic information
            titleLabel.setText(game.getTitle());
            developerLabel.setText(game.getDeveloper());
            publisherLabel.setText(game.getPublisher());
            releaseYearLabel.setText(game.getReleaseYear() != null ? game.getReleaseYear().toString() : "Unknown");
            ratingLabel.setText(game.getRating() != null ? game.getRating().toString() : "Not rated");
            formatLabel.setText(game.getFormat() != null ? game.getFormat() : "Unknown");
            languageLabel.setText(game.getLanguage() != null ? game.getLanguage() : "Unknown");
            playtimeLabel.setText(game.getPlaytime() != null ? game.getPlaytime() + " hours" : "Unknown");
            steamIdLabel.setText(game.getSteamId() != null ? game.getSteamId() : "N/A");
            
            // Set lists
            genresListView.setItems(FXCollections.observableArrayList(
                    game.getGenres() != null ? game.getGenres() : new ArrayList<>()));
            
            platformsListView.setItems(FXCollections.observableArrayList(
                    game.getPlatforms() != null ? game.getPlatforms() : new ArrayList<>()));
            
            tagsListView.setItems(FXCollections.observableArrayList(
                    game.getTags() != null ? game.getTags() : new ArrayList<>()));
            
            translatorsListView.setItems(FXCollections.observableArrayList(
                    game.getTranslators() != null ? game.getTranslators() : new ArrayList<>()));
            
            // Load cover image if available
            if (game.getCoverImagePath() != null && !game.getCoverImagePath().isEmpty()) {
                try {
                    File imageFile = new File(game.getCoverImagePath());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        coverImageView.setImage(image);
                    } else {
                        // Clear the image
                        coverImageView.setImage(null);
                    }
                } catch (Exception e) {
                    // Clear the image on error
                    coverImageView.setImage(null);
                }
            } else {
                // Clear the image
                coverImageView.setImage(null);
            }
            
            // Enable buttons
            editButton.setDisable(false);
            deleteButton.setDisable(false);
        } else {
            // Clear all fields if game is null
            titleLabel.setText("");
            developerLabel.setText("");
            publisherLabel.setText("");
            releaseYearLabel.setText("");
            ratingLabel.setText("");
            formatLabel.setText("");
            languageLabel.setText("");
            playtimeLabel.setText("");
            steamIdLabel.setText("");
            
            genresListView.setItems(FXCollections.observableArrayList());
            platformsListView.setItems(FXCollections.observableArrayList());
            tagsListView.setItems(FXCollections.observableArrayList());
            translatorsListView.setItems(FXCollections.observableArrayList());
            
            coverImageView.setImage(null);
            
            // Disable buttons
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }
    
    /**
     * Handles editing the current game.
     */
    @FXML
    private void handleEditGame() {
        if (mainController != null && currentGame != null) {
            mainController.handleEditGame(currentGame);
        }
    }
    
    /**
     * Handles deleting the current game.
     */
    @FXML
    private void handleDeleteGame() {
        if (mainController != null && currentGame != null) {
            mainController.handleDeleteGame(currentGame);
        }
    }
}