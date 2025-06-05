package com.example.bomberman;

import com.example.bomberman.*;
import com.example.bomberman.SoundManager;
import javafx.scene.input.KeyCode;

import java.util.*;

/**
 * Modèle principal du jeu Bomberman avec architecture MVC
 */
public class Game {
    // Configuration du jeu
    private static final int BOARD_WIDTH = 15;
    private static final int BOARD_HEIGHT = 13;

    // État du jeu
    private GameState gameState;
    private GameBoard board;
    private Player player1;
    private Player player2;
    private List<Bomb> bombs;
    private boolean gameRunning;

    // Gestion du temps
    private long gameStartTime;
    private long pauseStartTime;
    private long totalPauseTime;

    // Gestion des entrées
    private Set<KeyCode> pressedKeys;

    // Observateurs (pour le pattern Observer)
    private List<GameObserver> observers;

    // Statistiques de partie
    private int player1Score;
    private int player2Score;
    private int bombsPlaced;
    private int wallsDestroyed;

    /**
     * Interface pour les observateurs du jeu
     */
    public interface GameObserver {
        void onGameStateChanged(GameState newState);
        void onPlayerDamaged(Player player);
        void onBombExploded(Bomb bomb);
        void onPowerUpCollected(Player player, PowerUp.Type type);
        void onGameOver(Player winner);
    }

    /**
     * Constructeur du jeu
     */
    public Game() {
        this.observers = new ArrayList<>();
        this.pressedKeys = new HashSet<>();
        initializeGame();
    }

    /**
     * Initialise une nouvelle partie
     */
    public void initializeGame() {
        board = new GameBoard(BOARD_WIDTH, BOARD_HEIGHT);
        player1 = new Player(1, 1, javafx.scene.paint.Color.BLUE, 1);
        player2 = new Player(BOARD_WIDTH - 2, BOARD_HEIGHT - 2, javafx.scene.paint.Color.RED, 2);
        bombs = new ArrayList<>();
        gameState = GameState.PLAYING;
        gameRunning = true;
        gameStartTime = System.currentTimeMillis();
        totalPauseTime = 0;

        // Réinitialiser les statistiques
        player1Score = 0;
        player2Score = 0;
        bombsPlaced = 0;
        wallsDestroyed = 0;

        notifyObservers(obs -> obs.onGameStateChanged(gameState));
    }

    /**
     * Ajoute un observateur
     */
    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    /**
     * Retire un observateur
     */
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifie tous les observateurs
     */
    private void notifyObservers(java.util.function.Consumer<GameObserver> action) {
        observers.forEach(action);
    }

