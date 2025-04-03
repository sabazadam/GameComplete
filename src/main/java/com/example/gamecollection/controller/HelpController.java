package com.example.gamecollection.controller;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Controller for the help window.
 */
public class HelpController {
    @FXML
    private WebView webView;
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // No initialization needed
    }
    
    /**
     * Loads the help content from the specified HTML file.
     *
     * @param helpResource The URL of the help HTML file
     */
    public void loadHelp(URL helpResource) {
        if (helpResource != null) {
            webView.getEngine().load(helpResource.toExternalForm());
        }
    }
    
    /**
     * Closes the help window.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) webView.getScene().getWindow();
        stage.close();
    }
}