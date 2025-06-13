module com.example.bomberman {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires javafx.graphics;

    opens com.example.bomberman to javafx.fxml;
    opens com.example.bomberman.utils to javafx.fxml;

    exports com.example.bomberman;
    exports com.example.bomberman.utils;
    exports com.example.bomberman.enums;
    opens com.example.bomberman.enums to javafx.fxml;
    exports com.example.bomberman.controller;
    opens com.example.bomberman.controller to javafx.fxml;
    exports com.example.bomberman.models.world;
    opens com.example.bomberman.models.world to javafx.fxml;
    exports com.example.bomberman.service;
    opens com.example.bomberman.service to javafx.fxml;
    exports com.example.bomberman.models.entities;
    opens com.example.bomberman.models.entities to javafx.fxml;
}