    /**
     * Gère l'appui d'une touche
     */
    public void handleKeyPressed(KeyCode key) {
        if (!gameRunning) return;

        pressedKeys.add(key);

        // Actions instantanées
        switch (key) {
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
     * Gère le relâchement d'une touche
     */
    public void handleKeyReleased(KeyCode key) {
        pressedKeys.remove(key);
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
        updateBombs();

        // Vérifier les conditions de victoire
        checkWinConditions();
    }

    /**
     * Traite les mouvements des joueurs
     */
    private void processMovement() {
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
                notifyObservers(obs -> obs.onPowerUpCollected(player, powerUp.getType()));

                // Augmenter le score
                if (player == player1) {
                    player1Score += 10;
                } else {
                    player2Score += 10;
                }
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
            bombsPlaced++;
        }
    }

    /**
     * Met à jour les bombes
     */
    private void updateBombs() {
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

                notifyObservers(obs -> obs.onBombExploded(bomb));
            }
        }
    }

    /**
     * Gère l'explosion d'une bombe
     */
    private void handleExplosion(Bomb bomb) {
        int x = bomb.getX();
        int y = bomb.getY();
        int wallsDestroyedByThisBomb = 0;

        // Explosion au centre
        if (board.isWall(x, y)) wallsDestroyedByThisBomb++;
        board.explode(x, y);

        // Explosion dans les 4 directions
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        for (int dir = 0; dir < 4; dir++) {
            for (int i = 1; i <= bomb.getRange(); i++) {
                int newX = x + dx[dir] * i;
                int newY = y + dy[dir] * i;

                if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) {
                    break;
                }

                if (!board.canExplode(newX, newY)) {
                    break;
                }

                if (board.isWall(newX, newY)) {
                    wallsDestroyedByThisBomb++;
                    board.explode(newX, newY);
                    break; // L'explosion s'arrête après un mur destructible
                } else {
                    board.explode(newX, newY);
                }
            }
        }

        wallsDestroyed += wallsDestroyedByThisBomb;

        // Ajouter des points pour les murs détruits
        if (bomb.getPlayerId() == 1) {
            player1Score += wallsDestroyedByThisBomb * 5;
        } else {
            player2Score += wallsDestroyedByThisBomb * 5;
        }

        // Vérifier si les joueurs sont touchés
        if (board.isExplosion(player1.getX(), player1.getY())) {
            player1.takeDamage();
            notifyObservers(obs -> obs.onPlayerDamaged(player1));
        }
        if (board.isExplosion(player2.getX(), player2.getY())) {
            player2.takeDamage();
            notifyObservers(obs -> obs.onPlayerDamaged(player2));
        }
    }

    /**
     * Vérifie les conditions de victoire
     */
    private void checkWinConditions() {
        if (!player1.isAlive() || !player2.isAlive()) {
            gameRunning = false;
            gameState = GameState.GAME_OVER;

            Player winner = null;
            if (player1.isAlive()) {
                winner = player1;
                player1Score += 100; // Bonus de victoire
            } else if (player2.isAlive()) {
                winner = player2;
                player2Score += 100; // Bonus de victoire
            }

            notifyObservers(obs -> obs.onGameOver(winner));
            notifyObservers(obs -> obs.onGameStateChanged(gameState));
        }
    }

    /**
     * Bascule l'état de pause
     */
    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            gameRunning = false;
            pauseStartTime = System.currentTimeMillis();
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            gameRunning = true;
            totalPauseTime += System.currentTimeMillis() - pauseStartTime;
        }
        notifyObservers(obs -> obs.onGameStateChanged(gameState));
    }

    /**
     * Redémarre le jeu
     */
    public void restart() {
        // Conserver les profils des joueurs
        PlayerProfile profile1 = player1.getProfile();
        PlayerProfile profile2 = player2.getProfile();

        initializeGame();

        // Restaurer les profils
        player1.setProfile(profile1);
        player2.setProfile(profile2);
    }

    /**
     * Définit les profils des joueurs
     */
    public void setPlayerProfiles(PlayerProfile profile1, PlayerProfile profile2) {
        player1.setProfile(profile1);
        player2.setProfile(profile2);
    }

    /**
     * Calcule le temps de jeu total
     */
    public long getGameTime() {
        long currentTime = System.currentTimeMillis();
        long totalTime = currentTime - gameStartTime - totalPauseTime;

        if (gameState == GameState.PAUSED) {
            totalTime -= (currentTime - pauseStartTime);
        }

        return totalTime / 1000; // Retour en secondes
    }

    /**
     * Retourne un résumé de la partie
     */
    public GameSummary getGameSummary() {
        return new GameSummary(
                player1.getProfile(),
                player2.getProfile(),
                player1Score,
                player2Score,
                getGameTime(),
                bombsPlaced,
                wallsDestroyed,
                gameState == GameState.GAME_OVER ?
                        (player1.isAlive() ? player1.getProfile() :
                                player2.isAlive() ? player2.getProfile() : null) : null
        );
    }

    // Getters pour l'accès aux données du modèle
    public GameState getGameState() { return gameState; }
    public GameBoard getBoard() { return board; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public List<Bomb> getBombs() { return new ArrayList<>(bombs); }
    public boolean isGameRunning() { return gameRunning; }
    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }
    public int getBombsPlaced() { return bombsPlaced; }
    public int getWallsDestroyed() { return wallsDestroyed; }

    /**
     * Classe pour résumer une partie
     */
    public static class GameSummary {
        private final PlayerProfile player1Profile;
        private final PlayerProfile player2Profile;
        private final int player1Score;
        private final int player2Score;
        private final long gameTime;
        private final int bombsPlaced;
        private final int wallsDestroyed;
        private final PlayerProfile winner;

        public GameSummary(PlayerProfile player1Profile, PlayerProfile player2Profile,
                           int player1Score, int player2Score, long gameTime,
                           int bombsPlaced, int wallsDestroyed, PlayerProfile winner) {
            this.player1Profile = player1Profile;
            this.player2Profile = player2Profile;
            this.player1Score = player1Score;
            this.player2Score = player2Score;
            this.gameTime = gameTime;
            this.bombsPlaced = bombsPlaced;
            this.wallsDestroyed = wallsDestroyed;
            this.winner = winner;
        }

        // Getters
        public PlayerProfile getPlayer1Profile() { return player1Profile; }
        public PlayerProfile getPlayer2Profile() { return player2Profile; }
        public int getPlayer1Score() { return player1Score; }
        public int getPlayer2Score() { return player2Score; }
        public long getGameTime() { return gameTime; }
        public int getBombsPlaced() { return bombsPlaced; }
        public int getWallsDestroyed() { return wallsDestroyed; }
        public PlayerProfile getWinner() { return winner; }

        public boolean isDraw() { return winner == null; }
    }
}