package com.example.bomberman;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Contrôleur principal du jeu (version simplifiée)
 */
public class MainController implements Initializable {
    @FXML
    private Canvas gameCanvas;

    // Modèle du jeu
    private Game game;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    // Gestion des entrées
    private Set<KeyCode> pressedKeys;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = gameCanvas.getGraphicsContext2D();
        pressedKeys = new HashSet<>();
        game = new Game();

        // Focus sur le canvas pour recevoir les événements clavier
        Platform.runLater(() -> {
            gameCanvas.setFocusTraversable(true);
            gameCanvas.requestFocus();
        });

        startGameLoop();
    }

    /**
     * Gestion des touches pressées
     */
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        pressedKeys.add(event.getCode());
        game.handleKeyPressed(event.getCode());
    }

    /**
     * Gestion des touches relâchées
     */
    @FXML
    private void handleKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
        game.handleKeyReleased(event.getCode());
    }

    /**
     * Démarre la boucle de jeu
     */
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        gameLoop.start();
    }

    /**
     * Met à jour la logique du jeu
     */
    private void update() {
        game.update();
    }

    /**
     * Dessine le jeu (méthode render déplacée ici)
     */
    private void render() {
        // Effacer le canvas
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Récupérer les éléments du jeu
        GameBoard board = game.getBoard();
        Player player1 = game.getPlayer1();
        Player player2 = game.getPlayer2();

        // Taille des cases
        int tileSize = 40;

        // Dessiner le plateau
        if (board != null) {
            board.render(gc, tileSize);
        }

        // Dessiner les joueurs
        if (player1 != null && player1.isAlive()) {
            player1.render(gc, tileSize);
        }
        if (player2 != null && player2.isAlive()) {
            player2.render(gc, tileSize);
        }

        // Dessiner les bombes
        for (Bomb bomb : game.getBombs()) {
            bomb.render(gc, tileSize);
        }

        // Afficher les informations des joueurs
        renderUI();
    }

    /**
     * Affiche l'interface utilisateur
     */
    private void renderUI() {
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", 14));

        if (game.getPlayer1() != null) {
            String player1Info = "Joueur 1 (ZQSD + A) - Vies: " + game.getPlayer1().getLives();
            gc.fillText(player1Info, 10, 20);
        }

        if (game.getPlayer2() != null) {
            String player2Info = "Joueur 2 (Flèches + Espace) - Vies: " + game.getPlayer2().getLives();
            gc.fillText(player2Info, 10, 40);
        }

        // Afficher le score si disponible
        try {
            String scoreInfo = "Score J1: " + game.getPlayer1Score() + " | Score J2: " + game.getPlayer2Score();
            gc.fillText(scoreInfo, 10, 60);
        } catch (Exception e) {
            // Si les méthodes de score n'existent pas encore, on ignore
        }
    }
}