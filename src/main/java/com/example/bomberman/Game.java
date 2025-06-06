package com.example.bomberman;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Classe principale du jeu - Version simplifiée avec méthodes complètes
 */
public class Game {
    private static final int BOARD_WIDTH = 15;
    private static final int BOARD_HEIGHT = 13;

    private GameBoard board;
    private Player player1;
    private Player player2;
    private List<Bomb> bombs;
    private boolean gameRunning;

    // Scores et statistiques
    private int player1Score = 0;
    private int player2Score = 0;
    private int bombsPlaced = 0;
    private int wallsDestroyed = 0;

    // Pour gérer les touches pressées simultanément
    private Set<KeyCode> pressedKeys;

    public Game() {
        pressedKeys = new HashSet<>();
        initializeGame();
    }

    public void initializeGame() {
        board = new GameBoard(BOARD_WIDTH, BOARD_HEIGHT);
        player1 = new Player(1, 1, Color.BLUE, 1);
        player2 = new Player(BOARD_WIDTH - 2, BOARD_HEIGHT - 2, Color.RED, 2);
        bombs = new ArrayList<>();
        gameRunning = true;
        player1Score = 0;
        player2Score = 0;
        bombsPlaced = 0;
        wallsDestroyed = 0;
    }

    /**
     * Définit les profils des joueurs
     */
    public void setPlayerProfiles(PlayerProfile profile1, PlayerProfile profile2) {
        if (player1 != null) {
            player1.setProfile(profile1);
        }
        if (player2 != null) {
            player2.setProfile(profile2);
        }
    }

    public void handleKeyPressed(KeyCode key) {
        if (!gameRunning) return;

        pressedKeys.add(key);

        // Gestion des bombes (action instantanée)
        if (key == KeyCode.A) {
            placeBomb(player1);
        } else if (key == KeyCode.SPACE) {
            placeBomb(player2);
        }
    }

    public void handleKeyReleased(KeyCode key) {
        pressedKeys.remove(key);
    }

