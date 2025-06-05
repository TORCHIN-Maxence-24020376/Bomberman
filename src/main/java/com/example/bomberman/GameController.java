package com.example.bomberman;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Contrôleur de jeu complet avec interface améliorée
 */
public class GameController implements Initializable {

    @FXML private Canvas gameCanvas;
    @FXML private VBox gameInfoPanel;
    @FXML private Label player1InfoLabel;
    @FXML private Label player2InfoLabel;
    @FXML private Label gameStatusLabel;
    @FXML private Button pauseButton;
    @FXML private Button menuButton;
    @FXML private ProgressBar player1HealthBar;
    @FXML private ProgressBar player2HealthBar;
    @FXML private HBox player1PowerUpsBox;
    @FXML private HBox player2PowerUpsBox;

    // Modèle du jeu
    private Game game;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    // Gestion des entrées
    private Set<KeyCode> pressedKeys;

    // Utilitaires
    private SoundManager soundManager;
    private ProfileManager profileManager;

    // Configuration
    private static final int TILE_SIZE = 40;

    // État du jeu
    private boolean gameRunning;
    private long gameStartTime;
    private long pauseStartTime;
    private long totalPauseTime;
    private boolean isPaused = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = gameCanvas.getGraphicsContext2D();
        pressedKeys = new HashSet<>();
        soundManager = SoundManager.getInstance();
        profileManager = ProfileManager.getInstance();

        initializeGame();
        setupEventHandlers();

        // Focus sur le canvas pour recevoir les événements clavier
        Platform.runLater(() -> {
            gameCanvas.setFocusTraversable(true);
            gameCanvas.requestFocus();
        });

