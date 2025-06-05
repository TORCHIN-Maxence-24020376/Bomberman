package com.example.bomberman;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;


import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    @FXML
    private Canvas gameCanvas;

    private Game game;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = gameCanvas.getGraphicsContext2D();
        game = new Game();

        // Focus sur le canvas pour recevoir les événements clavier
        Platform.runLater(() -> {
            gameCanvas.setFocusTraversable(true);
            gameCanvas.requestFocus();
        });

        startGameLoop();
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        game.handleKeyPressed(event.getCode());
    }

    @FXML
    private void handleKeyReleased(KeyEvent event) {
        game.handleKeyReleased(event.getCode());
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                game.update();
                render();
            }
        };
        gameLoop.start();
    }

    private void render() {
        // Effacer le canvas
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Dessiner le plateau de jeu
        game.render(gc);
    }
}