    // Nouvelle méthode pour traiter les mouvements en continu
    public void processMovement() {
        if (!gameRunning) return;

        // Joueur 1 (ZQSD)
        if (pressedKeys.contains(KeyCode.Z)) {
            movePlayer(player1, 0, -1);
        }
        if (pressedKeys.contains(KeyCode.S)) {
            movePlayer(player1, 0, 1);
        }
        if (pressedKeys.contains(KeyCode.Q)) {
            movePlayer(player1, -1, 0);
        }
        if (pressedKeys.contains(KeyCode.D)) {
            movePlayer(player1, 1, 0);
        }

        // Joueur 2 (Flèches)
        if (pressedKeys.contains(KeyCode.UP)) {
            movePlayer(player2, 0, -1);
        }
        if (pressedKeys.contains(KeyCode.DOWN)) {
            movePlayer(player2, 0, 1);
        }
        if (pressedKeys.contains(KeyCode.LEFT)) {
            movePlayer(player2, -1, 0);
        }
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            movePlayer(player2, 1, 0);
        }
    }

    private void movePlayer(Player player, int dx, int dy) {
        if (!player.canMove()) return; // Vérifier si le joueur peut bouger

        int newX = player.getX() + dx;
        int newY = player.getY() + dy;

        if (board.isValidMove(newX, newY)) {
            player.setPosition(newX, newY);

            // Collecter power-up si disponible
            try {
                PowerUp powerUp = board.collectPowerUp(newX, newY);
                if (powerUp != null) {
                    player.applyPowerUp(powerUp.getType());
                    // Ajouter des points
                    if (player == player1) {
                        player1Score += 10;
                    } else {
                        player2Score += 10;
                    }
                }
            } catch (Exception e) {
                // Si PowerUp n'est pas encore implémenté, on ignore
            }
        }
    }

    private void placeBomb(Player player) {
        if (player.canPlaceBomb()) {
            Bomb bomb = new Bomb(player.getX(), player.getY(), player.getPlayerId());
            bombs.add(bomb);
            board.placeBomb(player.getX(), player.getY());
            player.placeBomb();
            bombsPlaced++; // ← AJOUTÉ : Compter les bombes placées
        }
    }

    public void update() {
        // Traiter les mouvements en continu
        processMovement();

        // Mettre à jour les bombes
        Iterator<Bomb> bombIterator = bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.update();

            if (bomb.hasExploded()) {
                // Gérer l'explosion
                int wallsDestroyedByThisBomb = handleExplosion(bomb);
                wallsDestroyed += wallsDestroyedByThisBomb; // ← AJOUTÉ : Compter les murs détruits

                bombIterator.remove();
                board.removeBomb(bomb.getX(), bomb.getY());

                // Rendre la bombe disponible au joueur
                if (bomb.getPlayerId() == 1) {
                    player1.bombExploded();
                } else {
                    player2.bombExploded();
                }
            }
        }

        // Vérifier les conditions de victoire
        checkWinConditions();
    }

    private int handleExplosion(Bomb bomb) {
        int x = bomb.getX();
        int y = bomb.getY();
        int wallsDestroyedByThisBomb = 0; // ← AJOUTÉ : Compter les murs détruits

        // Explosion au centre
        if (board.isWall(x, y)) {
            wallsDestroyedByThisBomb++;
        }
        board.explode(x, y);

        // Explosion dans les 4 directions
        for (int i = 1; i <= bomb.getRange(); i++) {
            // Droite
            if (x + i < BOARD_WIDTH && board.canExplode(x + i, y)) {
                if (board.isWall(x + i, y)) {
                    wallsDestroyedByThisBomb++;
                }
                board.explode(x + i, y);
                if (board.isWall(x + i, y)) break;
            }
            // Gauche
            if (x - i >= 0 && board.canExplode(x - i, y)) {
                if (board.isWall(x - i, y)) {
                    wallsDestroyedByThisBomb++;
                }
                board.explode(x - i, y);
                if (board.isWall(x - i, y)) break;
            }
            // Haut
            if (y - i >= 0 && board.canExplode(x, y - i)) {
                if (board.isWall(x, y - i)) {
                    wallsDestroyedByThisBomb++;
                }
                board.explode(x, y - i);
                if (board.isWall(x, y - i)) break;
            }
            // Bas
            if (y + i < BOARD_HEIGHT && board.canExplode(x, y + i)) {
                if (board.isWall(x, y + i)) {
                    wallsDestroyedByThisBomb++;
                }
                board.explode(x, y + i);
                if (board.isWall(x, y + i)) break;
            }
        }

        // Ajouter des points pour les murs détruits
        if (bomb.getPlayerId() == 1) {
            player1Score += wallsDestroyedByThisBomb * 5;
        } else {
            player2Score += wallsDestroyedByThisBomb * 5;
        }

        // Vérifier si les joueurs sont touchés
        if (board.isExplosion(player1.getX(), player1.getY())) {
            player1.takeDamage();
        }
        if (board.isExplosion(player2.getX(), player2.getY())) {
            player2.takeDamage();
        }

        return wallsDestroyedByThisBomb; // ← AJOUTÉ : Retourner le nombre de murs détruits
    }

    private void checkWinConditions() {
        if (!player1.isAlive()) {
            System.out.println("Joueur 2 gagne!");
            gameRunning = false;
        } else if (!player2.isAlive()) {
            System.out.println("Joueur 1 gagne!");
            gameRunning = false;
        }
    }

    // Getters pour le contrôleur
    public GameBoard getBoard() { return board; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public List<Bomb> getBombs() { return new ArrayList<>(bombs); }
    public boolean isGameRunning() { return gameRunning; }
    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }

    // ← AJOUTÉ : Nouvelles méthodes manquantes
    public int getBombsPlaced() { return bombsPlaced; }
    public int getWallsDestroyed() { return wallsDestroyed; }
}