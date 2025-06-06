module com.example.bomberman {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.bomberman to javafx.fxml;

    exports com.example.bomberman;
    exports com.example.bomberman.test;
    opens com.example.bomberman.test to javafx.fxml;
}