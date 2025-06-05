package com.example.bomberman;

import com.example.bomberman.GameState;
import com.example.bomberman.PlayerProfile;
import com.example.bomberman.PowerUp;
import com.example.bomberman.ProfileManager;
import com.example.bomberman.SoundManager;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Contrôleur principal du jeu avec architecture MVC
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
    private GameBoard board;
    private Player player1;
    private Player player2;
    private List<Bomb> bombs;
    private GameState gameState;

    // Vue
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    // Gestion des entrées
    private Set<KeyCode> pressedKeys;

    // Utilitaires
    private SoundManager soundManager;
    private ProfileManager profileManager;

    // Configuration
    private static final int BOARD_WIDTH = 15;
    private static final int BOARD_HEIGHT = 13;
    private static final int TILE_SIZE = 40;

    // État du jeu
    private boolean gameRunning;
    private long gameStartTime;
    private long pauseStartTime;
    private long totalPauseTime;

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
    }

    /**
     * Initialise une nouvelle partie
     */
    private void initializeGame() {
        board = new GameBoard(BOARD_WIDTH, BOARD_HEIGHT);
        player1 = new Player(1, 1, Color.BLUE, 1);
        player2 = new Player(BOARD_WIDTH - 2, BOARD_HEIGHT - 2, Color.RED, 2);
        bombs = new ArrayList<>();
        gameState = GameState.PLAYING;
        gameRunning = true;
        gameStartTime = System.currentTimeMillis();
        totalPauseTime = 0;

        updateUI();
        startGameLoop();

        // Jouer la musique de jeu
        soundManager.playBackgroundMusic("/sounds/game_music.mp3");
    }

    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        pauseButton.setOnAction(e -> togglePause());
        menuButton.setOnAction(e -> returnToMenu());

        // Événements clavier
        gameCanvas.setOnKeyPressed(this::handleKeyPressed);
        gameCanvas.setOnKeyReleased(this::handleKeyReleased);
    }

    /**
     * Définit les profils des joueurs
     */
    public void setPlayerProfiles(PlayerProfile profile1, PlayerProfile profile2) {
        player1.setProfile(profile1);
        player2.setProfile(profile2);

        // Personnaliser les couleurs selon les profils
        // (ici on peut ajouter la logique de thèmes)

        updateUI();
    }

    /**
     * Gestion des touches pressées
     */
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (!gameRunning) return;

        pressedKeys.add(event.getCode());

        // Gestion des bombes (action instantanée)
        switch (event.getCode()) {
            case A:
                placeBomb(player1);
                break;
            case SPACE:
                placeBomb(player2);
                break;
            case ESCAPE:
                togglePause();
                break;
        }
    }

    /**
     * Gestion des touches relâchées
     */
    @FXML
    private void handleKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
    }

    /**
     * Traite les mouvements en continu
     */
    private void processMovement() {
        if (!gameRunning || gameState != GameState.PLAYING) return;

        // Joueur 1 (ZQSD)
        int dx1 = 0, dy1 = 0;
        if (pressedKeys.contains(KeyCode.Z)) dy1 -= 1;
        if (pressedKeys.contains(KeyCode.S)) dy1 += 1;
        if (pressedKeys.contains(KeyCode.Q)) dx1 -= 1;
        if (pressedKeys.contains(KeyCode.D)) dx1 += 1;

        if (dx1 != 0 || dy1 != 0) {
            movePlayer(player1, dx1, dy1);
        }

        // Joueur 2 (Flèches)
        int dx2 = 0, dy2 = 0;
        if (pressedKeys.contains(KeyCode.UP)) dy2 -= 1;
        if (pressedKeys.contains(KeyCode.DOWN)) dy2 += 1;
        if (pressedKeys.contains(KeyCode.LEFT)) dx2 -= 1;
        if (pressedKeys.contains(KeyCode.RIGHT)) dx2 += 1;

        if (dx2 != 0 || dy2 != 0) {
            movePlayer(player2, dx2, dy2);
        }
    }

    /**
     * Déplace un joueur
     */
    private void movePlayer(Player player, int dx, int dy) {
        if (!player.canMove()) return;

        int newX = player.getX() + dx;
        int newY = player.getY() + dy;

        if (board.isValidMove(newX, newY)) {
            player.setPosition(newX, newY);

            // Vérifier s'il y a un power-up à collecter
            PowerUp powerUp = board.collectPowerUp(newX, newY);
            if (powerUp != null) {
                player.applyPowerUp(powerUp.getType());
                updateUI();
            }
        }
    }

    /**
     * Place une bombe
     */
    private void placeBomb(Player player) {
        if (player.canPlaceBomb()) {
            Bomb bomb = new Bomb(player.getX(), player.getY(), player.getPlayerId());
            bomb.setRange(player.getBombRange());
            bombs.add(bomb);
            board.placeBomb(player.getX(), player.getY());
            player.placeBomb();
        }
    }

    /**
     * Met à jour la logique du jeu
     */
    public void update() {
        if (!gameRunning || gameState != GameState.PLAYING) return;

        // Traiter les mouvements
        processMovement();

        // Mettre à jour les joueurs
        player1.update();
        player2.update();

        // Mettre à jour le plateau
        board.update();

        // Mettre à jour les bombes
        Iterator<Bomb> bombIterator = bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.update();

            if (bomb.hasExploded()) {
                handleExplosion(bomb);
                bombIterator.remove();
                board.removeBomb(bomb.getX(), bomb.getY());

                // Rendre la bombe disponible au joueur
                if (bomb.getPlayerId() == 1) {
                    player1.bombExploded();
                } else {
                    player2.bombExploded();
                }

                soundManager.playSound("bomb_explode");
            }
        }

        // Vérifier les conditions de victoire
        checkWinConditions();

        // Mettre à jour l'interface
        updateGameStatus();
    }

    /**
     * Gère l'explosion d'une bombe
     */
    private void handleExplosion(Bomb bomb) {
        int x = bomb.getX();
        int y = bomb.getY();

        // Explosion au centre
        board.explode(x, y);

        // Explosion dans les 4 directions
        for (int i = 1; i <= bomb.getRange(); i++) {
            // Droite
            if (x + i < BOARD_WIDTH && board.canExplode(x + i, y)) {
                board.explode(x + i, y);
                if (board.isWall(x + i, y)) break;
            }
            // Gauche
            if (x - i >= 0 && board.canExplode(x - i, y)) {
                board.explode(x - i, y);
                if (board.isWall(x - i, y)) break;
            }
            // Haut
            if (y - i >= 0 && board.canExplode(x, y - i)) {
                board.explode(x, y - i);
                if (board.isWall(x, y - i)) break;
            }
            // Bas
            if (y + i < BOARD_HEIGHT && board.canExplode(x, y + i)) {
                board.explode(x, y + i);
                if (board.isWall(x, y + i)) break;
            }
        }

        // Vérifier si les joueurs sont touchés
        if (board.isExplosion(player1.getX(), player1.getY())) {
            player1.takeDamage();
        }
        if (board.isExplosion(player2.getX(), player2.getY())) {
            player2.takeDamage();
        }
    }

    /**
     * Vérifie les conditions de victoire
     */
    private void checkWinConditions() {
        if (!player1.isAlive() || !player2.isAlive()) {
            gameRunning = false;
            gameState = GameState.GAME_OVER;

            // Déterminer le gagnant
            PlayerProfile winner = null;
            String winMessage;

            if (!player1.isAlive() && !player2.isAlive()) {
                winMessage = "Match nul !";
            } else if (!player1.isAlive()) {
                winner = player2.getProfile();
                winMessage = player2.getProfile() != null ?
                        player2.getProfile().getFullName() + " gagne !" : "Joueur 2 gagne !";
            } else {
                winner = player1.getProfile();
                winMessage = player1.getProfile() != null ?
                        player1.getProfile().getFullName() + " gagne !" : "Joueur 1 gagne !";
            }

            // Mettre à jour les statistiques
            updatePlayerStats(winner);

            // Afficher l'écran de fin
            showGameOverScreen(winMessage);
        }
    }

    /**
     * Met à jour les statistiques des joueurs
     */
    private void updatePlayerStats(PlayerProfile winner) {
        if (player1.getProfile() != null) {
            player1.getProfile().addGame(player1.getProfile() == winner);
        }
        if (player2.getProfile() != null) {
            player2.getProfile().addGame(player2.getProfile() == winner);
        }
        profileManager.saveProfiles();
    }

    /**
     * Affiche l'écran de fin de partie
     */
    private void showGameOverScreen(String message) {
        soundManager.stopBackgroundMusic();

        Platform.runLater(() -> {
            Alert gameOverAlert = new Alert(Alert.AlertType.INFORMATION);
            gameOverAlert.setTitle("Fin de partie");
            gameOverAlert.setHeaderText(message);

            long gameTime = (System.currentTimeMillis() - gameStartTime - totalPauseTime) / 1000;
            String timeText = String.format("Temps de jeu: %d:%02d", gameTime / 60, gameTime % 60);
            gameOverAlert.setContentText(timeText);

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
        });
    }

    /**
     * Redémarre une nouvelle partie
     */
    private void restartGame() {
        stopGameLoop();

        PlayerProfile profile1 = player1.getProfile();
        PlayerProfile profile2 = player2.getProfile();

        initializeGame();
        setPlayerProfiles(profile1, profile2);
    }

    /**
     * Bascule l'état de pause
     */
    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            gameRunning = false;
            pauseStartTime = System.currentTimeMillis();
            pauseButton.setText("Reprendre");
            gameStatusLabel.setText("PAUSE");
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            gameRunning = true;
            totalPauseTime += System.currentTimeMillis() - pauseStartTime;
            pauseButton.setText("Pause");
            gameStatusLabel.setText("En cours");
        }
    }

    /**
     * Retourne au menu principal
     */
    private void returnToMenu() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Retour au menu");
        confirmAlert.setHeaderText("Voulez-vous vraiment retourner au menu ?");
        confirmAlert.setContentText("La partie en cours sera perdue.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            stopGameLoop();
            soundManager.stopBackgroundMusic();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bomberman/menu-view.fxml"));
                Parent menuRoot = loader.load();

                Stage stage = (Stage) gameCanvas.getScene().getWindow();
                Scene menuScene = new Scene(menuRoot, 800, 600);
                menuScene.getStylesheets().add(getClass().getResource("/com/example/bomberman/styles.css").toExternalForm());

                stage.setScene(menuScene);
                stage.setTitle("Super Bomberman");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
     * Arrête la boucle de jeu
     */
    private void stopGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /**
     * Dessine le jeu
     */
    private void render() {
        // Effacer le canvas
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Dessiner le plateau
        board.render(gc, TILE_SIZE);

        // Dessiner les joueurs
        if (player1.isAlive()) {
            player1.render(gc, TILE_SIZE);
        }
        if (player2.isAlive()) {
            player2.render(gc, TILE_SIZE);
        }

        // Dessiner les bombes
        for (Bomb bomb : bombs) {
            bomb.render(gc, TILE_SIZE);
        }

        // Overlay de pause
        if (gameState == GameState.PAUSED) {
            gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.5));
            gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 36));
            gc.fillText("PAUSE", gameCanvas.getWidth()/2 - 60, gameCanvas.getHeight()/2);
        }
    }

    /**
     * Met à jour l'interface utilisateur
     */
    private void updateUI() {
        Platform.runLater(() -> {
            // Informations des joueurs
            updatePlayerInfo();
            updateHealthBars();
            updatePowerUpIndicators();
        });
    }

    /**
     * Met à jour les informations des joueurs
     */
    private void updatePlayerInfo() {
        String player1Info = "Joueur 1";
        if (player1.getProfile() != null) {
            player1Info = player1.getProfile().getFullName();
        }
        player1Info += " - Vies: " + player1.getLives();
        player1InfoLabel.setText(player1Info);

        String player2Info = "Joueur 2";
        if (player2.getProfile() != null) {
            player2Info = player2.getProfile().getFullName();
        }
        player2Info += " - Vies: " + player2.getLives();
        player2InfoLabel.setText(player2Info);
    }

    /**
     * Met à jour les barres de vie
     */
    private void updateHealthBars() {
        player1HealthBar.setProgress(player1.getLives() / 3.0);
        player2HealthBar.setProgress(player2.getLives() / 3.0);
    }

    /**
     * Met à jour les indicateurs de power-ups
     */
    private void updatePowerUpIndicators() {
        // Ici on pourrait ajouter des icônes visuelles des power-ups
        // Pour l'instant, on utilise le texte
    }

    /**
     * Met à jour le statut du jeu
     */
    private void updateGameStatus() {
        Platform.runLater(() -> {
            if (gameState == GameState.PLAYING) {
                long gameTime = (System.currentTimeMillis() - gameStartTime - totalPauseTime) / 1000;
                String timeText = String.format("Temps: %d:%02d", gameTime / 60, gameTime % 60);
                gameStatusLabel.setText(timeText);
            }
        });
    }
}