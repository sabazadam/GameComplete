package com.example.gamecollection.controller;

import com.example.gamecollection.model.Game;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the game form dialog used for adding and editing games.
 */
public class GameFormController {
    @FXML private TextField titleField;
    @FXML private TextField developerField;
    @FXML private TextField publisherField;
    @FXML private TextField genresField;
    @FXML private TextField platformsField;
    @FXML private TextField translatorsField;
    @FXML private TextField steamIdField;
    @FXML private TextField releaseYearField;
    @FXML private TextField playtimeField;
    @FXML private TextField formatField;
    @FXML private TextField languageField;
    @FXML private TextField ratingField;
    @FXML private TextField tagsField;
    @FXML private TextField coverImagePathField;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Pane dropZone;

    private Game game;
    private boolean editMode;
    private boolean saveClicked = false;

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // Set up any initial state
        browseButton.setOnAction(event -> handleBrowseImage());
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        
        // Set up drag and drop for cover image
        setupDragAndDrop();
    }
    
    /**
     * Sets up drag and drop functionality for the cover image.
     */
    private void setupDragAndDrop() {
        // Handle drag over
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && 
                    event.getDragboard().hasFiles() && 
                    isImageFile(event.getDragboard().getFiles().get(0))) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                dropZone.setStyle("-fx-border-color: #4a8eff; -fx-border-style: dashed; -fx-border-width: 2px; -fx-min-height: 100px;");
            }
            event.consume();
        });
        
        // Handle drag exited
        dropZone.setOnDragExited(event -> {
            dropZone.setStyle("-fx-border-color: #cccccc; -fx-border-style: dashed; -fx-border-width: 2px; -fx-min-height: 100px;");
            event.consume();
        });
        
        // Handle drag dropped
        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                if (isImageFile(file)) {
                    coverImagePathField.setText(file.getAbsolutePath());
                    success = true;
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    /**
     * Checks if a file is an image file.
     * 
     * @param file The file to check
     * @return true if the file is an image file, false otherwise
     */
    private boolean isImageFile(File file) {
        if (file == null) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || 
               name.endsWith(".jpeg") || name.endsWith(".gif");
    }

    /**
     * Sets the game to be edited.
     *
     * @param game The game to edit, or null for a new game
     */
    public void setGame(Game game) {
        this.editMode = (game != null);
        this.game = editMode ? game : new Game();
        
        // If in edit mode, populate the fields with the game's data
        if (editMode) {
            populateFields();
        }
    }

    /**
     * Populates the form fields with the game's data.
     */
    private void populateFields() {
        titleField.setText(game.getTitle());
        developerField.setText(game.getDeveloper());
        publisherField.setText(game.getPublisher());
        
        genresField.setText(joinList(game.getGenres()));
        platformsField.setText(joinList(game.getPlatforms()));
        translatorsField.setText(joinList(game.getTranslators()));
        
        steamIdField.setText(game.getSteamId());
        
        if (game.getReleaseYear() != null) {
            releaseYearField.setText(String.valueOf(game.getReleaseYear()));
        }
        
        if (game.getPlaytime() != null) {
            playtimeField.setText(String.valueOf(game.getPlaytime()));
        }
        
        formatField.setText(game.getFormat());
        languageField.setText(game.getLanguage());
        
        if (game.getRating() != null) {
            ratingField.setText(String.valueOf(game.getRating()));
        }
        
        tagsField.setText(joinList(game.getTags()));
        coverImagePathField.setText(game.getCoverImagePath());
    }

    /**
     * Handles browsing for a cover image.
     */
    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) browseButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            coverImagePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    /**
     * Handles saving the game.
     */
    @FXML
    private void handleSave() {
        if (validateInput()) {
            updateGameFromFields();
            saveClicked = true;
            closeDialog();
        }
    }

    /**
     * Handles canceling the dialog.
     */
    @FXML
    private void handleCancel() {
        saveClicked = false;
        closeDialog();
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Validates the user input.
     *
     * @return true if input is valid, false otherwise
     */
    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        
        // Check required fields
        if (isNullOrEmpty(titleField.getText())) {
            errorMessage.append("Title is required.\n");
        }
        
        if (isNullOrEmpty(developerField.getText())) {
            errorMessage.append("Developer is required.\n");
        }
        
        if (isNullOrEmpty(publisherField.getText())) {
            errorMessage.append("Publisher is required.\n");
        }
        
        // Validate numeric fields
        if (!isNullOrEmpty(releaseYearField.getText()) && !isValidInteger(releaseYearField.getText())) {
            errorMessage.append("Release year must be a valid number.\n");
        }
        
        if (!isNullOrEmpty(playtimeField.getText()) && !isValidDouble(playtimeField.getText())) {
            errorMessage.append("Playtime must be a valid number.\n");
        }
        
        if (!isNullOrEmpty(ratingField.getText()) && !isValidDouble(ratingField.getText())) {
            errorMessage.append("Rating must be a valid number.\n");
        }
        
        // Show error message if there are validation errors
        if (errorMessage.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }

    /**
     * Updates the game with values from the fields.
     */
    private void updateGameFromFields() {
        game.setTitle(titleField.getText());
        game.setDeveloper(developerField.getText());
        game.setPublisher(publisherField.getText());
        
        game.setGenres(splitList(genresField.getText()));
        game.setPlatforms(splitList(platformsField.getText()));
        game.setTranslators(splitList(translatorsField.getText()));
        
        game.setSteamId(steamIdField.getText());
        
        if (!isNullOrEmpty(releaseYearField.getText())) {
            game.setReleaseYear(Integer.parseInt(releaseYearField.getText()));
        }
        
        if (!isNullOrEmpty(playtimeField.getText())) {
            game.setPlaytime(Double.parseDouble(playtimeField.getText()));
        }
        
        game.setFormat(formatField.getText());
        game.setLanguage(languageField.getText());
        
        if (!isNullOrEmpty(ratingField.getText())) {
            game.setRating(Double.parseDouble(ratingField.getText()));
        }
        
        game.setTags(splitList(tagsField.getText()));
        game.setCoverImagePath(coverImagePathField.getText());
    }

    /**
     * Returns the game that was edited or created.
     *
     * @return the game
     */
    public Game getGame() {
        return game;
    }

    /**
     * Returns whether the save button was clicked.
     *
     * @return true if save was clicked, false otherwise
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }

    // Helper methods
    
    private String joinList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(", ", list);
    }

    private List<String> splitList(String text) {
        if (isNullOrEmpty(text)) {
            return FXCollections.observableArrayList();
        }
        
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    private boolean isValidInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDouble(String text) {
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}