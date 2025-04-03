package com.example.gamecollection;

import com.example.gamecollection.model.Game;
import com.example.gamecollection.repository.GameRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HelloController {
    @FXML
    private Label welcomeText;
    
    @FXML
    private TableView<Game> gameTableView;
    
    @FXML
    private TableColumn<Game, String> titleColumn;
    
    @FXML
    private TableColumn<Game, String> developerColumn;
    
    @FXML
    private TableColumn<Game, String> publisherColumn;
    
    @FXML
    private TableColumn<Game, Integer> releaseYearColumn;
    
    @FXML
    private TableColumn<Game, List<String>> platformsColumn;
    
    @FXML
    private TableColumn<Game, Double> ratingColumn;
    
    @FXML
    private Button importButton;
    
    @FXML
    private Button exportButton;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> sortComboBox;
    
    @FXML
    private ComboBox<String> filterCategoryComboBox;
    
    @FXML
    private ScrollPane detailsScrollPane;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label gameCountLabel;
    
    @FXML
    private Label databaseLabel;
    
    private GameRepository gameRepository;
    private ObservableList<Game> gameList = FXCollections.observableArrayList();
    
    private com.example.gamecollection.controller.GameDetailsController detailsController;
    
    // Current search term for highlighting
    private String currentSearchTerm = "";
    
    // Sort options
    private final String SORT_BY_TITLE = "Title";
    private final String SORT_BY_DEVELOPER = "Developer";
    private final String SORT_BY_PUBLISHER = "Publisher";
    private final String SORT_BY_RELEASE_YEAR = "Release Year";
    private final String SORT_BY_RATING = "Rating";
    
    /**
     * Sets the game repository and initializes the controller.
     */
    public void setGameRepository(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        
        // Try to load test data if available
        try {
            String samplePath = getClass().getResource("test-games.json").getFile();
            gameRepository.importGamesFromJson(samplePath);
            welcomeText.setText("Loaded " + gameRepository.getAllGames().size() + " sample games.");
        } catch (Exception e) {
            // If test-games.json is not available, try sample-games.json
            try {
                String samplePath = getClass().getResource("sample-games.json").getFile();
                gameRepository.importGamesFromJson(samplePath);
                welcomeText.setText("Loaded " + gameRepository.getAllGames().size() + " sample games.");
            } catch (Exception ex) {
                // Silent fail if no sample data is available
            }
        }
        
        // Initialize with data
        refreshGameList();
    }
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // Configure table columns
        titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setCellFactory(createHighlightingCellFactory(titleColumn));
        
        developerColumn = new TableColumn<>("Developer");
        developerColumn.setCellValueFactory(new PropertyValueFactory<>("developer"));
        developerColumn.setCellFactory(createHighlightingCellFactory(developerColumn));
        
        publisherColumn = new TableColumn<>("Publisher");
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        publisherColumn.setCellFactory(createHighlightingCellFactory(publisherColumn));
        
        releaseYearColumn = new TableColumn<>("Year");
        releaseYearColumn.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
        
        platformsColumn = new TableColumn<>("Platforms");
        platformsColumn.setCellValueFactory(new PropertyValueFactory<>("platforms"));
        platformsColumn.setCellFactory(column -> {
            return new TableCell<Game, List<String>>() {
                @Override
                protected void updateItem(List<String> platforms, boolean empty) {
                    super.updateItem(platforms, empty);
                    if (empty || platforms == null) {
                        setText(null);
                    } else {
                        setText(String.join(", ", platforms));
                    }
                }
            };
        });
        
        ratingColumn = new TableColumn<>("Rating");
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        
        // Add columns to table
        gameTableView.getColumns().setAll(
            titleColumn, developerColumn, publisherColumn, 
            releaseYearColumn, platformsColumn, ratingColumn
        );
        
        // Bind table to data
        gameTableView.setItems(gameList);
        
        // Enable sorting
        titleColumn.setSortable(true);
        developerColumn.setSortable(true);
        publisherColumn.setSortable(true);
        releaseYearColumn.setSortable(true);
        platformsColumn.setSortable(true);
        ratingColumn.setSortable(true);
        
        // Initialize status bar
        statusLabel.setText("Status: Ready");
        databaseLabel.setText("Local Database");
        updateStatusBar();
        
        // Initialize sort combo box
        sortComboBox.getItems().addAll(
            SORT_BY_TITLE,
            SORT_BY_DEVELOPER,
            SORT_BY_PUBLISHER,
            SORT_BY_RELEASE_YEAR,
            SORT_BY_RATING
        );
        
        // Add listener to sort combo box
        sortComboBox.setOnAction(event -> {
            String selectedSort = sortComboBox.getValue();
            if (selectedSort != null) {
                sortGames(selectedSort);
            }
        });
        
        // Initialize filter category combo box
        filterCategoryComboBox.getItems().addAll(
            "All Games",
            "Platform",
            "Genre",
            "Developer",
            "Publisher",
            "Release Year",
            "Rating"
        );
        filterCategoryComboBox.setValue("All Games");
        filterCategoryComboBox.setOnAction(event -> showFilterOptions());
        
        // Load the game details view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game-details.fxml"));
            Parent detailsView = loader.load();
            detailsController = loader.getController();
            detailsController.setMainController(this);
            detailsScrollPane.setContent(detailsView);
        } catch (IOException e) {
            showErrorAlert("Error", "Failed to load game details view", e.getMessage());
        }
        
        // Add selection listener to table
        gameTableView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> detailsController.setGame(newValue)
        );
        
        // Context menu for table
        gameTableView.setRowFactory(tv -> {
            TableRow<Game> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem editItem = new MenuItem("Edit Game");
            editItem.setOnAction(event -> handleEditGame(row.getItem()));
            
            MenuItem deleteItem = new MenuItem("Delete Game");
            deleteItem.setOnAction(event -> handleDeleteGame(row.getItem()));
            
            contextMenu.getItems().addAll(editItem, deleteItem);
            
            // Set context menu only when row is not empty
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );
            
            return row;
        });
    }

    /**
     * Refreshes the game list from the repository.
     */
    private void refreshGameList() {
        if (gameRepository != null) {
            gameList.clear();
            gameList.addAll(gameRepository.getAllGames());
            
            // Clear search term and refresh table
            currentSearchTerm = "";
            gameTableView.refresh();
            
            // Update status bar
            updateStatusBar();
        }
    }
    
    /**
     * Updates the status bar with current information.
     */
    private void updateStatusBar() {
        if (gameRepository != null) {
            int totalGames = gameRepository.getAllGames().size();
            int displayedGames = gameList.size();
            
            gameCountLabel.setText("Total Games: " + totalGames + 
                    (displayedGames != totalGames ? " (Showing " + displayedGames + ")" : ""));
            
            if (displayedGames == 0 && totalGames > 0) {
                statusLabel.setText("Status: No games match the current filter");
            } else if (displayedGames < totalGames) {
                statusLabel.setText("Status: Filtered view");
            } else {
                statusLabel.setText("Status: Ready");
            }
        }
    }
    
    /**
     * Shows the appropriate filter options based on the selected filter category.
     */
    private void showFilterOptions() {
        String selectedCategory = filterCategoryComboBox.getValue();
        
        if ("All Games".equals(selectedCategory)) {
            // Reset any filtering
            refreshGameList();
            return;
        }
        
        // Show a dialog to get filter value
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Filter by " + selectedCategory);
        dialog.setHeaderText("Enter value to filter by " + selectedCategory);
        dialog.setContentText("Value:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> {
            filterGameList(selectedCategory, value);
        });
    }
    
    /**
     * Filters the game list based on the selected category and value.
     */
    private void filterGameList(String category, String value) {
        if (gameRepository == null) return;
        
        List<Game> filteredGames = gameRepository.getAllGames();
        
        switch (category) {
            case "Platform":
                filteredGames = filteredGames.stream()
                        .filter(game -> game.getPlatforms() != null && 
                                game.getPlatforms().stream().anyMatch(p -> p.toLowerCase().contains(value.toLowerCase())))
                        .collect(Collectors.toList());
                break;
            case "Genre":
                filteredGames = filteredGames.stream()
                        .filter(game -> game.getGenres() != null && 
                                game.getGenres().stream().anyMatch(g -> g.toLowerCase().contains(value.toLowerCase())))
                        .collect(Collectors.toList());
                break;
            case "Developer":
                filteredGames = filteredGames.stream()
                        .filter(game -> game.getDeveloper() != null && 
                                game.getDeveloper().toLowerCase().contains(value.toLowerCase()))
                        .collect(Collectors.toList());
                break;
            case "Publisher":
                filteredGames = filteredGames.stream()
                        .filter(game -> game.getPublisher() != null && 
                                game.getPublisher().toLowerCase().contains(value.toLowerCase()))
                        .collect(Collectors.toList());
                break;
            case "Release Year":
                try {
                    int year = Integer.parseInt(value);
                    filteredGames = filteredGames.stream()
                            .filter(game -> game.getReleaseYear() != null && game.getReleaseYear() == year)
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    showErrorAlert("Invalid Input", "Please enter a valid year.", e.getMessage());
                    return;
                }
                break;
            case "Rating":
                try {
                    double rating = Double.parseDouble(value);
                    filteredGames = filteredGames.stream()
                            .filter(game -> game.getRating() != null && game.getRating() >= rating)
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    showErrorAlert("Invalid Input", "Please enter a valid rating.", e.getMessage());
                    return;
                }
                break;
        }
        
        gameList.clear();
        gameList.addAll(filteredGames);
        gameTableView.refresh();
        updateStatusBar();
        
        // Show message in status bar
        if (filteredGames.isEmpty()) {
            statusLabel.setText("Status: No games match the filter criteria");
        } else {
            statusLabel.setText("Status: Filtered by " + category + ": " + value);
        }
    }
    
    /**
     * Handles importing games from a JSON file.
     */
    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Game Collection");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        
        File file = fileChooser.showOpenDialog(importButton.getScene().getWindow());
        if (file != null) {
            try {
                List<Game> importedGames = gameRepository.importGamesFromJson(file.getAbsolutePath());
                welcomeText.setText("Imported " + importedGames.size() + " games.");
                refreshGameList();
            } catch (IOException e) {
                showErrorAlert("Import Error", "Could not import games", e.getMessage());
            }
        }
    }
    
    /**
     * Handles exporting games to a JSON file.
     */
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game Collection");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        
        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (file != null) {
            try {
                gameRepository.exportGamesToJson(file.getAbsolutePath());
                welcomeText.setText("Exported " + gameRepository.getAllGames().size() + " games.");
            } catch (IOException e) {
                showErrorAlert("Export Error", "Could not export games", e.getMessage());
            }
        }
    }
    
    /**
     * Handles the search action.
     */
    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        currentSearchTerm = (query != null) ? query.toLowerCase().trim() : "";
        
        if (!currentSearchTerm.isEmpty()) {
            List<Game> results = gameRepository.searchGames(currentSearchTerm);
            gameList.clear();
            gameList.addAll(results);
            welcomeText.setText("Found " + results.size() + " games matching '" + currentSearchTerm + "'");
            statusLabel.setText("Status: Searched for '" + currentSearchTerm + "'");
            
            // Refresh the table to update highlighting
            gameTableView.refresh();
            updateStatusBar();
        } else {
            refreshGameList();
            welcomeText.setText("Showing all games");
            statusLabel.setText("Status: Ready");
        }
    }
    
    /**
     * Creates a cell factory that highlights text matching the search term.
     * 
     * @param <T> The type of the cell content
     * @param column The table column
     * @return A cell factory with highlighting functionality
     */
    private <T> Callback<TableColumn<Game, T>, TableCell<Game, T>> createHighlightingCellFactory(TableColumn<Game, T> column) {
        return new Callback<TableColumn<Game, T>, TableCell<Game, T>>() {
            @Override
            public TableCell<Game, T> call(TableColumn<Game, T> col) {
                return new TableCell<Game, T>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            String text = item.toString();
                            
                            if (currentSearchTerm.isEmpty() || text.toLowerCase().indexOf(currentSearchTerm.toLowerCase()) < 0) {
                                // No highlighting
                                setText(text);
                                setStyle("");
                            } else {
                                // Create a highlighted text
                                setText(text);
                                setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                            }
                        }
                    }
                };
            }
        };
    }
    
    /**
     * Handles editing the selected game from the menu.
     */
    @FXML
    private void handleEditSelected() {
        Game selectedGame = gameTableView.getSelectionModel().getSelectedItem();
        if (selectedGame == null) {
            showErrorAlert("Edit Game", "No game selected", 
                         "Please select a game to edit from the table.");
            return;
        }
        
        handleEditGame(selectedGame);
    }
    
    /**
     * Handles deleting the selected game from the menu.
     */
    @FXML
    private void handleDeleteSelected() {
        Game selectedGame = gameTableView.getSelectionModel().getSelectedItem();
        if (selectedGame == null) {
            showErrorAlert("Delete Game", "No game selected", 
                         "Please select a game to delete from the table.");
            return;
        }
        
        handleDeleteGame(selectedGame);
    }
    
    /**
     * Handles adding a new game.
     */
    @FXML
    private void handleAddGame() {
        try {
            // Load the game form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game-form.fxml"));
            Parent root = loader.load();
            
            // Get the controller
            com.example.gamecollection.controller.GameFormController controller = loader.getController();
            controller.setGame(null); // null indicates a new game
            
            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Game");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(gameTableView.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // If save was clicked, add the game to the repository
            if (controller.isSaveClicked()) {
                Game newGame = controller.getGame();
                boolean success = gameRepository.addGame(newGame);
                
                if (success) {
                    refreshGameList();
                    welcomeText.setText("Game '" + newGame.getTitle() + "' added successfully.");
                } else {
                    showErrorAlert("Add Game", "Failed to add game", 
                                 "The game could not be added. It might already exist in the collection.");
                }
            }
        } catch (IOException e) {
            showErrorAlert("Add Game", "Error opening game form", e.getMessage());
        }
    }
    
    /**
     * Handles editing a game.
     */
    public void handleEditGame(Game game) {
        if (game == null) {
            showErrorAlert("Edit Game", "No game selected", 
                         "Please select a game to edit.");
            return;
        }
        
        try {
            // Load the game form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game-form.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the game
            com.example.gamecollection.controller.GameFormController controller = loader.getController();
            controller.setGame(game);
            
            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Game");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(gameTableView.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // If save was clicked, update the game in the repository
            if (controller.isSaveClicked()) {
                Game updatedGame = controller.getGame();
                boolean success = gameRepository.updateGame(updatedGame);
                
                if (success) {
                    refreshGameList();
                    welcomeText.setText("Game '" + updatedGame.getTitle() + "' updated successfully.");
                } else {
                    showErrorAlert("Edit Game", "Failed to update game", 
                                 "The game could not be updated. It might have been removed from the collection.");
                }
            }
        } catch (IOException e) {
            showErrorAlert("Edit Game", "Error opening game form", e.getMessage());
        }
    }
    
    /**
     * Handles deleting a game.
     */
    public void handleDeleteGame(Game game) {
        if (game == null) {
            showErrorAlert("Delete Game", "No game selected", 
                         "Please select a game to delete.");
            return;
        }
        
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Game");
        alert.setHeaderText("Delete Game: " + game.getTitle());
        alert.setContentText("Are you sure you want to delete this game from your collection?");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        // If user confirmed deletion, delete the game
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = gameRepository.deleteGame(game);
            
            if (success) {
                refreshGameList();
                welcomeText.setText("Game '" + game.getTitle() + "' deleted successfully.");
            } else {
                showErrorAlert("Delete Game", "Failed to delete game", 
                             "The game could not be deleted. It might have already been removed from the collection.");
            }
        }
    }
    
    /**
     * Shows an error alert dialog.
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert dialog.
     */
    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Handles displaying the help contents.
     */
    @FXML
    private void handleHelpContents() {
        try {
            // Load the help view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("help-view.fxml"));
            Parent root = loader.load();
            
            // Get the controller
            com.example.gamecollection.controller.HelpController controller = loader.getController();
            
            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Game Collection Help");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(welcomeText.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            
            // Load the help contents
            URL helpResource = getClass().getResource("help.html");
            controller.loadHelp(helpResource);
            
            // Show the dialog
            dialogStage.show();
            
        } catch (IOException e) {
            showErrorAlert("Help", "Error displaying help", e.getMessage());
        }
    }
    
    /**
     * Handles displaying the about dialog.
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Game Collection");
        alert.setHeaderText("Game Collection Manager");
        alert.setContentText(
            "Version 1.0\n\n" +
            "A simple application for managing your video game collection.\n\n" +
            "Created with JavaFX and JSON technology.\n\n" +
            "© 2025 Example"
        );
        
        alert.showAndWait();
    }
    
    /**
     * Sorts the games list based on the selected criterion.
     *
     * @param sortBy The criterion to sort by
     */
    private void sortGames(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return;
        }
        
        switch (sortBy) {
            case SORT_BY_TITLE:
                handleSortByTitle();
                break;
            case SORT_BY_DEVELOPER:
                handleSortByDeveloper();
                break;
            case SORT_BY_PUBLISHER:
                handleSortByPublisher();
                break;
            case SORT_BY_RELEASE_YEAR:
                handleSortByReleaseYear();
                break;
            case SORT_BY_RATING:
                handleSortByRating();
                break;
        }
    }
    
    /**
     * Sorts games by title.
     */
    @FXML
    private void handleSortByTitle() {
        gameTableView.getSortOrder().clear();
        gameTableView.getSortOrder().add(titleColumn);
        titleColumn.setSortType(TableColumn.SortType.ASCENDING);
        gameTableView.sort();
        welcomeText.setText("Sorted by Title");
    }
    
    /**
     * Sorts games by developer.
     */
    @FXML
    private void handleSortByDeveloper() {
        gameTableView.getSortOrder().clear();
        gameTableView.getSortOrder().add(developerColumn);
        developerColumn.setSortType(TableColumn.SortType.ASCENDING);
        gameTableView.sort();
        welcomeText.setText("Sorted by Developer");
    }
    
    /**
     * Sorts games by publisher.
     */
    @FXML
    private void handleSortByPublisher() {
        gameTableView.getSortOrder().clear();
        gameTableView.getSortOrder().add(publisherColumn);
        publisherColumn.setSortType(TableColumn.SortType.ASCENDING);
        gameTableView.sort();
        welcomeText.setText("Sorted by Publisher");
    }
    
    /**
     * Sorts games by release year.
     */
    @FXML
    private void handleSortByReleaseYear() {
        gameTableView.getSortOrder().clear();
        gameTableView.getSortOrder().add(releaseYearColumn);
        releaseYearColumn.setSortType(TableColumn.SortType.DESCENDING); // Newest first
        gameTableView.sort();
        welcomeText.setText("Sorted by Release Year (newest first)");
    }
    
    /**
     * Sorts games by rating.
     */
    @FXML
    private void handleSortByRating() {
        gameTableView.getSortOrder().clear();
        gameTableView.getSortOrder().add(ratingColumn);
        ratingColumn.setSortType(TableColumn.SortType.DESCENDING); // Highest first
        gameTableView.sort();
        welcomeText.setText("Sorted by Rating (highest first)");
    }
    
    /**
     * Handles keyboard shortcuts.
     *
     * @param event The key event
     */
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        // Define shortcuts
        KeyCombination ctrlN = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        KeyCombination ctrlO = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
        KeyCombination ctrlS = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        KeyCombination ctrlF = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        KeyCombination ctrlH = new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN);
        KeyCombination deleteKey = new KeyCodeCombination(KeyCode.DELETE);
        
        // Check which shortcut was pressed
        if (ctrlN.match(event)) {
            // Ctrl+N: Add new game
            handleAddGame();
            event.consume();
        } else if (ctrlO.match(event)) {
            // Ctrl+O: Import games
            handleImport();
            event.consume();
        } else if (ctrlS.match(event)) {
            // Ctrl+S: Export games
            handleExport();
            event.consume();
        } else if (ctrlF.match(event)) {
            // Ctrl+F: Focus search field
            searchField.requestFocus();
            event.consume();
        } else if (ctrlH.match(event)) {
            // Ctrl+H: Show help
            handleHelpContents();
            event.consume();
        } else if (deleteKey.match(event)) {
            // Delete: Delete selected game
            Game selectedGame = gameTableView.getSelectionModel().getSelectedItem();
            if (selectedGame != null) {
                handleDeleteGame(selectedGame);
                event.consume();
            }
        }
    }
    
    /**
     * Handles exiting the application.
     */
    @FXML
    private void handleExit() {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Exit Game Collection Manager");
        alert.setContentText("Are you sure you want to exit? Any unsaved changes will be lost.");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        // If user confirmed exit, close the application
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) welcomeText.getScene().getWindow();
            stage.close();
        }
    }
}