        startGameLoop();
    }

    /**
     * Initialise une nouvelle partie
     */
    private void initializeGame() {
        game = new Game();
        gameRunning = true;
        gameStartTime = System.currentTimeMillis();
        totalPauseTime = 0;
        isPaused = false;

        updateUI();

        // Jouer la musique de jeu
        soundManager.playBackgroundMusic("/sounds/game_music.mp3");
    }

    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        if (pauseButton != null) {
            pauseButton.setOnAction(e -> togglePause());
        }
        if (menuButton != null) {
            menuButton.setOnAction(e -> returnToMenu());
        }

        // Événements clavier
        gameCanvas.setOnKeyPressed(this::handleKeyPressed);
        gameCanvas.setOnKeyReleased(this::handleKeyReleased);
    }

    /**
     * Définit les profils des joueurs
     */
    public void setPlayerProfiles(PlayerProfile profile1, PlayerProfile profile2) {
        if (game != null) {
            game.setPlayerProfiles(profile1, profile2);
            updateUI();
        }
    }

    /**
     * Gestion des touches pressées
     */
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (!gameRunning) return;

        pressedKeys.add(event.getCode());
        game.handleKeyPressed(event.getCode());

        // Gestion spéciale pour la pause
        if (event.getCode() == KeyCode.ESCAPE) {
            togglePause();
        }
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
     * Met à jour la logique du jeu
     */
    public void update() {
        if (!gameRunning || isPaused) return;

        game.update();
        updateGameStatus();

        // Vérifier les conditions de victoire
        checkWinConditions();
    }

    /**
     * Vérifie les conditions de victoire
     */
    private void checkWinConditions() {
        Player player1 = game.getPlayer1();
        Player player2 = game.getPlayer2();

        if (!player1.isAlive() || !player2.isAlive()) {
            gameRunning = false;

            // Déterminer le gagnant
            String winMessage;
            if (!player1.isAlive() && !player2.isAlive()) {
                winMessage = "Match nul !";
            } else if (!player1.isAlive()) {
                winMessage = "Joueur 2 gagne !";
            } else {
                winMessage = "Joueur 1 gagne !";
            }

            // Afficher l'écran de fin
            Platform.runLater(() -> showGameOverScreen(winMessage));
        }
    }

    /**
     * Affiche l'écran de fin de partie
     */
    private void showGameOverScreen(String message) {
        soundManager.stopBackgroundMusic();

        Alert gameOverAlert = new Alert(Alert.AlertType.INFORMATION);
        gameOverAlert.setTitle("Fin de partie");
        gameOverAlert.setHeaderText(message);

        long gameTime = getGameTime();
        String timeText = String.format("Temps de jeu: %d:%02d", gameTime / 60, gameTime % 60);

        StringBuilder content = new StringBuilder();
        content.append(timeText).append("\n\n");
        content.append("Score Joueur 1: ").append(game.getPlayer1Score()).append("\n");
        content.append("Score Joueur 2: ").append(game.getPlayer2Score()).append("\n");
        content.append("Bombes placées: ").append(game.getBombsPlaced()).append("\n");
        content.append("Murs détruits: ").append(game.getWallsDestroyed());

        gameOverAlert.setContentText(content.toString());

        ButtonType newGameButton = new ButtonType("Nouvelle partie");
        ButtonType menuButton = new ButtonType("Menu principal");
        ButtonType quitButton = new ButtonType("Quitter");

        gameOverAlert.getButtonTypes().setAll(newGameButton, menuButton, quitButton);

        Optional<ButtonType> result = gameOverAlert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == newGameButton) {
                restartGame();
            } else if (result.get() == menuButton) {
                returnToMenu();
            } else {
                Platform.exit();
            }
        }
    }

    /**
     * Redémarre une nouvelle partie
     */
    private void restartGame() {
        stopGameLoop();
        initializeGame();
    }

    /**
     * Bascule l'état de pause
     */
    private void togglePause() {
        if (isPaused) {
            // Reprendre
            isPaused = false;
            gameRunning = true;
            totalPauseTime += System.currentTimeMillis() - pauseStartTime;
            if (pauseButton != null) {
                pauseButton.setText("Pause");
            }
            if (gameStatusLabel != null) {
                gameStatusLabel.setText("En cours");
            }
        } else {
            // Pause
            isPaused = true;
            gameRunning = false;
            pauseStartTime = System.currentTimeMillis();
            if (pauseButton != null) {
                pauseButton.setText("Reprendre");
            }
            if (gameStatusLabel != null) {
                gameStatusLabel.setText("PAUSE");
            }
        }
    }

    /**
     * Retourne au menu principal - VERSION ROBUSTE
     */
    private void returnToMenu() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Retour au menu");
        confirmAlert.setHeaderText("Voulez-vous vraiment quitter la partie ?");
        confirmAlert.setContentText("La partie en cours sera perdue.");

        ButtonType returnButton = new ButtonType("Retour au menu");
        ButtonType restartButton = new ButtonType("Nouvelle partie");
        ButtonType cancelButton = new ButtonType("Continuer", ButtonType.CANCEL.getButtonData());

        confirmAlert.getButtonTypes().setAll(returnButton, restartButton, cancelButton);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == returnButton) {
                stopGameLoop();
                soundManager.stopBackgroundMusic();

                try {
                    // Essayer de charger le menu
                    String[] possiblePaths = {
                            "/com/example/bomberman/menu-view.fxml",
                            "menu-view.fxml",
                            "/menu-view.fxml"
                    };

                    Parent menuRoot = null;
                    for (String path : possiblePaths) {
                        try {
                            URL resourceUrl = getClass().getResource(path);
                            if (resourceUrl != null) {
                                FXMLLoader loader = new FXMLLoader(resourceUrl);
                                menuRoot = loader.load();
                                System.out.println("Menu chargé depuis: " + path);
                                break;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }

                    if (menuRoot != null) {
                        Stage stage = (Stage) gameCanvas.getScene().getWindow();
                        Scene menuScene = new Scene(menuRoot, 800, 600);

                        // Charger CSS si disponible
                        try {
                            var cssResource = getClass().getResource("styles.css");
                            if (cssResource != null) {
                                menuScene.getStylesheets().add(cssResource.toExternalForm());
                            }
                        } catch (Exception cssError) {
                            // Ignorer l'erreur CSS
                        }

                        stage.setScene(menuScene);
                        stage.setTitle("Super Bomberman");
                    } else {
                        // Si pas de menu, proposer de fermer ou continuer
                        showNoMenuAlert();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showNoMenuAlert();
                }

            } else if (response == restartButton) {
                restartGame();
            }
            // Si Cancel, ne rien faire (continuer la partie)
        });
    }

    /**
     * Affiche une alerte quand le menu n'est pas disponible
     */
    private void showNoMenuAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Menu non disponible");
        alert.setHeaderText("Le menu principal n'est pas disponible");
        alert.setContentText("Voulez-vous fermer l'application ?");

        ButtonType closeButton = new ButtonType("Fermer l'application");
        ButtonType continueButton = new ButtonType("Continuer la partie");

        alert.getButtonTypes().setAll(closeButton, continueButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == closeButton) {
                Platform.exit();
            } else {
                // Continuer la partie
                isPaused = false;
                gameRunning = true;
            }
        });
    }

    /**
     * Démarre la boucle de jeu
     */
    private void startGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

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
     * Arrête la boucle de jeu
     */
    private void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }

    /**
     * Dessine le jeu
     */
    private void render() {
        // Effacer le canvas
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Récupérer les éléments du jeu
        GameBoard board = game.getBoard();
        Player player1 = game.getPlayer1();
        Player player2 = game.getPlayer2();

        // Dessiner le plateau
        if (board != null) {
            board.render(gc, TILE_SIZE);
        }

        // Dessiner les joueurs
        if (player1 != null && player1.isAlive()) {
            player1.render(gc, TILE_SIZE);
        }
        if (player2 != null && player2.isAlive()) {
            player2.render(gc, TILE_SIZE);
        }

        // Dessiner les bombes
        for (Bomb bomb : game.getBombs()) {
            bomb.render(gc, TILE_SIZE);
        }

        // Overlay de pause
        if (isPaused) {
            gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.5));
            gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 36));
            gc.fillText("PAUSE", gameCanvas.getWidth()/2 - 60, gameCanvas.getHeight()/2);

            gc.setFont(javafx.scene.text.Font.font("Arial", 16));
            gc.fillText("Appuyez sur Échap pour reprendre", gameCanvas.getWidth()/2 - 120, gameCanvas.getHeight()/2 + 40);
        }

        // Afficher les informations sur le canvas si les labels n'existent pas
        if (player1InfoLabel == null || player2InfoLabel == null) {
            renderGameInfo();
        }
    }

    /**
     * Affiche les informations de jeu sur le canvas
     */
    private void renderGameInfo() {
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", 14));

        if (game.getPlayer1() != null) {
            String player1Info = "Joueur 1 (ZQSD + A) - Vies: " + game.getPlayer1().getLives() +
                    " - Score: " + game.getPlayer1Score();
            gc.fillText(player1Info, 10, 20);
        }

        if (game.getPlayer2() != null) {
            String player2Info = "Joueur 2 (Flèches + Espace) - Vies: " + game.getPlayer2().getLives() +
                    " - Score: " + game.getPlayer2Score();
            gc.fillText(player2Info, 10, 40);
        }

        // Temps de jeu
        long gameTime = getGameTime();
        String timeText = String.format("Temps: %d:%02d", gameTime / 60, gameTime % 60);
        gc.fillText(timeText, 10, 60);
    }

    /**
     * Met à jour l'interface utilisateur
     */
    private void updateUI() {
        Platform.runLater(() -> {
            updatePlayerInfo();
            updateHealthBars();
        });
    }

    /**
     * Met à jour les informations des joueurs
     */
    private void updatePlayerInfo() {
        if (player1InfoLabel != null && game.getPlayer1() != null) {
            String player1Info = "Joueur 1 - Vies: " + game.getPlayer1().getLives() +
                    " - Score: " + game.getPlayer1Score();
            player1InfoLabel.setText(player1Info);
        }

        if (player2InfoLabel != null && game.getPlayer2() != null) {
            String player2Info = "Joueur 2 - Vies: " + game.getPlayer2().getLives() +
                    " - Score: " + game.getPlayer2Score();
            player2InfoLabel.setText(player2Info);
        }
    }

    /**
     * Met à jour les barres de vie
     */
    private void updateHealthBars() {
        if (player1HealthBar != null && game.getPlayer1() != null) {
            player1HealthBar.setProgress(game.getPlayer1().getLives() / 3.0);
        }
        if (player2HealthBar != null && game.getPlayer2() != null) {
            player2HealthBar.setProgress(game.getPlayer2().getLives() / 3.0);
        }
    }

    /**
     * Met à jour le statut du jeu
     */
    private void updateGameStatus() {
        if (gameStatusLabel != null && !isPaused) {
            Platform.runLater(() -> {
                long gameTime = getGameTime();
                String timeText = String.format("Temps: %d:%02d", gameTime / 60, gameTime % 60);
                gameStatusLabel.setText(timeText);
            });
        }
    }

    /**
     * Calcule le temps de jeu total
     */
    private long getGameTime() {
        long currentTime = System.currentTimeMillis();
        long totalTime = currentTime - gameStartTime - totalPauseTime;

        if (isPaused) {
            totalTime -= (currentTime - pauseStartTime);
        }

        return totalTime / 1000; // Retour en secondes
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Impossible d'afficher l'alerte: " + e.getMessage());
        }
    }
}