module com.example.gamecollection {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    opens com.example.gamecollection to javafx.fxml;
    opens com.example.gamecollection.controller to javafx.fxml;
    opens com.example.gamecollection.model to javafx.fxml, com.fasterxml.jackson.databind;
    
    exports com.example.gamecollection;
    exports com.example.gamecollection.model;
    exports com.example.gamecollection.repository;
    exports com.example.gamecollection.controller;
}