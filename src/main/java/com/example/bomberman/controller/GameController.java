package com.example.bomberman.controller;

import com.example.bomberman.models.entities.Bomb;
import com.example.bomberman.models.entities.Player;
import com.example.bomberman.models.world.Game;
import com.example.bomberman.models.world.GameBoard;
import com.example.bomberman.service.SoundManager;
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
    @FXML private Label timerLabel;
    
    // Nouveaux éléments d'interface
    @FXML private Label player1BombsLabel;
    @FXML private Label player2BombsLabel;
    @FXML private Label player1LivesLabel;
    @FXML private Label player2LivesLabel;
    @FXML private Label player1BombsInfoLabel;
    @FXML private Label player2BombsInfoLabel;
    @FXML private Label player1RangeLabel;
    @FXML private Label player2RangeLabel;
    @FXML private Label player1SpeedLabel;
    @FXML private Label player2SpeedLabel;

    // Modèle du jeu
    private Game game;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    // Gestion des entrées
    private Set<KeyCode> pressedKeys;

    // Utilitaires
    private SoundManager soundManager;

    // Configuration
    private static final int TILE_SIZE = 40;

    // État du jeu
    private boolean gameRunning;
    private long gameStartTime;
    private long pauseStartTime;
    private long totalPauseTime;
    private boolean isPaused = false;
    
    // Mode test de l'éditeur
    private boolean isTestMode = false;
    private Stage levelEditorStage = null;

    // Variables pour stocker l'état de la musique
    private String currentMusic = "game_music";
    private boolean wasPlayingMusic = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gc = gameCanvas.getGraphicsContext2D();
        pressedKeys = new HashSet<>();
        soundManager = SoundManager.getInstance();

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

        // Choisir aléatoirement entre les deux musiques du jeu
        String[] musics = { "game_music_A", "game_music_B", "game_music_C" };
        currentMusic = musics[new Random().nextInt(musics.length)];
        
        // Jouer la musique de jeu
        soundManager.playBackgroundMusic(currentMusic);
    }

    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        // Événements clavier
        gameCanvas.setOnKeyPressed(this::handleKeyPressed);
        gameCanvas.setOnKeyReleased(this::handleKeyReleased);
    }

    /**
     * Gestion des touches pressées
     */
    @FXML
    public void handleKeyPressed(KeyEvent event) {
        if (!gameRunning && !isPaused) return;

        pressedKeys.add(event.getCode());
        game.handleKeyPressed(event.getCode());

        // Gestion spéciale pour la pause
        if (event.getCode() == KeyCode.ESCAPE) {
            if (isPaused) {
                // Si déjà en pause, reprendre
                togglePause();
            } else {
                // Mettre en pause et afficher le menu
                togglePause();
                showPauseMenu();
            }
        }
    }

    /**
     * Gestion des touches relâchées
     */
    @FXML
    public void handleKeyReleased(KeyEvent event) {
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
        // Jouer la musique de résultat
        soundManager.playBackgroundMusic("result");

        // Afficher le panneau de fin de jeu dans l'interface
        VBox gameOverPanel = (VBox) gameCanvas.getScene().lookup("#gameOverPanel");
        if (gameOverPanel != null) {
            Label winnerLabel = (Label) gameOverPanel.lookup("#winnerLabel");
            Label finalScoreLabel = (Label) gameOverPanel.lookup("#finalScoreLabel");
            
            if (winnerLabel != null) {
                winnerLabel.setText(message);
            }
            
            if (finalScoreLabel != null) {
                finalScoreLabel.setText("Score: " + game.getPlayer1Score() + " - " + game.getPlayer2Score());
            }
            
            gameOverPanel.setVisible(true);
        } else {
            // Fallback si le panneau n'est pas trouvé
            Alert gameOverAlert = new Alert(Alert.AlertType.INFORMATION);
            gameOverAlert.setTitle("Fin de partie");
            gameOverAlert.setHeaderText(message);
            gameOverAlert.setContentText("Score final: " + game.getPlayer1Score() + " - " + game.getPlayer2Score());
            gameOverAlert.showAndWait();
        }
    }

    /**
     * Redémarre la partie
     */
    @FXML
    public void restartGame() {
        // Arrêter la boucle de jeu actuelle
        stopGameLoop();
        
        // Réinitialiser le jeu
        initializeGame();
        
        // Masquer le panneau de fin de jeu s'il est visible
        VBox gameOverPanel = (VBox) gameCanvas.getScene().lookup("#gameOverPanel");
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(false);
        }
        
        // Redémarrer la boucle de jeu
        startGameLoop();
        
        // Redonner le focus au canvas
        gameCanvas.requestFocus();
    }

    /**
     * Bascule l'état de pause du jeu
     */
    @FXML
    public void togglePause() {
        isPaused = !isPaused;
        
        if (isPaused) {
            pauseStartTime = System.currentTimeMillis();
            gameStatusLabel.setText("PAUSE");
            // Mettre en pause la musique
            soundManager.stopBackgroundMusic();
        } else {
            totalPauseTime += System.currentTimeMillis() - pauseStartTime;
            gameStatusLabel.setText("En cours");
            // Reprendre la musique
            soundManager.playBackgroundMusic(currentMusic);
        }
    }

    /**
     * Retourne au menu principal
     */
    @FXML
    public void returnToMainMenu() {
        // Si en mode test, retourner à l'éditeur de niveau
        if (isTestMode && levelEditorStage != null) {
            returnToLevelEditor();
            return;
        }
        
        try {
            // Arrêter la boucle de jeu
            stopGameLoop();
            
            // Arrêter la musique du jeu
            soundManager.stopBackgroundMusic();
            
            // Charger le menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/menu-view.fxml"));
            Parent menuRoot = loader.load();
            
            // Changer de scène
            Scene menuScene = new Scene(menuRoot, 1000, 700);
            
            // Charger le CSS du menu
            URL cssResource = getClass().getResource("/com/example/bomberman/view/menu-styles.css");
            if (cssResource != null) {
                menuScene.getStylesheets().add(cssResource.toExternalForm());
            }
            
            Stage stage = (Stage) gameCanvas.getScene().getWindow();
            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu Principal");
            
        } catch (IOException e) {
            e.printStackTrace();
            showNoMenuAlert();
        }
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
    public void render() {
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
            String player1Info = "Joueur 1 (ZQSD + A + E) - Vies: " + game.getPlayer1().getLives() +
                    " - Score: " + game.getPlayer1Score();
            gc.fillText(player1Info, 10, 20);
        }

        if (game.getPlayer2() != null) {
            String player2Info = "Joueur 2 (Flèches + Espace + Ctrl) - Vies: " + game.getPlayer2().getLives() +
                    " - Score: " + game.getPlayer2Score();
            gc.fillText(player2Info, 10, 40);
        }

        String gameTime = "Temps de jeu: " + formatTime(getGameTime());
        gc.fillText(gameTime, gameCanvas.getWidth() - 150, 20);
    }

    /**
     * Met à jour l'interface utilisateur
     */
    private void updateUI() {
        updatePlayerInfo();
        updateGameStatus();
        
        // Mettre à jour le timer
        long gameTime = getGameTime();
        timerLabel.setText(formatTime(gameTime));
    }

    /**
     * Met à jour les informations des joueurs
     */
    private void updatePlayerInfo() {
        // Joueur 1
        player1InfoLabel.setText("Joueur 1");
        player1LivesLabel.setText(String.valueOf(game.getPlayer1().getLives()));
        player1BombsLabel.setText(String.valueOf(game.getPlayer1().getMaxBombs()));
        player1BombsInfoLabel.setText(String.valueOf(game.getPlayer1().getMaxBombs()));
        player1RangeLabel.setText(String.valueOf(game.getPlayer1().getBombRange()));
        player1SpeedLabel.setText(String.valueOf(game.getPlayer1().getSpeed()));

        // Joueur 2
        player2InfoLabel.setText("Joueur 2");
        player2LivesLabel.setText(String.valueOf(game.getPlayer2().getLives()));
        player2BombsLabel.setText(String.valueOf(game.getPlayer2().getMaxBombs()));
        player2BombsInfoLabel.setText(String.valueOf(game.getPlayer2().getMaxBombs()));
        player2RangeLabel.setText(String.valueOf(game.getPlayer2().getBombRange()));
        player2SpeedLabel.setText(String.valueOf(game.getPlayer2().getSpeed()));
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
     * Obtient le temps de jeu en secondes
     */
    private long getGameTime() {
        if (!gameRunning) {
            return (pauseStartTime - gameStartTime - totalPauseTime) / 1000;
        }
        
        long currentTime = System.currentTimeMillis();
        if (isPaused) {
            return (pauseStartTime - gameStartTime - totalPauseTime) / 1000;
        } else {
            return (currentTime - gameStartTime - totalPauseTime) / 1000;
        }
    }
    
    /**
     * Formate le temps en minutes:secondes
     */
    private String formatTime(long timeInSeconds) {
        if (timeInSeconds < 0) {
            timeInSeconds = 0;
        }
        return String.format("%d:%02d", timeInSeconds / 60, timeInSeconds % 60);
    }

    /**
     * Affiche une alerte simple
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Charge un niveau personnalisé depuis un fichier
     */
    public void loadCustomLevel(String levelPath) {
        game = new Game();
        game.loadLevel(levelPath);
        
        // Adapter la taille du canvas
        if (gameCanvas != null) {
            gameCanvas.setWidth(Math.max(game.getBoard().getWidth() * TILE_SIZE, 800));
            gameCanvas.setHeight(Math.max(game.getBoard().getHeight() * TILE_SIZE, 600));
        }
        
        gameRunning = true;
        updateUI();
    }

    /**
     * Définit si le jeu est en mode test depuis l'éditeur de niveau
     */
    public void setTestMode(boolean testMode) {
        this.isTestMode = testMode;
        
        // Si en mode test, s'assurer que la musique est jouée
        if (testMode && soundManager.isMusicEnabled()) {
            // Choisir aléatoirement entre les musiques du jeu
            String[] musics = { "game_music_A", "game_music_B", "game_music_C" };
            currentMusic = musics[new Random().nextInt(musics.length)];
            
            // Arrêter la musique actuelle et jouer la musique du jeu
            soundManager.stopBackgroundMusic();
            soundManager.playBackgroundMusic(currentMusic);
        }
    }
    
    /**
     * Vérifie si le jeu est en mode test
     * @return true si le jeu est en mode test
     */
    public boolean isTestMode() {
        return isTestMode;
    }
    
    /**
     * Définit la référence vers la fenêtre de l'éditeur de niveau
     */
    public void setLevelEditorStage(Stage stage) {
        this.levelEditorStage = stage;
    }

    /**
     * Retourne à l'éditeur de niveau
     */
    private void returnToLevelEditor() {
        stopGameLoop();
        soundManager.stopBackgroundMusic();
        
        if (levelEditorStage != null) {
            try {
                // Recharger la scène de l'éditeur
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/view/level-editor-view.fxml"));
                Parent editorRoot = loader.load();
                
                // Changer de scène
                Scene editorScene = new Scene(editorRoot, levelEditorStage.getWidth(), levelEditorStage.getHeight());
                
                // Charger le CSS
                try {
                    var cssResource = getClass().getResource("/com/example/bomberman/style.css");
                    if (cssResource != null) {
                        editorScene.getStylesheets().add(cssResource.toExternalForm());
                    }
                } catch (Exception cssError) {
                    System.out.println("CSS non trouvé, continuation sans styles");
                }
                
                levelEditorStage.setScene(editorScene);
                levelEditorStage.setTitle("Super Bomberman - Éditeur de niveaux");
                
                // Jouer la musique de l'éditeur
                if (soundManager.isMusicEnabled()) {
                    soundManager.playBackgroundMusic("editor_music");
                }
                
                System.out.println("Retour à l'éditeur de niveaux");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de retourner à l'éditeur de niveaux.", Alert.AlertType.ERROR);
            }
        }
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
     * Affiche le menu de pause
     */
    private void showPauseMenu() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Menu de pause");
        confirmAlert.setHeaderText("Options");
        
        // Boutons différents selon le mode
        ButtonType returnButton = new ButtonType("Retour au menu principal");
        ButtonType restartButton = new ButtonType("Nouvelle partie");
        ButtonType continueButton = new ButtonType("Continuer", ButtonType.CANCEL.getButtonData());
        
        if (isTestMode) {
            ButtonType editorButton = new ButtonType("Retour à l'éditeur");
            confirmAlert.getButtonTypes().setAll(editorButton, restartButton, returnButton, continueButton);
        } else {
            confirmAlert.getButtonTypes().setAll(restartButton, returnButton, continueButton);
        }

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == returnButton) {
                returnToMainMenu();
            } else if (response == restartButton) {
                restartGame();
            } else if (isTestMode && response.getText().equals("Retour à l'éditeur")) {
                returnToLevelEditor();
            } else {
                // Si Cancel, reprendre la partie
                togglePause();
            }
        });
    }
}